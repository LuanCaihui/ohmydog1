package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.SymptomDAO;
import com.petblog.model.Symptom;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private Symptom mapRowToSymptom(ResultSet rs) throws SQLException {
        Symptom symptom = new Symptom();
        symptom.setId(rs.getInt("id"));
        symptom.setName(rs.getString("name"));
        symptom.setCategory(rs.getString("category"));
        return symptom;
    }
}

