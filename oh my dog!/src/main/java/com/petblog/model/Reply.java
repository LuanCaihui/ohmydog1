package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 回复实体类（对应表：replies）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reply {
    /**
     * 用户ID（外键关联users表，回复者）
     */
    private Integer userId;
    /**
     * 博客ID（外键关联blogs表，回复所属博客）
     */
    private Integer blogId;
    /**
     * 回复ID（自增主键）
     */
    private Integer replyId;
    /**
     * 父回复ID（用于嵌套回复，null表示一级回复）
     */
    private Integer parentReply;
    /**
     * 回复时间
     */
    private Date replyCreatedtime;
    /**
     * 回复内容
     */
    private String replyContent;
    /**
     * 是否可见（0：不可见，1：可见）
     */
    private Integer isVisible;
}
