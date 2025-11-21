// src/main/java/com/petblog/dao/impl/InformDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.InformDAO;
import com.petblog.model.Inform;
import com.petblog.util.JdbcUtil;
import com.petblog.util.SQLExceptionHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class InformDAOImpl extends BaseJdbcDAO<Inform> implements InformDAO {

    @Override
    public Inform findById(Integer informId) {
        String sql = "SELECT inform_id, user_id, inform_type, inform_time, inform_content, is_readed FROM informs WHERE inform_id = ?";
        try {
            return queryForObject(sql, this::mapRowToInform, informId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询通知", null);
        }
    }

    @Override
    public List<Inform> findByUserId(Integer userId, int pageNum, int pageSize) {
        String sql = "SELECT inform_id, user_id, inform_type, inform_time, inform_content, is_readed FROM informs WHERE user_id = ? ORDER BY inform_time DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToInform, userId, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "分页查询用户通知", null);
        }
    }

    @Override
    public List<Inform> findUnreadByUserId(Integer userId) {
        String sql = "SELECT inform_id, user_id, inform_type, inform_time, inform_content, is_readed FROM informs WHERE user_id = ? AND is_readed = 0 ORDER BY inform_time DESC";
        try {
            return queryForList(sql, this::mapRowToInform, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询用户未读通知", null);
        }
    }

    @Override
    public int countUnreadByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM informs WHERE user_id = ? AND is_readed = 0";
        try {
            Number count = (Number) queryForSingleValue(sql, userId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计用户未读通知数量", 0);
        }
    }

    @Override
    public int countByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM informs WHERE user_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, userId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计用户通知总数", 0);
        }
    }

    @Override
    public int insert(Inform inform) {
        String sql = "INSERT INTO informs (user_id, inform_type, inform_time, inform_content, is_readed) VALUES (?, ?, ?, ?, ?)";
        try {
            return insert(sql, inform.getUserId(), inform.getInformType(),
                         inform.getInformTime(), inform.getInformContent(), inform.getIsReaded());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "插入通知", 0);
        }
    }

    @Override
    public int batchInsert(List<Inform> informs) {
        String sql = "INSERT INTO informs (user_id, inform_type, inform_time, inform_content, is_readed) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        int count = 0;

        try {
            conn = JdbcUtil.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);

            for (Inform inform : informs) {
                pstmt.setInt(1, inform.getUserId());
                pstmt.setString(2, inform.getInformType());
                pstmt.setDate(3, new java.sql.Date(inform.getInformTime().getTime()));
                pstmt.setString(4, inform.getInformContent());
                pstmt.setInt(5, inform.getIsReaded());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            for (int result : results) {
                if (result > 0) count++;
            }

            conn.commit();
            return count;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                SQLExceptionHandler.handleSQLException(ex, "批量插入通知时回滚事务");
            }
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "批量插入通知", 0);
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                SQLExceptionHandler.handleSQLException(e, "批量插入通知后恢复自动提交");
            }
            JdbcUtil.close(conn, pstmt);
        }
    }

    @Override
    public int markAsRead(Integer informId) {
        String sql = "UPDATE informs SET is_readed = 1 WHERE inform_id = ?";
        try {
            return update(sql, informId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "标记通知为已读", 0);
        }
    }

    @Override
    public int markAllAsRead(Integer userId) {
        String sql = "UPDATE informs SET is_readed = 1 WHERE user_id = ? AND is_readed = 0";
        try {
            return update(sql, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "标记用户所有通知为已读", 0);
        }
    }

    @Override
    public int delete(Integer informId) {
        String sql = "DELETE FROM informs WHERE inform_id = ?";
        try {
            return delete(sql, informId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除通知", 0);
        }
    }

    @Override
    public int batchDelete(List<Integer> informIds) {
        if (informIds == null || informIds.isEmpty()) {
            return 0;
        }

        StringBuilder sql = new StringBuilder("DELETE FROM informs WHERE inform_id IN (");
        for (int i = 0; i < informIds.size(); i++) {
            sql.append("?");
            if (i < informIds.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");

        try {
            Object[] params = informIds.toArray();
            return update(sql.toString(), params);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "批量删除通知", 0);
        }
    }

    @Override
    public int clearAllByUserId(Integer userId) {
        String sql = "DELETE FROM informs WHERE user_id = ?";
        try {
            return delete(sql, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "清空用户所有通知", 0);
        }
    }

    private Inform mapRowToInform(ResultSet rs) throws SQLException {
        Inform inform = new Inform();
        inform.setInformId(rs.getInt("inform_id"));
        inform.setUserId(rs.getInt("user_id"));
        inform.setInformType(rs.getString("inform_type"));
        inform.setInformTime(rs.getDate("inform_time"));
        inform.setInformContent(rs.getString("inform_content"));
        inform.setIsReaded(rs.getInt("is_readed"));
        return inform;
    }
}