package com.petblog.dao;


import com.petblog.model.Reply;

import java.sql.SQLException;
import java.util.List;

/**
 * 评论回复DAO接口
 * 定义对replies表的所有数据操作方法
 * 该表用于管理用户对博客评论的二级回复（支持用户A回复用户B的评论）
 */
public interface ReplyDAO {

    /**
     * 根据回复ID查询回复详情
     * @param replyId 回复ID
     * @return 回复实体对象，包含完整信息
     */
    Reply findById(Integer replyId) throws SQLException;

    /**
     * 根据评论ID查询所有回复
     * @param commentId 评论ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 回复列表（按发布时间正序）
     */
    List<Reply> findByCommentId(Integer commentId, int pageNum, int pageSize) throws SQLException;

    /**
     * 根据用户ID查询其发布的所有回复
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 回复列表（按发布时间倒序）
     */
    List<Reply> findByUserId(Integer userId, int pageNum, int pageSize) throws SQLException;

    /**
     * 根据博客ID查询所有一级评论（parentReply为null的回复）
     * @param blogId 博客ID
     * @return 评论列表（按发布时间正序）
     */
    List<Reply> findByBlogId(Integer blogId) throws SQLException;

    /**
     * 统计某评论的回复总数
     * @param commentId 评论ID
     * @return 回复数量
     */
    int countByCommentId(Integer commentId) throws SQLException;

    /**
     * 统计用户发布的回复总数
     * @param userId 用户ID
     * @return 回复数量
     */
    int countByUserId(Integer userId) throws SQLException;

    /**
     * 新增回复
     * @param reply 回复实体（包含评论ID、内容、回复者ID等信息）
     * @return 新增回复的ID（自增主键），失败返回0
     */
    int insert(Reply reply) throws SQLException;

    /**
     * 更新回复内容
     * @param reply 回复实体（需包含回复ID和新内容）
     * @return 影响行数（1表示成功，0表示失败）
     */
    int updateContent(Reply reply) throws SQLException;

    /**
     * 更新回复的状态（正常/删除）
     * @param replyId 回复ID
     * @param status 状态（0=已删除，1=正常）
     * @return 影响行数
     */
    int updateStatus(Integer replyId, Integer status) throws SQLException;

    /**
     * 删除回复（物理删除或逻辑删除）
     * @param replyId 回复ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer replyId) throws SQLException;

    /**
     * 删除某评论下的所有回复
     * @param commentId 评论ID
     * @return 影响行数
     */
    int deleteByCommentId(Integer commentId) throws SQLException;

    /**
     * 删除用户发布的所有回复
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Integer userId) throws SQLException;
}
    