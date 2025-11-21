package com.petblog.Service;

import com.petblog.dao.RepostDAO;
import com.petblog.dao.impl.RepostDAOImpl;
import com.petblog.model.Repost;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class RepostService extends BaseService {

    private final RepostDAO repostDAO;

    public RepostService() {
        this.repostDAO = new RepostDAOImpl();
    }

    /**
     * 根据转发ID查询转发详情
     */
    public Repost getRepostById(Integer repostId) {
        try {
            return repostDAO.findById(repostId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询转发详情");
            return null;
        }
    }

    /**
     * 根据原博客ID查询所有转发记录
     */
    public List<Repost> getRepostsByOriginalBlogId(Integer originalBlogId, int pageNum, int pageSize) {
        try {
            return repostDAO.findByOriginalBlogId(originalBlogId, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据原博客ID查询转发记录");
            return null;
        }
    }

    /**
     * 根据用户ID查询其所有转发记录
     */
    public List<Repost> getRepostsByUserId(Integer userId, int pageNum, int pageSize) {
        try {
            return repostDAO.findByUserId(userId, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据用户ID查询转发记录");
            return null;
        }
    }

    /**
     * 统计原博客的转发总数
     */
    public int countRepostsByOriginalBlogId(Integer originalBlogId) {
        try {
            return repostDAO.countByOriginalBlogId(originalBlogId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计原博客转发数量");
            return 0;
        }
    }

    /**
     * 统计用户的转发总数
     */
    public int countRepostsByUserId(Integer userId) {
        try {
            return repostDAO.countByUserId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计用户转发数量");
            return 0;
        }
    }

    /**
     * 检查用户是否已转发过指定博客
     */
    public boolean hasUserReposted(Integer userId, Integer originalBlogId) {
        try {
            return repostDAO.hasReposted(userId, originalBlogId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "检查用户是否已转发");
            return false;
        }
    }

    /**
     * 新增转发记录
     */
    public Integer createRepost(Repost repost) {
        try {
            return repostDAO.insert(repost);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增转发记录");
            return 0;
        }
    }

    /**
     * 更新转发说明内容
     */
    public boolean updateRepostContent(Integer repostId, String repostContent) {
        try {
            int result = repostDAO.updateContent(repostId, repostContent);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新转发说明内容");
            return false;
        }
    }

    /**
     * 删除转发记录
     */
    public boolean deleteRepost(Integer repostId) {
        try {
            int result = repostDAO.delete(repostId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除转发记录");
            return false;
        }
    }

    /**
     * 删除用户的所有转发记录
     */
    public boolean deleteRepostsByUserId(Integer userId) {
        try {
            int result = repostDAO.deleteByUserId(userId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除用户所有转发记录");
            return false;
        }
    }

    /**
     * 移除所有针对指定博客的转发记录
     */
    public boolean deleteRepostsByOriginalBlogId(Integer originalBlogId) {
        try {
            int result = repostDAO.deleteByOriginalBlogId(originalBlogId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "移除博客所有转发记录");
            return false;
        }
    }
}
