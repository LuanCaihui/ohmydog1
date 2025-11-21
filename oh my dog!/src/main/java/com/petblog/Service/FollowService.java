package com.petblog.Service;


import com.petblog.dao.FollowDAO;
import com.petblog.dao.impl.FollowDAOImpl;
import com.petblog.model.Follow;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class FollowService extends BaseService {

    private FollowDAO followDAO = new FollowDAOImpl();

    /**
     * 查询用户的所有关注列表（我关注了谁）
     */
    public List<Integer> getFollowingIds(Integer userId, int pageNum, int pageSize) {
        try {
            return followDAO.findFollowingIds(userId, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询用户关注列表");
            return null;
        }
    }

    /**
     * 查询用户的所有粉丝列表（谁关注了我）
     */
    public List<Integer> getFollowerIds(Integer userId, int pageNum, int pageSize) {
        try {
            return followDAO.findFollowerIds(userId, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询用户粉丝列表");
            return null;
        }
    }

    /**
     * 统计用户的关注数量（我关注了多少人）
     */
    public int countFollowing(Integer userId) {
        try {
            return followDAO.countFollowing(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计用户关注数量");
            return 0;
        }
    }

    /**
     * 统计用户的粉丝数量（多少人关注了我）
     */
    public int countFollowers(Integer userId) {
        try {
            return followDAO.countFollowers(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计用户粉丝数量");
            return 0;
        }
    }

    /**
     * 检查用户A是否关注了用户B
     */
    public boolean isUserFollowing(Integer fromUserId, Integer toUserId) {
        try {
            return followDAO.isFollowing(fromUserId, toUserId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "检查用户关注关系");
            return false;
        }
    }

    /**
     * 新增关注关系（用户A关注用户B）
     */
    public boolean followUser(Follow follow) {
        try {
            int result = followDAO.insert(follow);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增关注关系");
            return false;
        }
    }

    /**
     * 取消关注（删除用户A对用户B的关注关系）
     */
    public boolean unfollowUser(Integer fromUserId, Integer toUserId) {
        try {
            int result = followDAO.delete(fromUserId, toUserId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "取消关注关系");
            return false;
        }
    }

    /**
     * 移除用户的所有关注关系（用户A关注的所有人）
     */
    public boolean removeAllFollowing(Integer fromUserId) {
        try {
            int result = followDAO.deleteAllFollowing(fromUserId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "移除用户所有关注关系");
            return false;
        }
    }

    /**
     * 移除用户的所有粉丝关系（所有关注用户A的人）
     */
    public boolean removeAllFollowers(Integer toUserId) {
        try {
            int result = followDAO.deleteAllFollowers(toUserId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "移除用户所有粉丝关系");
            return false;
        }
    }
}
