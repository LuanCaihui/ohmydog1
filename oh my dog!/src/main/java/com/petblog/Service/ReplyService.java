package com.petblog.Service;

import com.petblog.dao.ReplyDAO;
import com.petblog.dao.impl.ReplyDAOImpl;
import com.petblog.model.Reply;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class ReplyService extends BaseService {

    private final ReplyDAO replyDAO;

    public ReplyService() {
        this.replyDAO = new ReplyDAOImpl();
    }

    /**
     * 根据回复ID查询回复详情
     */
    public Reply getReplyById(Integer replyId) {
        try {
            return replyDAO.findById(replyId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询回复详情");
            return null;
        }
    }

    /**
     * 根据评论ID查询所有回复
     */
    public List<Reply> getRepliesByCommentId(Integer commentId, int pageNum, int pageSize) {
        try {
            return replyDAO.findByCommentId(commentId, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据评论ID查询回复列表");
            return null;
        }
    }

    /**
     * 根据用户ID查询其发布的所有回复
     */
    public List<Reply> getRepliesByUserId(Integer userId, int pageNum, int pageSize) {
        try {
            return replyDAO.findByUserId(userId, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据用户ID查询回复列表");
            return null;
        }
    }

    /**
     * 统计某评论的回复总数
     */
    public int countRepliesByCommentId(Integer commentId) {
        try {
            return replyDAO.countByCommentId(commentId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计评论回复数量");
            return 0;
        }
    }

    /**
     * 统计用户发布的回复总数
     */
    public int countRepliesByUserId(Integer userId) {
        try {
            return replyDAO.countByUserId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计用户回复数量");
            return 0;
        }
    }

    /**
     * 新增回复
     */
    public Integer createReply(Reply reply) {
        try {
            return replyDAO.insert(reply);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增回复");
            return 0;
        }
    }

    /**
     * 更新回复内容
     */
    public boolean updateReplyContent(Reply reply) {
        try {
            int result = replyDAO.updateContent(reply);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新回复内容");
            return false;
        }
    }

    /**
     * 更新回复的状态（正常/删除）
     */
    public boolean updateReplyStatus(Integer replyId, Integer status) {
        try {
            int result = replyDAO.updateStatus(replyId, status);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新回复状态");
            return false;
        }
    }

    /**
     * 删除回复（物理删除或逻辑删除）
     */
    public boolean deleteReply(Integer replyId) {
        try {
            int result = replyDAO.delete(replyId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除回复");
            return false;
        }
    }

    /**
     * 删除某评论下的所有回复
     */
    public boolean deleteRepliesByCommentId(Integer commentId) {
        try {
            int result = replyDAO.deleteByCommentId(commentId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除评论下所有回复");
            return false;
        }
    }

    /**
     * 删除用户发布的所有回复
     */
    public boolean deleteRepliesByUserId(Integer userId) {
        try {
            int result = replyDAO.deleteByUserId(userId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除用户所有回复");
            return false;
        }
    }
}
