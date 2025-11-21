package com.petblog.Service;

import com.petblog.dao.MessageDAO;
import com.petblog.dao.impl.MessageDAOImpl;
import com.petblog.model.Message;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class MessageService extends BaseService {

    private final MessageDAO messageDAO;

    public MessageService() {
        this.messageDAO = new MessageDAOImpl();
    }

    /**
     * 根据私信ID查询私信详情
     */
    public Message getMessageById(Integer messageId) {
        try {
            return messageDAO.findById(messageId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询私信详情");
            return null;
        }
    }

    /**
     * 查询用户的私信列表（与其他用户的对话列表）
     */
    public List<Message> getConversationList(Integer userId) {
        try {
            return messageDAO.findConversationList(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询用户私信对话列表");
            return null;
        }
    }

    /**
     * 查询两个用户之间的私信记录
     */
    public List<Message> getChatRecords(Integer fromUserId, Integer toUserId, int pageNum, int pageSize) {
        try {
            return messageDAO.findChatRecords(fromUserId, toUserId, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询用户间私信记录");
            return null;
        }
    }

    /**
     * 统计用户的未读私信总数
     */
    public int countUserUnreadTotal(Integer userId) {
        try {
            return messageDAO.countUnreadTotal(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计用户未读私信总数");
            return 0;
        }
    }

    /**
     * 统计两个用户之间的未读私信数量
     */
    public int countUnreadByConversation(Integer fromUserId, Integer toUserId) {
        try {
            return messageDAO.countUnreadByConversation(fromUserId, toUserId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计对话未读私信数量");
            return 0;
        }
    }

    /**
     * 发送私信（新增私信记录）
     */
    public Integer sendMessage(Message message) {
        try {
            return messageDAO.insert(message);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "发送私信");
            return 0;
        }
    }

    /**
     * 将对话中的未读私信标记为已读
     */
    public boolean markConversationAsRead(Integer fromUserId, Integer toUserId) {
        try {
            int result = messageDAO.markAsRead(fromUserId, toUserId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "标记对话为已读");
            return false;
        }
    }

    /**
     * 删除单条私信（通常为逻辑删除，更新状态）
     */
    public boolean deleteMessage(Integer messageId) {
        try {
            int result = messageDAO.delete(messageId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除私信");
            return false;
        }
    }

    /**
     * 清空两个用户之间的对话记录
     */
    public boolean clearConversation(Integer userId1, Integer userId2) {
        try {
            int result = messageDAO.clearConversation(userId1, userId2);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "清空对话记录");
            return false;
        }
    }
}
