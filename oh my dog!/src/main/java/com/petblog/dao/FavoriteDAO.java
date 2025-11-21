package com.petblog.dao;
import com.petblog.model.Favorite;

import java.sql.SQLException;
import java.util.List;

/**
 * 用户收藏DAO接口
 * 定义对favorites表的所有数据操作方法
 * 该表用于记录用户收藏博客的关联关系（支持用户收藏多篇博客，一篇博客可被多个用户收藏）
 */
public interface FavoriteDAO {

    /**
     * 根据用户ID查询其收藏的所有博客ID
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 收藏的博客ID列表（按收藏时间倒序）
     */
    List<Integer> findBlogIdsByUserId(Integer userId, int pageNum, int pageSize) throws SQLException;

    /**
     * 根据博客ID查询所有收藏该博客的用户ID
     * @param blogId 博客ID
     * @return 收藏用户ID列表
     */
    List<Integer> findUserIdsByBlogId(Integer blogId) throws SQLException;

    /**
     * 统计用户的收藏总数
     * @param userId 用户ID
     * @return 收藏数量
     */
    int countByUserId(Integer userId) throws SQLException;

    /**
     * 统计博客的被收藏次数
     * @param blogId 博客ID
     * @return 被收藏数量
     */
    int countByBlogId(Integer blogId) throws SQLException;

    /**
     * 检查用户是否已收藏指定博客
     * @param userId 用户ID
     * @param blogId 博客ID
     * @return 已收藏返回true，否则返回false
     */
    boolean isFavorite(Integer userId, Integer blogId) throws SQLException;

    /**
     * 新增博客收藏
     * @param favorite 收藏实体（包含用户ID、博客ID和收藏时间）
     * @return 影响行数（1表示成功，0表示失败）
     */
    int insert(Favorite favorite) throws SQLException;

    /**
     * 取消博客收藏（删除收藏关系）
     * @param userId 用户ID
     * @param blogId 博客ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer userId, Integer blogId) throws SQLException;

    /**
     * 删除用户的所有收藏（通常在删除用户时调用）
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Integer userId) throws SQLException;

    /**
     * 移除所有用户对指定博客的收藏（通常在删除博客时调用）
     * @param blogId 博客ID
     * @return 影响行数
     */
    int deleteByBlogId(Integer blogId) throws SQLException;
}