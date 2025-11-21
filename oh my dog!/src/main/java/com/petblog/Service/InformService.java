package com.petblog.Service;

import com.petblog.dao.InformDAO;
import com.petblog.dao.impl.InformDAOImpl;
import com.petblog.model.Inform;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class InformService extends BaseService {

    private InformDAO informDAO = new InformDAOImpl();

    /**
     * 根据通知ID查询通知详情
     */
    public Inform getInformById(Integer informId) {
        try {
            return informDAO.findById(informId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询通知详情");
            return null;
        }
    }

    /**
     * 查询用户的所有通知
     */
    public List<Inform> getInformsByUserId(Integer userId, int pageNum, int pageSize) {
        try {
            return informDAO.findByUserId(userId, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询用户通知列表");
            return null;
        }
    }

    /**
     * 查询用户的未读通知
     */
    public List<Inform> getUnreadInformsByUserId(Integer userId) {
        try {
            return informDAO.findUnreadByUserId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询用户未读通知");
            return null;
        }
    }

    /**
     * 统计用户的未读通知数量
     */
    public int countUnreadInformsByUserId(Integer userId) {
        try {
            return informDAO.countUnreadByUserId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计用户未读通知数量");
            return 0;
        }
    }

    /**
     * 统计用户的通知总数
     */
    public int countInformsByUserId(Integer userId) {
        try {
            return informDAO.countByUserId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计用户通知总数");
            return 0;
        }
    }

    /**
     * 新增通知
     */
    public Integer createInform(Inform inform) {
        try {
            return informDAO.insert(inform);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增通知");
            return 0;
        }
    }

    /**
     * 批量新增通知
     */
    public boolean batchCreateInforms(List<Inform> informs) {
        try {
            int result = informDAO.batchInsert(informs);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "批量新增通知");
            return false;
        }
    }

    /**
     * 将通知标记为已读
     */
    public boolean markInformAsRead(Integer informId) {
        try {
            int result = informDAO.markAsRead(informId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "标记通知为已读");
            return false;
        }
    }

    /**
     * 将用户的所有未读通知标记为已读
     */
    public boolean markAllInformsAsRead(Integer userId) {
        try {
            int result = informDAO.markAllAsRead(userId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "标记所有通知为已读");
            return false;
        }
    }

    /**
     * 删除指定通知
     */
    public boolean deleteInform(Integer informId) {
        try {
            int result = informDAO.delete(informId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除通知");
            return false;
        }
    }

    /**
     * 批量删除用户的通知
     */
    public boolean batchDeleteInforms(List<Integer> informIds) {
        try {
            int result = informDAO.batchDelete(informIds);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "批量删除通知");
            return false;
        }
    }

    /**
     * 清除用户的所有通知
     */
    public boolean clearAllInformsByUserId(Integer userId) {
        try {
            int result = informDAO.clearAllByUserId(userId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "清除用户所有通知");
            return false;
        }
    }
}

