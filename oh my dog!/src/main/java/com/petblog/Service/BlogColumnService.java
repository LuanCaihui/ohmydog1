package com.petblog.Service;

import com.petblog.dao.BlogColumnDAO;
import com.petblog.dao.impl.BlogColumnDAOImpl;
import com.petblog.model.BlogColumn;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class BlogColumnService extends BaseService {

    private BlogColumnDAO blogColumnDAO = new BlogColumnDAOImpl();

    /**
     * 创建博客专栏关联
     */
    public boolean createBlogColumn(BlogColumn blogColumn) {
        try {
            blogColumnDAO.insert(blogColumn);
            return true;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "创建博客专栏关联");
            return false;
        }
    }

    /**
     * 更新博客专栏关联
     */
    public boolean updateBlogColumn(BlogColumn blogColumn) {
        try {
            blogColumnDAO.update(blogColumn);
            return true;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新博客专栏关联");
            return false;
        }
    }

    /**
     * 删除博客专栏关联
     */
    public boolean deleteBlogColumn(int id) {
        try {
            blogColumnDAO.delete(id);
            return true;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除博客专栏关联");
            return false;
        }
    }

    /**
     * 根据ID查找博客专栏关联
     */
    public BlogColumn getBlogColumnById(int id) {
        try {
            return blogColumnDAO.findById(id);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询博客专栏关联");
            return null;
        }
    }

    /**
     * 根据博客ID查找所有专栏关联
     */
    public List<BlogColumn> getBlogColumnsByBlogId(int blogId) {
        try {
            return blogColumnDAO.findByBlogId(blogId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据博客ID查询专栏关联");
            return null;
        }
    }

    /**
     * 根据专栏ID查找所有博客关联
     */
    public List<BlogColumn> getBlogColumnsByColumnId(int columnId) {
        try {
            return blogColumnDAO.findByColumnId(columnId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据专栏ID查询博客关联");
            return null;
        }
    }

    /**
     * 查找所有博客专栏关联
     */
    public List<BlogColumn> getAllBlogColumns() {
        try {
            return blogColumnDAO.findAll();
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询所有博客专栏关联");
            return null;
        }
    }
}
