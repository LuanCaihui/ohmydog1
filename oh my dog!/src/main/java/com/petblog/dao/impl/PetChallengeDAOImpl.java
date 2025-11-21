// src/main/java/com/petblog/dao/impl/PetChallengeDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.PetChallengeDAO;
import com.petblog.model.PetChallenge;
import com.petblog.util.JdbcUtil;
import com.petblog.util.SQLExceptionHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PetChallengeDAOImpl extends BaseJdbcDAO<Integer> implements PetChallengeDAO {

    @Override
    public List<Integer> findChallengeIdsByPetId(Integer petId) {
        String sql = "SELECT challenge_id FROM petchallenge WHERE pet_id = ? ORDER BY challenge_id DESC";
        try {
            return queryForList(sql, rs -> {
                try {
                    return rs.getInt("challenge_id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, petId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询宠物参与的挑战ID", null);
        }
    }

    @Override
    public List<Integer> findPetIdsByChallengeId(Integer challengeId, int pageNum, int pageSize) {
        String sql = "SELECT pet_id FROM petchallenge WHERE challenge_id = ? LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, rs -> {
                try {
                    return rs.getInt("pet_id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, challengeId, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "分页查询挑战参与的宠物ID", null);
        }
    }

    @Override
    public int countPetsByChallengeId(Integer challengeId) {
        String sql = "SELECT COUNT(*) FROM petchallenge WHERE challenge_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, challengeId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计挑战参与的宠物数量", 0);
        }
    }

    @Override
    public boolean hasParticipated(Integer petId, Integer challengeId) {
        String sql = "SELECT COUNT(*) FROM petchallenge WHERE pet_id = ? AND challenge_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, petId, challengeId);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查宠物是否参与挑战", false);
        }
    }

    @Override
    public int insert(PetChallenge petChallenge) {
        String sql = "INSERT INTO petchallenge (pet_id, challenge_id) VALUES (?, ?)";
        try {
            return insert(sql, petChallenge.getPetId(), petChallenge.getChallengeId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "插入宠物挑战记录", 0);
        }
    }

    @Override
    public int batchInsert(List<PetChallenge> petChallenges) {
        String sql = "INSERT INTO petchallenge (pet_id, challenge_id) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        int count = 0;

        try {
            conn = JdbcUtil.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);

            for (PetChallenge petChallenge : petChallenges) {
                pstmt.setInt(1, petChallenge.getPetId());
                pstmt.setInt(2, petChallenge.getChallengeId());
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
                SQLExceptionHandler.handleSQLException(ex, "批量插入宠物挑战记录时回滚事务");
            }
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "批量插入宠物挑战记录", 0);
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                SQLExceptionHandler.handleSQLException(e, "批量插入宠物挑战记录后恢复自动提交");
            }
            JdbcUtil.close(conn, pstmt);
        }
    }

    @Override
    public int deleteByPetId(Integer petId) {
        String sql = "DELETE FROM petchallenge WHERE pet_id = ?";
        try {
            return delete(sql, petId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除宠物的所有挑战记录", 0);
        }
    }

    @Override
    public int deleteByChallengeId(Integer challengeId) {
        String sql = "DELETE FROM petchallenge WHERE challenge_id = ?";
        try {
            return delete(sql, challengeId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除挑战的所有宠物记录", 0);
        }
    }

    @Override
    public int delete(Integer petId, Integer challengeId) {
        String sql = "DELETE FROM petchallenge WHERE pet_id = ? AND challenge_id = ?";
        try {
            return delete(sql, petId, challengeId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除宠物挑战关系", 0);
        }
    }
}