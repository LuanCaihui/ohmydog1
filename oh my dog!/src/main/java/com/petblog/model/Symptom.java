package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 症状实体类
 * 对应数据库表：symptoms
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Symptom {

    /**
     * 症状ID（主键，自增）
     */
    private Integer id;

    /**
     * 症状名称（如"呕吐"）
     */
    private String name;

    /**
     * 症状分类（可空，如"消化系统"、"呼吸系统"等）
     */
    private String category;
}

