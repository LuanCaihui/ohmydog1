package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * 投票实体类（用户对博客的投票记录）
 * 对应数据库表：votes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vote {

    /**
     * 投票时间（默认当前时间）
     */
    private LocalDateTime voteCreateTime;

    /**
     * 用户ID（复合主键之一，关联users表的user_id）
     */
    private Integer userId;

    /**
     * 博客ID（复合主键之一，关联blogs表的blog_id）
     */
    private Integer blogId;

    /**
     * 复合主键类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VotePK implements Serializable {
        private Integer userId;
        private Integer blogId;
    }
}
