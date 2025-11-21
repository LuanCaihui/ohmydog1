package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 消息实体类（对应表：message）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    /**
     * 发送者ID（联合主键，关联users表）
     */
    private Integer senderId;
    /**
     * 接收者ID（联合主键，关联users表）
     */
    private Integer receiverId;
    /**
     * 消息ID（联合主键）
     */
    private Integer messageId;
    /**
     * 消息内容
     */
    private String messageContent;
    /**
     * 消息创建时间
     */
    private Date creationTime;
    /**
     * 消息更新时间（如：撤回时间）
     */
    private Date updateTime;
    /**
     * 是否撤回（0：未撤回，1：已撤回）
     */
    private Integer isWithdraw;
    /**
     * 是否已读（0：未读，1：已读）
     */
    private Integer isRead;
}
