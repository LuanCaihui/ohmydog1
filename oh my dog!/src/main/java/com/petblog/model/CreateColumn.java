package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 专栏-用户创建关联实体类（对应表：createcolumn）
 * 用于维护用户与专栏的创建关系（多对多）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateColumn {
    /**
     * 专栏ID（联合主键）
     */
    private Integer columnId;
    /**
     * 用户ID（联合主键）
     */
    private Integer userId;
}
