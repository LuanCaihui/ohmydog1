package com.petblog.Service;

import com.petblog.dao.BlogTopicDAO;
import com.petblog.dao.impl.BlogTopicDAOImpl;
import com.petblog.model.BlogTopic;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class BlogTopicService extends BaseService {

    private BlogTopicDAO blogTopicDAO = new BlogTopicDAOImpl();

    /**
     * 根据博客ID查询关联的所有话题ID
     */
    public List<Integer> getTopicIdsByBlogId(Integer blogId) {
        try {
            return blogTopicDAO.findTopicIdsByBlogId(blogId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据博客ID查询话题ID列表");
            return null;
        }
    }

    /**
     * 根据话题ID查询关联的所有博客ID（分页）
     */
    public List<Integer> getBlogIdsByTopicId(Integer topicId, int pageNum, int pageSize) {
        try {
            return blogTopicDAO.findBlogIdsByTopicId(topicId, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据话题ID查询博客ID列表");
            return null;
        }
    }

    /**
     * 统计某话题下的博客总数
     */
    public int countBlogsByTopicId(Integer topicId) {
        try {
            return blogTopicDAO.countBlogsByTopicId(topicId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计话题下博客数量");
            return 0;
        }
    }

    /**
     * 检查博客与话题的关联关系是否存在
     */
    public boolean isBlogTopicExists(Integer blogId, Integer topicId) {
        try {
            return blogTopicDAO.exists(blogId, topicId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "检查博客与话题关联关系");
            return false;
        }
    }

    /**
     * 新增博客与话题的关联关系
     */
    public boolean createBlogTopic(BlogTopic blogTopic) {
        try {
            int result = blogTopicDAO.insert(blogTopic);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "创建博客与话题关联");
            return false;
        }
    }

    /**
     * 批量新增博客与话题的关联关系
     */
    public boolean batchCreateBlogTopics(List<BlogTopic> blogTopics) {
        try {
            int result = blogTopicDAO.batchInsert(blogTopics);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "批量创建博客与话题关联");
            return false;
        }
    }

    /**
     * 解除某篇博客与所有话题的关联
     */
    public boolean deleteBlogTopicsByBlogId(Integer blogId) {
        try {
            int result = blogTopicDAO.deleteByBlogId(blogId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "解除博客与所有话题关联");
            return false;
        }
    }

    /**
     * 解除某个话题与所有博客的关联
     */
    public boolean deleteBlogTopicsByTopicId(Integer topicId) {
        try {
            int result = blogTopicDAO.deleteByTopicId(topicId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "解除话题与所有博客关联");
            return false;
        }
    }

    /**
     * 解除指定博客与指定话题的关联
     */
    public boolean deleteBlogTopic(Integer blogId, Integer topicId) {
        try {
            int result = blogTopicDAO.delete(blogId, topicId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "解除博客与话题关联");
            return false;
        }
    }
}
