package com.petblog.Service;

import com.petblog.model.Symptom;
import com.petblog.model.SymptomQuestion;
import com.petblog.model.DiseaseSymptom;
import com.petblog.dao.SymptomDAO;
import com.petblog.dao.impl.SymptomDAOImpl;
import com.petblog.dao.DiseaseSymptomDAO;
import com.petblog.dao.impl.DiseaseSymptomDAOImpl;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 结构化问诊服务
 * 负责生成主诉问题和相关追问
 */
public class StructuredQuestionService {
    private final SymptomDAO symptomDAO = new SymptomDAOImpl();
    private final DiseaseSymptomDAO diseaseSymptomDAO = new DiseaseSymptomDAOImpl();
    
    // 主诉类别映射：类别名称 -> 代表性症状名称（用于选择该类别下的第一个症状作为主诉）
    private static final Map<String, String> CATEGORY_REPRESENTATIVE_SYMPTOMS = new HashMap<>();
    static {
        CATEGORY_REPRESENTATIVE_SYMPTOMS.put("呼吸系统", "咳嗽");
        CATEGORY_REPRESENTATIVE_SYMPTOMS.put("消化系统", "呕吐");
        CATEGORY_REPRESENTATIVE_SYMPTOMS.put("皮肤", "皮肤瘙痒");
        CATEGORY_REPRESENTATIVE_SYMPTOMS.put("全身", "发热");
        CATEGORY_REPRESENTATIVE_SYMPTOMS.put("眼部", "眼睛红肿");
        CATEGORY_REPRESENTATIVE_SYMPTOMS.put("口腔", "恶臭口气");
        CATEGORY_REPRESENTATIVE_SYMPTOMS.put("泌尿系统", "频繁排尿");
        CATEGORY_REPRESENTATIVE_SYMPTOMS.put("神经系统", "抽搐");
        CATEGORY_REPRESENTATIVE_SYMPTOMS.put("骨骼肌肉系统", "跛行");
        CATEGORY_REPRESENTATIVE_SYMPTOMS.put("耳部", "频繁抓挠耳朵");
        CATEGORY_REPRESENTATIVE_SYMPTOMS.put("循环系统", "心跳加快");
        CATEGORY_REPRESENTATIVE_SYMPTOMS.put("行为", "焦虑");
    }
    
    // 主诉显示名称映射：用于在界面上显示更友好的名称
    private static final Map<String, String> CATEGORY_DISPLAY_NAMES = new HashMap<>();
    static {
        CATEGORY_DISPLAY_NAMES.put("全身", "发热");
        // 其他类别保持原样
    }
    
    /**
     * 获取主诉问题列表（按症状类别分组）
     */
    public List<SymptomQuestion> getMainComplaintQuestions() throws SQLException {
        List<SymptomQuestion> questions = new ArrayList<>();
        List<Symptom> allSymptoms = symptomDAO.findAll();
        
        // 按类别分组症状
        Map<String, List<Symptom>> categoryMap = new HashMap<>();
        for (Symptom symptom : allSymptoms) {
            if (symptom.getCategory() != null && !symptom.getCategory().isEmpty()) {
                categoryMap.computeIfAbsent(symptom.getCategory(), k -> new ArrayList<>()).add(symptom);
            }
        }
        
        // 为每个类别创建一个主诉问题
        for (Map.Entry<String, List<Symptom>> entry : categoryMap.entrySet()) {
            String category = entry.getKey();
            List<Symptom> symptoms = entry.getValue();
            
            if (symptoms.isEmpty()) {
                continue;
            }
            
            // 选择该类别下的代表性症状，如果没有配置，则选择第一个症状
            Symptom representativeSymptom = null;
            String representativeName = CATEGORY_REPRESENTATIVE_SYMPTOMS.get(category);
            if (representativeName != null) {
                representativeSymptom = symptoms.stream()
                    .filter(s -> s.getName().equals(representativeName))
                    .findFirst()
                    .orElse(symptoms.get(0));
            } else {
                representativeSymptom = symptoms.get(0);
            }
            
            SymptomQuestion question = new SymptomQuestion();
            question.setSymptomId(representativeSymptom.getId());
            question.setSymptomName(category); // 使用类别名称作为显示名称
            
            // 使用显示名称映射，如果"全身"则显示为"发热"
            String displayName = CATEGORY_DISPLAY_NAMES.getOrDefault(category, category);
            question.setQuestionType(SymptomQuestion.QuestionType.MAIN_COMPLAINT);
            question.setQuestionText("您的宠物是否有" + displayName + "相关的症状？");
            question.setOptions(null);
            question.setIsMultiple(false);
            question.setParentSymptomId(null);
            question.setDimension(null);
            questions.add(question);
        }
        
        // 按类别名称排序，确保显示顺序一致
        questions.sort(Comparator.comparing(SymptomQuestion::getSymptomName));
        
        return questions;
    }
    
