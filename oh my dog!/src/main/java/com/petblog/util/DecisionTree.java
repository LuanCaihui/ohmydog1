package com.petblog.util;

import com.petblog.model.Disease;
import com.petblog.model.DiseaseSymptom;
import com.petblog.model.Symptom;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 决策树/信息增益计算类
 * 用于动态生成下一题，选择信息量最大的症状提问
 */
public class DecisionTree {
    
    /**
     * 问题结果类，包含症状ID和解释
     */
    public static class QuestionResult {
        private Integer symptomId;
        private String explanation;
        
        public QuestionResult(Integer symptomId, String explanation) {
            this.symptomId = symptomId;
            this.explanation = explanation;
        }
        
        public Integer getSymptomId() {
            return symptomId;
        }
        
        public String getExplanation() {
            return explanation;
        }
    }

    /**
     * 计算信息增益，选择下一个最佳问题
     * @param candidateSymptomId 候选症状ID
     * @param selectedSymptoms 已选择的症状ID列表
     * @param diseases 所有疾病列表
     * @param diseaseSymMap 疾病ID -> 该疾病的所有症状关系列表的映射
     * @return 信息增益值（越大越好）
     */
    private static double calculateInformationGain(Integer candidateSymptomId,
                                                     List<Integer> selectedSymptoms,
                                                     List<Disease> diseases,
                                                     Map<Integer, List<DiseaseSymptom>> diseaseSymMap) {
        if (diseases == null || diseases.isEmpty()) {
            return 0.0;
        }

        // 计算当前状态下的疾病概率分布
        Map<Integer, Double> currentProbs = calculateDiseaseProbabilities(selectedSymptoms, diseases, diseaseSymMap);
        
        // 计算如果选择该症状后的疾病概率分布
        List<Integer> newSelected = new java.util.ArrayList<>(selectedSymptoms);
        newSelected.add(candidateSymptomId);
        Map<Integer, Double> newProbs = calculateDiseaseProbabilities(newSelected, diseases, diseaseSymMap);

        // 信息增益 = 熵的减少量
        double currentEntropy = calculateEntropy(currentProbs);
        double newEntropy = calculateEntropy(newProbs);
        
        return currentEntropy - newEntropy;
    }

