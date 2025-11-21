package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 收藏实体类（对应表：favorites）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {
    /**
     * 用户ID（联合主键，关联users表）
     */
    private Integer userId;
    /**
     * 博客ID（联合主键，关联blogs表）
     */
    private Integer blogId;
    /**
     * 收藏时间（默认当前时间）
     */
    private Date favoriteTime;
}