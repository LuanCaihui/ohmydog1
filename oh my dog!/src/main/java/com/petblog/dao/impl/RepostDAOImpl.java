// src/main/java/com/petblog/dao/impl/RepostDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.RepostDAO;
import com.petblog.model.Repost;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class RepostDAOImpl extends BaseJdbcDAO<Repost> implements RepostDAO {

    @Override
    public Repost findById(Integer repostId) {
        String sql = "SELECT blog_id, user_id, reposts_time, repost_id FROM reposts WHERE repost_id = ?";
        try {
            return queryForObject(sql, this::mapRowToRepost, repostId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据转发ID查询转发信息", null);
        }
    }

    @Override
    public List<Repost> findByOriginalBlogId(Integer originalBlogId, int pageNum, int pageSize) {
        String sql = "SELECT blog_id, user_id, reposts_time, repost_id FROM reposts WHERE blog_id = ? ORDER BY reposts_time DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToRepost, originalBlogId, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据原博客ID查询转发列表", null);
        }
    }

    @Override
    public List<Repost> findByUserId(Integer userId, int pageNum, int pageSize) {
        String sql = "SELECT blog_id, user_id, reposts_time, repost_id FROM reposts WHERE user_id = ? ORDER BY reposts_time DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToRepost, userId, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据用户ID查询转发列表", null);
        }
    }

    @Override
    public int countByOriginalBlogId(Integer originalBlogId) {
        String sql = "SELECT COUNT(*) FROM reposts WHERE blog_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, originalBlogId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计原博客的转发数量", 0);
        }
    }

    @Override
    public int countByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM reposts WHERE user_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, userId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计用户的转发数量", 0);
        }
    }

    @Override
    public boolean hasReposted(Integer userId, Integer originalBlogId) {
        String sql = "SELECT COUNT(*) FROM reposts WHERE user_id = ? AND blog_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, userId, originalBlogId);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查用户是否已转发过博客", false);
        }
    }

    @Override
    public int insert(Repost repost) {
        String sql = "INSERT INTO reposts (blog_id, user_id, reposts_time, repost_id) VALUES (?, ?, ?, ?)";
        try {
            return insert(sql, repost.getBlogId(), repost.getUserId(),
                         repost.getRepostsTime(), repost.getRepostId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "插入转发记录", 0);
        }
    }

    @Override
    public int updateContent(Integer repostId, String repostContent) {
        // 根据实体类，我们没有转发内容字段，因此此方法保持空实现
        return 0;
    }

    @Override
    public int delete(Integer repostId) {
        String sql = "DELETE FROM reposts WHERE repost_id = ?";
        try {
            return delete(sql, repostId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除转发记录", 0);
        }
    }

    @Override
    public int deleteByUserId(Integer userId) {
        String sql = "DELETE FROM reposts WHERE user_id = ?";
        try {
            return update(sql, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除用户的所有转发记录", 0);
        }
    }

    @Override
    public int deleteByOriginalBlogId(Integer originalBlogId) {
        String sql = "DELETE FROM reposts WHERE blog_id = ?";
        try {
            return update(sql, originalBlogId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除针对指定博客的转发记录", 0);
        }
    }

    private Repost mapRowToRepost(ResultSet rs) throws SQLException {
        Repost repost = new Repost();
        repost.setBlogId(rs.getInt("blog_id"));
        repost.setUserId(rs.getInt("user_id"));
        repost.setRepostsTime(rs.getObject("reposts_time", LocalDateTime.class));
        repost.setRepostId(rs.getInt("repost_id"));
        return repost;
    }
}