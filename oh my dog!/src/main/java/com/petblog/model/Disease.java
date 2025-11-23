package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 疾病实体类
 * 对应数据库表：diseases
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Disease {

    /**
     * 疾病ID（主键，自增）
     */
    private Integer id;

    /**
     * 疾病名称（如"犬瘟热"）
     */
    private String name;

    /**
     * 器官系统（非空，如"呼吸系统"、"消化系统"等）
     */
    private String organSystem;

    /**
     * 疾病描述（症状说明、病因等，可空）
     */
    private String description;
}

