package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 举报实体类
 * 对应数据库表：reports
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    /**
     * 博客ID（外键，关联blogs表，被举报博客）
     */
    private Integer blogId;

    /**
     * 用户ID（外键，关联users表，举报人）
     */
    private Integer userId;

    /**
     * 举报ID（主键，自增，降序排列）
     */
    private Integer reportId;

    /**
     * 举报原因
     */
    private String reason;

    /**
     * 举报状态（0：未处理，1：已处理-封禁，2：已处理-仅封博客，3：已处理-驳回）
     */
    private Integer reportStatus;

    /**
     * 举报时间
     */
    private LocalDateTime reportCreatedTime;

    /**
     * 举报处理时间
     */
    private LocalDateTime reportHandledTime;
}