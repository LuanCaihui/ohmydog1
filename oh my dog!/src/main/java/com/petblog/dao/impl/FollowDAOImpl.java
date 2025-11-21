package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.FollowDAO;
import com.petblog.model.Follow;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class FollowDAOImpl extends BaseJdbcDAO<Integer> implements FollowDAO {

    @Override
    public List<Integer> findFollowingIds(Integer userId, int pageNum, int pageSize) {
        String sql = "SELECT followee_id FROM follows WHERE follower_id = ? ORDER BY follow_time DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, rs -> {
                try {
                    return rs.getInt("followee_id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, userId, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询用户关注的人ID列表", null);
        }
    }

    @Override
    public List<Integer> findFollowerIds(Integer userId, int pageNum, int pageSize) {
        String sql = "SELECT follower_id FROM follows WHERE followee_id = ? ORDER BY follow_time DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, rs -> {
                try {
                    return rs.getInt("follower_id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, userId, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询用户的粉丝ID列表", null);
        }
    }

    @Override
    public int countFollowing(Integer userId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE follower_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, userId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计用户关注数量", 0);
        }
    }

    @Override
    public int countFollowers(Integer userId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE followee_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, userId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计用户粉丝数量", 0);
        }
    }

    @Override
    public boolean isFollowing(Integer fromUserId, Integer toUserId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE follower_id = ? AND followee_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, fromUserId, toUserId);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查用户是否关注他人", false);
        }
    }

    @Override
    public int insert(Follow follow) {
        String sql = "INSERT INTO follows (follower_id, followee_id, follow_time) VALUES (?, ?, ?)";
        try {
            return insert(sql, follow.getFollowerId(), follow.getFolloweeId(), follow.getFollowTime());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "添加关注关系", 0);
        }
    }

    @Override
    public int delete(Integer fromUserId, Integer toUserId) {
        String sql = "DELETE FROM follows WHERE follower_id = ? AND followee_id = ?";
        try {
            return delete(sql, fromUserId, toUserId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "取消关注关系", 0);
        }
    }

    @Override
    public int deleteAllFollowing(Integer fromUserId) {
        String sql = "DELETE FROM follows WHERE follower_id = ?";
        try {
            return delete(sql, fromUserId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除用户的所有关注关系", 0);
        }
    }

    @Override
    public int deleteAllFollowers(Integer toUserId) {
        String sql = "DELETE FROM follows WHERE followee_id = ?";
        try {
            return delete(sql, toUserId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除用户的所有粉丝关系", 0);
        }
    }
}