    /**
     * 计算疾病概率分布（公开方法，供外部调用）
     */
    public static Map<Integer, Double> calculateDiseaseProbabilities(List<Integer> selectedSymptoms,
                                                                        List<Disease> diseases,
                                                                        Map<Integer, List<DiseaseSymptom>> diseaseSymMap) {
        Map<Integer, Double> probs = new java.util.HashMap<>();
        double totalProb = 0.0;

        for (Disease d : diseases) {
            double p = 1.0;
            List<DiseaseSymptom> dsList = diseaseSymMap.get(d.getId());

            if (dsList == null || dsList.isEmpty()) {
                p = 0.01; // 很小的概率
            } else {
                for (Integer sId : selectedSymptoms) {
                    boolean found = false;
                    for (DiseaseSymptom ds : dsList) {
                        if (ds.getSymptomId().equals(sId)) {
                            // 检查是否互斥
                            if (ds.getIsExclusive() != null && ds.getIsExclusive()) {
                                p = 0.0; // 互斥症状，概率为0
                                break;
                            }
                            if (ds.getWeight() != null) {
                                p *= ds.getWeight();
                            } else {
                                p *= 0.5;
                            }
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        p *= 0.1;
                    }
                    // 如果已经互斥，直接跳出
                    if (p == 0.0) {
                        break;
                    }
                }
                // 归一化
                if (selectedSymptoms.size() > 0) {
                    p = Math.pow(p, 1.0 / selectedSymptoms.size());
                }
            }

            probs.put(d.getId(), p);
            totalProb += p;
        }

        // 归一化概率
        if (totalProb > 0) {
            for (Integer diseaseId : probs.keySet()) {
                probs.put(diseaseId, probs.get(diseaseId) / totalProb);
            }
        }

        return probs;
    }

    /**
     * 计算熵（信息熵）
     */
    private static double calculateEntropy(Map<Integer, Double> probs) {
        double entropy = 0.0;
        for (Double prob : probs.values()) {
            if (prob > 0) {
                entropy -= prob * Math.log(prob) / Math.log(2);
            }
        }
        return entropy;
    }

    /**
     * 获取下一个最佳问题（带解释）
     * @param selectedSymptoms 已选择的症状ID列表（用户选择"是"的症状）
     * @param askedSymptoms 所有已问过的症状ID列表（包括选择"是"和"否"的，用于避免重复提问）
     * @param allSymptoms 所有症状列表
     * @param diseases 所有疾病列表
     * @param diseaseSymMap 疾病ID -> 该疾病的所有症状关系列表的映射
     * @return 问题结果（包含症状ID和解释），如果没有则返回null
     */
    public static QuestionResult getNextQuestionWithExplanation(List<Integer> selectedSymptoms,
                                                                List<Integer> askedSymptoms,
                                                                List<Symptom> allSymptoms,
                                                                List<Disease> diseases,
                                                                Map<Integer, List<DiseaseSymptom>> diseaseSymMap) {
        QuestionResult result = getNextQuestionInternal(selectedSymptoms, askedSymptoms, allSymptoms, diseases, diseaseSymMap);
        return result;
    }
    
    /**
     * 获取下一个最佳问题（症状ID）
     * 新的逻辑：根据当前最可能的疾病来选择下一个问题
     * 当某个病症的可能性更高时，后续问题会围绕这个病症相关的症状展开提问
     * @param selectedSymptoms 已选择的症状ID列表（用户选择"是"的症状）
     * @param askedSymptoms 所有已问过的症状ID列表（包括选择"是"和"否"的，用于避免重复提问）
     * @param allSymptoms 所有症状列表
     * @param diseases 所有疾病列表
     * @param diseaseSymMap 疾病ID -> 该疾病的所有症状关系列表的映射
     * @return 下一个最佳症状ID，如果没有则返回null
     */
    public static Integer getNextQuestion(List<Integer> selectedSymptoms,
                                          List<Integer> askedSymptoms,
                                          List<Symptom> allSymptoms,
                                          List<Disease> diseases,
                                          Map<Integer, List<DiseaseSymptom>> diseaseSymMap) {
        QuestionResult result = getNextQuestionInternal(selectedSymptoms, askedSymptoms, allSymptoms, diseases, diseaseSymMap);
        return result != null ? result.getSymptomId() : null;
    }
    
    /**
     * 内部方法：获取下一个最佳问题（带解释）
     */
    private static QuestionResult getNextQuestionInternal(List<Integer> selectedSymptoms,
                                                          List<Integer> askedSymptoms,
                                                          List<Symptom> allSymptoms,
                                                          List<Disease> diseases,
                                                          Map<Integer, List<DiseaseSymptom>> diseaseSymMap) {
        if (allSymptoms == null || allSymptoms.isEmpty()) {
            return null;
        }

        // 如果askedSymptoms为null，使用selectedSymptoms作为默认值（向后兼容）
        // 创建一个final变量用于lambda表达式
        final List<Integer> finalAskedSymptoms = (askedSymptoms != null) ? askedSymptoms : selectedSymptoms;

        // 过滤掉已问过的症状（避免重复提问）
        List<Symptom> candidates = allSymptoms.stream()
            .filter(s -> !finalAskedSymptoms.contains(s.getId()))
            .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            return null; // 所有症状都已问过
        }

        // 计算当前所有疾病的概率分布
        Map<Integer, Double> diseaseProbs = calculateDiseaseProbabilities(selectedSymptoms, diseases, diseaseSymMap);
        
        // 找到概率最高的疾病（最可能的疾病）
        Integer topDiseaseId = null;
        double maxProb = 0.0;
        String topDiseaseName = null;
        for (Map.Entry<Integer, Double> entry : diseaseProbs.entrySet()) {
            if (entry.getValue() > maxProb) {
                maxProb = entry.getValue();
                topDiseaseId = entry.getKey();
            }
        }
        
        // 获取疾病名称
        if (topDiseaseId != null && diseases != null) {
            for (Disease d : diseases) {
                if (d.getId().equals(topDiseaseId)) {
                    topDiseaseName = d.getName();
                    break;
                }
            }
        }

        // 如果找到了最可能的疾病（概率>0），优先选择与该疾病相关的症状
        if (topDiseaseId != null && maxProb > 0.1) {
            // 获取该疾病的所有相关症状
            List<DiseaseSymptom> topDiseaseSymptoms = diseaseSymMap.get(topDiseaseId);
            if (topDiseaseSymptoms != null && !topDiseaseSymptoms.isEmpty()) {
                // 创建一个症状ID到权重的映射（权重高的症状优先）
                Map<Integer, Double> symptomWeights = new java.util.HashMap<>();
                for (DiseaseSymptom ds : topDiseaseSymptoms) {
                    if (ds.getSymptomId() != null) {
                        double weight = (ds.getWeight() != null) ? ds.getWeight() : 1.0;
                        // 如果症状是必需的，权重更高
                        if (ds.getIsRequired() != null && ds.getIsRequired()) {
                            weight *= 2.0;
                        }
                        symptomWeights.put(ds.getSymptomId(), weight);
                    }
                }
                
                // 从候选症状中，优先选择与最可能疾病相关的症状
                // 结合信息增益和疾病相关性
                double maxScore = -1.0;
                Integer bestSymptomId = null;
                String bestSymptomName = null;
                
                for (Symptom s : candidates) {
                    // 计算信息增益
                    double infoGain = calculateInformationGain(s.getId(), selectedSymptoms, diseases, diseaseSymMap);
                    
                    // 获取该症状与最可能疾病的相关性权重
                    Double diseaseRelevance = symptomWeights.get(s.getId());
                    double relevance = (diseaseRelevance != null) ? diseaseRelevance : 0.5;
                    
                    // 综合评分：信息增益 * 疾病相关性 * 疾病概率
                    // 当疾病概率高时，更倾向于选择该疾病相关的症状
                    double score = infoGain * relevance * maxProb;
                    
                    if (score > maxScore) {
                        maxScore = score;
                        bestSymptomId = s.getId();
                        bestSymptomName = s.getName();
                    }
                }
                
                if (bestSymptomId != null) {
                    // 生成解释
                    String explanation;
                    if (topDiseaseName != null && maxProb > 0.3) {
                        explanation = String.format("根据当前症状分析，最可能的疾病是「%s」（概率%.1f%%）。这个问题有助于进一步确认或排除该疾病。", 
                            topDiseaseName, maxProb * 100);
                    } else {
                        explanation = "这个问题能够提供最大的诊断信息量，有助于缩小可能的疾病范围。";
                    }
                    return new QuestionResult(bestSymptomId, explanation);
                }
            }
        }
        
        // 如果没有找到最可能的疾病或相关症状，回退到原来的信息增益方法
        double maxGain = -1.0;
        Integer bestSymptomId = null;
        String bestSymptomName = null;

        for (Symptom s : candidates) {
            double gain = calculateInformationGain(s.getId(), selectedSymptoms, diseases, diseaseSymMap);
            if (gain > maxGain) {
                maxGain = gain;
                bestSymptomId = s.getId();
                bestSymptomName = s.getName();
            }
        }

        if (bestSymptomId != null) {
            String explanation = "这个问题能够提供最大的诊断信息量，有助于缩小可能的疾病范围。";
            return new QuestionResult(bestSymptomId, explanation);
        }
        
        return null;
    }
    
