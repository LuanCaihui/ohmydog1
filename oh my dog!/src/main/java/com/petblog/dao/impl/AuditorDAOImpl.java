
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.AuditorDAO;
import com.petblog.model.Auditor;
import com.petblog.util.SQLExceptionHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AuditorDAOImpl extends BaseJdbcDAO<Auditor> implements AuditorDAO {

    @Override
    public void insert(Auditor auditor) {
        String sql = "INSERT INTO auditor (name, status) VALUES (?, ?)";
        try {
            insert(sql, auditor.getName(), auditor.getStatus());
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "插入审计员数据");
        }
    }

    @Override
    public void update(Auditor auditor) {
        String sql = "UPDATE auditor SET name = ?, status = ? WHERE id = ?";
        try {
            update(sql, auditor.getName(), auditor.getStatus(), auditor.getId());
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "更新审计员数据");
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM auditor WHERE id = ?";
        try {
            delete(sql, id);
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "删除审计员数据");
        }
    }

    @Override
    public Auditor findById(int id) {
        String sql = "SELECT id, name, status FROM auditor WHERE id = ?";
        try {
            return queryForObject(sql, this::mapRowToAuditor, id);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询审计员", null);
        }
    }

    @Override
    public Auditor findByName(String name) {
        String sql = "SELECT id, name, status FROM auditor WHERE name = ?";
        try {
            return queryForObject(sql, this::mapRowToAuditor, name);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据名称查询审计员", null);
        }
    }

    @Override
    public List<Auditor> findAll() {
        String sql = "SELECT id, name, status FROM auditor";
        try {
            return queryForList(sql, this::mapRowToAuditor);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询所有审计员", null);
        }
    }

    @Override
    public List<Auditor> findByStatus(String status) {
        String sql = "SELECT id, name, status FROM auditor WHERE status = ?";
        try {
            return queryForList(sql, this::mapRowToAuditor, status);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据状态查询审计员", null);
        }
    }

    private Auditor mapRowToAuditor(ResultSet rs) throws SQLException {
        Auditor auditor = new Auditor();
        auditor.setId(rs.getInt("id"));
        auditor.setName(rs.getString("name"));
        auditor.setStatus(rs.getString("status"));
        return auditor;
    }
}
