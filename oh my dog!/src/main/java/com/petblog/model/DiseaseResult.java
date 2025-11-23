package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 疾病诊断结果类
 * 封装疾病及其计算出的概率，用于排序和比较
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseResult implements Comparable<DiseaseResult> {
    
    /**
     * 疾病对象
     */
    private Disease disease;
    
    /**
     * 当前计算出的概率（0.0 - 1.0）
     */
    private double probability;
    
    /**
     * 支持该疾病的证据症状列表（用于前端展示）
     */
    private java.util.List<DiseaseSymptom> evidenceSymptoms;
    
    /**
     * 诊疗建议
     */
    private String recommendation;
    
    /**
     * 降序排列，概率高的在前
     */
    @Override
    public int compareTo(DiseaseResult o) {
        return Double.compare(o.probability, this.probability);
    }
}

