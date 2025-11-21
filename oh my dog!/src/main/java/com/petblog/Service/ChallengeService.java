package com.petblog.Service;

import com.petblog.dao.ChallengeDAO;
import com.petblog.dao.impl.ChallengeDAOImpl;
import com.petblog.model.Challenge;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ChallengeService extends BaseService {

    private ChallengeDAO challengeDAO = new ChallengeDAOImpl();

    /**
     * 根据挑战ID查询挑战详情
     */
    public Challenge getChallengeById(Integer challengeId) {
        try {
            return challengeDAO.findById(challengeId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询挑战详情");
            return null;
        }
    }

    /**
     * 查询所有进行中的挑战活动
     */
    public List<Challenge> getActiveChallenges(int pageNum, int pageSize) {
        try {
            return challengeDAO.findActiveChallenges(pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询进行中的挑战活动");
            return null;
        }
    }

    /**
     * 查询已结束的挑战活动
     */
    public List<Challenge> getCompletedChallenges(int pageNum, int pageSize) {
        try {
            return challengeDAO.findCompletedChallenges(pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询已结束的挑战活动");
            return null;
        }
    }

    /**
     * 根据挑战标题模糊搜索
     */
    public List<Challenge> searchChallengesByTitle(String keyword, Integer status, int pageNum, int pageSize) {
        try {
            return challengeDAO.searchByTitle(keyword, status, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据标题搜索挑战活动");
            return null;
        }
    }

    /**
     * 统计不同状态的挑战数量
     */
    public int countChallengesByStatus(Integer status) {
        try {
            return challengeDAO.countByStatus(status);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计挑战数量");
            return 0;
        }
    }

    /**
     * 新增挑战活动
     */
    public Integer createChallenge(Challenge challenge) {
        try {
            return challengeDAO.insert(challenge);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增挑战活动");
            return 0;
        }
    }

    /**
     * 更新挑战活动信息
     */
    public boolean updateChallenge(Challenge challenge) {
        try {
            int result = challengeDAO.update(challenge);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新挑战活动信息");
            return false;
        }
    }

    /**
     * 更新挑战状态
     */
    public boolean updateChallengeStatus(Integer challengeId, Integer status, Date endTime) {
        try {
            int result = challengeDAO.updateStatus(challengeId, status, endTime);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新挑战状态");
            return false;
        }
    }

    /**
     * 删除挑战活动
     */
    public boolean deleteChallenge(Integer challengeId) {
        try {
            int result = challengeDAO.delete(challengeId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除挑战活动");
            return false;
        }
    }

    /**
     * 查询指定时间范围内的挑战活动
     */
    public List<Challenge> getChallengesByTimeRange(Date startTime, Date endTime) {
        try {
            return challengeDAO.findByTimeRange(startTime, endTime);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询时间范围内的挑战活动");
            return null;
        }
    }
}
