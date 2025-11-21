package com.petblog.dao;

import com.petblog.model.BlogColumn;

import java.sql.SQLException;
import java.util.List;

public interface BlogColumnDAO {
    
    void insert(BlogColumn blogColumn) throws SQLException;
    void update(BlogColumn blogColumn) throws SQLException;
    void delete(int id) throws SQLException;
    BlogColumn findById(int id) throws SQLException;
    List<BlogColumn> findByBlogId(int blogId) throws SQLException;
    List<BlogColumn> findByColumnId(int columnId) throws SQLException;
    List<BlogColumn> findAll() throws SQLException;
}