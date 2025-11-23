package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 疾病-症状关系实体类
 * 对应数据库表：disease_symptoms
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseSymptom {

    /**
     * 主键ID（自增）
     */
    private Integer id;

    /**
     * 疾病ID（外键，关联diseases表）
     */
    private Integer diseaseId;

    /**
     * 症状ID（外键，关联symptoms表）
     */
    private Integer symptomId;

    /**
     * 重要度权重（默认1.0），用于朴素贝叶斯增强权重
     * 示例：犬瘟热 + 发热 → weight = 0.9
     */
    private Float weight;

    /**
     * 是否必需症状（默认false）
     * true表示该症状是该疾病的必需症状
     */
    private Boolean isRequired;

    /**
     * 是否互斥症状（默认false）
     * true表示该症状与该疾病互斥（如果出现此症状，则不可能是该疾病）
     */
    private Boolean isExclusive;
}

