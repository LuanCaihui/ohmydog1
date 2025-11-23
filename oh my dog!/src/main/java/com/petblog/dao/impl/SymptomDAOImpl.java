package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.SymptomDAO;
import com.petblog.model.Symptom;
import com.petblog.model.SymptomWithWeight;
import com.petblog.util.JdbcUtil;
import com.petblog.util.SQLExceptionHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 症状数据访问实现类
 */
public class SymptomDAOImpl extends BaseJdbcDAO<Symptom> implements SymptomDAO {

    @Override
    public Symptom findById(Integer id) throws SQLException {
        String sql = "SELECT id, name, category FROM symptoms WHERE id = ?";
        try {
            return queryForObject(sql, this::mapRowToSymptom, id);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询症状", null);
        }
    }

    @Override
    public List<Symptom> findAll() throws SQLException {
        String sql = "SELECT id, name, category FROM symptoms ORDER BY id";
        try {
            return queryForList(sql, this::mapRowToSymptom);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询所有症状", null);
        }
    }

    @Override
    public List<Symptom> findByName(String name) throws SQLException {
        String sql = "SELECT id, name, category FROM symptoms WHERE name LIKE ? ORDER BY id";
        try {
            return queryForList(sql, this::mapRowToSymptom, "%" + name + "%");
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据名称查询症状", null);
        }
    }

    @Override
    public int insert(Symptom symptom) throws SQLException {
        String sql = "INSERT INTO symptoms (name, category) VALUES (?, ?)";
        try {
            return insert(sql, symptom.getName(), symptom.getCategory());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "新增症状", 0);
        }
    }

    @Override
    public int update(Symptom symptom) throws SQLException {
        String sql = "UPDATE symptoms SET name = ?, category = ? WHERE id = ?";
        try {
            return update(sql, symptom.getName(), symptom.getCategory(), symptom.getId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新症状", 0);
        }
    }

    @Override
    public int delete(Integer id) throws SQLException {
        String sql = "DELETE FROM symptoms WHERE id = ?";
        try {
            return delete(sql, id);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除症状", 0);
        }
    }

    @Override
    public List<SymptomWithWeight> findCandidateSymptoms(List<Integer> topDiseaseIds, List<Integer> askedSymptomIds) throws SQLException {
        List<SymptomWithWeight> symptoms = new ArrayList<>();
        if (topDiseaseIds == null || topDiseaseIds.isEmpty()) {
            return symptoms;
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT s.id, s.name, s.category, ds.weight, ds.disease_id, ");
        sql.append("ds.is_required, ds.is_exclusive ");
        sql.append("FROM symptoms s ");
        sql.append("JOIN disease_symptoms ds ON s.id = ds.symptom_id ");
        sql.append("WHERE ds.disease_id IN (");
        
        // 动态拼接 ID
        for (int i = 0; i < topDiseaseIds.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(") ");
        
        // 排除已问过的
        if (askedSymptomIds != null && !askedSymptomIds.isEmpty()) {
            sql.append("AND s.id NOT IN (");
            for (int i = 0; i < askedSymptomIds.size(); i++) {
                sql.append(i == 0 ? "?" : ",?");
            }
            sql.append(") ");
        }
        
        // 按照关联权重排序，优先问权重高的核心症状
        sql.append("ORDER BY ds.weight DESC, ds.is_required DESC");
        
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int index = 1;
            for (Integer id : topDiseaseIds) {
                ps.setInt(index++, id);
            }
            if (askedSymptomIds != null && !askedSymptomIds.isEmpty()) {
                for (Integer id : askedSymptomIds) {
                    ps.setInt(index++, id);
                }
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Symptom symptom = new Symptom();
                symptom.setId(rs.getInt("id"));
                symptom.setName(rs.getString("name"));
                symptom.setCategory(rs.getString("category"));
                
                SymptomWithWeight sw = new SymptomWithWeight();
                sw.setSymptom(symptom);
                sw.setWeight(rs.getDouble("weight"));
                sw.setDiseaseId(rs.getInt("disease_id"));
                sw.setIsRequired(rs.getBoolean("is_required"));
                sw.setIsExclusive(rs.getBoolean("is_exclusive"));
                
                symptoms.add(sw);
            }
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查找候选症状", new ArrayList<>());
        }
        
        return symptoms;
    }
    
    @Override
    public List<Symptom> findByCategory(String category, List<Integer> askedSymptomIds, int limit) throws SQLException {
        List<Symptom> symptoms = new ArrayList<>();
        if (category == null || category.isEmpty()) {
            return symptoms;
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, name, category FROM symptoms WHERE category = ? ");
        
        // 排除已问过的
        if (askedSymptomIds != null && !askedSymptomIds.isEmpty()) {
            sql.append("AND id NOT IN (");
            for (int i = 0; i < askedSymptomIds.size(); i++) {
                sql.append(i == 0 ? "?" : ",?");
            }
            sql.append(") ");
        }
        
        sql.append("ORDER BY id LIMIT ?");
        
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int index = 1;
            ps.setString(index++, category);
            if (askedSymptomIds != null && !askedSymptomIds.isEmpty()) {
                for (Integer id : askedSymptomIds) {
                    ps.setInt(index++, id);
                }
            }
            ps.setInt(index++, limit);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                symptoms.add(mapRowToSymptom(rs));
            }
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "按类别查找症状", new ArrayList<>());
        }
        
        return symptoms;
    }

    private Symptom mapRowToSymptom(ResultSet rs) throws SQLException {
        Symptom symptom = new Symptom();
        symptom.setId(rs.getInt("id"));
        symptom.setName(rs.getString("name"));
        symptom.setCategory(rs.getString("category"));
        return symptom;
    }
}

