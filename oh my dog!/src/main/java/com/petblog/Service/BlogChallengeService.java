package com.petblog.Service;

import com.petblog.dao.BlogChallengeDAO;
import com.petblog.dao.impl.BlogChallengeDAOImpl;
import com.petblog.model.BlogChallenge;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class BlogChallengeService extends BaseService {

    private BlogChallengeDAO blogChallengeDAO = new BlogChallengeDAOImpl();

    /**
     * 创建博客挑战关联
     */
    public boolean createBlogChallenge(BlogChallenge blogChallenge) {
        try {
            blogChallengeDAO.insert(blogChallenge);
            return true;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "创建博客挑战关联");
            return false;
        }
    }

    /**
     * 更新博客挑战关联
     */
    public boolean updateBlogChallenge(BlogChallenge blogChallenge) {
        try {
            blogChallengeDAO.update(blogChallenge);
            return true;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新博客挑战关联");
            return false;
        }
    }

    /**
     * 删除博客挑战关联
     */
    public boolean deleteBlogChallenge(int id) {
        try {
            blogChallengeDAO.delete(id);
            return true;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除博客挑战关联");
            return false;
        }
    }

    /**
     * 根据ID查找博客挑战关联
     */
    public BlogChallenge getBlogChallengeById(int id) {
        try {
            return blogChallengeDAO.findById(id);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询博客挑战关联");
            return null;
        }
    }

    /**
     * 根据博客ID查找所有挑战关联
     */
    public List<BlogChallenge> getBlogChallengesByBlogId(int blogId) {
        try {
            return blogChallengeDAO.findByBlogId(blogId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据博客ID查询挑战关联");
            return null;
        }
    }

    /**
     * 根据挑战ID查找所有博客关联
     */
    public List<BlogChallenge> getBlogChallengesByChallengeId(int challengeId) {
        try {
            return blogChallengeDAO.findByChallengeId(challengeId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据挑战ID查询博客关联");
            return null;
        }
    }

    /**
     * 查找所有博客挑战关联
     */
    public List<BlogChallenge> getAllBlogChallenges() {
        try {
            return blogChallengeDAO.findAll();
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询所有博客挑战关联");
            return null;
        }
    }
}
