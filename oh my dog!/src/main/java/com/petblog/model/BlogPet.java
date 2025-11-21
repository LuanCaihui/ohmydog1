package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 博客-宠物关联实体类（对应表：blogpet）
 * 用于维护博客与宠物的多对多关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogPet {
    /**
     * 博客ID（联合主键）
     */
    private Integer blogId;
    /**
     * 宠物ID（联合主键）
     */
    private Integer petId;
}
