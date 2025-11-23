package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.DiseaseSymptomDAO;
import com.petblog.model.DiseaseSymptom;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 疾病-症状关系数据访问实现类
 */
public class DiseaseSymptomDAOImpl extends BaseJdbcDAO<DiseaseSymptom> implements DiseaseSymptomDAO {

    @Override
    public DiseaseSymptom findById(Integer id) throws SQLException {
        String sql = "SELECT id, disease_id, symptom_id, weight, is_required, is_exclusive FROM disease_symptoms WHERE id = ?";
        try {
            return queryForObject(sql, this::mapRowToDiseaseSymptom, id);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询疾病-症状关系", null);
        }
    }

    @Override
    public List<DiseaseSymptom> findByDiseaseId(Integer diseaseId) throws SQLException {
        String sql = "SELECT id, disease_id, symptom_id, weight, is_required, is_exclusive FROM disease_symptoms WHERE disease_id = ? ORDER BY weight DESC";
        try {
            return queryForList(sql, this::mapRowToDiseaseSymptom, diseaseId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据疾病ID查询关系", null);
        }
    }

    @Override
    public List<DiseaseSymptom> findBySymptomId(Integer symptomId) throws SQLException {
        String sql = "SELECT id, disease_id, symptom_id, weight, is_required, is_exclusive FROM disease_symptoms WHERE symptom_id = ? ORDER BY weight DESC";
        try {
            return queryForList(sql, this::mapRowToDiseaseSymptom, symptomId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据症状ID查询关系", null);
        }
    }

    @Override
    public DiseaseSymptom findByDiseaseAndSymptom(Integer diseaseId, Integer symptomId) throws SQLException {
        String sql = "SELECT id, disease_id, symptom_id, weight, is_required, is_exclusive FROM disease_symptoms WHERE disease_id = ? AND symptom_id = ?";
        try {
            return queryForObject(sql, this::mapRowToDiseaseSymptom, diseaseId, symptomId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据疾病和症状ID查询关系", null);
        }
    }

    @Override
    public List<DiseaseSymptom> findAll() throws SQLException {
        String sql = "SELECT id, disease_id, symptom_id, weight, is_required, is_exclusive FROM disease_symptoms ORDER BY id";
        try {
            return queryForList(sql, this::mapRowToDiseaseSymptom);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询所有疾病-症状关系", null);
        }
    }

    @Override
    public int insert(DiseaseSymptom diseaseSymptom) throws SQLException {
        String sql = "INSERT INTO disease_symptoms (disease_id, symptom_id, weight, is_required, is_exclusive) VALUES (?, ?, ?, ?, ?)";
        try {
            Float weight = diseaseSymptom.getWeight() != null ? diseaseSymptom.getWeight() : 1.0f;
            Boolean isRequired = diseaseSymptom.getIsRequired() != null ? diseaseSymptom.getIsRequired() : false;
            Boolean isExclusive = diseaseSymptom.getIsExclusive() != null ? diseaseSymptom.getIsExclusive() : false;
            return insert(sql, diseaseSymptom.getDiseaseId(), diseaseSymptom.getSymptomId(), weight, isRequired, isExclusive);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "新增疾病-症状关系", 0);
        }
    }

    @Override
    public int update(DiseaseSymptom diseaseSymptom) throws SQLException {
        String sql = "UPDATE disease_symptoms SET disease_id = ?, symptom_id = ?, weight = ?, is_required = ?, is_exclusive = ? WHERE id = ?";
        try {
            Float weight = diseaseSymptom.getWeight() != null ? diseaseSymptom.getWeight() : 1.0f;
            Boolean isRequired = diseaseSymptom.getIsRequired() != null ? diseaseSymptom.getIsRequired() : false;
            Boolean isExclusive = diseaseSymptom.getIsExclusive() != null ? diseaseSymptom.getIsExclusive() : false;
            return update(sql, diseaseSymptom.getDiseaseId(), diseaseSymptom.getSymptomId(), 
                         weight, isRequired, isExclusive, diseaseSymptom.getId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新疾病-症状关系", 0);
        }
    }

    @Override
    public int delete(Integer id) throws SQLException {
        String sql = "DELETE FROM disease_symptoms WHERE id = ?";
        try {
            return delete(sql, id);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除疾病-症状关系", 0);
        }
    }

    @Override
    public int deleteByDiseaseAndSymptom(Integer diseaseId, Integer symptomId) throws SQLException {
        String sql = "DELETE FROM disease_symptoms WHERE disease_id = ? AND symptom_id = ?";
        try {
            return delete(sql, diseaseId, symptomId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据疾病和症状ID删除关系", 0);
        }
    }

    private DiseaseSymptom mapRowToDiseaseSymptom(ResultSet rs) throws SQLException {
        DiseaseSymptom ds = new DiseaseSymptom();
        ds.setId(rs.getInt("id"));
        ds.setDiseaseId(rs.getInt("disease_id"));
        ds.setSymptomId(rs.getInt("symptom_id"));
        // 处理weight可能为null的情况
        Object weightObj = rs.getObject("weight");
        if (weightObj != null) {
            ds.setWeight(rs.getFloat("weight"));
        } else {
            ds.setWeight(1.0f); // 默认值
        }
        // 处理is_required和is_exclusive
        ds.setIsRequired(rs.getBoolean("is_required"));
        ds.setIsExclusive(rs.getBoolean("is_exclusive"));
        return ds;
    }
}

