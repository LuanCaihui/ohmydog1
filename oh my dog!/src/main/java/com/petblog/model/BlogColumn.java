package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 博客-专栏关联实体类（对应表：blogcolumn）
 * 用于维护博客与专栏的多对多关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogColumn {
    /**
     * 博客ID（联合主键，10位无符号补零）
     */
    private Integer blogId;
    /**
     * 专栏ID（联合主键）
     */
    private Integer columnId;
}