
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.AuthenticationDAO;
import com.petblog.model.Authentication;
import com.petblog.util.SQLExceptionHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AuthenticationDAOImpl extends BaseJdbcDAO<Authentication> implements AuthenticationDAO {

    @Override
    public void insert(Authentication auth) {
        String sql = "INSERT INTO authentication (authentication_id, unit, title, user_id) VALUES (?, ?, ?, ?)";
        try {
            insert(sql, auth.getAuthenticationId(), auth.getUnit(), auth.getTitle());
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "插入认证信息");
        }
    }

    @Override
    public void update(Authentication auth) {
        String sql = "UPDATE authentication SET unit = ?, title = ?, user_id = ? WHERE authentication_id = ?";
        try {
            update(sql, auth.getUnit(), auth.getTitle(),  auth.getAuthenticationId());
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "更新认证信息");
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM authentication WHERE authentication_id = ?";
        try {
            delete(sql, id);
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "删除认证信息");
        }
    }

    @Override
    public Authentication findById(int id) {
        String sql = "SELECT authentication_id, unit, title, user_id FROM authentication WHERE authentication_id = ?";
        try {
            return queryForObject(sql, this::mapRowToAuthentication, id);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询认证信息", null);
        }
    }

    @Override
    public Authentication findByUserId(int userId) {
        String sql = "SELECT authentication_id, unit, title, user_id FROM authentication WHERE user_id = ?";
        try {
            return queryForObject(sql, this::mapRowToAuthentication, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据用户ID查询认证信息", null);
        }
    }

    @Override
    public List<Authentication> findAll() {
        String sql = "SELECT authentication_id, unit, title, user_id FROM authentication";
        try {
            return queryForList(sql, this::mapRowToAuthentication);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询所有认证信息", null);
        }
    }
    @Override
    public Authentication selectById(Integer authId) throws SQLException {
        String sql = "SELECT authentication_id, unit, title FROM authentication WHERE authentication_id = ?";
        try {
            return queryForObject(sql, this::mapRowToAuthentication, authId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询认证信息", null);
        }
    }


    private Authentication mapRowToAuthentication(ResultSet rs) throws SQLException {
        Authentication auth = new Authentication();
        auth.setAuthenticationId(rs.getInt("authentication_id"));
        auth.setUnit(rs.getString("unit"));
        auth.setTitle(rs.getString("title"));

        return auth;
    }
}
