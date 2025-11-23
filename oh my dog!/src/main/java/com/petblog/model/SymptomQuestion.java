package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 结构化症状问题模型
 * 用于支持动态问诊系统
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymptomQuestion {
    
    /**
     * 问题ID（对应症状ID）
     */
    private Integer symptomId;
    
    /**
     * 症状名称
     */
    private String symptomName;
    
    /**
     * 问题类型：MAIN_COMPLAINT（主诉）、FOLLOW_UP（追问）、DETAIL（详细属性）
     */
    private QuestionType questionType;
    
    /**
     * 问题文本
     */
    private String questionText;
    
    /**
     * 问题选项（用于单选、多选）
     * 例如：["1-3天", "4-7天", "1-3周", ">3周"]
     */
    private List<String> options;
    
    /**
     * 是否为多选（false为单选）
     */
    private Boolean isMultiple;
    
    /**
     * 父症状ID（如果是追问，指向主诉或父症状）
     */
    private Integer parentSymptomId;
    
    /**
     * 问题维度：TYPE（类型）、DURATION（持续时间）、SEVERITY（严重程度）、
     * TRIGGER（诱因）、ACCOMPANYING（伴随症状）、RED_FLAG（危险信号）
     */
    private QuestionDimension dimension;
    
    /**
     * 问题类型枚举
     */
    public enum QuestionType {
        MAIN_COMPLAINT,  // 主诉
        FOLLOW_UP,       // 追问
        DETAIL           // 详细属性
    }
    
    /**
     * 问题维度枚举
     */
    public enum QuestionDimension {
        TYPE,           // 类型
        DURATION,       // 持续时间
        SEVERITY,       // 严重程度
        TRIGGER,        // 诱因
        ACCOMPANYING,   // 伴随症状
        RED_FLAG        // 危险信号
    }
}

