package com.petblog.dao;

import com.petblog.model.Message;

import java.sql.SQLException;
import java.util.List;

/**
 * 私信DAO接口
 * 定义对message表的所有数据操作方法
 * 该表用于管理用户之间的私信交流（支持用户A向用户B发送私信）
 */
public interface MessageDAO {

    /**
     * 根据私信ID查询私信详情
     * @param messageId 私信ID
     * @return 私信实体对象，包含完整信息
     */
    Message findById(Integer messageId) throws SQLException;

    /**
     * 查询用户的私信列表（与其他用户的对话列表）
     * @param userId 用户ID
     * @return 包含最近一条消息的对话列表（按最后消息时间倒序）
     */
    List<Message> findConversationList(Integer userId) throws SQLException;

    /**
     * 查询两个用户之间的私信记录
     * @param fromUserId 发送方用户ID
     * @param toUserId 接收方用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 私信记录列表（按发送时间正序）
     */
    List<Message> findChatRecords(Integer fromUserId, Integer toUserId, int pageNum, int pageSize) throws SQLException;

    /**
     * 统计用户的未读私信总数
     * @param userId 接收方用户ID
     * @return 未读私信数量
     */
    int countUnreadTotal(Integer userId) throws SQLException;

    /**
     * 统计两个用户之间的未读私信数量
     * @param fromUserId 发送方用户ID
     * @param toUserId 接收方用户ID
     * @return 未读私信数量
     */
    int countUnreadByConversation(Integer fromUserId, Integer toUserId)  throws SQLException;

    /**
     * 发送私信（新增私信记录）
     * @param message 私信实体（包含发送方、接收方、内容等信息）
     * @return 新增私信的ID（自增主键），失败返回0
     */
    int insert(Message message) throws SQLException;

    /**
     * 将对话中的未读私信标记为已读
     * @param fromUserId 发送方用户ID
     * @param toUserId 接收方用户ID
     * @return 影响行数
     */
    int markAsRead(Integer fromUserId, Integer toUserId) throws SQLException;

    /**
     * 删除单条私信（通常为逻辑删除，更新状态）
     * @param messageId 私信ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer messageId) throws SQLException;

    /**
     * 清空两个用户之间的对话记录
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 影响行数
     */
    int clearConversation(Integer userId1, Integer userId2) throws SQLException;
}