package com.petblog.dao.impl;

import com.petblog.dao.LikeDAO;
import com.petblog.model.Like;
import com.petblog.util.SQLExceptionHandler;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LikeDAO接口的JDBC实现类
 */
public class LikeDAOImpl implements LikeDAO {

    private final Connection connection;

    public LikeDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Like findById(Integer likeId) {
        // 注意：由于Like表使用userId和blogId作为联合主键，没有单独的likeId字段
        // 此方法可能需要重新设计或者根据实际需求调整
        return null;
    }

    @Override
    public List<Integer> findBlogIdsByUserId(Integer userId, int pageNum, int pageSize) {
        List<Integer> blogIds = new ArrayList<>();
        String sql = "SELECT blog_id FROM likes WHERE user_id = ? ORDER BY like_time DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, (pageNum - 1) * pageSize);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                blogIds.add(rs.getInt("blog_id"));
            }
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "分页查询用户点赞的博客ID", blogIds);
        }
        return blogIds;
    }

    @Override
    public List<Integer> findUserIdsByBlogId(Integer blogId) {
        List<Integer> userIds = new ArrayList<>();
        String sql = "SELECT user_id FROM likes WHERE blog_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, blogId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                userIds.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询博客的点赞用户ID", userIds);
        }
        return userIds;
    }

    @Override
    public int countByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计用户点赞总数", 0);
        }
        return 0;
    }

    @Override
    public int countByBlogId(Integer blogId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE blog_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, blogId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计博客点赞数", 0);
        }
        return 0;
    }

    @Override
    public boolean isLiked(Integer userId, Integer blogId) {
        String sql = "SELECT 1 FROM likes WHERE user_id = ? AND blog_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, blogId);

            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查用户是否点赞博客", false);
        }
    }

    @Override
    public int insert(Like like) {
        String sql = "INSERT INTO likes (user_id, blog_id, like_time) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, like.getUserId());
            stmt.setInt(2, like.getBlogId());
            stmt.setTimestamp(3, new Timestamp(like.getLikeTime().getTime()));

            return stmt.executeUpdate();
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "添加点赞记录", 0);
        }
    }

    @Override
    public int delete(Integer userId, Integer blogId) {
        String sql = "DELETE FROM likes WHERE user_id = ? AND blog_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, blogId);

            return stmt.executeUpdate();
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "取消点赞记录", 0);
        }
    }

    @Override
    public int deleteByUserId(Integer userId) {
        String sql = "DELETE FROM likes WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            return stmt.executeUpdate();
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除用户的所有点赞记录", 0);
        }
    }

    @Override
    public int deleteByBlogId(Integer blogId) {
        String sql = "DELETE FROM likes WHERE blog_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, blogId);

            return stmt.executeUpdate();
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除博客的所有点赞记录", 0);
        }
    }
}