    /**
     * 获取下一个最佳问题（症状ID）- 向后兼容的重载方法
     * @param selectedSymptoms 已选择的症状ID列表
     * @param allSymptoms 所有症状列表
     * @param diseases 所有疾病列表
     * @param diseaseSymMap 疾病ID -> 该疾病的所有症状关系列表的映射
     * @return 下一个最佳症状ID，如果没有则返回null
     */
    public static Integer getNextQuestion(List<Integer> selectedSymptoms,
                                          List<Symptom> allSymptoms,
                                          List<Disease> diseases,
                                          Map<Integer, List<DiseaseSymptom>> diseaseSymMap) {
        return getNextQuestion(selectedSymptoms, selectedSymptoms, allSymptoms, diseases, diseaseSymMap);
    }

    /**
     * 判断是否应该结束问诊（概率足够高）
     * @param selectedSymptoms 已选择的症状ID列表
     * @param diseases 所有疾病列表
     * @param diseaseSymMap 疾病ID -> 该疾病的所有症状关系列表的映射
     * @param threshold 概率阈值（默认0.7）
     * @return true表示可以结束问诊，false表示继续提问
     */
    public static boolean shouldStop(List<Integer> selectedSymptoms,
                                      List<Disease> diseases,
                                      Map<Integer, List<DiseaseSymptom>> diseaseSymMap,
                                      double threshold) {
        if (selectedSymptoms == null || selectedSymptoms.isEmpty()) {
            return false;
        }

        Map<Integer, Double> probs = calculateDiseaseProbabilities(selectedSymptoms, diseases, diseaseSymMap);
        
        // 找到最大概率
        double maxProb = probs.values().stream()
            .mapToDouble(Double::doubleValue)
            .max()
            .orElse(0.0);

        return maxProb >= threshold;
    }
}