    /**
     * 根据主诉生成相关追问
     * @param mainComplaintId 主诉症状ID
     * @param selectedSymptoms 已选择的症状列表
     * @return 追问列表
     */
    public List<SymptomQuestion> getFollowUpQuestions(Integer mainComplaintId, List<Integer> selectedSymptoms) throws SQLException {
        List<SymptomQuestion> questions = new ArrayList<>();
        
        if (mainComplaintId == null) {
            return questions;
        }
        
        Symptom mainSymptom = symptomDAO.findById(mainComplaintId);
        if (mainSymptom == null) {
            return questions;
        }
        
        String mainComplaintName = mainSymptom.getName();
        
        // 根据主诉生成不同类型的追问
        questions.addAll(generateTypeQuestions(mainComplaintId, mainComplaintName));
        questions.addAll(generateDurationQuestions(mainComplaintId, mainComplaintName));
        questions.addAll(generateSeverityQuestions(mainComplaintId, mainComplaintName));
        questions.addAll(generateTriggerQuestions(mainComplaintId, mainComplaintName));
        questions.addAll(generateAccompanyingQuestions(mainComplaintId, mainComplaintName, selectedSymptoms));
        questions.addAll(generateRedFlagQuestions(mainComplaintId, mainComplaintName));
        
        return questions;
    }
    
    /**
     * 生成类型问题（如：干咳/湿咳）
     */
    private List<SymptomQuestion> generateTypeQuestions(Integer parentId, String mainComplaint) {
        List<SymptomQuestion> questions = new ArrayList<>();
        
        Map<String, List<String>> typeMap = new HashMap<>();
        typeMap.put("咳嗽", Arrays.asList("干咳", "湿咳", "带痰", "带血"));
        typeMap.put("呕吐", Arrays.asList("未消化食物", "黄色液体", "带血", "白色泡沫"));
        typeMap.put("腹泻", Arrays.asList("水样", "软便", "带血", "带黏液"));
        typeMap.put("发热", Arrays.asList("轻度发热(37.5-38.5℃)", "中度发热(38.5-39.5℃)", "高热(>39.5℃)"));
        
        List<String> options = typeMap.get(mainComplaint);
        if (options != null && !options.isEmpty()) {
            SymptomQuestion question = new SymptomQuestion();
            question.setSymptomId(parentId); // 使用父症状ID
            question.setSymptomName(mainComplaint + "类型");
            question.setQuestionType(SymptomQuestion.QuestionType.FOLLOW_UP);
            question.setQuestionText(mainComplaint + "的类型是？");
            question.setOptions(options);
            question.setIsMultiple(false);
            question.setParentSymptomId(parentId);
            question.setDimension(SymptomQuestion.QuestionDimension.TYPE);
            questions.add(question);
        }
        
        return questions;
    }
    
    /**
     * 生成持续时间问题
     */
    private List<SymptomQuestion> generateDurationQuestions(Integer parentId, String mainComplaint) {
        List<SymptomQuestion> questions = new ArrayList<>();
        
        SymptomQuestion question = new SymptomQuestion();
        question.setSymptomId(parentId);
        question.setSymptomName(mainComplaint + "持续时间");
        question.setQuestionType(SymptomQuestion.QuestionType.FOLLOW_UP);
        question.setQuestionText(mainComplaint + "持续了多长时间？");
        question.setOptions(Arrays.asList("1-3天", "4-7天", "1-3周", "超过3周"));
        question.setIsMultiple(false);
        question.setParentSymptomId(parentId);
        question.setDimension(SymptomQuestion.QuestionDimension.DURATION);
        questions.add(question);
        
        return questions;
    }
    
