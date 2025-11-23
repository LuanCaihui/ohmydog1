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
    
    // 常见主诉症状（可以从数据库或配置中读取）
    private static final List<String> MAIN_COMPLAINTS = Arrays.asList(
        "咳嗽", "呕吐", "腹泻", "食欲不振", "发热", "呼吸困难", 
        "皮肤瘙痒", "跛行", "抽搐", "流鼻涕", "眼部分泌物", "耳部异味"
    );
    
    /**
     * 获取主诉问题列表
     */
    public List<SymptomQuestion> getMainComplaintQuestions() throws SQLException {
        List<SymptomQuestion> questions = new ArrayList<>();
        List<Symptom> allSymptoms = symptomDAO.findAll();
        
        // 从所有症状中筛选出主诉症状
        for (Symptom symptom : allSymptoms) {
            if (MAIN_COMPLAINTS.contains(symptom.getName())) {
                SymptomQuestion question = new SymptomQuestion();
                question.setSymptomId(symptom.getId());
                question.setSymptomName(symptom.getName());
                question.setQuestionType(SymptomQuestion.QuestionType.MAIN_COMPLAINT);
                question.setQuestionText("您的宠物是否有" + symptom.getName() + "的症状？");
                question.setOptions(null);
                question.setIsMultiple(false);
                question.setParentSymptomId(null);
                question.setDimension(null);
                questions.add(question);
            }
        }
        
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
        
        // 根据主诉推荐常见的伴随症状
        Map<String, List<String>> accompanyingMap = new HashMap<>();
        accompanyingMap.put("咳嗽", Arrays.asList("发热", "流鼻涕", "呼吸困难", "食欲不振"));
        accompanyingMap.put("呕吐", Arrays.asList("腹泻", "食欲不振", "发热", "腹痛"));
        accompanyingMap.put("腹泻", Arrays.asList("呕吐", "食欲不振", "发热", "脱水"));
        accompanyingMap.put("发热", Arrays.asList("咳嗽", "流鼻涕", "食欲不振", "精神不振"));
        accompanyingMap.put("呼吸困难", Arrays.asList("咳嗽", "流鼻涕", "发热", "精神不振"));
        
        List<String> accompanyingSymptoms = accompanyingMap.get(mainComplaint);
        if (accompanyingSymptoms != null && !accompanyingSymptoms.isEmpty()) {
            // 查找这些伴随症状的ID
            List<Symptom> allSymptoms = symptomDAO.findAll();
            List<String> availableSymptoms = new ArrayList<>();
            
            for (String symptomName : accompanyingSymptoms) {
                for (Symptom symptom : allSymptoms) {
                    if (symptom.getName().equals(symptomName) && !selectedSymptoms.contains(symptom.getId())) {
                        availableSymptoms.add(symptomName);
                        break;
                    }
                }
            }
            
            if (!availableSymptoms.isEmpty()) {
                SymptomQuestion question = new SymptomQuestion();
                question.setSymptomId(parentId);
                question.setSymptomName("伴随症状");
                question.setQuestionType(SymptomQuestion.QuestionType.FOLLOW_UP);
                question.setQuestionText("除了" + mainComplaint + "，是否还有以下症状？");
                question.setOptions(availableSymptoms);
                question.setIsMultiple(true); // 伴随症状可以多选
                question.setParentSymptomId(parentId);
                question.setDimension(SymptomQuestion.QuestionDimension.ACCOMPANYING);
                questions.add(question);
            }
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
     * 根据用户对上一个问题的回答，生成下一个追问
     * @param parentSymptomId 父症状ID（主诉）
     * @param answeredDimension 已回答的问题维度
     * @param selectedSymptoms 已选择的症状列表
     * @return 下一个追问，如果没有则返回null
     */
    public SymptomQuestion getNextFollowUpQuestion(Integer parentSymptomId, 
                                                   SymptomQuestion.QuestionDimension answeredDimension,
                                                   List<Integer> selectedSymptoms) throws SQLException {
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
        
        // 获取下一个维度的追问
        SymptomQuestion.QuestionDimension nextDimension = priority.get(answeredIndex + 1);
        
        List<SymptomQuestion> questions = new ArrayList<>();
        switch (nextDimension) {
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
        
        return questions.isEmpty() ? null : questions.get(0);
    }
}

