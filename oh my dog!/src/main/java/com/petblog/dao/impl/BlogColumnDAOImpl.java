
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.BlogColumnDAO;
import com.petblog.model.BlogColumn;
import com.petblog.util.SQLExceptionHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BlogColumnDAOImpl extends BaseJdbcDAO<BlogColumn> implements BlogColumnDAO {

    @Override
    public void insert(BlogColumn blogColumn) {
        String sql = "INSERT INTO blogcolumn (blog_id, column_id) VALUES (?, ?)";
        try {
            insert(sql, blogColumn.getBlogId(), blogColumn.getColumnId());
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "插入博客专栏关联数据");
        }
    }

    @Override
    public void update(BlogColumn blogColumn) {
        // BlogColumn是关联表，通常不需要更新，如果需要可以先删除再插入
        delete(blogColumn.getBlogId());
        insert(blogColumn);
    }

    @Override
    public void delete(int id) {
        // 这里假设id是blog_id
        String sql = "DELETE FROM blogcolumn WHERE blog_id = ?";
        try {
            delete(sql, id);
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "删除博客专栏关联数据");
        }
    }

    @Override
    public BlogColumn findById(int id) {
        String sql = "SELECT blog_id, column_id FROM blogcolumn WHERE blog_id = ?";
        try {
            return queryForObject(sql, this::mapRowToBlogColumn, id);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询博客专栏关联数据", null);
        }
    }

    @Override
    public List<BlogColumn> findByBlogId(int blogId) {
        String sql = "SELECT blog_id, column_id FROM blogcolumn WHERE blog_id = ?";
        try {
            return queryForList(sql, this::mapRowToBlogColumn, blogId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据博客ID查询专栏关联数据", null);
        }
    }

    @Override
    public List<BlogColumn> findByColumnId(int columnId) {
        String sql = "SELECT blog_id, column_id FROM blogcolumn WHERE column_id = ?";
        try {
            return queryForList(sql, this::mapRowToBlogColumn, columnId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据专栏ID查询博客关联数据", null);
        }
    }

    @Override
    public List<BlogColumn> findAll() {
        String sql = "SELECT blog_id, column_id FROM blogcolumn";
        try {
            return queryForList(sql, this::mapRowToBlogColumn);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询所有博客专栏关联数据", null);
        }
    }

    private BlogColumn mapRowToBlogColumn(ResultSet rs) throws SQLException {
        BlogColumn blogColumn = new BlogColumn();
        blogColumn.setBlogId(rs.getInt("blog_id"));
        blogColumn.setColumnId(rs.getInt("column_id"));
        return blogColumn;
    }
}
