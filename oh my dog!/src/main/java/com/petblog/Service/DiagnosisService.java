package com.petblog.Service;

import com.petblog.dao.DiseaseDAO;
import com.petblog.dao.SymptomDAO;
import com.petblog.dao.impl.DiseaseDAOImpl;
import com.petblog.dao.impl.SymptomDAOImpl;
import com.petblog.model.*;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 核心诊断服务类
 * 实现智能问题选择和停止止损机制
 */
public class DiagnosisService {
    
    private DiseaseDAO diseaseDAO = new DiseaseDAOImpl();
    private SymptomDAO symptomDAO = new SymptomDAOImpl();
    private DiseaseSymptomService diseaseSymptomService = new DiseaseSymptomService();
    
    /**
     * 计算并返回下一步：可能是具体的诊断结果，也可能是下一个问题
     * 
     * @param session 诊断会话状态
     * @return 包含 finished 标志和结果/问题的 Map
     */
    public Map<String, Object> nextStep(DiagnosisSession session) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 获取所有疾病并计算概率（使用加分制算法 - 匹配度百分比）
            List<DiseaseResult> diseaseProbabilities = calculateProbabilities(session);
            
            // 2. 检查是否有明确的诊断结果 (结束条件)
            if (shouldStopDiagnosis(session, diseaseProbabilities)) {
                result.put("finished", true);
                result.put("diseases", formatDiseaseResults(diseaseProbabilities));
                return result;
            }
            
            // 3. 选择下一个最佳问题 (智能选择逻辑)
            SymptomWithWeight nextQuestion = selectNextQuestion(session, diseaseProbabilities);
            if (nextQuestion == null) {
                // 如果找不到有价值的问题，说明所有相关症状都已问过
                // 这种情况下应该强制结束诊断，而不是返回错误
                System.out.println("DiagnosisService.nextStep: 找不到下一个问题（所有相关症状都已问过），强制结束诊断");
                List<Map<String, Object>> formattedDiseases = formatDiseaseResults(diseaseProbabilities);
                System.out.println("DiagnosisService.nextStep: 格式化后的疾病数量=" + formattedDiseases.size());
                if (formattedDiseases.isEmpty()) {
                    System.out.println("DiagnosisService.nextStep: 警告：格式化后的疾病列表为空！");
                }
                result.put("finished", true);
                result.put("diseases", formattedDiseases);
            } else {
                result.put("finished", false);
                result.put("nextQuestion", nextQuestion.getSymptom());
                result.put("nextSymptomId", nextQuestion.getSymptom().getId());
                result.put("name", nextQuestion.getSymptom().getName());
                result.put("description", ""); // 可以后续扩展
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", "诊断计算失败: " + e.getMessage());
            result.put("finished", true);
            result.put("diseases", new ArrayList<>());
        }
        
