package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 挑战实体类（对应表：challenges）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Challenge {
    /**
     * 挑战ID（自增主键）
     */
    private Integer challengeId;
    /**
     * 挑战标题
     */
    private String challengeTitle;
    /**
     * 挑战开始时间
     */
    private Date challengeStartTime;
    /**
     * 挑战结束时间
     */
    private Date challengeEndTime;
    /**
     * 挑战状态（如：进行中、已结束、规划中）
     */
    private String chellengeStatus;
    /**
     * 是否被取消（0：未取消，1：已取消）
     */
    private Integer challengeIsCancell;
    /**
     * 用户ID（外键，关联users表，创建挑战的用户）
     * 注：数据库字段名拼写错误（uer_id），实体类按正确语义命名
     */
    private Integer userId;
    /**
     * 挑战描述
     */
    private String challengeDescription;
}
