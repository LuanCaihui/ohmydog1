package com.petblog.dao;

import com.petblog.model.Symptom;
import java.sql.SQLException;
import java.util.List;

/**
 * 症状数据访问接口
 * 定义对symptoms表的所有数据操作方法
 */
public interface SymptomDAO {

    /**
     * 根据症状ID查询症状详情
     * @param id 症状ID
     * @return 症状实体对象，不存在则返回null
     */
    Symptom findById(Integer id) throws SQLException;

    /**
     * 查询所有症状
     * @return 症状列表
     */
    List<Symptom> findAll() throws SQLException;

    /**
     * 根据症状名称查询症状（支持模糊查询）
     * @param name 症状名称
     * @return 症状列表
     */
    List<Symptom> findByName(String name) throws SQLException;

    /**
     * 新增症状
     * @param symptom 症状实体对象
     * @return 新增症状的ID，失败返回0
     */
    int insert(Symptom symptom) throws SQLException;

    /**
     * 更新症状信息
     * @param symptom 症状实体对象
     * @return 受影响的行数
     */
    int update(Symptom symptom) throws SQLException;

    /**
     * 删除症状
     * @param id 症状ID
     * @return 受影响的行数
     */
    int delete(Integer id) throws SQLException;
    
    /**
     * 关键方法：获取 Top N 疾病相关联的所有症状，并排除已问过的
     * 用于智能问题选择：根据当前概率最高的疾病来动态生成问题
     * 
     * @param topDiseaseIds 概率最高的几个疾病ID列表
     * @param askedSymptomIds 已经问过的症状ID列表
     * @return 候选症状列表（包含症状信息和权重），按权重降序排列
     */
    List<com.petblog.model.SymptomWithWeight> findCandidateSymptoms(
            List<Integer> topDiseaseIds, 
            List<Integer> askedSymptomIds
    ) throws SQLException;
    
    /**
     * 按类别查找症状，排除已问过的症状
     * 用于回退机制：当疾病关联的症状不足时，从同类别症状中补充
     * 
     * @param category 症状类别
     * @param askedSymptomIds 已经问过的症状ID列表
     * @param limit 最多返回的症状数量
     * @return 症状列表
     */
    List<Symptom> findByCategory(String category, List<Integer> askedSymptomIds, int limit) throws SQLException;
}

