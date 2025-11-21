package com.petblog.dao;
import com.petblog.model.BlogTopic;

import java.sql.SQLException;
import java.util.List;

/**
 * 博客-话题关联DAO接口
 * 定义对blogtopic表的所有数据操作方法
 * 该表用于维护博客与话题的多对多关系（一篇博客可关联多个话题，一个话题可包含多篇博客）
 */
public interface BlogTopicDAO {

    /**
     * 根据博客ID查询关联的所有话题ID
     * @param blogId 博客ID
     * @return 关联的话题ID列表
     */
    List<Integer> findTopicIdsByBlogId(Integer blogId) throws SQLException;

    /**
     * 根据话题ID查询关联的所有博客ID
     * @param topicId 话题ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 关联的博客ID列表（按发布时间倒序）
     */
    List<Integer> findBlogIdsByTopicId(Integer topicId, int pageNum, int pageSize) throws SQLException;

    /**
     * 统计某话题下的博客总数
     * @param topicId 话题ID
     * @return 博客数量
     */
    int countBlogsByTopicId(Integer topicId) throws SQLException;

    /**
     * 检查博客与话题的关联关系是否存在
     * @param blogId 博客ID
     * @param topicId 话题ID
     * @return 存在返回true，否则返回false
     */
    boolean exists(Integer blogId, Integer topicId) throws SQLException;

    /**
     * 新增博客与话题的关联关系
     * @param blogTopic 博客-话题关联实体
     * @return 影响行数（1表示成功，0表示失败）
     */
    int insert(BlogTopic blogTopic) throws SQLException;

    /**
     * 批量新增博客与话题的关联关系
     * @param blogTopics 博客-话题关联实体列表
     * @return 成功插入的数量
     */
    int batchInsert(List<BlogTopic> blogTopics) throws SQLException;

    /**
     * 解除某篇博客与所有话题的关联
     * @param blogId 博客ID
     * @return 影响行数
     */
    int deleteByBlogId(Integer blogId) throws SQLException;

    /**
     * 解除某个话题与所有博客的关联
     * @param topicId 话题ID
     * @return 影响行数
     */
    int deleteByTopicId(Integer topicId) throws SQLException;

    /**
     * 解除指定博客与指定话题的关联
     * @param blogId 博客ID
     * @param topicId 话题ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer blogId, Integer topicId) throws SQLException;
}
    