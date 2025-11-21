// src/main/java/com/petblog/dao/impl/FavoriteDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.FavoriteDAO;
import com.petblog.model.Favorite;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class FavoriteDAOImpl extends BaseJdbcDAO<Integer> implements FavoriteDAO {

    @Override
    public List<Integer> findBlogIdsByUserId(Integer userId, int pageNum, int pageSize) {
        String sql = "SELECT blog_id FROM favorites WHERE user_id = ? ORDER BY favorite_time DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, rs -> {
                try {
                    return rs.getInt("blog_id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, userId, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询用户收藏的博客ID列表", null);
        }
    }

    @Override
    public List<Integer> findUserIdsByBlogId(Integer blogId) {
        String sql = "SELECT user_id FROM favorites WHERE blog_id = ?";
        try {
            return queryForList(sql, rs -> {
                try {
                    return rs.getInt("user_id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, blogId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询收藏博客的用户ID列表", null);
        }
    }

    @Override
    public int countByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE user_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, userId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计用户收藏数量", 0);
        }
    }

    @Override
    public int countByBlogId(Integer blogId) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE blog_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, blogId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计博客被收藏数量", 0);
        }
    }

    @Override
    public boolean isFavorite(Integer userId, Integer blogId) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE user_id = ? AND blog_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, userId, blogId);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查用户是否收藏博客", false);
        }
    }

    @Override
    public int insert(Favorite favorite) {
        String sql = "INSERT INTO favorites (user_id, blog_id, favorite_time) VALUES (?, ?, ?)";
        try {
            return insert(sql, favorite.getUserId(), favorite.getBlogId(), favorite.getFavoriteTime());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "添加博客收藏记录", 0);
        }
    }

    @Override
    public int delete(Integer userId, Integer blogId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND blog_id = ?";
        try {
            return delete(sql, userId, blogId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "取消博客收藏记录", 0);
        }
    }

    @Override
    public int deleteByUserId(Integer userId) {
        String sql = "DELETE FROM favorites WHERE user_id = ?";
        try {
            return delete(sql, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除用户的所有收藏记录", 0);
        }
    }

    @Override
    public int deleteByBlogId(Integer blogId) {
        String sql = "DELETE FROM favorites WHERE blog_id = ?";
        try {
            return delete(sql, blogId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除博客的所有收藏记录", 0);
        }
    }
}