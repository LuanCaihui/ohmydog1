// src/main/java/com/petblog/dao/impl/VoteDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.VoteDAO;
import com.petblog.model.Vote;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class VoteDAOImpl extends BaseJdbcDAO<Vote> implements VoteDAO {

    @Override
    public Vote findById(Integer voteId) {
        // Vote使用联合主键，这里假设voteId是userId
        String sql = "SELECT user_id, blog_id, vote_create_time FROM votes WHERE user_id = ?";
        try {
            return queryForObject(sql, this::mapRowToVote, voteId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询投票记录", null);
        }
    }

    @Override
    public List<Vote> findByTarget(Integer targetType, Integer targetId) {
        String sql = "SELECT user_id, blog_id, vote_create_time FROM votes WHERE target_type = ? AND target_id = ?";
        try {
            return queryForList(sql, this::mapRowToVote, targetType, targetId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据目标类型和ID查询投票记录", null);
        }
    }

    @Override
    public List<Vote> findByUserAndType(Integer userId, Integer targetType) {
        String sql = "SELECT user_id, blog_id, vote_create_time FROM votes WHERE user_id = ? AND target_type = ?";
        try {
            return queryForList(sql, this::mapRowToVote, userId, targetType);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据用户和类型查询投票记录", null);
        }
    }

    @Override
    public int countUpVotes(Integer targetType, Integer targetId) {
        String sql = "SELECT COUNT(*) FROM votes WHERE target_type = ? AND target_id = ? AND vote_type = 1";
        try {
            Number count = (Number) queryForSingleValue(sql, targetType, targetId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计点赞数量", 0);
        }
    }

    @Override
    public int countDownVotes(Integer targetType, Integer targetId) {
        String sql = "SELECT COUNT(*) FROM votes WHERE target_type = ? AND target_id = ? AND vote_type = -1";
        try {
            Number count = (Number) queryForSingleValue(sql, targetType, targetId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计点踩数量", 0);
        }
    }

    @Override
    public int getUserVoteStatus(Integer userId, Integer targetType, Integer targetId) {
        String sql = "SELECT vote_type FROM votes WHERE user_id = ? AND target_type = ? AND target_id = ?";
        try {
            Number voteType = (Number) queryForSingleValue(sql, userId, targetType, targetId);
            return voteType != null ? voteType.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "获取用户投票状态", 0);
        }
    }

    @Override
    public int insert(Vote vote) {
        String sql = "INSERT INTO votes (user_id, blog_id, vote_create_time) VALUES (?, ?, ?)";
        try {
            return insert(sql, vote.getUserId(), vote.getBlogId(), vote.getVoteCreateTime());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "添加投票记录", 0);
        }
    }

    @Override
    public int updateVoteType(Integer voteId, Integer voteType) {
        // 假设voteId是userId
        String sql = "UPDATE votes SET vote_type = ? WHERE user_id = ?";
        try {
            return update(sql, voteType, voteId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新投票类型", 0);
        }
    }

    @Override
    public int deleteByUserAndTarget(Integer userId, Integer targetType, Integer targetId) {
        String sql = "DELETE FROM votes WHERE user_id = ? AND target_type = ? AND target_id = ?";
        try {
            return delete(sql, userId, targetType, targetId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除用户对目标的投票", 0);
        }
    }

    @Override
    public int deleteByTarget(Integer targetType, Integer targetId) {
        String sql = "DELETE FROM votes WHERE target_type = ? AND target_id = ?";
        try {
            return delete(sql, targetType, targetId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除目标的所有投票", 0);
        }
    }

    @Override
    public int deleteByUserId(Integer userId) {
        String sql = "DELETE FROM votes WHERE user_id = ?";
        try {
            return delete(sql, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除用户的所有投票", 0);
        }
    }

    private Vote mapRowToVote(ResultSet rs) throws SQLException {
        Vote vote = new Vote();
        vote.setUserId(rs.getInt("user_id"));
        vote.setBlogId(rs.getInt("blog_id"));
        vote.setVoteCreateTime(rs.getObject("vote_create_time", LocalDateTime.class));
        return vote;
    }
}