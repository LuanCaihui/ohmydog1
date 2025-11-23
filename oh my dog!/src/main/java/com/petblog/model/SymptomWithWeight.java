package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 带权重的症状类
 * 用于在问题选择时临时存储症状及其在特定疾病中的权重
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymptomWithWeight {
    
    /**
     * 症状对象
     */
    private Symptom symptom;
    
    /**
     * 该症状在疾病中的权重
     */
    private Double weight;
    
    /**
     * 关联的疾病ID（用于计算得分）
     */
    private Integer diseaseId;
    
    /**
     * 是否必需症状
     */
    private Boolean isRequired;
    
    /**
     * 是否互斥症状
     */
    private Boolean isExclusive;
}

