package com.petblog.Service;

import com.petblog.dao.VoteDAO;
import com.petblog.dao.impl.VoteDAOImpl;
import com.petblog.model.Vote;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class VoteService extends BaseService {

    private final VoteDAO voteDAO;

    public VoteService() {
        this.voteDAO = new VoteDAOImpl();
    }

    /**
     * 根据投票ID查询投票详情
     */
    public Vote getVoteById(Integer voteId) {
        try {
            return voteDAO.findById(voteId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询投票详情");
            return null;
        }
    }

    /**
     * 根据目标内容查询所有投票记录
     */
    public List<Vote> getVotesByTarget(Integer targetType, Integer targetId) {
        try {
            return voteDAO.findByTarget(targetType, targetId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据目标查询投票记录");
            return null;
        }
    }

    /**
     * 查询用户对指定类型内容的投票记录
     */
    public List<Vote> getVotesByUserAndType(Integer userId, Integer targetType) {
        try {
            return voteDAO.findByUserAndType(userId, targetType);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询用户投票记录");
            return null;
        }
    }

    /**
     * 统计指定内容的赞成票数量
     */
    public int countUpVotes(Integer targetType, Integer targetId) {
        try {
            return voteDAO.countUpVotes(targetType, targetId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计赞成票数量");
            return 0;
        }
    }

    /**
     * 统计指定内容的反对票数量
     */
    public int countDownVotes(Integer targetType, Integer targetId) {
        try {
            return voteDAO.countDownVotes(targetType, targetId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计反对票数量");
            return 0;
        }
    }

    /**
     * 检查用户对指定内容的投票状态
     */
    public int getUserVoteStatus(Integer userId, Integer targetType, Integer targetId) {
        try {
            return voteDAO.getUserVoteStatus(userId, targetType, targetId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "检查用户投票状态");
            return 0;
        }
    }

    /**
     * 新增投票记录
     */
    public Integer createVote(Vote vote) {
        try {
            return voteDAO.insert(vote);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增投票记录");
            return 0;
        }
    }

    /**
     * 更新用户的投票（从赞成改为反对或反之）
     */
    public boolean updateVoteType(Integer voteId, Integer voteType) {
        try {
            int result = voteDAO.updateVoteType(voteId, voteType);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新投票类型");
            return false;
        }
    }

    /**
     * 取消用户对指定内容的投票
     */
    public boolean cancelVote(Integer userId, Integer targetType, Integer targetId) {
        try {
            int result = voteDAO.deleteByUserAndTarget(userId, targetType, targetId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "取消投票");
            return false;
        }
    }

    /**
     * 删除指定内容的所有投票记录
     */
    public boolean deleteVotesByTarget(Integer targetType, Integer targetId) {
        try {
            int result = voteDAO.deleteByTarget(targetType, targetId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除目标投票记录");
            return false;
        }
    }

    /**
     * 删除用户的所有投票记录
     */
    public boolean deleteVotesByUserId(Integer userId) {
        try {
            int result = voteDAO.deleteByUserId(userId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除用户投票记录");
            return false;
        }
    }
}
