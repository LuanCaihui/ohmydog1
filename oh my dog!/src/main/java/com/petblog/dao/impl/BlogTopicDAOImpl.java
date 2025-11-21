// src/main/java/com/petblog/dao/impl/BlogTopicDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.BlogTopicDAO;
import com.petblog.model.BlogTopic;
import com.petblog.util.JdbcUtil;
import com.petblog.util.SQLExceptionHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BlogTopicDAOImpl extends BaseJdbcDAO<Integer> implements BlogTopicDAO {

    @Override
    public List<Integer> findTopicIdsByBlogId(Integer blogId) {
        String sql = "SELECT topic_id FROM blogtopic WHERE blog_id = ?";
        try {
            return queryForList(sql, rs -> {
                try {
                    return rs.getInt("topic_id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, blogId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据博客ID查询主题ID列表", null);
        }
    }

    @Override
    public List<Integer> findBlogIdsByTopicId(Integer topicId, int pageNum, int pageSize) {
        String sql = "SELECT blog_id FROM blogtopic WHERE topic_id = ? ORDER BY blog_id DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, rs -> {
                try {
                    return rs.getInt("blog_id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, topicId, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "分页查询指定主题的博客ID列表", null);
        }
    }

    @Override
    public int countBlogsByTopicId(Integer topicId) {
        String sql = "SELECT COUNT(*) FROM blogtopic WHERE topic_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, topicId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计指定主题的博客数量", 0);
        }
    }

    @Override
    public boolean exists(Integer blogId, Integer topicId) {
        String sql = "SELECT COUNT(*) FROM blogtopic WHERE blog_id = ? AND topic_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, blogId, topicId);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查博客主题关联是否存在", false);
        }
    }

    @Override
    public int insert(BlogTopic blogTopic) {
        String sql = "INSERT INTO blogtopic (blog_id, topic_id) VALUES (?, ?)";
        try {
            return insert(sql, blogTopic.getBlogId(), blogTopic.getTopicId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "插入博客主题关联数据", 0);
        }
    }

    @Override
    public int batchInsert(List<BlogTopic> blogTopics) {
        String sql = "INSERT INTO blogtopic (blog_id, topic_id) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        int count = 0;

        try {
            conn = JdbcUtil.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);

            for (BlogTopic blogTopic : blogTopics) {
                pstmt.setInt(1, blogTopic.getBlogId());
                pstmt.setInt(2, blogTopic.getTopicId());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            for (int result : results) {
                if (result > 0) count++;
            }

            conn.commit();
            return count;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                throw SQLExceptionHandler.handleSQLException(ex, "回滚批量插入操作");
            }
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "批量插入博客主题关联数据", 0);
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                throw SQLExceptionHandler.handleSQLException(e, "恢复自动提交模式");
            }
            JdbcUtil.close(conn, pstmt);
        }
    }

    @Override
    public int deleteByBlogId(Integer blogId) {
        String sql = "DELETE FROM blogtopic WHERE blog_id = ?";
        try {
            return delete(sql, blogId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据博客ID删除主题关联数据", 0);
        }
    }

    @Override
    public int deleteByTopicId(Integer topicId) {
        String sql = "DELETE FROM blogtopic WHERE topic_id = ?";
        try {
            return delete(sql, topicId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据主题ID删除博客关联数据", 0);
        }
    }

    @Override
    public int delete(Integer blogId, Integer topicId) {
        String sql = "DELETE FROM blogtopic WHERE blog_id = ? AND topic_id = ?";
        try {
            return delete(sql, blogId, topicId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除指定的博客主题关联数据", 0);
        }
    }
}
