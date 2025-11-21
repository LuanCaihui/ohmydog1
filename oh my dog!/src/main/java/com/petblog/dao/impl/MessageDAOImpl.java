// src/main/java/com/petblog/dao/impl/MessageDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.MessageDAO;
import com.petblog.model.Message;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class MessageDAOImpl extends BaseJdbcDAO<Message> implements MessageDAO {

    @Override
    public Message findById(Integer messageId) {
        String sql = "SELECT sender_id, receiver_id, message_id, message_content, creation_time, update_time, is_withdraw, is_read FROM message WHERE message_id = ?";
        try {
            return queryForObject(sql, this::mapRowToMessage, messageId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询消息", null);
        }
    }

    @Override
    public List<Message> findConversationList(Integer userId) {
        String sql = "SELECT sender_id, receiver_id, message_id, message_content, creation_time, update_time, is_withdraw, is_read FROM message WHERE sender_id = ? OR receiver_id = ? GROUP BY sender_id, receiver_id ORDER BY creation_time DESC";
        try {
            return queryForList(sql, this::mapRowToMessage, userId, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询用户会话列表", null);
        }
    }

    @Override
    public List<Message> findChatRecords(Integer fromUserId, Integer toUserId, int pageNum, int pageSize) {
        String sql = "SELECT sender_id, receiver_id, message_id, message_content, creation_time, update_time, is_withdraw, is_read FROM message WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY creation_time ASC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToMessage, fromUserId, toUserId, toUserId, fromUserId, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询聊天记录", null);
        }
    }

    @Override
    public int countUnreadTotal(Integer userId) {
        String sql = "SELECT COUNT(*) FROM message WHERE receiver_id = ? AND is_read = 0";
        try {
            Number count = (Number) queryForSingleValue(sql, userId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计未读消息总数", 0);
        }
    }

    @Override
    public int countUnreadByConversation(Integer fromUserId, Integer toUserId) {
        String sql = "SELECT COUNT(*) FROM message WHERE sender_id = ? AND receiver_id = ? AND is_read = 0";
        try {
            Number count = (Number) queryForSingleValue(sql, fromUserId, toUserId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计会话未读消息数", 0);
        }
    }

    @Override
    public int insert(Message message) {
        String sql = "INSERT INTO message (sender_id, receiver_id, message_content, creation_time, update_time, is_withdraw, is_read) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            return insert(sql, message.getSenderId(), message.getReceiverId(), message.getMessageContent(),
                         message.getCreationTime(), message.getUpdateTime(), message.getIsWithdraw(), message.getIsRead());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "发送消息", 0);
        }
    }

    @Override
    public int markAsRead(Integer fromUserId, Integer toUserId) {
        String sql = "UPDATE message SET is_read = 1 WHERE sender_id = ? AND receiver_id = ? AND is_read = 0";
        try {
            return update(sql, fromUserId, toUserId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "标记消息为已读", 0);
        }
    }

    @Override
    public int delete(Integer messageId) {
        String sql = "DELETE FROM message WHERE message_id = ?";
        try {
            return delete(sql, messageId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除消息", 0);
        }
    }

    @Override
    public int clearConversation(Integer userId1, Integer userId2) {
        String sql = "DELETE FROM message WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
        try {
            return delete(sql, userId1, userId2, userId2, userId1);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "清空会话消息", 0);
        }
    }

    private Message mapRowToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setSenderId(rs.getInt("sender_id"));
        message.setReceiverId(rs.getInt("receiver_id"));
        message.setMessageId(rs.getInt("message_id"));
        message.setMessageContent(rs.getString("message_content"));
        message.setCreationTime(rs.getDate("creation_time"));
        message.setUpdateTime(rs.getDate("update_time"));
        message.setIsWithdraw(rs.getInt("is_withdraw"));
        message.setIsRead(rs.getInt("is_read"));
        return message;
    }
}