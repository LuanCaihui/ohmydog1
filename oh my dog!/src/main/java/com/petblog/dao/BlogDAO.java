package com.petblog.dao;

import com.petblog.model.Blog;

import java.sql.SQLException;
import java.util.List;

public interface BlogDAO {
    
    void insert(Blog blog) throws SQLException;
    void update(Blog blog) throws SQLException;
    void delete(int id) throws SQLException;
    Blog findById(int id)throws SQLException;
    List<Blog> findAll()throws SQLException;
    List<Blog> findByAuthorId(int authorId)throws SQLException;
    List<Blog> findByTopicId(int topicId)throws SQLException;
    List<Blog> findRecentBlogs(int limit)throws SQLException;
    int countAll() throws SQLException;
}