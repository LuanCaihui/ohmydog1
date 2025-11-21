package com.petblog.dao;

import com.petblog.model.Repost;

import java.sql.SQLException;
import java.util.List;

/**
 * 博客转发DAO接口
 * 定义对reposts表的所有数据操作方法
 * 该表用于管理用户对博客的转发行为（支持用户转发他人博客并添加转发说明）
 */
public interface RepostDAO {

    /**
     * 根据转发ID查询转发详情
     * @param repostId 转发ID
     * @return 转发实体对象，包含完整信息
     */
    Repost findById(Integer repostId) throws SQLException;

    /**
     * 根据原博客ID查询所有转发记录
     * @param originalBlogId 原博客ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 转发记录列表（按转发时间倒序）
     */
    List<Repost> findByOriginalBlogId(Integer originalBlogId, int pageNum, int pageSize) throws SQLException;

    /**
     * 根据用户ID查询其所有转发记录
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 转发记录列表（按转发时间倒序）
     */
    List<Repost> findByUserId(Integer userId, int pageNum, int pageSize) throws SQLException;

    /**
     * 统计原博客的转发总数
     * @param originalBlogId 原博客ID
     * @return 转发数量
     */
    int countByOriginalBlogId(Integer originalBlogId) throws SQLException;

    /**
     * 统计用户的转发总数
     * @param userId 用户ID
     * @return 转发数量
     */
    int countByUserId(Integer userId) throws SQLException;

    /**
     * 检查用户是否已转发过指定博客
     * @param userId 用户ID
     * @param originalBlogId 原博客ID
     * @return 已转发返回true，否则返回false
     */
    boolean hasReposted(Integer userId, Integer originalBlogId) throws SQLException;

    /**
     * 新增转发记录
     * @param repost 转发实体（包含原博客ID、用户ID、转发说明等信息）
     * @return 新增转发的ID（自增主键），失败返回0
     */
    int insert(Repost repost) throws SQLException;

    /**
     * 更新转发说明内容
     * @param repostId 转发ID
     * @param repostContent 新的转发说明
     * @return 影响行数（1表示成功，0表示失败）
     */
    int updateContent(Integer repostId, String repostContent) throws SQLException;

    /**
     * 删除转发记录
     * @param repostId 转发ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer repostId) throws SQLException;

    /**
     * 删除用户的所有转发记录
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Integer userId) throws SQLException;

    /**
     * 移除所有针对指定博客的转发记录
     * @param originalBlogId 原博客ID
     * @return 影响行数
     */
    int deleteByOriginalBlogId(Integer originalBlogId) throws SQLException;
}