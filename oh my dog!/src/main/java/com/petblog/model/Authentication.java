package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证信息实体类
 * 对应数据库表：authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Authentication {

    /**
     * 认证ID（主键）
     */
    private Integer authenticationId;

    /**
     * 单位信息（非空）
     */
    private String unit;

    /**
     * 职称信息（非空）
     */
    private String title;
}

