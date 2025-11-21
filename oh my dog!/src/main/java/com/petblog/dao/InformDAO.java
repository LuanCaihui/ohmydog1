package com.petblog.dao;

import com.petblog.model.Inform;

import java.sql.SQLException;
import java.util.List;

/**
 * 通知DAO接口
 * 定义对informs表的所有数据操作方法
 * 该表用于管理系统向用户推送的各类通知（如点赞提醒、回复通知等）
 */
public interface InformDAO {

    /**
     * 根据通知ID查询通知详情
     * @param informId 通知ID
     * @return 通知实体对象，包含完整信息
     */
    Inform findById(Integer informId) throws SQLException;

    /**
     * 查询用户的所有通知
     * @param userId 接收通知的用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 通知列表（按创建时间倒序）
     */
    List<Inform> findByUserId(Integer userId, int pageNum, int pageSize) throws SQLException;

    /**
     * 查询用户的未读通知
     * @param userId 接收通知的用户ID
     * @return 未读通知列表（按创建时间倒序）
     */
    List<Inform> findUnreadByUserId(Integer userId) throws SQLException;

    /**
     * 统计用户的未读通知数量
     * @param userId 接收通知的用户ID
     * @return 未读通知数量
     */
    int countUnreadByUserId(Integer userId) throws SQLException;

    /**
     * 统计用户的通知总数
     * @param userId 接收通知的用户ID
     * @return 通知总数
     */
    int countByUserId(Integer userId) throws SQLException;

    /**
     * 新增通知
     * @param inform 通知实体（包含接收用户ID、通知类型、内容等信息）
     * @return 新增通知的ID（自增主键），失败返回0
     */
    int insert(Inform inform)throws SQLException;

    /**
     * 批量新增通知（如多人收到同一事件的通知）
     * @param informs 通知实体列表
     * @return 成功插入的数量
     */
    int batchInsert(List<Inform> informs)throws SQLException;

    /**
     * 将通知标记为已读
     * @param informId 通知ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int markAsRead(Integer informId) throws SQLException;

    /**
     * 将用户的所有未读通知标记为已读
     * @param userId 接收通知的用户ID
     * @return 影响行数
     */
    int markAllAsRead(Integer userId) throws SQLException;

    /**
     * 删除指定通知
     * @param informId 通知ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer informId) throws SQLException;

    /**
     * 批量删除用户的通知（如删除选中的通知）
     * @param informIds 通知ID列表
     * @return 成功删除的数量
     */
    int batchDelete(List<Integer> informIds) throws SQLException;

    /**
     * 清除用户的所有通知
     * @param userId 接收通知的用户ID
     * @return 影响行数
     */
    int clearAllByUserId(Integer userId) throws SQLException;
}