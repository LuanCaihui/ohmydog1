package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 话题实体类
 * 对应数据库表：topics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Topic {

    /**
     * 话题ID（主键，自增）
     */
    private Integer topicId;

    /**
     * 话题名称（如"养育成长""健康医疗"）
     */
    private String topicName;

    /**
     * 话题创建时间
     */
    private LocalDateTime topicCreateTime;
}
