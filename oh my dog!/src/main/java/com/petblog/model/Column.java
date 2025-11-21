package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 专栏实体类（对应表：columns）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Column {
    /**
     * 专栏ID（自增主键，10位无符号补零）
     */
    private Integer columnId;
    /**
     * 专栏名称
     */
    private String columnName;
    /**
     * 专栏描述（大文本）
     */
    private String columnDescription;
    /**
     * 专栏创建时间
     */
    private Date columnCreatedtime;
    /**
     * 用户ID（外键，关联users表，创建专栏的用户）
     */
    private Integer userId;
}
