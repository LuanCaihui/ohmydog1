package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.CreateColumnDAO;
import com.petblog.model.CreateColumn;
import com.petblog.util.JdbcUtil;
import com.petblog.util.SQLExceptionHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CreateColumnDAOImpl extends BaseJdbcDAO<Integer> implements CreateColumnDAO {

    @Override
    public List<Integer> findCreatorIdsByColumnId(Integer columnId) {
        String sql = "SELECT user_id FROM createcolumn WHERE column_id = ?";
        try {
            return queryForList(sql, rs -> {
                try {
                    return rs.getInt("user_id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, columnId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据专栏ID查询创建者ID列表", null);
        }
    }

    @Override
    public List<Integer> findColumnIdsByCreatorId(Integer userId) {
        String sql = "SELECT column_id FROM createcolumn WHERE user_id = ? ORDER BY column_id DESC";
        try {
            return queryForList(sql, rs -> {
                try {
                    return rs.getInt("column_id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据用户ID查询创建的专栏ID列表", null);
        }
    }

    @Override
    public boolean isCreator(Integer userId, Integer columnId) {
        String sql = "SELECT COUNT(*) FROM createcolumn WHERE user_id = ? AND column_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, userId, columnId);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查用户是否是专栏创建者", false);
        }
    }

    @Override
    public int insert(CreateColumn createColumn) {
        String sql = "INSERT INTO createcolumn (column_id, user_id) VALUES (?, ?)";
        try {
            return insert(sql, createColumn.getColumnId(), createColumn.getUserId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "插入创建专栏记录", 0);
        }
    }

    @Override
    public int batchInsert(List<CreateColumn> createColumns) {
        String sql = "INSERT INTO createcolumn (column_id, user_id) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        int count = 0;

        try {
            conn = JdbcUtil.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);

            for (CreateColumn createColumn : createColumns) {
                pstmt.setInt(1, createColumn.getColumnId());
                pstmt.setInt(2, createColumn.getUserId());
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
                throw SQLExceptionHandler.handleSQLException(ex, "回滚批量插入操作");
            }
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "批量插入创建专栏记录", 0);
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                throw SQLExceptionHandler.handleSQLException(e, "恢复自动提交模式");
            }
            JdbcUtil.close(conn, pstmt);
        }
    }

    @Override
    public int delete(Integer userId, Integer columnId) {
        String sql = "DELETE FROM createcolumn WHERE user_id = ? AND column_id = ?";
        try {
            return delete(sql, userId, columnId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除指定用户创建的专栏记录", 0);
        }
    }

    @Override
    public int deleteByColumnId(Integer columnId) {
        String sql = "DELETE FROM createcolumn WHERE column_id = ?";
        try {
            return delete(sql, columnId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除指定专栏的所有创建记录", 0);
        }
    }

    @Override
    public int deleteByUserId(Integer userId) {
        String sql = "DELETE FROM createcolumn WHERE user_id = ?";
        try {
            return delete(sql, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除指定用户的所有创建专栏记录", 0);
        }
    }
}
