package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * 转发表实体类（用户转发博客的记录）
 * 对应数据库表：reposts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Repost {

    /**
     * 原博客ID（复合主键之一，关联blogs表的blog_id）
     */
    private Integer blogId;

    /**
     * 用户ID（复合主键之一，关联users表的user_id，标识转发者）
     */
    private Integer userId;

    /**
     * 转发时间
     */
    private LocalDateTime repostsTime;

    /**
     * 新博客ID（转发后生成的新博客ID，关联blogs表的blog_id）
     */
    private Integer repostId;

    /**
     * 复合主键类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepostPK implements Serializable {
        private Integer blogId;
        private Integer userId;
    }
}
