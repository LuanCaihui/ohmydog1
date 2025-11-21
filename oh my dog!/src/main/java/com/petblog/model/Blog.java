package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 博客实体类（对应表：blogs）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Blog {
    /**
     * 博客ID（自增主键）
     */
    private Integer blogId;
    /**
     * 用户ID（外键，关联users表）
     */
    private Integer userId;
    /**
     * 博客标题
     */
    private String blogTitle;
    /**
     * 博客内容（大文本）
     */
    private String blogContent;
    /**
     * 博客更新时间
     */
    private Date blogUpdateTime;
    /**
     * 博客创建时间
     */
    private Date blogCreateTime;
    /**
     * 是否被封禁（0：未封禁，1：已封禁）
     */
    private Integer isShielded;
    
    /**
     * 点赞数（统计字段，不存储在blogs表中）
     */
    private Integer likeCount;
    
    /**
     * 收藏数（统计字段，不存储在blogs表中）
     */
    private Integer favoriteCount;
    
    /**
     * 评论数（统计字段，不存储在blogs表中）
     */
    private Integer commentCount;
    
    /**
     * 转发数（统计字段，不存储在blogs表中）
     */
    private Integer repostCount;
    
    /**
     * 用户名（关联查询字段，不存储在blogs表中）
     */
    private String userName;
    
    /**
     * 用户头像路径（关联查询字段，不存储在blogs表中）
     */
    private String userAvatarPath;
}
