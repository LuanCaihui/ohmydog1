package com.petblog.dao;

import com.petblog.model.BlogChallenge;

import java.sql.SQLException;
import java.util.List;

public interface BlogChallengeDAO {
    
    void insert(BlogChallenge blogChallenge) throws SQLException;
    void update(BlogChallenge blogChallenge) throws SQLException;
    void delete(int id) throws SQLException;
    BlogChallenge findById(int id) throws SQLException;
    List<BlogChallenge> findByBlogId(int blogId) throws SQLException;
    List<BlogChallenge> findByChallengeId(int challengeId) throws SQLException;
    List<BlogChallenge> findAll() throws SQLException;
}