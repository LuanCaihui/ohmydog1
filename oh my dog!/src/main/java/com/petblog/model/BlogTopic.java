package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 博客-话题关联实体类（对应表：blogtopic）
 * 用于维护博客与话题的多对多关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogTopic {
    /**
     * 博客ID（联合主键）
     */
    private Integer blogId;
    /**
     * 话题ID（联合主键）
     */
    private Integer topicId;
}