    /**
     * 生成严重程度问题
     */
    private List<SymptomQuestion> generateSeverityQuestions(Integer parentId, String mainComplaint) {
        List<SymptomQuestion> questions = new ArrayList<>();
        
        SymptomQuestion question = new SymptomQuestion();
        question.setSymptomId(parentId);
        question.setSymptomName(mainComplaint + "严重程度");
        question.setQuestionType(SymptomQuestion.QuestionType.FOLLOW_UP);
        question.setQuestionText(mainComplaint + "的严重程度如何？");
        question.setOptions(Arrays.asList("轻度（不影响正常活动）", "中度（影响部分活动）", "重度（严重影响活动）", "非常严重（无法正常活动）"));
        question.setIsMultiple(false);
        question.setParentSymptomId(parentId);
        question.setDimension(SymptomQuestion.QuestionDimension.SEVERITY);
        questions.add(question);
        
        return questions;
    }
    
    /**
     * 生成诱因问题
     */
    private List<SymptomQuestion> generateTriggerQuestions(Integer parentId, String mainComplaint) {
        List<SymptomQuestion> questions = new ArrayList<>();
        
        Map<String, List<String>> triggerMap = new HashMap<>();
        triggerMap.put("咳嗽", Arrays.asList("运动后更明显", "夜间加重", "进食后加重", "无明显诱因"));
        triggerMap.put("呕吐", Arrays.asList("进食后立即呕吐", "空腹时呕吐", "运动后呕吐", "无明显诱因"));
        triggerMap.put("腹泻", Arrays.asList("换粮后出现", "进食后加重", "应激后出现", "无明显诱因"));
        
        List<String> options = triggerMap.get(mainComplaint);
        if (options == null) {
            options = Arrays.asList("运动后更明显", "进食后加重", "无明显诱因");
        }
        
        SymptomQuestion question = new SymptomQuestion();
        question.setSymptomId(parentId);
        question.setSymptomName(mainComplaint + "诱因");
        question.setQuestionType(SymptomQuestion.QuestionType.FOLLOW_UP);
        question.setQuestionText("什么情况下" + mainComplaint + "会更明显？");
        question.setOptions(options);
        question.setIsMultiple(false);
        question.setParentSymptomId(parentId);
        question.setDimension(SymptomQuestion.QuestionDimension.TRIGGER);
        questions.add(question);
        
        return questions;
    }
    
    /**
     * 生成伴随症状问题
     */
    private List<SymptomQuestion> generateAccompanyingQuestions(Integer parentId, String mainComplaint, List<Integer> selectedSymptoms) throws SQLException {
        List<SymptomQuestion> questions = new ArrayList<>();
        
        // 获取主诉的类别
        Symptom mainSymptom = symptomDAO.findById(parentId);
        String mainComplaintCategory = null;
        if (mainSymptom != null && mainSymptom.getCategory() != null) {
            mainComplaintCategory = mainSymptom.getCategory();
            System.out.println("结构化问题：主诉ID=" + parentId + ", 主诉名称=" + mainComplaint + ", 主诉类别=" + mainComplaintCategory);
        }
        
        // 根据主诉推荐常见的伴随症状（只推荐同类别症状）
        Map<String, List<String>> accompanyingMap = new HashMap<>();
        accompanyingMap.put("咳嗽", Arrays.asList("发热", "流鼻涕", "呼吸困难", "打喷嚏"));
        accompanyingMap.put("呕吐", Arrays.asList("腹泻", "食欲不振", "发热", "腹痛"));
        accompanyingMap.put("腹泻", Arrays.asList("呕吐", "食欲不振", "发热", "脱水"));
        accompanyingMap.put("发热", Arrays.asList("咳嗽", "流鼻涕", "食欲不振", "精神不振"));
        accompanyingMap.put("呼吸困难", Arrays.asList("咳嗽", "流鼻涕", "发热", "精神不振"));
        accompanyingMap.put("皮肤瘙痒", Arrays.asList("皮疹", "皮肤红肿", "脱毛", "皮肤脱屑"));
        
        List<String> accompanyingSymptoms = accompanyingMap.get(mainComplaint);
        
        // 如果主诉有明确的类别，只推荐同类别症状
        List<Symptom> allSymptoms = symptomDAO.findAll();
        List<String> availableSymptoms = new ArrayList<>();
        
        if (accompanyingSymptoms != null && !accompanyingSymptoms.isEmpty()) {
            // 查找这些伴随症状的ID，并过滤同类别
            for (String symptomName : accompanyingSymptoms) {
                for (Symptom symptom : allSymptoms) {
                    if (symptom.getName().equals(symptomName) && !selectedSymptoms.contains(symptom.getId())) {
                        // 如果主诉有类别，只添加同类别的症状
                        if (mainComplaintCategory == null || mainComplaintCategory.equals(symptom.getCategory())) {
                            availableSymptoms.add(symptomName);
                        }
                        break;
                    }
                }
            }
        } else if (mainComplaintCategory != null) {
            // 如果主诉不在映射表中，但主诉有类别，推荐同类别的其他常见症状
            for (Symptom symptom : allSymptoms) {
                if (mainComplaintCategory.equals(symptom.getCategory()) 
                    && !symptom.getId().equals(parentId) 
                    && !selectedSymptoms.contains(symptom.getId())) {
                    // 只添加前5个同类别症状，避免选项过多
                    if (availableSymptoms.size() < 5) {
                        availableSymptoms.add(symptom.getName());
                    }
                }
            }
        }
        
        if (!availableSymptoms.isEmpty()) {
            // 为多选题添加"无"选项
            List<String> optionsWithNone = new ArrayList<>(availableSymptoms);
            optionsWithNone.add("无");
            
            SymptomQuestion question = new SymptomQuestion();
            question.setSymptomId(parentId);
            question.setSymptomName("伴随症状");
            question.setQuestionType(SymptomQuestion.QuestionType.FOLLOW_UP);
            question.setQuestionText("除了" + mainComplaint + "，是否还有以下症状？");
            question.setOptions(optionsWithNone);
            question.setIsMultiple(true); // 伴随症状可以多选
            question.setParentSymptomId(parentId);
            question.setDimension(SymptomQuestion.QuestionDimension.ACCOMPANYING);
            questions.add(question);
        }
        
        return questions;
    }
    
