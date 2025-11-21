package com.petblog.Service;

import com.petblog.dao.ColumnDAO;
import com.petblog.dao.impl.ColumnDAOImpl;
import com.petblog.model.Column;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class ColumnService extends BaseService {

    private ColumnDAO columnDAO = new ColumnDAOImpl();

    /**
     * 根据专栏ID查询专栏详情
     */
    public Column getColumnById(Integer columnId) {
        try {
            return columnDAO.findById(columnId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询专栏详情");
            return null;
        }
    }

    /**
     * 根据创建者ID查询其创建的所有专栏
     */
    public List<Column> getColumnsByCreatorId(Integer userId) {
        try {
            return columnDAO.findByCreatorId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据创建者ID查询专栏列表");
            return null;
        }
    }

    /**
     * 查询热门专栏
     */
    public List<Column> getPopularColumns(int limit) {
        try {
            return columnDAO.findPopularColumns(limit);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询热门专栏");
            return null;
        }
    }

    /**
     * 根据专栏名称模糊搜索
     */
    public List<Column> searchColumnsByName(String keyword, int pageNum, int pageSize) {
        try {
            return columnDAO.searchByName(keyword, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据名称搜索专栏");
            return null;
        }
    }

    /**
     * 统计专栏总数
     */
    public int countAllColumns() {
        try {
            return columnDAO.countAll();
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计专栏总数");
            return 0;
        }
    }

    /**
     * 统计指定用户创建的专栏数量
     */
    public int countColumnsByCreatorId(Integer userId) {
        try {
            return columnDAO.countByCreatorId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计用户创建的专栏数量");
            return 0;
        }
    }

    /**
     * 新增专栏
     */
    public Integer createColumn(Column column) {
        try {
            return columnDAO.insert(column);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增专栏");
            return 0;
        }
    }

    /**
     * 更新专栏信息
     */
    public boolean updateColumn(Column column) {
        try {
            int result = columnDAO.update(column);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新专栏信息");
            return false;
        }
    }

    /**
     * 增加专栏的订阅量
     */
    public boolean incrementColumnSubscribeCount(Integer columnId) {
        try {
            int result = columnDAO.incrementSubscribeCount(columnId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "增加专栏订阅量");
            return false;
        }
    }

    /**
     * 减少专栏的订阅量
     */
    public boolean decrementColumnSubscribeCount(Integer columnId) {
        try {
            int result = columnDAO.decrementSubscribeCount(columnId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "减少专栏订阅量");
            return false;
        }
    }

    /**
     * 删除专栏
     */
    public boolean deleteColumn(Integer columnId) {
        try {
            int result = columnDAO.delete(columnId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除专栏");
            return false;
        }
    }

    /**
     * 检查专栏名称是否已存在
     */
    public boolean isColumnNameExists(String columnName) {
        try {
            return columnDAO.existsByName(columnName);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "检查专栏名称是否存在");
            return false;
        }
    }
}
