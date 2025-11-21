package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.ColumnDAO;
import com.petblog.model.Column;
import com.petblog.util.SQLExceptionHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ColumnDAOImpl extends BaseJdbcDAO<Column> implements ColumnDAO {

    @Override
    public Column findById(Integer columnId) {
        String sql = "SELECT Column_id, Column_name, Column_description, Column_createdtime, user_id FROM columns WHERE Column_id = ?";
        try {
            return queryForObject(sql, this::mapRowToColumn, columnId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询专栏信息", null);
        }
    }

    @Override
    public List<Column> findByCreatorId(Integer userId) {
        String sql = "SELECT column_id, column_name, column_description, column_createdtime, user_id FROM columns WHERE user_id = ? ORDER BY column_createdtime DESC";
        try {
            return queryForList(sql, this::mapRowToColumn, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询用户创建的专栏", null);
        }
    }

    @Override
    public List<Column> findPopularColumns(int limit) {
        // 假设有一个订阅数字段
        String sql = "SELECT column_id, column_name, column_description, column_createdtime, user_id FROM columns ORDER BY subscribe_count DESC LIMIT ?";
        try {
            return queryForList(sql, this::mapRowToColumn, limit);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询热门专栏", null);
        }
    }

    @Override
    public List<Column> searchByName(String keyword, int pageNum, int pageSize) {
        String sql = "SELECT column_id, column_name, column_description, column_createdtime, user_id FROM columns WHERE column_name LIKE ? LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToColumn, "%" + keyword + "%", pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "搜索专栏名称", null);
        }
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM columns";
        try {
            Number count = (Number) queryForSingleValue(sql);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计所有专栏数量", 0);
        }
    }

    @Override
    public int countByCreatorId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM columns WHERE user_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, userId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计用户创建的专栏数量", 0);
        }
    }

    @Override
    public int insert(Column column) {
        String sql = "INSERT INTO columns (Column_name, Column_description, Column_createdtime, user_id) VALUES (?, ?, ?, ?)";
        try {
            return insert(sql, column.getColumnName(), column.getColumnDescription(),
                         column.getColumnCreatedtime(), column.getUserId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "插入专栏信息", 0);
        }
    }

    @Override
    public int update(Column column) {
        String sql = "UPDATE columns SET Column_name = ?, Column_description = ? WHERE Column_id = ?";
        try {
            return update(sql, column.getColumnName(), column.getColumnDescription(), column.getColumnId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新专栏信息", 0);
        }
    }

    @Override
    public int incrementSubscribeCount(Integer columnId) {
        String sql = "UPDATE columns SET subscribe_count = subscribe_count + 1 WHERE Column_id = ?";
        try {
            return update(sql, columnId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "增加专栏订阅数", 0);
        }
    }

    @Override
    public int decrementSubscribeCount(Integer columnId) {
        String sql = "UPDATE columns SET subscribe_count = subscribe_count - 1 WHERE Column_id = ? AND subscribe_count > 0";
        try {
            return update(sql, columnId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "减少专栏订阅数", 0);
        }
    }

    @Override
    public int delete(Integer columnId) {
        String sql = "DELETE FROM columns WHERE Column_id = ?";
        try {
            return delete(sql, columnId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除专栏信息", 0);
        }
    }

    @Override
    public boolean existsByName(String columnName) {
        String sql = "SELECT COUNT(*) FROM columns WHERE Column_name = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, columnName);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查专栏名称是否存在", false);
        }
    }

    private Column mapRowToColumn(ResultSet rs) throws SQLException {
        Column column = new Column();
        column.setColumnId(rs.getInt("Column_id"));
        column.setColumnName(rs.getString("Column_name"));
        column.setColumnDescription(rs.getString("Column_description"));
        column.setColumnCreatedtime(rs.getDate("Column_createdtime"));
        column.setUserId(rs.getInt("user_id"));
        return column;
    }
}

