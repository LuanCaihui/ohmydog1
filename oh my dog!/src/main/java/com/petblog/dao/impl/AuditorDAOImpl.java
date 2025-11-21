
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
        String sql = "INSERT INTO auditors (auditor_name, auditor_password) VALUES (?, ?)";
        try {
            insert(sql, auditor.getAuditorName(), auditor.getAuditorPassword());
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "插入审计员数据");
        }
    }

    @Override
    public void update(Auditor auditor) {
        String sql = "UPDATE auditors SET auditor_name = ?, auditor_password = ? WHERE auditor_id = ?";
        try {
            update(sql, auditor.getAuditorName(), auditor.getAuditorPassword(), auditor.getAuditorId());
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "更新审计员数据");
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM auditors WHERE auditor_id = ?";
        try {
            delete(sql, id);
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "删除审计员数据");
        }
    }

    @Override
    public Auditor findById(int id) {
        String sql = "SELECT auditor_id, auditor_name, auditor_password FROM auditors WHERE auditor_id = ?";
        try {
            return queryForObject(sql, this::mapRowToAuditor, id);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询审计员", null);
        }
    }

    @Override
    public Auditor findByName(String name) {
        String sql = "SELECT auditor_id, auditor_name, auditor_password FROM auditors WHERE TRIM(auditor_name) = TRIM(?)";
        try {
            String trimmedName = name != null ? name.trim() : "";
            System.out.println("执行SQL查询: " + sql + ", 参数: name=" + trimmedName);
            Auditor result = queryForObject(sql, this::mapRowToAuditor, trimmedName);
            System.out.println("查询结果: " + (result != null ? "找到 id=" + result.getAuditorId() + ", name=" + result.getAuditorName() : "未找到"));
            return result;
        } catch (SQLException e) {
            System.err.println("查询审核员失败: " + e.getMessage());
            e.printStackTrace();
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据名称查询审计员", null);
        }
    }

    @Override
    public List<Auditor> findAll() {
        String sql = "SELECT auditor_id, auditor_name, auditor_password FROM auditors";
        try {
            return queryForList(sql, this::mapRowToAuditor);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询所有审计员", null);
        }
    }

    @Override
    public List<Auditor> findByStatus(String status) {
        // 注意：数据库中没有status字段，这个方法可能不适用
        // 暂时返回所有审核员
        return findAll();
    }

    private Auditor mapRowToAuditor(ResultSet rs) throws SQLException {
        Auditor auditor = new Auditor();
        auditor.setAuditorId(rs.getInt("auditor_id"));
        auditor.setAuditorName(rs.getString("auditor_name"));
        auditor.setAuditorPassword(rs.getString("auditor_password"));
        return auditor;
    }
}
