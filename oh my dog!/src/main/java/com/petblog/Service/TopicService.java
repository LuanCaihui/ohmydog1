package com.petblog.Service;

import com.petblog.dao.TopicDAO;
import com.petblog.dao.impl.TopicDAOImpl;
import com.petblog.model.Topic;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class TopicService extends BaseService {

    private final TopicDAO topicDAO;

    public TopicService() {
        this.topicDAO = new TopicDAOImpl();
    }

    /**
     * 根据话题ID查询话题详情
     */
    public Topic getTopicById(Integer topicId) {
        try {
            return topicDAO.findById(topicId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询话题详情");
            return null;
        }
    }

    /**
     * 根据话题名称精确查询
     */
    public Topic getTopicByName(String topicName) {
        try {
            return topicDAO.findByName(topicName);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据名称查询话题");
            return null;
        }
    }

    /**
     * 根据话题名称模糊搜索
     */
    public List<Topic> searchTopicsByName(String keyword, int pageNum, int pageSize) {
        try {
            return topicDAO.searchByName(keyword, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据名称搜索话题");
            return null;
        }
    }

    /**
     * 查询热门话题（按关联博客数量排序）
     */
    public List<Topic> getPopularTopics(int limit) {
        try {
            return topicDAO.findPopularTopics(limit);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询热门话题");
            return null;
        }
    }

    /**
     * 查询最新创建的话题
     */
    public List<Topic> getLatestTopics(int limit) {
        try {
            return topicDAO.findLatestTopics(limit);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询最新话题");
            return null;
        }
    }

    /**
     * 统计话题总数
     */
    public int countAllTopics() {
        try {
            return topicDAO.countAll();
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计话题总数");
            return 0;
        }
    }

    /**
     * 新增话题
     */
    public Integer createTopic(Topic topic) {
        try {
            return topicDAO.insert(topic);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增话题");
            return 0;
        }
    }

    /**
     * 更新话题信息（名称、描述等）
     */
    public boolean updateTopic(Topic topic) {
        try {
            int result = topicDAO.update(topic);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新话题信息");
            return false;
        }
    }

    /**
     * 增加话题的关联博客数量
     */
    public boolean incrementTopicBlogCount(Integer topicId) {
        try {
            int result = topicDAO.incrementBlogCount(topicId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "增加话题博客数量");
            return false;
        }
    }

    /**
     * 减少话题的关联博客数量
     */
    public boolean decrementTopicBlogCount(Integer topicId) {
        try {
            int result = topicDAO.decrementBlogCount(topicId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "减少话题博客数量");
            return false;
        }
    }

    /**
     * 删除话题（仅允许管理员操作）
     */
    public boolean deleteTopic(Integer topicId) {
        try {
            int result = topicDAO.delete(topicId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除话题");
            return false;
        }
    }

    /**
     * 检查话题名称是否已存在
     */
    public boolean isTopicNameExists(String topicName) {
        try {
            return topicDAO.existsByName(topicName);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "检查话题名称是否存在");
            return false;
        }
    }
}
