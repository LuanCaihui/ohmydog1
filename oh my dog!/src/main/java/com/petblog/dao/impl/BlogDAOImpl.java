// src/main/java/com/petblog/dao/impl/BlogDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.BlogDAO;
import com.petblog.model.Blog;
import com.petblog.util.SQLExceptionHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BlogDAOImpl extends BaseJdbcDAO<Blog> implements BlogDAO {

    @Override
    public void insert(Blog blog) {
        String sql = "INSERT INTO blogs (user_id, blog_title, blog_content, blog_update_time, blog_create_time, is_shielded) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            insert(sql, blog.getUserId(), blog.getBlogTitle(), blog.getBlogContent(),
                  blog.getBlogUpdateTime(), blog.getBlogCreateTime(), blog.getIsShielded());
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "插入博客数据");
        }
    }

    @Override
    public void update(Blog blog) {
        String sql = "UPDATE blogs SET user_id = ?, blog_title = ?, blog_content = ?, blog_update_time = ?, is_shielded = ? WHERE blog_id = ?";
        try {
            update(sql, blog.getUserId(), blog.getBlogTitle(), blog.getBlogContent(),
                  blog.getBlogUpdateTime(), blog.getIsShielded(), blog.getBlogId());
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "更新博客数据");
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM blogs WHERE blog_id = ?";
        try {
            delete(sql, id);
        } catch (SQLException e) {
            throw SQLExceptionHandler.handleSQLException(e, "删除博客数据");
        }
    }

    @Override
    public Blog findById(int id) {
        String sql = "SELECT blog_id, user_id, blog_title, blog_content, blog_update_time, blog_create_time, is_shielded FROM blogs WHERE blog_id = ?";
        try {
            return queryForObject(sql, this::mapRowToBlog, id);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询博客数据", null);
        }
    }

    @Override
    public List<Blog> findAll() {
        String sql = "SELECT b.blog_id, b.user_id, b.blog_title, b.blog_content, b.blog_update_time, b.blog_create_time, b.is_shielded, " +
                     "u.user_name, u.user_avatar_path, " +
                     "COALESCE((SELECT COUNT(*) FROM likes WHERE likes.blog_id = b.blog_id), 0) as like_count, " +
                     "COALESCE((SELECT COUNT(*) FROM favorites WHERE favorites.blog_id = b.blog_id), 0) as favorite_count, " +
                     "COALESCE((SELECT COUNT(*) FROM replies WHERE replies.blog_id = b.blog_id AND replies.parent_reply IS NULL), 0) as comment_count, " +
                     "COALESCE((SELECT COUNT(*) FROM reposts WHERE reposts.blog_id = b.blog_id), 0) as repost_count " +
                     "FROM blogs b " +
                     "LEFT JOIN users u ON b.user_id = u.user_id " +
                     "ORDER BY b.blog_create_time DESC";
        try {
            List<Blog> blogs = queryForList(sql, this::mapRowToBlogWithStats);
            System.out.println("查询博客成功，返回 " + (blogs != null ? blogs.size() : 0) + " 条记录");
            if (blogs != null && !blogs.isEmpty()) {
                Blog firstBlog = blogs.get(0);
                System.out.println("第一条博客统计: likeCount=" + firstBlog.getLikeCount() + 
                                 ", favoriteCount=" + firstBlog.getFavoriteCount() + 
                                 ", commentCount=" + firstBlog.getCommentCount() + 
                                 ", repostCount=" + firstBlog.getRepostCount());
            }
            return blogs;
        } catch (SQLException e) {
            System.err.println("查询博客统计失败: " + e.getMessage());
            e.printStackTrace();
            SQLExceptionHandler.handleSQLException(e, "查询所有博客数据");
            // 如果统计查询失败，尝试返回基础数据
            try {
                String basicSql = "SELECT b.blog_id, b.user_id, b.blog_title, b.blog_content, b.blog_update_time, b.blog_create_time, b.is_shielded, " +
                                 "u.user_name, u.user_avatar_path " +
                                 "FROM blogs b " +
                                 "LEFT JOIN users u ON b.user_id = u.user_id " +
                                 "ORDER BY b.blog_create_time DESC";
                List<Blog> blogs = queryForList(basicSql, this::mapRowToBlogWithUser);
                // 为每个博客设置默认统计值
                if (blogs != null) {
                    for (Blog blog : blogs) {
                        blog.setLikeCount(0);
                        blog.setFavoriteCount(0);
                        blog.setCommentCount(0);
                        blog.setRepostCount(0);
                    }
                }
                return blogs;
            } catch (SQLException e2) {
                System.err.println("基础查询也失败: " + e2.getMessage());
                e2.printStackTrace();
                return SQLExceptionHandler.handleSQLExceptionWithDefault(e2, "查询所有博客数据（基础查询）", null);
            }
        }
    }

    @Override
    public List<Blog> findByAuthorId(int authorId) {
        String sql = "SELECT blog_id, user_id, blog_title, blog_content, blog_update_time, blog_create_time, is_shielded FROM blogs WHERE user_id = ?";
        try {
            return queryForList(sql, this::mapRowToBlog, authorId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据作者ID查询博客数据", null);
        }
    }

    @Override
    public List<Blog> findByTopicId(int topicId) {
        String sql = "SELECT b.blog_id, b.user_id, b.blog_title, b.blog_content, b.blog_update_time, b.blog_create_time, b.is_shielded " +
                     "FROM blogs b " +
                     "JOIN blogtopic bt ON b.blog_id = bt.blog_id " +
                     "WHERE bt.topic_id = ?";
        try {
            return queryForList(sql, this::mapRowToBlog, topicId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据主题ID查询博客数据", null);
        }
    }

    @Override
    public List<Blog> findRecentBlogs(int limit) {
        String sql = "SELECT blog_id, user_id, blog_title, blog_content, blog_update_time, blog_create_time, is_shielded FROM blogs ORDER BY blog_create_time DESC LIMIT ?";
        try {
            return queryForList(sql, this::mapRowToBlog, limit);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询最新博客数据", null);
        }
    }

    /**
     * 将ResultSet映射为Blog对象（基础字段）
     */
    private Blog mapRowToBlog(ResultSet rs) throws SQLException {
        Blog blog = new Blog();
        blog.setBlogId(rs.getInt("blog_id"));
        blog.setUserId(rs.getInt("user_id"));
        blog.setBlogTitle(rs.getString("blog_title"));
        blog.setBlogContent(rs.getString("blog_content"));
        blog.setBlogUpdateTime(rs.getDate("blog_update_time"));
        blog.setBlogCreateTime(rs.getDate("blog_create_time"));
        blog.setIsShielded(rs.getInt("is_shielded"));
        return blog;
    }
    
    /**
     * 将ResultSet映射为Blog对象（包含用户信息）
     */
    private Blog mapRowToBlogWithUser(ResultSet rs) throws SQLException {
        Blog blog = mapRowToBlog(rs);
        // 尝试获取用户信息
        try {
            blog.setUserName(rs.getString("user_name"));
        } catch (SQLException e) {
            blog.setUserName(null);
        }
        try {
            blog.setUserAvatarPath(rs.getString("user_avatar_path"));
        } catch (SQLException e) {
            blog.setUserAvatarPath(null);
        }
        return blog;
    }
    
    /**
     * 将ResultSet映射为Blog对象（包含统计字段和用户信息）
     */
    private Blog mapRowToBlogWithStats(ResultSet rs) throws SQLException {
        Blog blog = mapRowToBlogWithUser(rs);
        // 获取统计字段，使用COALESCE确保不为null
        try {
            int likeCount = rs.getInt("like_count");
            blog.setLikeCount(rs.wasNull() ? 0 : likeCount);
        } catch (SQLException e) {
            System.err.println("获取like_count失败: " + e.getMessage());
            blog.setLikeCount(0);
        }
        
        try {
            int favoriteCount = rs.getInt("favorite_count");
            blog.setFavoriteCount(rs.wasNull() ? 0 : favoriteCount);
        } catch (SQLException e) {
            System.err.println("获取favorite_count失败: " + e.getMessage());
            blog.setFavoriteCount(0);
        }
        
        try {
            int commentCount = rs.getInt("comment_count");
            blog.setCommentCount(rs.wasNull() ? 0 : commentCount);
        } catch (SQLException e) {
            System.err.println("获取comment_count失败: " + e.getMessage());
            blog.setCommentCount(0);
        }
        
        try {
            int repostCount = rs.getInt("repost_count");
            blog.setRepostCount(rs.wasNull() ? 0 : repostCount);
        } catch (SQLException e) {
            System.err.println("获取repost_count失败: " + e.getMessage());
            blog.setRepostCount(0);
        }
        
        return blog;
    }
}

