package com.petblog.Service;

import com.petblog.dao.BlogDAO;
import com.petblog.dao.impl.BlogDAOImpl;
import com.petblog.model.Blog;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class BlogService extends BaseService {

    private BlogDAO blogDAO = new BlogDAOImpl();

    /**
     * 创建新博客
     */
    public void createBlog(Blog blog) {
        try {
            blogDAO.insert(blog);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "创建博客");
        }
    }

    /**
     * 更新博客
     */
    public void updateBlog(Blog blog) {
        try {
            blogDAO.update(blog);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新博客");
        }
    }

    /**
     * 删除博客
     */
    public boolean deleteBlog(int blogId) {
        try {
            blogDAO.delete(blogId);
            return true;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除博客");
            return false;
        }
    }

    /**
     * 根据ID查找博客
     */
    public Blog getBlogById(int id) {
        try {
            return blogDAO.findById(id);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询博客");
            return null;
        }
    }

    /**
     * 查找所有博客
     */
    public List<Blog> getAllBlogs() {
        try {
            return blogDAO.findAll();
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询所有博客");
            return null;
        }
    }

    /**
     * 根据作者ID查找博客
     */
    public List<Blog> getBlogsByAuthorId(int authorId) {
        try {
            return blogDAO.findByAuthorId(authorId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据作者ID查询博客");
            return null;
        }
    }

    /**
     * 根据话题ID查找博客
     */
    public List<Blog> getBlogsByTopicId(int topicId) {
        try {
            return blogDAO.findByTopicId(topicId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据话题ID查询博客");
            return null;
        }
    }

    /**
     * 获取最新的博客
     */
    public List<Blog> getRecentBlogs(int limit) {
        try {
            return blogDAO.findRecentBlogs(limit);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询最新博客");
            return null;
        }
    }
}
