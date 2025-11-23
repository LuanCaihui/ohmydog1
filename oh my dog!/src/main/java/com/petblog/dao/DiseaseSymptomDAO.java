package com.petblog.dao;

import com.petblog.model.DiseaseSymptom;
import java.sql.SQLException;
import java.util.List;

/**
 * 疾病-症状关系数据访问接口
 * 定义对disease_symptoms表的所有数据操作方法
 */
public interface DiseaseSymptomDAO {

    /**
     * 根据ID查询关系详情
     * @param id 关系ID
     * @return 关系实体对象，不存在则返回null
     */
    DiseaseSymptom findById(Integer id) throws SQLException;

    /**
     * 根据疾病ID查询所有相关症状关系
     * @param diseaseId 疾病ID
     * @return 关系列表
     */
    List<DiseaseSymptom> findByDiseaseId(Integer diseaseId) throws SQLException;

    /**
     * 根据症状ID查询所有相关疾病关系
     * @param symptomId 症状ID
     * @return 关系列表
     */
    List<DiseaseSymptom> findBySymptomId(Integer symptomId) throws SQLException;

    /**
     * 根据疾病ID和症状ID查询关系
     * @param diseaseId 疾病ID
     * @param symptomId 症状ID
     * @return 关系实体对象，不存在则返回null
     */
    DiseaseSymptom findByDiseaseAndSymptom(Integer diseaseId, Integer symptomId) throws SQLException;

    /**
     * 查询所有关系
     * @return 关系列表
     */
    List<DiseaseSymptom> findAll() throws SQLException;

    /**
     * 新增疾病-症状关系
     * @param diseaseSymptom 关系实体对象
     * @return 新增关系的ID，失败返回0
     */
    int insert(DiseaseSymptom diseaseSymptom) throws SQLException;

    /**
     * 更新关系权重
     * @param diseaseSymptom 关系实体对象
     * @return 受影响的行数
     */
    int update(DiseaseSymptom diseaseSymptom) throws SQLException;

    /**
     * 删除关系
     * @param id 关系ID
     * @return 受影响的行数
     */
    int delete(Integer id) throws SQLException;

    /**
     * 根据疾病ID和症状ID删除关系
     * @param diseaseId 疾病ID
     * @param symptomId 症状ID
     * @return 受影响的行数
     */
    int deleteByDiseaseAndSymptom(Integer diseaseId, Integer symptomId) throws SQLException;
}