    /**
     * 生成危险信号问题
     */
    private List<SymptomQuestion> generateRedFlagQuestions(Integer parentId, String mainComplaint) {
        List<SymptomQuestion> questions = new ArrayList<>();
        
        Map<String, List<String>> redFlagMap = new HashMap<>();
        redFlagMap.put("咳嗽", Arrays.asList("呼吸困难", "胸口压迫感", "咳血", "无法平卧"));
        redFlagMap.put("呼吸困难", Arrays.asList("无法平卧", "口唇发紫", "意识模糊", "极度虚弱"));
        redFlagMap.put("呕吐", Arrays.asList("持续呕吐无法进食", "呕吐物带血", "腹痛剧烈", "意识模糊"));
        redFlagMap.put("腹泻", Arrays.asList("带血", "脱水严重", "精神极度萎靡", "无法站立"));
        
        List<String> redFlags = redFlagMap.get(mainComplaint);
        if (redFlags == null) {
            redFlags = Arrays.asList("症状急剧加重", "无法正常活动", "意识模糊", "极度虚弱");
        }
        
        SymptomQuestion question = new SymptomQuestion();
        question.setSymptomId(parentId);
        question.setSymptomName("危险信号");
        question.setQuestionType(SymptomQuestion.QuestionType.FOLLOW_UP);
        question.setQuestionText("是否出现以下危险信号？");
        question.setOptions(redFlags);
        question.setIsMultiple(true); // 危险信号可以多选
        question.setParentSymptomId(parentId);
        question.setDimension(SymptomQuestion.QuestionDimension.RED_FLAG);
        questions.add(question);
        
        return questions;
    }
    
