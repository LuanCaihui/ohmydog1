package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.DiseaseDAO;
import com.petblog.model.Disease;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 疾病数据访问实现类
 */
public class DiseaseDAOImpl extends BaseJdbcDAO<Disease> implements DiseaseDAO {

    @Override
    public Disease findById(Integer id) throws SQLException {
        String sql = "SELECT id, name, organ_system, description FROM diseases WHERE id = ?";
        try {
            return queryForObject(sql, this::mapRowToDisease, id);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询疾病", null);
        }
    }

    @Override
    public List<Disease> findAll() throws SQLException {
        String sql = "SELECT id, name, organ_system, description FROM diseases ORDER BY id";
        try {
            return queryForList(sql, this::mapRowToDisease);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询所有疾病", null);
        }
    }

    @Override
    public List<Disease> findByName(String name) throws SQLException {
        String sql = "SELECT id, name, organ_system, description FROM diseases WHERE name LIKE ? ORDER BY id";
        try {
            return queryForList(sql, this::mapRowToDisease, "%" + name + "%");
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据名称查询疾病", null);
        }
    }

    @Override
    public List<Disease> findBySymptomId(Integer symptomId) throws SQLException {
        String sql = "SELECT DISTINCT d.id, d.name, d.organ_system, d.description " +
                     "FROM diseases d " +
                     "INNER JOIN disease_symptoms ds ON d.id = ds.disease_id " +
                     "WHERE ds.symptom_id = ? " +
                     "ORDER BY d.id";
        try {
            return queryForList(sql, this::mapRowToDisease, symptomId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据症状ID查询疾病", null);
        }
    }

    @Override
    public List<Disease> findBySymptomIds(List<Integer> symptomIds) throws SQLException {
        if (symptomIds == null || symptomIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 构建IN子句的占位符
        String placeholders = symptomIds.stream()
            .map(id -> "?")
            .collect(Collectors.joining(","));
        
        // 使用GROUP BY和SUM(weight)来计算每个疾病的匹配权重
        String sql = "SELECT d.id, d.name, d.organ_system, d.description, SUM(ds.weight) as total_weight " +
                     "FROM diseases d " +
                     "INNER JOIN disease_symptoms ds ON d.id = ds.disease_id " +
                     "WHERE ds.symptom_id IN (" + placeholders + ") " +
                     "GROUP BY d.id, d.name, d.organ_system, d.description " +
                     "ORDER BY total_weight DESC";
        
        try {
            List<Object> params = new ArrayList<>(symptomIds);
            return queryForList(sql, this::mapRowToDiseaseWithWeight, params.toArray());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据症状ID列表查询疾病", null);
        }
    }

    @Override
    public int insert(Disease disease) throws SQLException {
        String sql = "INSERT INTO diseases (name, organ_system, description) VALUES (?, ?, ?)";
        try {
            return insert(sql, disease.getName(), disease.getOrganSystem(), disease.getDescription());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "新增疾病", 0);
        }
    }

    @Override
    public int update(Disease disease) throws SQLException {
        String sql = "UPDATE diseases SET name = ?, organ_system = ?, description = ? WHERE id = ?";
        try {
            return update(sql, disease.getName(), disease.getOrganSystem(), disease.getDescription(), disease.getId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新疾病", 0);
        }
    }

    @Override
    public int delete(Integer id) throws SQLException {
        String sql = "DELETE FROM diseases WHERE id = ?";
        try {
            return delete(sql, id);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除疾病", 0);
        }
    }

    private Disease mapRowToDisease(ResultSet rs) throws SQLException {
        Disease disease = new Disease();
        disease.setId(rs.getInt("id"));
        disease.setName(rs.getString("name"));
        disease.setOrganSystem(rs.getString("organ_system"));
        disease.setDescription(rs.getString("description"));
        return disease;
    }

    private Disease mapRowToDiseaseWithWeight(ResultSet rs) throws SQLException {
        // 这个方法用于带权重的查询，但Disease模型中没有weight字段
        // 所以只映射基本字段
        return mapRowToDisease(rs);
    }
}

