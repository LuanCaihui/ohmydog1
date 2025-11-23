package com.petblog.util;

import com.petblog.model.Disease;
import com.petblog.model.DiseaseSymptom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 朴素贝叶斯诊断算法
 * 用于根据用户选择的症状诊断可能的疾病
 * 改进版本：使用改进的几何平均，考虑症状匹配比例
 */
public class NaiveBayes {

    /**
     * 诊断结果类
     */
    public static class Result {
        public String disease;
        public Integer diseaseId;
        public double probability;

        public Result(String disease, Integer diseaseId, double probability) {
            this.disease = disease;
            this.diseaseId = diseaseId;
            this.probability = probability;
        }
    }

    /**
     * 计算改进的归一化概率（考虑症状匹配比例）
     * @param rawProb 原始概率
     * @param symptomCount 症状总数
     * @param matchedSymptomCount 匹配的症状数
     * @return 归一化后的概率
     */
    private static double calculateNormalizedProbability(double rawProb, int symptomCount, int matchedSymptomCount) {
        if (symptomCount == 0) {
            return rawProb;
        }
        
        double matchRatio = (double) matchedSymptomCount / symptomCount;
        
        // 进一步优化的归一化：显著减少惩罚，提高概率值
        if (matchRatio > 0.5) {
            // 高匹配比例（>50%）：使用非常温和的归一化
            // 归一化因子 = 1 / (症状数 * (1 - 匹配比例 * 0.7))
            // 进一步减少归一化惩罚
            double normalizationFactor = 1.0 / (symptomCount * (1.0 - matchRatio * 0.7));
            return Math.pow(rawProb, normalizationFactor);
        } else if (matchRatio > 0.2) {
            // 中等匹配比例（20%-50%）：使用温和的几何平均
            // 使用症状数的0.6次方，进一步减少惩罚
            return Math.pow(rawProb, 0.6 / symptomCount);
        } else {
            // 低匹配比例（<20%）：仍然使用较温和的几何平均
            // 使用0.7次方而不是完整的1.0次方
            return Math.pow(rawProb, 0.7 / symptomCount);
        }
    }

    /**
     * 诊断方法
     * @param symptoms 用户选择的症状ID列表
     * @param diseases 所有疾病列表
     * @param diseaseSymMap 疾病ID -> 该疾病的所有症状关系列表的映射
     * @return 诊断结果（最可能的疾病和概率）
     */
    public static Result diagnose(List<Integer> symptoms,
                                   List<Disease> diseases,
                                   Map<Integer, List<DiseaseSymptom>> diseaseSymMap) {
        if (symptoms == null || symptoms.isEmpty() || diseases == null || diseases.isEmpty()) {
            return null;
        }

        // 存储每个疾病的原始似然值
        Map<Integer, Double> diseaseLikelihoods = new java.util.HashMap<>();
        double totalLikelihood = 0.0;

        for (Disease d : diseases) {
            double p = 1.0;
            List<DiseaseSymptom> dsList = diseaseSymMap.get(d.getId());
            int matchedSymptomCount = 0;

            if (dsList == null || dsList.isEmpty()) {
                p = 0.01; // 没有症状关系的疾病给很小的概率
            } else {
                // 使用疾病的症状总数进行归一化，而不是用户选择的症状数
                int diseaseSymptomCount = dsList.size();
                
                for (Integer sId : symptoms) {
                    boolean found = false;
                    for (DiseaseSymptom ds : dsList) {
                        if (ds.getSymptomId().equals(sId)) {
                            // 检查是否互斥
                            if (ds.getIsExclusive() != null && ds.getIsExclusive()) {
                                p = 0.0; // 互斥症状，概率为0
                                break;
                            }
                            // 利用 weight 作为似然增强
                            if (ds.getWeight() != null) {
                                p *= ds.getWeight();
                            } else {
                                p *= 1.2; // 提高默认权重
                            }
                            matchedSymptomCount++;
                            found = true;
                            break;
                        }
                    }
                    // 未出现的症状 → 进一步提高概率（从0.3改为0.6），减少惩罚
                    // 因为用户可能只选择了部分症状，未选择的症状不应该过度惩罚
                    if (!found) {
                        p *= 0.6;
                    }
                    // 如果已经互斥，直接跳出
                    if (p == 0.0) {
                        break;
                    }
                }
                
                // 使用疾病的症状总数进行归一化，而不是用户选择的症状数
                // 这样更合理，因为一个疾病的症状可能很多，用户不可能全部选择
                if (diseaseSymptomCount > 0 && p > 0.0) {
                    p = calculateNormalizedProbability(p, diseaseSymptomCount, matchedSymptomCount);
                }
            }

            diseaseLikelihoods.put(d.getId(), p);
            totalLikelihood += p;
        }

        // 如果总似然值为0，返回null
        if (totalLikelihood <= 0.0) {
            return null;
        }

        // 归一化概率：找到最大概率的疾病
        double bestProb = 0.0;
        String bestDisease = null;
        Integer bestDiseaseId = null;

        for (Map.Entry<Integer, Double> entry : diseaseLikelihoods.entrySet()) {
            double normalizedProb = entry.getValue() / totalLikelihood;
            if (normalizedProb > bestProb) {
                bestProb = normalizedProb;
                // 找到对应的疾病
                for (Disease d : diseases) {
                    if (d.getId().equals(entry.getKey())) {
                        bestDisease = d.getName();
                        bestDiseaseId = d.getId();
                        break;
                    }
                }
            }
        }

        // 如果最大概率太低，返回null
        if (bestProb < 0.01) {
            return null;
        }

        return new Result(bestDisease, bestDiseaseId, bestProb);
    }
    