    /**
     * 根据用户对上一个问题的回答，生成下一个追问（支持智能跳过）
     * @param parentSymptomId 父症状ID（主诉）
     * @param answeredDimension 已回答的问题维度
     * @param selectedSymptoms 已选择的症状列表
     * @param diseaseProbs 当前疾病概率分布（用于判断维度重要性，可为null）
     * @param diseaseSymMap 疾病-症状关系映射（用于判断维度重要性，可为null）
     * @return 下一个追问，如果没有则返回null
     */
    public SymptomQuestion getNextFollowUpQuestion(Integer parentSymptomId, 
                                                   SymptomQuestion.QuestionDimension answeredDimension,
                                                   List<Integer> selectedSymptoms,
                                                   Map<Integer, Double> diseaseProbs,
                                                   Map<Integer, List<DiseaseSymptom>> diseaseSymMap) throws SQLException {
        if (parentSymptomId == null) {
            return null;
        }
        
        Symptom mainSymptom = symptomDAO.findById(parentSymptomId);
        if (mainSymptom == null) {
            return null;
        }
        
        String mainComplaintName = mainSymptom.getName();
        
        // 按照优先级顺序生成追问
        List<SymptomQuestion.QuestionDimension> priority = Arrays.asList(
            SymptomQuestion.QuestionDimension.TYPE,
            SymptomQuestion.QuestionDimension.DURATION,
            SymptomQuestion.QuestionDimension.SEVERITY,
            SymptomQuestion.QuestionDimension.TRIGGER,
            SymptomQuestion.QuestionDimension.ACCOMPANYING,
            SymptomQuestion.QuestionDimension.RED_FLAG
        );
        
        // 找到已回答维度的索引
        int answeredIndex = priority.indexOf(answeredDimension);
        if (answeredIndex == -1 || answeredIndex >= priority.size() - 1) {
            return null; // 所有问题都已问完
        }
        
        // 智能跳过：从当前维度开始，找到下一个重要的维度
        for (int i = answeredIndex + 1; i < priority.size(); i++) {
            SymptomQuestion.QuestionDimension candidateDimension = priority.get(i);
            
            // 判断该维度是否重要（如果提供了疾病概率信息）
            if (diseaseProbs != null && diseaseSymMap != null) {
                if (!isDimensionImportant(candidateDimension, parentSymptomId, diseaseProbs, diseaseSymMap)) {
                    continue; // 跳过不重要的维度
                }
            }
            
            // 生成该维度的问题
            List<SymptomQuestion> questions = new ArrayList<>();
            switch (candidateDimension) {
                case TYPE:
                    questions = generateTypeQuestions(parentSymptomId, mainComplaintName);
                    break;
                case DURATION:
                    questions = generateDurationQuestions(parentSymptomId, mainComplaintName);
                    break;
                case SEVERITY:
                    questions = generateSeverityQuestions(parentSymptomId, mainComplaintName);
                    break;
                case TRIGGER:
                    questions = generateTriggerQuestions(parentSymptomId, mainComplaintName);
                    break;
                case ACCOMPANYING:
                    questions = generateAccompanyingQuestions(parentSymptomId, mainComplaintName, selectedSymptoms);
                    break;
                case RED_FLAG:
                    questions = generateRedFlagQuestions(parentSymptomId, mainComplaintName);
                    break;
            }
            
            if (!questions.isEmpty()) {
                return questions.get(0);
            }
        }
        
        return null; // 所有维度都已尝试，没有可用的问题
    }
    
    /**
     * 向后兼容的重载方法
     */
    public SymptomQuestion getNextFollowUpQuestion(Integer parentSymptomId, 
                                                   SymptomQuestion.QuestionDimension answeredDimension,
                                                   List<Integer> selectedSymptoms) throws SQLException {
        return getNextFollowUpQuestion(parentSymptomId, answeredDimension, selectedSymptoms, null, null);
    }
    
    /**
     * 判断某个维度对当前诊断是否重要
     * @param dimension 问题维度
     * @param mainComplaintId 主诉ID
     * @param diseaseProbs 疾病概率分布
     * @param diseaseSymMap 疾病-症状关系映射
     * @return true表示重要，false表示可以跳过
     */
    private boolean isDimensionImportant(SymptomQuestion.QuestionDimension dimension,
                                        Integer mainComplaintId,
                                        Map<Integer, Double> diseaseProbs,
                                        Map<Integer, List<DiseaseSymptom>> diseaseSymMap) {
        // 危险信号和伴随症状总是重要的
        if (dimension == SymptomQuestion.QuestionDimension.RED_FLAG || 
            dimension == SymptomQuestion.QuestionDimension.ACCOMPANYING) {
            return true;
        }
        
        // 如果疾病概率分布不明确（熵高），所有维度都重要
        double entropy = calculateEntropy(diseaseProbs);
        if (entropy > 2.0) { // 高熵，概率分布不明确
            return true;
        }
        
        // 如果某个疾病的概率已经很高（>40%），可以跳过一些细节维度
        double maxProb = diseaseProbs.values().stream()
            .mapToDouble(Double::doubleValue)
            .max()
            .orElse(0.0);
        
        if (maxProb > 0.4) {
            // 概率已经比较明确，可以跳过一些不太重要的维度
            // TRIGGER（诱因）和SEVERITY（严重程度）在某些情况下可以跳过
            if (dimension == SymptomQuestion.QuestionDimension.TRIGGER || 
                dimension == SymptomQuestion.QuestionDimension.SEVERITY) {
                return false; // 可以跳过
            }
        }
        
        return true; // 默认都重要
    }
    
    /**
     * 计算熵（用于判断概率分布的确定性）
     */
    private double calculateEntropy(Map<Integer, Double> probs) {
        double entropy = 0.0;
        for (Double prob : probs.values()) {
            if (prob > 0) {
                entropy -= prob * Math.log(prob) / Math.log(2);
            }
        }
        return entropy;
    }
}

