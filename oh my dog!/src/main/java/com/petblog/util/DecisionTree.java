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
     * 改进版：考虑症状的稀有性（在越少疾病中出现的症状，信息增益越高）
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
        double baseGain = currentEntropy - newEntropy;
        
        // 计算症状的稀有性和诊断价值
        int diseaseCount = 0;
        double totalWeight = 0.0;
        double maxWeight = 0.0;
        boolean hasRequired = false;
        
        // 统计该症状在哪些疾病中出现，以及权重信息
        Map<Integer, Double> diseaseWeights = new java.util.HashMap<>();
        for (Map.Entry<Integer, List<DiseaseSymptom>> entry : diseaseSymMap.entrySet()) {
            for (DiseaseSymptom ds : entry.getValue()) {
                if (ds.getSymptomId() != null && ds.getSymptomId().equals(candidateSymptomId)) {
                    diseaseCount++;
                    double weight = (ds.getWeight() != null) ? ds.getWeight() : 0.5;
                    totalWeight += weight;
                    if (weight > maxWeight) {
                        maxWeight = weight;
                    }
                    if (ds.getIsRequired() != null && ds.getIsRequired()) {
                        hasRequired = true;
                    }
                    diseaseWeights.put(entry.getKey(), weight);
                    break;
                }
            }
        }
        
        // 如果症状在疾病-症状关系表中没有关联，给予负的奖励（降低优先级）
        // 因为这样的症状对诊断没有帮助
        if (diseaseCount == 0) {
            // 没有关联的症状，信息增益应该很小或为负
            return baseGain - 0.5; // 惩罚没有关联的症状
        }
        
        // 稀有性因子：症状在越少疾病中出现，稀有性越高（但至少要在1个疾病中出现）
        // 如果症状在所有疾病中都出现，稀有性接近0
        // 如果症状只在1个疾病中出现，稀有性接近1
        double rarityFactor = 1.0 / (diseaseCount + 1);
        
        // 考虑权重：权重高的症状更有价值
        double avgWeight = totalWeight / diseaseCount;
        // 如果症状是必需的，增加价值
        double requiredBonus = hasRequired ? 0.3 : 0.0;
        
        // 稀有性奖励 = 稀有性因子 × (平均权重 + 必需奖励)
        double rarityReward = rarityFactor * (0.5 + avgWeight + requiredBonus);
        
        // 计算症状对概率分布的影响程度
        // 如果选择该症状后，概率分布变化很大，说明这个症状很有诊断价值
        double probChange = 0.0;
        for (Integer diseaseId : currentProbs.keySet()) {
            double oldProb = currentProbs.get(diseaseId);
            double newProb = newProbs.getOrDefault(diseaseId, 0.0);
            probChange += Math.abs(oldProb - newProb);
        }
        
        // 综合信息增益 = 基础信息增益 + 稀有性奖励 + 概率变化奖励
        // 概率变化越大，说明症状越有诊断价值
        double finalGain = baseGain + rarityReward * 0.3 + probChange * 0.2;
        
        return finalGain;
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
     * @param mainComplaintId 主诉症状ID（可选，用于过滤相关问题）
     * @return 问题结果（包含症状ID和解释），如果没有则返回null
     */
    public static QuestionResult getNextQuestionWithExplanation(List<Integer> selectedSymptoms,
                                                                List<Integer> askedSymptoms,
                                                                List<Symptom> allSymptoms,
                                                                List<Disease> diseases,
                                                                Map<Integer, List<DiseaseSymptom>> diseaseSymMap,
                                                                Integer mainComplaintId) {
        QuestionResult result = getNextQuestionInternal(selectedSymptoms, askedSymptoms, allSymptoms, diseases, diseaseSymMap, mainComplaintId);
        return result;
    }
    
    /**
     * 获取下一个最佳问题（带解释）- 向后兼容的重载方法（不包含主诉ID）
     */
    public static QuestionResult getNextQuestionWithExplanation(List<Integer> selectedSymptoms,
                                                                List<Integer> askedSymptoms,
                                                                List<Symptom> allSymptoms,
                                                                List<Disease> diseases,
                                                                Map<Integer, List<DiseaseSymptom>> diseaseSymMap) {
        return getNextQuestionWithExplanation(selectedSymptoms, askedSymptoms, allSymptoms, diseases, diseaseSymMap, null);
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
        QuestionResult result = getNextQuestionInternal(selectedSymptoms, askedSymptoms, allSymptoms, diseases, diseaseSymMap, null);
        return result != null ? result.getSymptomId() : null;
    }
    
    /**
     * 内部方法：获取下一个最佳问题（带解释）
     * 修改后的逻辑：根据主诉类别，只问同类别症状，使用信息增益选择最佳问题
     */
    private static QuestionResult getNextQuestionInternal(List<Integer> selectedSymptoms,
                                                          List<Integer> askedSymptoms,
                                                          List<Symptom> allSymptoms,
                                                          List<Disease> diseases,
                                                          Map<Integer, List<DiseaseSymptom>> diseaseSymMap,
                                                          Integer mainComplaintId) {
        if (allSymptoms == null || allSymptoms.isEmpty()) {
            return null;
        }

        // 如果askedSymptoms为null，使用selectedSymptoms作为默认值（向后兼容）
        // 创建一个final变量用于lambda表达式
        final List<Integer> finalAskedSymptoms = (askedSymptoms != null) ? askedSymptoms : selectedSymptoms;

        // 获取主诉的类别（如果提供了主诉ID）
        String mainComplaintCategory = null;
        System.out.println("决策树：接收到的mainComplaintId=" + mainComplaintId);
        if (mainComplaintId != null && allSymptoms != null) {
            for (Symptom s : allSymptoms) {
                if (s.getId() != null && s.getId().equals(mainComplaintId)) {
                    mainComplaintCategory = s.getCategory();
                    System.out.println("决策树：主诉ID=" + mainComplaintId + ", 主诉名称=" + s.getName() + ", 主诉类别=" + mainComplaintCategory);
                    break;
                }
            }
            if (mainComplaintCategory == null) {
                System.out.println("决策树：警告 - 主诉ID=" + mainComplaintId + " 在症状列表中未找到");
            }
        } else {
            System.out.println("决策树：警告 - mainComplaintId为null或allSymptoms为null");
        }

        // 必须要有主诉类别，否则无法进行问诊
        if (mainComplaintCategory == null || mainComplaintCategory.isEmpty()) {
            System.out.println("决策树：没有主诉类别（mainComplaintId=" + mainComplaintId + ", mainComplaintCategory=" + mainComplaintCategory + "），无法进行问诊，结束诊断");
            return null;
        }

        // 过滤掉已问过的症状（避免重复提问）
        final String finalMainComplaintCategory = mainComplaintCategory;
        List<Symptom> candidates = allSymptoms.stream()
            .filter(s -> !finalAskedSymptoms.contains(s.getId()))
            .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            return null; // 所有症状都已问过
        }
        
        // 只保留同类别症状（强制要求）
        List<Symptom> sameCategoryCandidates = candidates.stream()
            .filter(s -> finalMainComplaintCategory.equals(s.getCategory()))
            .collect(Collectors.toList());
        
        // 如果还有同类别的问题可以问，继续问同类别的问题
        if (!sameCategoryCandidates.isEmpty()) {
            candidates = sameCategoryCandidates;
            System.out.println("决策树：主诉类别=" + finalMainComplaintCategory + "，筛选出" + candidates.size() + "个同类别候选症状");
        } else {
            // 同类别症状已问完，直接结束诊断
            System.out.println("决策树：主诉类别=" + finalMainComplaintCategory + "，同类别症状已问完，结束诊断");
            return null;
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
                
                System.out.println("决策树：开始从" + candidates.size() + "个同类别候选症状中选择最佳问题（优先考虑疾病ID=" + topDiseaseId + "）");
                for (Symptom s : candidates) {
                    // 验证类别匹配
                    if (!finalMainComplaintCategory.equals(s.getCategory())) {
                        continue;
                    }
                    
                    // 计算信息增益
                    double infoGain = calculateInformationGain(s.getId(), selectedSymptoms, diseases, diseaseSymMap);
                    
                    // 获取该症状与最可能疾病的相关性权重
                    Double diseaseRelevance = symptomWeights.get(s.getId());
                    double relevance = (diseaseRelevance != null) ? diseaseRelevance : 0.5;
                    
                    // 综合评分：信息增益 × 疾病相关性权重 × 疾病概率
                    // 当疾病概率高时，更倾向于选择该疾病相关的症状
                    double score = infoGain * relevance * maxProb;
                    
                    System.out.println("决策树：症状 " + s.getName() + " (ID=" + s.getId() + ") - 信息增益=" + String.format("%.6f", infoGain) + 
                                      ", 疾病相关性=" + String.format("%.2f", relevance) + 
                                      ", 疾病概率=" + String.format("%.2f", maxProb) + 
                                      ", 综合评分=" + String.format("%.6f", score));
                    
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
                    System.out.println("决策树：选择症状 " + bestSymptomName + " (ID=" + bestSymptomId + ")，综合评分=" + String.format("%.6f", maxScore));
                    return new QuestionResult(bestSymptomId, explanation);
                }
            }
        }
        
        // 如果没有找到最可能的疾病或相关症状，回退到原来的信息增益方法
        // 但是，在纯信息增益方法中，如果某个症状与某个疾病有关联，仍然要优先考虑
        // 重新计算疾病概率（因为可能已经选择了新症状）
        diseaseProbs = calculateDiseaseProbabilities(selectedSymptoms, diseases, diseaseSymMap);
        
        // 重新找到概率最高的疾病
        topDiseaseId = null;
        maxProb = 0.0;
        topDiseaseName = null;
        for (Map.Entry<Integer, Double> entry : diseaseProbs.entrySet()) {
            if (entry.getValue() > maxProb) {
                maxProb = entry.getValue();
                topDiseaseId = entry.getKey();
            }
        }
        
        if (topDiseaseId != null && diseases != null) {
            for (Disease d : diseases) {
                if (d.getId().equals(topDiseaseId)) {
                    topDiseaseName = d.getName();
                    break;
                }
            }
        }
        
        // 如果现在满足条件（概率>10%），再次尝试优先选择方法
        if (topDiseaseId != null && maxProb > 0.1) {
            List<DiseaseSymptom> topDiseaseSymptoms = diseaseSymMap.get(topDiseaseId);
            if (topDiseaseSymptoms != null && !topDiseaseSymptoms.isEmpty()) {
                Map<Integer, Double> symptomWeights = new java.util.HashMap<>();
                for (DiseaseSymptom ds : topDiseaseSymptoms) {
                    if (ds.getSymptomId() != null) {
                        double weight = (ds.getWeight() != null) ? ds.getWeight() : 1.0;
                        if (ds.getIsRequired() != null && ds.getIsRequired()) {
                            weight *= 2.0;
                        }
                        symptomWeights.put(ds.getSymptomId(), weight);
                    }
                }
                
                double maxScore = -1.0;
                Integer bestSymptomId = null;
                String bestSymptomName = null;
                
                System.out.println("决策树：在纯信息增益阶段重新检查，发现疾病概率=" + String.format("%.2f", maxProb) + "，再次启用优先选择方法（疾病ID=" + topDiseaseId + "）");
                for (Symptom s : candidates) {
                    if (!finalMainComplaintCategory.equals(s.getCategory())) {
                        continue;
                    }
                    
                    double infoGain = calculateInformationGain(s.getId(), selectedSymptoms, diseases, diseaseSymMap);
                    Double diseaseRelevance = symptomWeights.get(s.getId());
                    double relevance = (diseaseRelevance != null) ? diseaseRelevance : 0.5;
                    double score = infoGain * relevance * maxProb;
                    
                    System.out.println("决策树：症状 " + s.getName() + " (ID=" + s.getId() + ") - 信息增益=" + String.format("%.6f", infoGain) + 
                                      ", 疾病相关性=" + String.format("%.2f", relevance) + 
                                      ", 疾病概率=" + String.format("%.2f", maxProb) + 
                                      ", 综合评分=" + String.format("%.6f", score));
                    
                    if (score > maxScore) {
                        maxScore = score;
                        bestSymptomId = s.getId();
                        bestSymptomName = s.getName();
                    }
                }
                
                if (bestSymptomId != null) {
                    String explanation;
                    if (topDiseaseName != null && maxProb > 0.3) {
                        explanation = String.format("根据当前症状分析，最可能的疾病是「%s」（概率%.1f%%）。这个问题有助于进一步确认或排除该疾病。", 
                            topDiseaseName, maxProb * 100);
                    } else {
                        explanation = "这个问题能够提供最大的诊断信息量，有助于缩小可能的疾病范围。";
                    }
                    System.out.println("决策树：选择症状 " + bestSymptomName + " (ID=" + bestSymptomId + ")，综合评分=" + String.format("%.6f", maxScore));
                    return new QuestionResult(bestSymptomId, explanation);
                }
            }
        }
        
        // 如果仍然不满足条件，使用纯信息增益方法
        double maxGain = -1.0;
        Integer bestSymptomId = null;
        String bestSymptomName = null;

        System.out.println("决策树：回退到纯信息增益方法，从" + candidates.size() + "个同类别候选症状中选择");
        for (Symptom s : candidates) {
            // 验证类别匹配
            if (!finalMainComplaintCategory.equals(s.getCategory())) {
                continue;
            }
            
            double gain = calculateInformationGain(s.getId(), selectedSymptoms, diseases, diseaseSymMap);
            System.out.println("决策树：症状 " + s.getName() + " (ID=" + s.getId() + ", 类别=" + s.getCategory() + ") 的信息增益=" + String.format("%.6f", gain));
            
            if (gain > maxGain) {
                maxGain = gain;
                bestSymptomId = s.getId();
                bestSymptomName = s.getName();
            }
        }

        if (bestSymptomId != null) {
            System.out.println("决策树：选择症状 " + bestSymptomName + " (ID=" + bestSymptomId + ")，信息增益=" + String.format("%.6f", maxGain));
            String explanation = "这个问题能够提供最大的诊断信息量，有助于缩小可能的疾病范围。";
            return new QuestionResult(bestSymptomId, explanation);
        } else {
            System.out.println("决策树：没有找到合适的同类别症状");
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


