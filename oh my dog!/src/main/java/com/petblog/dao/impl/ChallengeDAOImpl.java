
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.ChallengeDAO;
import com.petblog.model.Challenge;
import com.petblog.util.SQLExceptionHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ChallengeDAOImpl extends BaseJdbcDAO<Challenge> implements ChallengeDAO {

    @Override
    public Challenge findById(Integer challengeId) {
        String sql = "SELECT challenge_id, challenge_title, challenge_start_time, challenge_end_time, chellenge_status, challenge_is_cancell, uer_id, challenge_description FROM challenges WHERE challenge_id = ?";
        try {
            return queryForObject(sql, this::mapRowToChallenge, challengeId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询挑战信息", null);
        }
    }

    @Override
    public List<Challenge> findActiveChallenges(int pageNum, int pageSize) {
        String sql = "SELECT challenge_id, challenge_title, challenge_start_time, challenge_end_time, chellenge_status, challenge_is_cancell, uer_id, challenge_description FROM challenges WHERE chellenge_status = '进行中' ORDER BY challenge_start_time DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToChallenge, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询进行中的挑战", null);
        }
    }

    @Override
    public List<Challenge> findCompletedChallenges(int pageNum, int pageSize) {
        String sql = "SELECT challenge_id, challenge_title, challenge_start_time, challenge_end_time, chellenge_status, challenge_is_cancell, uer_id, challenge_description FROM challenges WHERE chellenge_status = '已结束' ORDER BY challenge_end_time DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToChallenge, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询已完成的挑战", null);
        }
    }

    @Override
    public List<Challenge> searchByTitle(String keyword, Integer status, int pageNum, int pageSize) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT challenge_id, challenge_title, challenge_start_time, challenge_end_time, chellenge_status, challenge_is_cancell, uer_id, challenge_description FROM challenges WHERE challenge_title LIKE ?");

        if (status != null) {
            sqlBuilder.append(" AND chellenge_status = ?");
        }

        sqlBuilder.append(" ORDER BY challenge_start_time DESC LIMIT ? OFFSET ?");

        try {
            if (status != null) {
                return queryForList(sqlBuilder.toString(), this::mapRowToChallenge,
                                   "%" + keyword + "%", status, pageSize, (pageNum - 1) * pageSize);
            } else {
                return queryForList(sqlBuilder.toString(), this::mapRowToChallenge,
                                   "%" + keyword + "%", pageSize, (pageNum - 1) * pageSize);
            }
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "搜索挑战标题", null);
        }
    }

    @Override
    public int countByStatus(Integer status) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) FROM challenges");
        if (status != null) {
            sqlBuilder.append(" WHERE chellenge_status = ?");
        }

        try {
            Number count;
            if (status != null) {
                count = (Number) queryForSingleValue(sqlBuilder.toString(), status);
            } else {
                count = (Number) queryForSingleValue(sqlBuilder.toString());
            }
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计挑战数量", 0);
        }
    }

    @Override
    public int insert(Challenge challenge) {
        String sql = "INSERT INTO challenges (challenge_title, challenge_start_time, challenge_end_time, chellenge_status, challenge_is_cancell, uer_id, challenge_description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            return insert(sql, challenge.getChallengeTitle(), challenge.getChallengeStartTime(),
                         challenge.getChallengeEndTime(), challenge.getChellengeStatus(),
                         challenge.getChallengeIsCancell(), challenge.getUserId(),
                         challenge.getChallengeDescription());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "插入挑战信息", 0);
        }
    }

    @Override
    public int update(Challenge challenge) {
        String sql = "UPDATE challenges SET challenge_title = ?, challenge_start_time = ?, challenge_end_time = ?, chellenge_status = ?, challenge_is_cancell = ?, uer_id = ?, challenge_description = ? WHERE challenge_id = ?";
        try {
            return update(sql, challenge.getChallengeTitle(), challenge.getChallengeStartTime(),
                         challenge.getChallengeEndTime(), challenge.getChellengeStatus(),
                         challenge.getChallengeIsCancell(), challenge.getUserId(),
                         challenge.getChallengeDescription(), challenge.getChallengeId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新挑战信息", 0);
        }
    }

    @Override
    public int updateStatus(Integer challengeId, Integer status, Date endTime) {
        String sql = "UPDATE challenges SET chellenge_status = ?, challenge_end_time = ? WHERE challenge_id = ?";
        try {
            return update(sql, status, endTime, challengeId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新挑战状态", 0);
        }
    }

    @Override
    public int delete(Integer challengeId) {
        String sql = "DELETE FROM challenges WHERE challenge_id = ?";
        try {
            return delete(sql, challengeId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除挑战信息", 0);
        }
    }

    @Override
    public List<Challenge> findByTimeRange(Date startTime, Date endTime) {
        String sql = "SELECT challenge_id, challenge_title, challenge_start_time, challenge_end_time, chellenge_status, challenge_is_cancell, uer_id, challenge_description FROM challenges WHERE challenge_start_time >= ? AND challenge_end_time <= ?";
        try {
            return queryForList(sql, this::mapRowToChallenge, startTime, endTime);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据时间范围查询挑战", null);
        }
    }

    private Challenge mapRowToChallenge(ResultSet rs) throws SQLException {
        Challenge challenge = new Challenge();
        challenge.setChallengeId(rs.getInt("challenge_id"));
        challenge.setChallengeTitle(rs.getString("challenge_title"));
        challenge.setChallengeStartTime(rs.getDate("challenge_start_time"));
        challenge.setChallengeEndTime(rs.getDate("challenge_end_time"));
        challenge.setChellengeStatus(rs.getString("chellenge_status"));
        challenge.setChallengeIsCancell(rs.getInt("challenge_is_cancell"));
        challenge.setUserId(rs.getInt("uer_id")); // 注意：数据库字段名是 uer_id（拼写错误）
        challenge.setChallengeDescription(rs.getString("challenge_description"));
        return challenge;
    }
}
