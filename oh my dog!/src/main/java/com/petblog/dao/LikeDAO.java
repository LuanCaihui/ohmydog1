package com.petblog.dao;

import com.petblog.model.Like;

import java.sql.SQLException;
import java.util.List;

/**
 * 用户点赞DAO接口
 * 定义对likes表的所有数据操作方法
 * 该表用于记录用户对博客的点赞关系（支持用户对多篇博客点赞，一篇博客可被多个用户点赞）
 */
public interface LikeDAO {

    /**
     * 根据点赞记录ID查询详情
     * @param likeId 点赞记录ID
     * @return 点赞实体对象
     */
    Like findById(Integer likeId) throws SQLException;

    /**
     * 根据用户ID查询其点赞的所有博客ID
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 点赞的博客ID列表（按点赞时间倒序）
     */
    List<Integer> findBlogIdsByUserId(Integer userId, int pageNum, int pageSize) throws SQLException;

    /**
     * 根据博客ID查询所有点赞该博客的用户ID
     * @param blogId 博客ID
     * @return 点赞用户ID列表
     */
    List<Integer> findUserIdsByBlogId(Integer blogId) throws SQLException;

    /**
     * 统计用户的点赞总数
     * @param userId 用户ID
     * @return 点赞数量
     */
    int countByUserId(Integer userId) throws SQLException;

    /**
     * 统计博客的获赞总数
     * @param blogId 博客ID
     * @return 获赞数量
     */
    int countByBlogId(Integer blogId) throws SQLException;

    /**
     * 检查用户是否已点赞指定博客
     * @param userId 用户ID
     * @param blogId 博客ID
     * @return 已点赞返回true，否则返回false
     */
    boolean isLiked(Integer userId, Integer blogId) throws SQLException;

    /**
     * 新增点赞记录
     * @param like 点赞实体（包含用户ID、博客ID和点赞时间）
     * @return 影响行数（1表示成功，0表示失败）
     */
    int insert(Like like) throws SQLException;

    /**
     * 取消点赞（删除点赞记录）
     * @param userId 用户ID
     * @param blogId 博客ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer userId, Integer blogId) throws SQLException;

    /**
     * 删除用户的所有点赞记录
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Integer userId) throws SQLException;

    /**
     * 移除所有用户对指定博客的点赞记录
     * @param blogId 博客ID
     * @return 影响行数
     */
    int deleteByBlogId(Integer blogId) throws SQLException;
}