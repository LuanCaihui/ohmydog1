package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 宠物实体类（对应表：pets）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pet {
    /**
     * 宠物ID（自增主键）
     */
    private Integer petId;
    /**
     * 宠物名称（非空）
     */
    private String petName;
    /**
     * 宠物性别（非空，如：公、母、未知）
     */
    private String petGender;
    /**
     * 宠物品种（非空，如：萨摩耶、德国牧羊犬）
     */
    private String petBreed;
    /**
     * 宠物出生日期（非空）
     */
    private Date petBirthdate;
    /**
     * 用户ID（非空，外键关联users表，宠物所属用户）
     */
    private Integer userId;
    /**
     * 宠物头像路径（非空）
     */
    private String petAvatarPath;
}