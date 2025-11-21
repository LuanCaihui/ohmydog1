// src/main/java/com/petblog/dao/impl/InvestigateDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.InvestigateDAO;
import com.petblog.model.Investigate;
import com.petblog.util.JdbcUtil;
import com.petblog.util.SQLExceptionHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InvestigateDAOImpl extends BaseJdbcDAO<Investigate> implements InvestigateDAO {

    @Override
    public Investigate findById(Integer investigateId) {
        String sql = "SELECT medicine_id, user_id FROM investigate WHERE medicine_id = ?";
        try {
            List<Investigate> results = queryForList(sql, this::mapRowToInvestigate, investigateId);
            return results.isEmpty() ? null : results.get(0);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询调查记录", null);
        }
    }

    @Override
    public List<Investigate> findByUserId(Integer userId) {
        String sql = "SELECT medicine_id, user_id FROM investigate WHERE user_id = ? ORDER BY medicine_id DESC";
        try {
            return queryForList(sql, this::mapRowToInvestigate, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询用户的调查记录", null);
        }
    }

    @Override
public List<Integer> findUserIdsByMedicineId(Integer medicineId) {
    String sql = "SELECT user_id FROM investigate WHERE medicineID = ?";
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
        conn = JdbcUtil.getConnection();
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, medicineId);
        rs = pstmt.executeQuery();
        List<Integer> userIds = new ArrayList<>();
        while (rs.next()) {
            userIds.add(rs.getInt("user_id"));
        }
        return userIds;
    } catch (SQLException e) {
        return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询药品的调查用户ID", null);
    } finally {
        JdbcUtil.close(conn, pstmt, rs);
    }
}

    @Override
    public int countByMedicineId(Integer medicineId) {
        String sql = "SELECT COUNT(*) FROM investigate WHERE medicine_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, medicineId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计药品的调查次数", 0);
        }
    }

    @Override
    public boolean hasParticipated(Integer userId, Integer medicineId) {
        String sql = "SELECT COUNT(*) FROM investigate WHERE user_id = ? AND medicine_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, userId, medicineId);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查用户是否参与调查", false);
        }
    }

    @Override
    public int insert(Investigate investigate) {
        String sql = "INSERT INTO investigate (medicine_id, user_id) VALUES (?, ?)";
        try {
            return insert(sql, investigate.getMedicineId(), investigate.getUserId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "插入调查记录", 0);
        }
    }

    @Override
public int updateFeedback(Investigate investigate) {
    // TODO: investigate 表中没有 feedback 字段，需要根据实际业务需求调整
    // 暂时返回0表示未执行任何操作
    return 0;
}

    @Override
    public int delete(Integer investigateId) {
        // investigateId在这里可能需要特殊处理，因为是联合主键
        // 假设传入的是medicine_id
        String sql = "DELETE FROM investigate WHERE medicine_id = ?";
        try {
            return delete(sql, investigateId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除调查记录", 0);
        }
    }

    @Override
    public int deleteByUserId(Integer userId) {
        String sql = "DELETE FROM investigate WHERE user_id = ?";
        try {
            return delete(sql, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除用户的所有调查记录", 0);
        }
    }

    @Override
    public int deleteByMedicineId(Integer medicineId) {
        String sql = "DELETE FROM investigate WHERE medicine_id = ?";
        try {
            return delete(sql, medicineId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除药品的所有调查记录", 0);
        }
    }

    private Investigate mapRowToInvestigate(ResultSet rs) throws SQLException {
        Investigate investigate = new Investigate();
        investigate.setMedicineId(rs.getInt("medicine_id"));
        investigate.setUserId(rs.getInt("user_id"));
        return investigate;
    }
}