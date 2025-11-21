package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 宠物-挑战关联实体类（对应表：petchallenge）
 * 用于维护宠物与挑战的多对多关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetChallenge {
    /**
     * 宠物ID（联合主键，关联pets表）
     */
    private Integer petId;
    /**
     * 挑战ID（联合主键，关联challenges表）
     */
    private Integer challengeId;
}