        return result;
    }
    
    /**
     * 核心改进：智能问题选择
     * 策略：只关注概率最高的前3名疾病，从这些疾病的关联症状中选择
     */
    private SymptomWithWeight selectNextQuestion(DiagnosisSession session, List<DiseaseResult> rankedDiseases) {
        if (rankedDiseases.isEmpty()) {
            System.out.println("selectNextQuestion: 疾病列表为空");
            return null;
        }
        
        // 策略：只关注概率最高的前3名疾病
        List<Integer> topDiseaseIds = rankedDiseases.stream()
                .limit(3)
                .map(dr -> dr.getDisease().getId())
                .collect(Collectors.toList());
        
        System.out.println("selectNextQuestion: Top3疾病ID=" + topDiseaseIds + "，已问症状数=" + session.getAskedSymptoms().size());
        System.out.println("selectNextQuestion: 已问症状列表=" + session.getAskedSymptoms());
        
        // 获取主诉类别（用于回退机制）
        String mainComplaintCategory = null;
        if (session.getMainComplaintId() != null) {
            try {
                Symptom mainComplaint = symptomDAO.findById(session.getMainComplaintId());
                if (mainComplaint != null && mainComplaint.getCategory() != null) {
                    mainComplaintCategory = mainComplaint.getCategory();
                    System.out.println("selectNextQuestion: 主诉类别=" + mainComplaintCategory);
                }
            } catch (SQLException e) {
                System.out.println("selectNextQuestion: 获取主诉类别失败: " + e.getMessage());
            }
        }
        
        try {
            // 从数据库获取这前3名疾病的所有未问症状
            List<SymptomWithWeight> candidates = symptomDAO.findCandidateSymptoms(
                    topDiseaseIds, 
                    session.getAskedSymptoms()
            );
            
            System.out.println("selectNextQuestion: 找到候选症状数=" + candidates.size());
            if (candidates.size() > 0) {
                System.out.println("selectNextQuestion: 候选症状ID列表=" + candidates.stream()
                    .map(sw -> sw.getSymptom().getId())
                    .collect(Collectors.toList()));
            }
            
            if (candidates.isEmpty()) {
                // 如果Top3疾病没有候选症状，尝试扩大范围到Top5
                if (rankedDiseases.size() > 3) {
                    System.out.println("selectNextQuestion: Top3疾病无候选症状，尝试Top5");
                    List<Integer> top5DiseaseIds = rankedDiseases.stream()
                            .limit(5)
                            .map(dr -> dr.getDisease().getId())
                            .collect(Collectors.toList());
                    candidates = symptomDAO.findCandidateSymptoms(
                            top5DiseaseIds, 
                            session.getAskedSymptoms()
                    );
                    System.out.println("selectNextQuestion: Top5疾病候选症状数=" + candidates.size());
                }
                
                // 如果仍然为空，尝试从主诉类别中补充同类别症状（回退机制）
                if (candidates.isEmpty() && mainComplaintCategory != null) {
                    System.out.println("selectNextQuestion: 所有Top疾病都没有候选症状，尝试从主诉类别(" + mainComplaintCategory + ")补充症状");
                    try {
                        List<Symptom> categorySymptoms = symptomDAO.findByCategory(mainComplaintCategory, session.getAskedSymptoms(), 10);
                        if (!categorySymptoms.isEmpty()) {
                            System.out.println("selectNextQuestion: 从主诉类别找到" + categorySymptoms.size() + "个候选症状");
                            // 转换为SymptomWithWeight（使用默认权重）
                            for (Symptom symptom : categorySymptoms) {
                                SymptomWithWeight sw = new SymptomWithWeight();
                                sw.setSymptom(symptom);
                                sw.setWeight(1.0); // 默认权重
                                sw.setDiseaseId(null);
                                sw.setIsRequired(false);
                                sw.setIsExclusive(false);
                                candidates.add(sw);
                            }
                        }
                    } catch (SQLException e) {
                        System.out.println("selectNextQuestion: 从主诉类别查找症状失败: " + e.getMessage());
                    }
                }
                
                if (candidates.isEmpty()) {
                    System.out.println("selectNextQuestion: 所有候选症状都为空，无法继续问诊");
                    return null;
                }
            } else if (candidates.size() < 3 && mainComplaintCategory != null) {
                // 如果候选症状太少（少于3个），也从主诉类别补充
                System.out.println("selectNextQuestion: 候选症状较少(" + candidates.size() + ")，从主诉类别补充");
                try {
                    List<Integer> alreadyCandidateIds = candidates.stream()
                        .map(sw -> sw.getSymptom().getId())
                        .collect(Collectors.toList());
                    
                    List<Integer> allAskedIds = new ArrayList<>(session.getAskedSymptoms());
                    allAskedIds.addAll(alreadyCandidateIds);
                    
                    List<Symptom> categorySymptoms = symptomDAO.findByCategory(mainComplaintCategory, allAskedIds, 5);
                    for (Symptom symptom : categorySymptoms) {
                        SymptomWithWeight sw = new SymptomWithWeight();
                        sw.setSymptom(symptom);
                        sw.setWeight(0.8); // 稍微低一点的权重，因为是补充的
                        sw.setDiseaseId(null);
                        sw.setIsRequired(false);
                        sw.setIsExclusive(false);
                        candidates.add(sw);
                    }
                    System.out.println("selectNextQuestion: 补充后候选症状数=" + candidates.size());
                } catch (SQLException e) {
                    System.out.println("selectNextQuestion: 从主诉类别补充症状失败: " + e.getMessage());
                }
            }
            
            // 构建疾病ID到概率的映射，用于计算得分
            // 根据实际查询时使用的疾病范围构建映射
            Map<Integer, Double> diseaseProbMap = new HashMap<>();
            // 确定实际使用的疾病范围（根据是否扩大了范围）
            int actualLimit = 3; // 默认Top3
            if (rankedDiseases.size() > 5 && candidates.size() > 0) {
                // 如果尝试了Top5或Top10，需要包含所有可能的疾病
                actualLimit = Math.min(10, rankedDiseases.size());
            } else if (rankedDiseases.size() > 3 && candidates.size() > 0) {
                actualLimit = Math.min(5, rankedDiseases.size());
            }
            for (DiseaseResult dr : rankedDiseases.subList(0, actualLimit)) {
                diseaseProbMap.put(dr.getDisease().getId(), dr.getProbability());
            }
            
            // 构建症状ID到候选对象的映射（可能有多个疾病关联同一症状）
            Map<Integer, List<SymptomWithWeight>> symptomMap = new HashMap<>();
            for (SymptomWithWeight sw : candidates) {
                symptomMap.computeIfAbsent(sw.getSymptom().getId(), k -> new ArrayList<>()).add(sw);
            }
            
            // 内部类：用于存储症状和得分
            class SymptomScore {
                SymptomWithWeight symptom;
                double score;
                
                SymptomScore(SymptomWithWeight symptom, double score) {
                    this.symptom = symptom;
                    this.score = score;
                }
            }
            
            // 计算每个候选症状的得分
            List<SymptomScore> scoredSymptoms = new ArrayList<>();
            for (Map.Entry<Integer, List<SymptomWithWeight>> entry : symptomMap.entrySet()) {
                double score = 0.0;
                
                // 遍历该症状关联的所有疾病，累加得分
                for (SymptomWithWeight sw : entry.getValue()) {
                    Double diseaseProb = diseaseProbMap.get(sw.getDiseaseId());
                    if (diseaseProb != null) {
                        // 得分 = 症状权重 * 疾病概率
                        // 如果是必需症状，额外加分
                        double weight = sw.getWeight() != null ? sw.getWeight() : 1.0;
                        if (sw.getIsRequired() != null && sw.getIsRequired()) {
                            weight *= 1.5; // 必需症状加权
                        }
                        score += weight * diseaseProb;
                    }
                }
                
                // 选择权重最高的那个关联（如果有多个疾病关联同一症状）
                SymptomWithWeight bestSW = entry.getValue().stream()
                        .max(Comparator.comparing(sw -> sw.getWeight() != null ? sw.getWeight() : 0.0))
                        .orElse(entry.getValue().get(0));
                
                // 如果症状类别与主诉类别相同，增加得分（优先选择同类别的症状）
                if (mainComplaintCategory != null && bestSW.getSymptom().getCategory() != null 
                    && mainComplaintCategory.equals(bestSW.getSymptom().getCategory())) {
                    score *= 1.5; // 同类别的症状得分增加50%
                    System.out.println("selectNextQuestion: 症状" + bestSW.getSymptom().getName() + "与主诉类别相同，得分加权");
                }
                
                scoredSymptoms.add(new SymptomScore(bestSW, score));
            }
            
            // 按得分排序
            scoredSymptoms.sort((a, b) -> Double.compare(b.score, a.score));
            
            // 总是选择得分最高的症状
            // 但避免重复最近问过的问题（如果得分最高的最近问过，选择下一个）
            
            SymptomWithWeight bestSymptom = null;
            double maxScore = scoredSymptoms.isEmpty() ? -1.0 : scoredSymptoms.get(0).score;
            
            if (!scoredSymptoms.isEmpty()) {
                // 优先选择得分最高的，但如果最近问过，则选择下一个未问过的
                for (SymptomScore ss : scoredSymptoms) {
                    if (!session.isRecentlyAsked(ss.symptom.getSymptom().getId())) {
                        bestSymptom = ss.symptom;
                        break;
                    }
                }
                
                // 如果所有候选症状都最近问过，选择得分最高的（避免无法继续问诊）
                if (bestSymptom == null) {
                    bestSymptom = scoredSymptoms.get(0).symptom;
                    System.out.println("selectNextQuestion: 所有候选症状都最近问过，选择得分最高的");
                }
                
                // 记录最近问过的症状
                if (bestSymptom != null) {
                    session.addRecentAskedSymptom(bestSymptom.getSymptom().getId());
                }
            }
            
            System.out.println("selectNextQuestion: 最佳症状=" + (bestSymptom != null ? bestSymptom.getSymptom().getName() : "null") + "，得分=" + maxScore);
            
            // 截断机制 (Pruning)：如果最强的问题得分都很低，说明问了也没用
            // 但不要过早结束，只有在得分非常低（<0.01）且问题数已经很多时才结束
            // 否则继续问问题，让 shouldStopDiagnosis 来决定是否结束
            if (maxScore < 0.01 && session.getQuestionCount() >= 15) {
                System.out.println("selectNextQuestion: 最佳问题得分=" + maxScore + "，且已问" + session.getQuestionCount() + "个问题，触发截断机制");
                return null; // 触发强制结束
            }
            
            // 如果得分很低但问题数不多，仍然返回最佳症状（让系统继续问）
            if (maxScore < 0.01) {
                System.out.println("selectNextQuestion: 最佳问题得分较低(" + maxScore + ")，但问题数=" + session.getQuestionCount() + "，继续问诊");
            }
            
            return bestSymptom;
        } catch (SQLException e) {
            System.out.println("selectNextQuestion: 数据库查询异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 概率计算逻辑（使用加分制算法 - 匹配度百分比）
     * 核心公式：匹配度 = 当前命中症状的总权重 / 该疾病所有症状的总权重
     * 
     * 详细逻辑：
     * 1. 计算分母（TotalScore）：所有症状的权重之和，必需症状权重×2.0
     * 2. 计算分子（CurrentScore）：已选择症状的权重之和，必需症状权重×2.0
     * 3. 处理"明确回答否"的惩罚：如果必需症状被否定，概率×0.1
     * 4. 最终概率 = CurrentScore / TotalScore
     */
    private List<DiseaseResult> calculateProbabilities(DiagnosisSession session) {
        try {
            // 获取所有疾病
            List<Disease> diseases = diseaseDAO.findAll();
            if (diseases == null || diseases.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 构建疾病-症状关系映射
            Map<Integer, List<DiseaseSymptom>> diseaseSymMap = buildDiseaseSymptomMap();
            
            // 获取用户数据（使用 final 变量以便在 lambda 中使用）
            final List<Integer> selectedIds = session.getSelectedSymptoms() != null 
                    ? session.getSelectedSymptoms() 
                    : new ArrayList<>(); // 用户选"是"的症状ID列表
            final List<Integer> askedIds = session.getAskedSymptoms() != null 
                    ? session.getAskedSymptoms() 
                    : new ArrayList<>(); // 用户已回答过的症状ID列表
            final List<Integer> uncertainIds = session.getUncertainSymptoms() != null 
                    ? session.getUncertainSymptoms() 
                    : new ArrayList<>(); // 用户回答"不确定"的症状ID列表
            final Integer mainComplaintId = session.getMainComplaintId(); // 主诉ID
            
            List<DiseaseResult> results = new ArrayList<>();
            
            // 遍历每个疾病
            for (Disease disease : diseases) {
                List<DiseaseSymptom> symptoms = diseaseSymMap.get(disease.getId());
                
                if (symptoms == null || symptoms.isEmpty()) {
                    continue; // 没有关联症状的疾病，跳过
                }
                
                double totalPossibleScore = 0.0; // 分母：该疾病所有症状的总权重
                double currentScore = 0.0; // 分子：当前命中症状的总权重
                boolean criticalSymptomMissing = false; // 标记：是否有关键症状被否定（强必需）
                boolean weakRequiredSymptomMissing = false; // 标记：是否弱必需症状被否定
                boolean hasExclusiveSymptom = false; // 标记：是否有特异性症状被选中
                
                // 检查疾病是否包含主诉症状
                boolean containsMainComplaint = false;
                if (mainComplaintId != null) {
                    containsMainComplaint = symptoms.stream()
                            .anyMatch(ds -> ds.getSymptomId().equals(mainComplaintId));
                }
                
                // 遍历该疾病的所有症状
                for (DiseaseSymptom ds : symptoms) {
                    Integer symptomId = ds.getSymptomId();
                    
                    // 获取症状权重，默认为100（如果没有设置权重）
                    double weight = (ds.getWeight() != null) ? ds.getWeight() : 100.0;
                    
                    // 如果是必需症状，权重加倍，强调其重要性
                    double effectiveWeight = (ds.getIsRequired() != null && ds.getIsRequired()) 
                                            ? weight * 2.0 
                                            : weight;
                    
                    // 1. 计算总分（分母）- 累加所有症状的有效权重
                    totalPossibleScore += effectiveWeight;
                    
                    // 2. 计算当前得分（分子）
                    if (selectedIds.contains(symptomId)) {
                        // 命中症状，加分（使用有效权重）
                        currentScore += effectiveWeight;
                        
                        // 新增：特异性症状奖励
                        if (ds.getIsExclusive() != null && ds.getIsExclusive()) {
                            // 特异性症状被选中，给予巨大加分，确保该病直接冲到第一名
                            // 使用500.0作为额外加分，这个值远大于普通症状的权重
                            currentScore += 500.0;
                            hasExclusiveSymptom = true;
                            System.out.println("DiagnosisService.calculateProbabilities: 疾病" + disease.getName() 
                                + "的特异性症状(ID=" + symptomId + ")被选中，给予500分奖励");
                        }
                    } else if (askedIds.contains(symptomId) && !uncertainIds.contains(symptomId)) {
                        // 核心逻辑：用户明确回答了"否" (在asked里但不在selected里，且不在uncertain里)
                        if (ds.getIsRequired() != null && ds.getIsRequired()) {
                            // 判断是强必需还是弱必需
                            // 如果权重很高（>80）或没有权重信息，认为是强必需
                            // 否则是弱必需
                            double symptomWeight = (ds.getWeight() != null) ? ds.getWeight() : 100.0;
                            if (symptomWeight > 80.0) {
                                // 强必需症状被否定
                                criticalSymptomMissing = true;
                            } else {
                                // 弱必需症状被否定
                                weakRequiredSymptomMissing = true;
                            }
                        }
                        // 注意：普通症状被否定时，不加减分，因为它无法获得分数，这本身就是一种惩罚
                        // （因为分母大了，但分子没增加）
                    }
                    // 注意：如果症状在uncertainIds中，既不加分也不触发必需症状惩罚
                    // 注意：还没问到的症状（不在askedIds里）既不加分也不减分
                    // 它们保留了"未来的可能性"
                }
                
                // 3. 计算基础概率
                double probability = 0.0;
                if (totalPossibleScore > 0.0) {
                    probability = currentScore / totalPossibleScore;
                }
                
                // 4. 特异性症状奖励：如果有特异性症状被选中，直接提升概率到95%
                if (hasExclusiveSymptom) {
                    probability = 0.95;
                    System.out.println("DiagnosisService.calculateProbabilities: 疾病" + disease.getName() 
                        + "的特异性症状被选中，概率直接设为95%");
                } else {
                    // 5. 应用惩罚
                    if (criticalSymptomMissing) {
                        // 强必需症状被否定，概率大幅降低（但不完全为0，防止误操作）
                        probability *= 0.1;
                    } else if (weakRequiredSymptomMissing) {
                        // 弱必需症状被否定，概率适度降低
                        probability *= 0.4;
                    }
                    
                    // 6. 主诉权重增强：如果疾病不包含主诉症状，概率降低
                    if (mainComplaintId != null && !containsMainComplaint) {
                        probability *= 0.5;
                        System.out.println("DiagnosisService.calculateProbabilities: 疾病" + disease.getName() + "不包含主诉症状，概率降低50%");
                    }
                }
                
                // 7. 限制最大概率（从实际出发，不应该有100%的概率，最高95%）
                if (probability > 0.95) {
                    probability = 0.95;
                }
                
                // 8. 封装结果（过滤掉极低概率）
                if (probability > 0.01) {
                    DiseaseResult result = new DiseaseResult();
                    result.setDisease(disease);
                    result.setProbability(probability);
                    
                    // 获取支持该疾病的证据症状（只保留用户已选择的症状）
                    List<DiseaseSymptom> evidenceSymptoms = symptoms.stream()
                            .filter(ds -> selectedIds.contains(ds.getSymptomId()))
                            .collect(Collectors.toList());
                    result.setEvidenceSymptoms(evidenceSymptoms);
                    
                    results.add(result);
                }
            }
            
            // 排序：概率高的在前
            Collections.sort(results, (a, b) -> Double.compare(b.getProbability(), a.getProbability()));
            
            System.out.println("DiagnosisService.calculateProbabilities: 计算完成，共" + results.size() + "个疾病，最高概率=" 
                    + (results.isEmpty() ? "0" : (results.get(0).getProbability() * 100) + "%"));
            
            return results;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 构建疾病-症状关系映射
     */
    private Map<Integer, List<DiseaseSymptom>> buildDiseaseSymptomMap() {
        Map<Integer, List<DiseaseSymptom>> map = new HashMap<>();
        List<DiseaseSymptom> allRelations = diseaseSymptomService.getAllRelations();
        
        if (allRelations != null) {
            for (DiseaseSymptom ds : allRelations) {
                Integer diseaseId = ds.getDiseaseId();
                map.computeIfAbsent(diseaseId, k -> new ArrayList<>()).add(ds);
            }
        }
        
        return map;
    }
    
    /**
     * 动态结束阈值 - 停止止损机制
     * 要求至少问一定数量的问题才可能结束诊断
     */
    private boolean shouldStopDiagnosis(DiagnosisSession session, List<DiseaseResult> probs) {
        if (probs.isEmpty()) {
            return true;
        }
        
        double topProb = probs.get(0).getProbability();
        int qCount = session.getQuestionCount();
        
        System.out.println("DiagnosisService.shouldStopDiagnosis: 当前最大概率=" + (topProb * 100) + "%，已问问题数=" + qCount);
        
        // 最少问题数要求：至少问8个问题才可能结束诊断（不包括主诉）
        int minQuestions = 8;
        if (qCount < minQuestions) {
            System.out.println("DiagnosisService.shouldStopDiagnosis: 问题数不足" + minQuestions + "个，继续问诊");
            return false;
        }
        
        // 动态阈值：问得越多，门槛越低
        // 大幅提高阈值，确保收集足够信息
        double threshold;
        if (qCount <= 8) {
            threshold = 0.90; // 前8个问题需要90%以上概率
        } else if (qCount <= 12) {
            threshold = 0.85; // 9-12个问题需要85%以上概率
        } else if (qCount <= 16) {
            threshold = 0.75; // 13-16个问题需要75%以上概率
        } else if (qCount <= 20) {
            threshold = 0.65; // 17-20个问题需要65%以上概率
        } else {
            threshold = 0.55; // 21个问题以上需要55%以上概率
        }
        
        System.out.println("DiagnosisService.shouldStopDiagnosis: 当前阈值=" + (threshold * 100) + "%");
        
        // 条件1: 达到绝对阈值
        if (topProb >= threshold) {
            System.out.println("DiagnosisService.shouldStopDiagnosis: 满足绝对阈值条件，结束诊断");
            return true;
        }
        
        // 条件2: 相对优势 (第一名比第二名高出很多，且问题数足够多)
        if (probs.size() > 1 && qCount >= 12) { // 至少12个问题才考虑相对优势
            double secondProb = probs.get(1).getProbability();
            double probDiff = topProb - secondProb;
            System.out.println("DiagnosisService.shouldStopDiagnosis: 第二概率=" + (secondProb * 100) + "%，相对差异=" + (probDiff * 100) + "%");
            // 提高相对优势要求：差异更大，且第一名概率更高
            if (probDiff > 0.40 && topProb > 0.60) {
                System.out.println("DiagnosisService.shouldStopDiagnosis: 满足相对优势条件，结束诊断");
                return true;
            }
        }
        
        // 条件3: 问题问太多了（强制结束）
        if (qCount >= 30) {
            System.out.println("DiagnosisService.shouldStopDiagnosis: 已问30个问题，强制结束");
            return true;
        }
        
        System.out.println("DiagnosisService.shouldStopDiagnosis: 未满足结束条件，继续问诊");
        return false;
    }
    
    /**
     * 格式化疾病结果为前端需要的格式
     */
    private List<Map<String, Object>> formatDiseaseResults(List<DiseaseResult> results) {
        List<Map<String, Object>> formatted = new ArrayList<>();
        
        for (DiseaseResult dr : results.subList(0, Math.min(5, results.size()))) {
            Map<String, Object> diseaseMap = new HashMap<>();
            diseaseMap.put("disease", dr.getDisease().getName());
            diseaseMap.put("diseaseId", dr.getDisease().getId());
            diseaseMap.put("probability", dr.getProbability());
            
            // 格式化证据症状（不显示权重）
            if (dr.getEvidenceSymptoms() != null && !dr.getEvidenceSymptoms().isEmpty()) {
                List<Map<String, Object>> evidenceList = new ArrayList<>();
                for (DiseaseSymptom ds : dr.getEvidenceSymptoms()) {
                    Map<String, Object> evidence = new HashMap<>();
                    try {
                        Symptom symptom = symptomDAO.findById(ds.getSymptomId());
                        if (symptom != null) {
                            evidence.put("symptomName", symptom.getName());
                            // 不显示权重和isRequired
                            evidenceList.add(evidence);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                diseaseMap.put("evidenceSymptoms", evidenceList);
            } else {
                diseaseMap.put("evidenceSymptoms", new ArrayList<>());
            }
            
            // 添加疾病描述
            if (dr.getDisease().getDescription() != null && !dr.getDisease().getDescription().trim().isEmpty()) {
                diseaseMap.put("description", dr.getDisease().getDescription());
            } else {
                diseaseMap.put("description", "暂无疾病描述信息");
            }
            
            // 添加诊疗建议（如果 DiagnosisResult 中有）
            if (dr.getRecommendation() != null && !dr.getRecommendation().isEmpty()) {
                diseaseMap.put("recommendation", dr.getRecommendation());
            } else {
                // 如果没有，生成一个默认建议
                diseaseMap.put("recommendation", generateDefaultRecommendation(dr.getDisease().getName(), dr.getProbability()));
            }
            
            formatted.add(diseaseMap);
        }
        
        return formatted;
    }
    
    /**
     * 生成默认的诊疗建议
     */
    private String generateDefaultRecommendation(String diseaseName, double probability) {
        if (probability >= 0.7) {
            return "根据当前症状，您的宠物很可能患有" + diseaseName + "。建议尽快带宠物到专业宠物医院进行详细检查，以便获得准确的诊断和及时的治疗。";
        } else if (probability >= 0.45) {
            return "根据当前症状，您的宠物可能患有" + diseaseName + "。建议带宠物到专业宠物医院进行进一步检查，以确认诊断。";
        } else if (probability >= 0.2) {
            return "根据当前症状，您的宠物有较低的可能性患有" + diseaseName + "。建议继续观察宠物状况，如有异常请及时就医。";
        } else {
            return "根据当前症状，无法确定明确的诊断。建议继续观察宠物状况，如有异常请及时咨询专业兽医。";
        }
    }
}

