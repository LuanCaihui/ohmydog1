package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 宠物药品实体类（对应表：petmedicine）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medicine {
    /**
     * 药品ID（自增主键）
     */
    private Integer medicineId;
    /**
     * 药品名称（非空）
     */
    private String medicineName;
    /**
     * 药品说明（非空，大文本）
     */
    private String content;
    /**
     * 发布时间（非空）
     */
    private Date releaseTime;
    /**
     * 用户ID（非空，外键关联users表，发布药品的用户）
     */
    private Integer userId;
}
