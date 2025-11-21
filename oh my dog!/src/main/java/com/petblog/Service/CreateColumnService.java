package com.petblog.Service;

import com.petblog.dao.CreateColumnDAO;
import com.petblog.dao.impl.CreateColumnDAOImpl;
import com.petblog.model.CreateColumn;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class CreateColumnService extends BaseService {

    private CreateColumnDAO createColumnDAO = new CreateColumnDAOImpl();

    /**
     * 根据专栏ID查询所有创建者ID
     */
    public List<Integer> getCreatorIdsByColumnId(Integer columnId) {
        try {
            return createColumnDAO.findCreatorIdsByColumnId(columnId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据专栏ID查询创建者列表");
            return null;
        }
    }

    /**
     * 根据用户ID查询其创建的所有专栏ID
     */
    public List<Integer> getColumnIdsByCreatorId(Integer userId) {
        try {
            return createColumnDAO.findColumnIdsByCreatorId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据用户ID查询专栏列表");
            return null;
        }
    }

    /**
     * 检查用户是否为专栏的创建者
     */
    public boolean isUserCreatorOfColumn(Integer userId, Integer columnId) {
        try {
            return createColumnDAO.isCreator(userId, columnId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "检查用户是否为专栏创建者");
            return false;
        }
    }

    /**
     * 新增用户与专栏的创建关联关系
     */
    public boolean createColumnCreator(CreateColumn createColumn) {
        try {
            int result = createColumnDAO.insert(createColumn);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增专栏创建者关联");
            return false;
        }
    }

    /**
     * 批量新增用户与专栏的创建关联关系
     */
    public boolean batchCreateColumnCreators(List<CreateColumn> createColumns) {
        try {
            int result = createColumnDAO.batchInsert(createColumns);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "批量新增专栏创建者关联");
            return false;
        }
    }

    /**
     * 移除用户与专栏的创建关联关系
     */
    public boolean removeColumnCreator(Integer userId, Integer columnId) {
        try {
            int result = createColumnDAO.delete(userId, columnId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "移除专栏创建者关联");
            return false;
        }
    }

    /**
     * 移除专栏的所有创建者关联
     */
    public boolean removeAllCreatorsOfColumn(Integer columnId) {
        try {
            int result = createColumnDAO.deleteByColumnId(columnId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "移除专栏所有创建者关联");
            return false;
        }
    }

    /**
     * 移除用户创建的所有专栏关联
     */
    public boolean removeAllColumnsOfCreator(Integer userId) {
        try {
            int result = createColumnDAO.deleteByUserId(userId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "移除用户所有专栏关联");
            return false;
        }
    }
}
