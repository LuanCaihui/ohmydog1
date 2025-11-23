package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 问诊记录实体类
 * 对应数据库表：consultations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consultation {

    /**
     * 主键ID（自增）
     */
    private Integer id;

    /**
     * 用户ID（外键，关联users表）
     */
    private Integer userId;

    /**
     * 用户选择的症状（JSON格式存储）
     */
    private String selectedSymptoms;

    /**
     * 系统诊断的疾病ID（外键，关联diseases表）
     */
    private Integer resultDiseaseId;

    /**
     * 置信度（0.0-1.0）
     */
    private Float probability;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

