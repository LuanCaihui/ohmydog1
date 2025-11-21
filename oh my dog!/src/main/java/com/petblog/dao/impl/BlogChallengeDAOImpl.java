// src/main/java/com/petblog/dao/impl/BlogChallengeDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.BlogChallengeDAO;
import com.petblog.model.BlogChallenge;
import com.petblog.util.SQLExceptionHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BlogChallengeDAOImpl extends BaseJdbcDAO<BlogChallenge> implements BlogChallengeDAO {

    @Override
    public void insert(BlogChallenge blogChallenge) {
        String sql = "INSERT INTO blogchallenge (challenge_id, blog_id) VALUES (?, ?)";
        try {
            insert(sql, blogChallenge.getChallengeId(), blogChallenge.getBlogId());
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "插入博客挑战关联信息");
        }
    }

    @Override
    public void update(BlogChallenge blogChallenge) {
        // 关联表通常不需要更新，可以先删除再插入
        delete(blogChallenge.getChallengeId());
        insert(blogChallenge);
    }

    @Override
    public void delete(int id) {
        // 这里假设id是challenge_id
        String sql = "DELETE FROM blogchallenge WHERE challenge_id = ?";
        try {
            delete(sql, id);
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "删除博客挑战关联信息");
        }
    }

    @Override
    public BlogChallenge findById(int id) {
        String sql = "SELECT challenge_id, blog_id FROM blogchallenge WHERE challenge_id = ?";
        try {
            return queryForObject(sql, this::mapRowToBlogChallenge, id);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询博客挑战关联信息", null);
        }
    }

    @Override
    public List<BlogChallenge> findByBlogId(int blogId) {
        String sql = "SELECT challenge_id, blog_id FROM blogchallenge WHERE blog_id = ?";
        try {
            return queryForList(sql, this::mapRowToBlogChallenge, blogId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据博客ID查询挑战关联信息", null);
        }
    }

    @Override
    public List<BlogChallenge> findByChallengeId(int challengeId) {
        String sql = "SELECT challenge_id, blog_id FROM blogchallenge WHERE challenge_id = ?";
        try {
            return queryForList(sql, this::mapRowToBlogChallenge, challengeId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据挑战ID查询博客关联信息", null);
        }
    }

    @Override
    public List<BlogChallenge> findAll() {
        String sql = "SELECT challenge_id, blog_id FROM blogchallenge";
        try {
            return queryForList(sql, this::mapRowToBlogChallenge);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询所有博客挑战关联信息", null);
        }
    }

    private BlogChallenge mapRowToBlogChallenge(ResultSet rs) throws SQLException {
        BlogChallenge blogChallenge = new BlogChallenge();
        blogChallenge.setChallengeId(rs.getInt("challenge_id"));
        blogChallenge.setBlogId(rs.getInt("blog_id"));
        return blogChallenge;
    }
}
