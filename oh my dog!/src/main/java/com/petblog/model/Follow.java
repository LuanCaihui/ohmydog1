package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 关注实体类（对应表：follows）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Follow {
    /**
     * 关注者ID（联合主键，关联users表）
     */
    private Integer followerId;
    /**
     * 被关注者ID（联合主键，关联users表）
     */
    private Integer followeeId;
    /**
     * 关注时间
     */
    private Date followTime;
}