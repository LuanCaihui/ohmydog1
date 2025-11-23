package com.petblog.dao;

import com.petblog.model.Disease;
import java.sql.SQLException;
import java.util.List;

/**
 * 疾病数据访问接口
 * 定义对diseases表的所有数据操作方法
 */
public interface DiseaseDAO {

    /**
     * 根据疾病ID查询疾病详情
     * @param id 疾病ID
     * @return 疾病实体对象，不存在则返回null
     */
    Disease findById(Integer id) throws SQLException;

    /**
     * 查询所有疾病
     * @return 疾病列表
     */
    List<Disease> findAll() throws SQLException;

    /**
     * 根据疾病名称查询疾病（支持模糊查询）
     * @param name 疾病名称
     * @return 疾病列表
     */
    List<Disease> findByName(String name) throws SQLException;

    /**
     * 根据症状ID查询相关疾病（通过disease_symptoms关联表）
     * @param symptomId 症状ID
     * @return 疾病列表
     */
    List<Disease> findBySymptomId(Integer symptomId) throws SQLException;

    /**
     * 根据多个症状ID查询相关疾病（用于诊断）
     * @param symptomIds 症状ID列表
     * @return 疾病列表（包含权重信息）
     */
    List<Disease> findBySymptomIds(List<Integer> symptomIds) throws SQLException;

    /**
     * 新增疾病
     * @param disease 疾病实体对象
     * @return 新增疾病的ID，失败返回0
     */
    int insert(Disease disease) throws SQLException;

    /**
     * 更新疾病信息
     * @param disease 疾病实体对象
     * @return 受影响的行数
     */
    int update(Disease disease) throws SQLException;

    /**
     * 删除疾病
     * @param id 疾病ID
     * @return 受影响的行数
     */
    int delete(Integer id) throws SQLException;
}