    /**
     * 诊断方法 - 返回多个可能的疾病（按概率排序）
     * @param symptoms 用户选择的症状ID列表
     * @param diseases 所有疾病列表
     * @param diseaseSymMap 疾病ID -> 该疾病的所有症状关系列表的映射
     * @param topN 返回前N个最可能的疾病，如果为0或负数则返回所有
     * @return 诊断结果列表（按概率从高到低排序）
     */
    public static List<Result> diagnoseMultiple(List<Integer> symptoms,
                                                List<Disease> diseases,
                                                Map<Integer, List<DiseaseSymptom>> diseaseSymMap,
                                                int topN) {
        if (symptoms == null || symptoms.isEmpty() || diseases == null || diseases.isEmpty()) {
            return new ArrayList<>();
        }

        // 存储每个疾病的原始似然值
        Map<Integer, Double> diseaseLikelihoods = new java.util.HashMap<>();
        double totalLikelihood = 0.0;

        for (Disease d : diseases) {
            double p = 1.0;
            List<DiseaseSymptom> dsList = diseaseSymMap.get(d.getId());
            int matchedSymptomCount = 0;

            if (dsList == null || dsList.isEmpty()) {
                p = 0.01; // 没有症状关系的疾病给很小的概率
            } else {
                // 使用疾病的症状总数进行归一化，而不是用户选择的症状数
                int diseaseSymptomCount = dsList.size();
                
                for (Integer sId : symptoms) {
                    boolean found = false;
                    for (DiseaseSymptom ds : dsList) {
                        if (ds.getSymptomId().equals(sId)) {
                            // 检查是否互斥
                            if (ds.getIsExclusive() != null && ds.getIsExclusive()) {
                                p = 0.0; // 互斥症状，概率为0
                                break;
                            }
                            // 利用 weight 作为似然增强
                            if (ds.getWeight() != null) {
                                p *= ds.getWeight();
                            } else {
                                p *= 1.2; // 提高默认权重
                            }
                            matchedSymptomCount++;
                            found = true;
                            break;
                        }
                    }
                    // 未出现的症状 → 进一步提高概率（从0.3改为0.6），减少惩罚
                    // 因为用户可能只选择了部分症状，未选择的症状不应该过度惩罚
                    if (!found) {
                        p *= 0.6;
                    }
                    // 如果已经互斥，直接跳出
                    if (p == 0.0) {
                        break;
                    }
                }
                
                // 使用疾病的症状总数进行归一化，而不是用户选择的症状数
                // 这样更合理，因为一个疾病的症状可能很多，用户不可能全部选择
                if (diseaseSymptomCount > 0 && p > 0.0) {
                    p = calculateNormalizedProbability(p, diseaseSymptomCount, matchedSymptomCount);
                }
            }

            diseaseLikelihoods.put(d.getId(), p);
            totalLikelihood += p;
        }

        // 如果总似然值为0，返回空列表
        if (totalLikelihood <= 0.0) {
            return new ArrayList<>();
        }

        // 计算所有疾病的归一化概率并排序
        List<Result> results = new ArrayList<>();
        for (Disease d : diseases) {
            Double likelihood = diseaseLikelihoods.get(d.getId());
            if (likelihood != null && likelihood > 0) {
                double normalizedProb = likelihood / totalLikelihood;
                // 只添加概率大于0.01的疾病
                if (normalizedProb >= 0.01) {
                    results.add(new Result(d.getName(), d.getId(), normalizedProb));
                }
            }
        }

        // 按概率从高到低排序
        results.sort((a, b) -> Double.compare(b.probability, a.probability));

        // 如果指定了topN，只返回前N个
        if (topN > 0 && results.size() > topN) {
            return results.subList(0, topN);
        }

        return results;
    }
}
