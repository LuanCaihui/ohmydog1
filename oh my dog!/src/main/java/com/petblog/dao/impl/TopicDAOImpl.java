// src/main/java/com/petblog/dao/impl/TopicDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.TopicDAO;
import com.petblog.model.Topic;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class TopicDAOImpl extends BaseJdbcDAO<Topic> implements TopicDAO {

    @Override
    public Topic findById(Integer topicId) {
        String sql = "SELECT topic_id, topic_name, topic_create_time FROM topics WHERE topic_id = ?";
        try {
            return queryForObject(sql, this::mapRowToTopic, topicId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询话题详情", null);
        }
    }

    @Override
    public Topic findByName(String topicName) {
        String sql = "SELECT topic_id, topic_name, topic_create_time FROM topics WHERE topic_name = ?";
        try {
            return queryForObject(sql, this::mapRowToTopic, topicName);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据名称查询话题", null);
        }
    }

    @Override
    public List<Topic> searchByName(String keyword, int pageNum, int pageSize) {
        String sql = "SELECT topic_id, topic_name, topic_create_time FROM topics WHERE topic_name LIKE ? LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToTopic, "%" + keyword + "%", pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "搜索话题", null);
        }
    }

    @Override
    public List<Topic> findPopularTopics(int limit) {
        // 假设通过关联的博客数量来确定热门话题
        String sql = "SELECT t.topic_id, t.topic_name, t.topic_create_time FROM topics t " +
                     "LEFT JOIN blogtopic bt ON t.topic_id = bt.topic_id " +
                     "GROUP BY t.topic_id, t.topic_name, t.topic_create_time " +
                     "ORDER BY COUNT(bt.blog_id) DESC LIMIT ?";
        try {
            return queryForList(sql, this::mapRowToTopic, limit);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询热门话题", null);
        }
    }

    @Override
    public List<Topic> findLatestTopics(int limit) {
        String sql = "SELECT topic_id, topic_name, topic_create_time FROM topics ORDER BY topic_create_time DESC LIMIT ?";
        try {
            return queryForList(sql, this::mapRowToTopic, limit);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询最新话题", null);
        }
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM topics";
        try {
            Number count = (Number) queryForSingleValue(sql);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计话题总数", 0);
        }
    }

    @Override
    public int insert(Topic topic) {
        String sql = "INSERT INTO topics (topic_name, topic_create_time) VALUES (?, ?)";
        try {
            return insert(sql, topic.getTopicName(), topic.getTopicCreateTime());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "添加话题", 0);
        }
    }

    @Override
    public int update(Topic topic) {
        String sql = "UPDATE topics SET topic_name = ? WHERE topic_id = ?";
        try {
            return update(sql, topic.getTopicName(), topic.getTopicId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新话题信息", 0);
        }
    }

    @Override
    public int incrementBlogCount(Integer topicId) {
        // 假设有一个博客计数字段
        String sql = "UPDATE topics SET blog_count = blog_count + 1 WHERE topic_id = ?";
        try {
            return update(sql, topicId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "增加话题博客数量", 0);
        }
    }

    @Override
    public int decrementBlogCount(Integer topicId) {
        // 假设有一个博客计数字段
        String sql = "UPDATE topics SET blog_count = blog_count - 1 WHERE topic_id = ? AND blog_count > 0";
        try {
            return update(sql, topicId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "减少话题博客数量", 0);
        }
    }

    @Override
    public int delete(Integer topicId) {
        String sql = "DELETE FROM topics WHERE topic_id = ?";
        try {
            return delete(sql, topicId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除话题", 0);
        }
    }

    @Override
    public boolean existsByName(String topicName) {
        String sql = "SELECT COUNT(*) FROM topics WHERE topic_name = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, topicName);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查话题名称是否存在", false);
        }
    }

    private Topic mapRowToTopic(ResultSet rs) throws SQLException {
        Topic topic = new Topic();
        topic.setTopicId(rs.getInt("topic_id"));
        topic.setTopicName(rs.getString("topic_name"));
        topic.setTopicCreateTime(rs.getObject("topic_create_time", LocalDateTime.class));
        return topic;
    }
}