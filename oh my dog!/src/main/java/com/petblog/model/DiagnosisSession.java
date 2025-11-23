package com.petblog.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 诊断会话状态类
 * 封装当前问诊的状态信息
 */
@Data
public class DiagnosisSession {
    
    /**
     * 用户回答"是"的症状ID列表
     */
    private List<Integer> selectedSymptoms = new ArrayList<>();
    
    /**
     * 用户回答过（是/否/不确定）的症状ID列表
     */
    private List<Integer> askedSymptoms = new ArrayList<>();
    
    /**
     * 用户回答"不确定"的症状ID列表（用于区分"否"和"不确定"）
     */
    private List<Integer> uncertainSymptoms = new ArrayList<>();
    
    /**
     * 已回答的问题总数
     */
    private int questionCount = 0;
    
    /**
     * 主诉ID（用户最初选择的症状）
     */
    private Integer mainComplaintId;
    
    /**
     * 主诉类别（用于兼容旧逻辑，可选）
     */
    private String mainComplaintCategory;
    
    /**
     * 最近问过的症状ID列表（用于避免连续问相同的问题）
     * 记录最近3个问题，避免重复
     */
    private List<Integer> recentAskedSymptoms = new ArrayList<>();
    
    /**
     * 添加已选择的症状
     */
    public void addSelectedSymptom(Integer symptomId) {
        if (!selectedSymptoms.contains(symptomId)) {
            selectedSymptoms.add(symptomId);
        }
    }
    
    /**
     * 添加已问过的症状
     */
    public void addAskedSymptom(Integer symptomId) {
        if (!askedSymptoms.contains(symptomId)) {
            askedSymptoms.add(symptomId);
        }
    }
    
    /**
     * 增加问题计数
     */
    public void incrementQuestionCount() {
        questionCount++;
    }
    
    /**
     * 添加最近问过的症状（用于避免重复）
     */
    public void addRecentAskedSymptom(Integer symptomId) {
        if (!recentAskedSymptoms.contains(symptomId)) {
            recentAskedSymptoms.add(symptomId);
            // 只保留最近3个
            if (recentAskedSymptoms.size() > 3) {
                recentAskedSymptoms.remove(0);
            }
        }
    }
    
    /**
     * 检查症状是否最近问过
     */
    public boolean isRecentlyAsked(Integer symptomId) {
        return recentAskedSymptoms.contains(symptomId);
    }
    
    /**
     * 添加不确定的症状
     */
    public void addUncertainSymptom(Integer symptomId) {
        if (!uncertainSymptoms.contains(symptomId)) {
            uncertainSymptoms.add(symptomId);
        }
    }
    
    /**
     * 检查症状是否被标记为不确定
     */
    public boolean isUncertain(Integer symptomId) {
        return uncertainSymptoms.contains(symptomId);
    }
}

