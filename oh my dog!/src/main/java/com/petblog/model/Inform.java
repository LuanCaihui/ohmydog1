package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 通知实体类（对应表：informs）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inform {
    /**
     * 通知ID（自增主键）
     */
    private Integer informId;
    /**
     * 用户ID（外键，关联users表，通知接收用户）
     */
    private Integer userId;
    /**
     * 通知类型（如：举报驳回、系统通知、审核通知）
     */
    private String informType;
    /**
     * 通知时间
     */
    private Date informTime;
    /**
     * 通知内容
     */
    private String informContent;
    /**
     * 是否已读（0：未读，1：已读）
     */
    private Integer isReaded;
}