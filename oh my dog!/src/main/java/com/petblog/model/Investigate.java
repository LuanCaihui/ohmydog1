package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 药品-用户调查关联实体类（对应表：investigate）
 * 用于维护用户与宠物药品的调查关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Investigate {
    /**
     * 药品ID（联合主键，关联petmedicine表）
     */
    private Integer medicineId;
    /**
     * 用户ID（联合主键，关联users表）
     */
    private Integer userId;
}