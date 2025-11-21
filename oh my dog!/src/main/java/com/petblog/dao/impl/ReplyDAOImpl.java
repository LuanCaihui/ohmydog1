// src/main/java/com/petblog/dao/impl/ReplyDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.ReplyDAO;
import com.petblog.model.Reply;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ReplyDAOImpl extends BaseJdbcDAO<Reply> implements ReplyDAO {

    @Override
    public Reply findById(Integer replyId) {
        String sql = "SELECT user_id, blog_id, reply_id, parentReply, reply_createdtime, reply_content, is_visible FROM replies WHERE reply_id = ?";
        try {
            return queryForObject(sql, this::mapRowToReply, replyId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据回复ID查询回复信息", null);
        }
    }

    @Override
    public List<Reply> findByCommentId(Integer commentId, int pageNum, int pageSize) {
        String sql = "SELECT user_id, blog_id, reply_id, parentReply, reply_createdtime, reply_content, is_visible FROM replies WHERE parentReply = ? AND is_visible = 1 ORDER BY reply_createdtime ASC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToReply, commentId, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据评论ID查询回复列表", null);
        }
    }

    @Override
    public List<Reply> findByUserId(Integer userId, int pageNum, int pageSize) {
        String sql = "SELECT user_id, blog_id, reply_id, parentReply, reply_createdtime, reply_content, is_visible FROM replies WHERE user_id = ? ORDER BY reply_createdtime DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToReply, userId, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据用户ID查询回复列表", null);
        }
    }

    @Override
    public int countByCommentId(Integer commentId) {
        String sql = "SELECT COUNT(*) FROM replies WHERE parentReply = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, commentId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计评论的回复数量", 0);
        }
    }

    @Override
    public int countByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM replies WHERE user_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, userId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计用户的回复数量", 0);
        }
    }

    @Override
    public int insert(Reply reply) {
        String sql = "INSERT INTO replies (user_id, blog_id, parentReply, reply_createdtime, reply_content, is_visible) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            return insert(sql, reply.getUserId(), reply.getBlogId(), reply.getParentReply(),
                         reply.getReplyCreatedtime(), reply.getReplyContent(), reply.getIsVisible());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "插入回复信息", 0);
        }
    }

    @Override
    public int updateContent(Reply reply) {
        String sql = "UPDATE replies SET reply_content = ? WHERE reply_id = ?";
        try {
            return update(sql, reply.getReplyContent(), reply.getReplyId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新回复内容", 0);
        }
    }

    @Override
    public int updateStatus(Integer replyId, Integer status) {
        String sql = "UPDATE replies SET is_visible = ? WHERE reply_id = ?";
        try {
            return update(sql, status, replyId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新回复状态", 0);
        }
    }

    @Override
    public int delete(Integer replyId) {
        String sql = "DELETE FROM replies WHERE reply_id = ?";
        try {
            return delete(sql, replyId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除回复信息", 0);
        }
    }

    @Override
    public int deleteByCommentId(Integer commentId) {
        String sql = "DELETE FROM replies WHERE parentReply = ?";
        try {
            return delete(sql, commentId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据评论ID删除回复", 0);
        }
    }

    @Override
    public int deleteByUserId(Integer userId) {
        String sql = "DELETE FROM replies WHERE user_id = ?";
        try {
            return delete(sql, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据用户ID删除回复", 0);
        }
    }

    private Reply mapRowToReply(ResultSet rs) throws SQLException {
        Reply reply = new Reply();
        reply.setUserId(rs.getInt("user_id"));
        reply.setBlogId(rs.getInt("blog_id"));
        reply.setReplyId(rs.getInt("reply_id"));
        reply.setParentReply(rs.getInt("parentReply"));
        reply.setReplyCreatedtime(rs.getDate("reply_createdtime"));
        reply.setReplyContent(rs.getString("reply_content"));
        reply.setIsVisible(rs.getInt("is_visible"));
        return reply;
    }
}