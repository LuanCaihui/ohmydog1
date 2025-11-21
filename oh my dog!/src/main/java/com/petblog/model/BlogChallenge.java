package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 博客-挑战关联实体类（对应表：blogchallenge）
 * 用于维护博客与挑战的多对多关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogChallenge {
    /**
     * 挑战ID（联合主键）
     */
    private Integer challengeId;
    /**
     * 博客ID（联合主键）
     */
    private Integer blogId;
}
