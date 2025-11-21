package com.petblog.dao;

import com.petblog.model.Topic;

import java.sql.SQLException;
import java.util.List;

/**
 * 话题DAO接口
 * 定义对topics表的所有数据操作方法，用于管理平台内的话题标签及相关信息
 */
public interface TopicDAO {

    /**
     * 根据话题ID查询话题话题详情
     * @param topicId 话题ID
     * @return 话题实体对象，包含完整信息
     */
    Topic findById(Integer topicId) throws SQLException;

    /**
     * 根据话题名称精确查询
     * @param topicName 话题名称
     * @return 话题实体对象，不存在则返回null
     */
    Topic findByName(String topicName) throws SQLException;

    /**
     * 根据话题名称模糊搜索
     * @param keyword 搜索关键词
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 符合条件的话题列表
     */
    List<Topic> searchByName(String keyword, int pageNum, int pageSize) throws SQLException;

    /**
     * 查询热门话题（按关联博客数量排序）
     * @param limit 最多返回数量
     * @return 热门话题列表
     */
    List<Topic> findPopularTopics(int limit) throws SQLException;

    /**
     * 查询最新创建的话题
     * @param limit 最多返回数量
     * @return 最新话题列表（按创建时间倒序）
     */
    List<Topic> findLatestTopics(int limit) throws SQLException;

    /**
     * 统计话题总数
     * @return 所有话题的数量
     */
    int countAll() throws SQLException;

    /**
     * 新增话题
     * @param topic 话题实体（包含名称、描述等信息）
     * @return 新增话题的ID（自增主键），失败返回0
     */
    int insert(Topic topic) throws SQLException;

    /**
     * 更新话题信息（名称、描述等）
     * @param topic 话题实体（需包含话题ID）
     * @return 影响行数（1表示成功，0表示失败）
     */
    int update(Topic topic) throws SQLException;

    /**
     * 更新话题的关联博客数量（+1）
     * @param topicId 话题ID
     * @return 影响行数
     */
    int incrementBlogCount(Integer topicId) throws SQLException;

    /**
     * 更新话题的关联博客数量（-1）
     * @param topicId 话题ID
     * @return 影响行数
     */
    int decrementBlogCount(Integer topicId) throws SQLException;

    /**
     * 删除话题（仅允许管理员操作）
     * @param topicId 话题ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer topicId) throws SQLException;

    /**
     * 检查话题名称是否已存在
     * @param topicName 话题名称
     * @return 存在返回true，否则返回false
     */
    boolean existsByName(String topicName) throws SQLException;
}