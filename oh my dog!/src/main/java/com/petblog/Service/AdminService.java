package com.petblog.Service;

import com.petblog.dao.*;
import com.petblog.dao.impl.*;
import com.petblog.model.*;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import static com.petblog.util.JdbcUtil.getConnection;

/**
 * 管理员服务类
 * 提供管理员后台所需的所有业务逻辑
 */
public class AdminService extends BaseService {
    
    private UserDAO userDAO = new UserDAOImpl();
    private BlogDAO blogDAO = new BlogDAOImpl();
    private ReplyDAO replyDAO = new ReplyDAOImpl();
    private ConsultationDAO consultationDAO = new ConsultationDAOImpl();
    private LikeDAO likeDAO;
    private FavoriteDAO favoriteDAO = new FavoriteDAOImpl();
    private LikeService likeService = new LikeService();
    
    public AdminService() {
        try {
            this.likeDAO = new LikeDAOImpl(getConnection());
        } catch (SQLException e) {
            handleException(e, "初始化LikeDAO失败");
        }
    }

    // ==================== 用户管理 ====================
    
    /**
     * 获取所有用户列表（分页）
     */
    public Map<String, Object> getAllUsers(int pageNum, int pageSize) {
        return getAllUsersWithLevel(pageNum, pageSize);
    }
    
    /**
     * 搜索用户
     */
    public Map<String, Object> searchUsers(String keyword, int pageNum, int pageSize) {
        return searchUsersWithLevel(keyword, pageNum, pageSize);
    }
    
    /**
     * 更新用户等级（使用is_ban字段的扩展：0=普通用户，1=已封禁，2=活跃用户，3=VIP用户）
     * 注意：这里使用is_ban字段扩展，实际项目中应该添加user_level字段
     */
    public boolean updateUserLevel(Integer userId, Integer level) {
        try {
            // 这里使用updateStatus方法，但实际应该添加updateUserLevel方法
            // 暂时使用is_ban字段，0=正常，1=封禁，2=活跃，3=VIP
            // 注意：这只是一个临时方案，实际应该添加user_level字段
            return userDAO.updateStatus(userId, level) > 0;
        } catch (Exception e) {
            handleException(new SQLException(e), "更新用户等级");
            return false;
        }
    }
    
    /**
     * 获取用户历史记录（博客、问诊、点赞、评论、举报等）
     */
    public Map<String, Object> getUserHistory(Integer userId) {
        try {
            Map<String, Object> history = new HashMap<>();
            
            // 用户基本信息
            User user = userDAO.selectById(userId);
            if (user == null) {
                history.put("error", "用户不存在");
                return history;
            }
            history.put("user", user);
            
            // 用户发布的博客（包括已封禁的）
            try {
                List<Blog> blogs = blogDAO.findByAuthorId(userId);
                history.put("blogs", blogs != null ? blogs : new ArrayList<>());
            } catch (Exception e) {
                history.put("blogs", new ArrayList<>());
            }
            
            // 用户参与的问诊
            try {
                List<Consultation> consultations = consultationDAO.findByUserId(userId);
                history.put("consultations", consultations != null ? consultations : new ArrayList<>());
            } catch (Exception e) {
                history.put("consultations", new ArrayList<>());
            }
            
            // 用户的评论
            try {
                List<Reply> replies = replyDAO.findByUserId(userId, 1, 100);
                history.put("replies", replies != null ? replies : new ArrayList<>());
            } catch (Exception e) {
                history.put("replies", new ArrayList<>());
            }
            
            // 用户的举报记录
            // 注意：ReportDAO需要添加根据userId查询的方法
            // 这里暂时返回空列表
            history.put("reports", new ArrayList<>());
            
            // 用户等级信息
            try {
                UserXpService xpService = new UserXpService();
                Map<String, Object> levelInfo = xpService.getUserLevelInfo(userId);
                history.put("levelInfo", levelInfo);
            } catch (Exception e) {
                // 忽略等级信息错误
            }
            
            return history;
        } catch (SQLException e) {
            return handleException(e, "获取用户历史记录", new HashMap<>());
        }
    }
    
    /**
     * 获取用户列表（包含等级信息）
     */
    public Map<String, Object> getAllUsersWithLevel(int pageNum, int pageSize) {
        try {
            List<User> users = userDAO.selectAll(pageNum, pageSize);
            int total = userDAO.countTotalUsers();
            
            // 为每个用户添加等级信息
            UserXpService xpService = new UserXpService();
            List<Map<String, Object>> usersWithLevel = new ArrayList<>();
            
            for (User user : users) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userId", user.getUserId());
                userMap.put("userName", user.getUserName());
                userMap.put("email", user.getEmail());
                userMap.put("registrationDate", user.getRegistrationDate());
                userMap.put("lastLogin", user.getLastLogin());
                userMap.put("isBan", user.getIsBan());
                userMap.put("userAvatarPath", user.getUserAvatarPath());
                
                // 添加等级信息
                Map<String, Object> levelInfo = xpService.getUserLevelInfo(user.getUserId());
                userMap.putAll(levelInfo);
                
                usersWithLevel.add(userMap);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("users", usersWithLevel);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            return result;
        } catch (SQLException e) {
            return handleException(e, "获取用户列表", new HashMap<>());
        }
    }
    
    /**
     * 搜索用户（包含等级信息）
     */
    public Map<String, Object> searchUsersWithLevel(String keyword, int pageNum, int pageSize) {
        try {
            List<User> users = userDAO.searchUsers(keyword, pageNum, pageSize);
            int total = userDAO.countTotalUsers(); // 简化处理，实际应该统计搜索结果
            
            // 为每个用户添加等级信息
            UserXpService xpService = new UserXpService();
            List<Map<String, Object>> usersWithLevel = new ArrayList<>();
            
            for (User user : users) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userId", user.getUserId());
                userMap.put("userName", user.getUserName());
                userMap.put("email", user.getEmail());
                userMap.put("registrationDate", user.getRegistrationDate());
                userMap.put("lastLogin", user.getLastLogin());
                userMap.put("isBan", user.getIsBan());
                userMap.put("userAvatarPath", user.getUserAvatarPath());
                
                // 添加等级信息
                Map<String, Object> levelInfo = xpService.getUserLevelInfo(user.getUserId());
                userMap.putAll(levelInfo);
                
                usersWithLevel.add(userMap);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("users", usersWithLevel);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            return result;
        } catch (SQLException e) {
            return handleException(e, "搜索用户", new HashMap<>());
        }
    }
    
    // ==================== 博客管理 ====================
    
    /**
     * 获取所有博客列表（分页，包含统计信息，包括已封禁的博客）
     */
    public Map<String, Object> getAllBlogs(int pageNum, int pageSize) {
        try {
            // 查询所有博客（包括已封禁的），用于管理员后台
            String sql = "SELECT b.blog_id, b.user_id, b.blog_title, b.blog_content, b.blog_update_time, b.blog_create_time, b.is_shielded, " +
                        "u.user_name, u.user_avatar_path " +
                        "FROM blogs b " +
                        "LEFT JOIN users u ON b.user_id = u.user_id " +
                        "ORDER BY b.blog_create_time DESC";
            
            // 直接使用JdbcUtil执行查询
            List<Blog> allBlogs = new ArrayList<>();
            java.sql.Connection conn = null;
            java.sql.PreparedStatement pstmt = null;
            java.sql.ResultSet rs = null;
            
            try {
                conn = com.petblog.util.JdbcUtil.getConnection();
                pstmt = conn.prepareStatement(sql);
                rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Blog blog = new Blog();
                    blog.setBlogId(rs.getInt("blog_id"));
                    blog.setUserId(rs.getInt("user_id"));
                    blog.setBlogTitle(rs.getString("blog_title"));
                    blog.setBlogContent(rs.getString("blog_content"));
                    blog.setBlogUpdateTime(rs.getDate("blog_update_time"));
                    blog.setBlogCreateTime(rs.getDate("blog_create_time"));
                    blog.setIsShielded(rs.getInt("is_shielded"));
                    blog.setUserName(rs.getString("user_name"));
                    blog.setUserAvatarPath(rs.getString("user_avatar_path"));
                    allBlogs.add(blog);
                }
            } catch (SQLException e) {
                handleException(e, "查询所有博客（管理员）");
            } finally {
                com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
            }
            
            // 分页处理
            int total = allBlogs.size();
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<Blog> pagedBlogs = start < total ? allBlogs.subList(start, end) : new ArrayList<>();
            
            // 为每个博客添加统计信息
            for (Blog blog : pagedBlogs) {
                try {
                    // 获取点赞数
                    int likeCount = likeService.countBlogLikes(blog.getBlogId());
                    blog.setLikeCount(likeCount);
                    
                    // 获取收藏数
                    int favoriteCount = favoriteDAO.countByBlogId(blog.getBlogId());
                    blog.setFavoriteCount(favoriteCount);
                    
                    // 获取评论数
                    List<Reply> replies = replyDAO.findByBlogId(blog.getBlogId());
                    blog.setCommentCount(replies != null ? replies.size() : 0);
                } catch (Exception e) {
                    // 忽略统计错误
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("blogs", pagedBlogs);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            return result;
        } catch (Exception e) {
            return handleException(new SQLException(e), "获取博客列表", new HashMap<>());
        }
    }
    
    /**
     * 根据ID获取博客详情（包括已封禁的）
     */
    public Blog getBlogById(Integer blogId) {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            // 直接查询数据库，不经过DAO的过滤
            String sql = "SELECT b.blog_id, b.user_id, b.blog_title, b.blog_content, b.blog_update_time, b.blog_create_time, b.is_shielded, " +
                        "u.user_name, u.user_avatar_path " +
                        "FROM blogs b " +
                        "LEFT JOIN users u ON b.user_id = u.user_id " +
                        "WHERE b.blog_id = ?";
            
            conn = com.petblog.util.JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, blogId);
            rs = pstmt.executeQuery();
            
            Blog blog = null;
            if (rs.next()) {
                blog = new Blog();
                blog.setBlogId(rs.getInt("blog_id"));
                blog.setUserId(rs.getInt("user_id"));
                blog.setBlogTitle(rs.getString("blog_title"));
                blog.setBlogContent(rs.getString("blog_content"));
                blog.setBlogUpdateTime(rs.getDate("blog_update_time"));
                blog.setBlogCreateTime(rs.getDate("blog_create_time"));
                blog.setIsShielded(rs.getInt("is_shielded"));
                blog.setUserName(rs.getString("user_name"));
                blog.setUserAvatarPath(rs.getString("user_avatar_path"));
                
                // 添加统计信息
                try {
                    int likeCount = likeService.countBlogLikes(blog.getBlogId());
                    blog.setLikeCount(likeCount);
                    
                    int favoriteCount = favoriteDAO.countByBlogId(blog.getBlogId());
                    blog.setFavoriteCount(favoriteCount);
                    
                    List<Reply> replies = replyDAO.findByBlogId(blog.getBlogId());
                    blog.setCommentCount(replies != null ? replies.size() : 0);
                } catch (Exception e) {
                    // 忽略统计错误
                }
            }
            
            return blog;
        } catch (SQLException e) {
            handleException(e, "获取博客详情");
            return null;
        } finally {
            // 确保连接总是被关闭
            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
        }
    }
    
    /**
     * 获取博客统计信息
     */
    public Map<String, Object> getBlogStats() {
        try {
            List<Blog> allBlogs = blogDAO.findAll();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBlogs", allBlogs.size());
            
            // 统计总阅读量、点赞数、评论数等
            int totalLikes = 0;
            int totalFavorites = 0;
            int totalComments = 0;
            
            for (Blog blog : allBlogs) {
                try {
                    totalLikes += likeService.countBlogLikes(blog.getBlogId());
                    totalFavorites += favoriteDAO.countByBlogId(blog.getBlogId());
                    List<Reply> replies = replyDAO.findByBlogId(blog.getBlogId());
                    totalComments += replies != null ? replies.size() : 0;
                } catch (Exception e) {
                    // 忽略单个博客的统计错误
                }
            }
            
            stats.put("totalLikes", totalLikes);
            stats.put("totalFavorites", totalFavorites);
            stats.put("totalComments", totalComments);
            
            return stats;
        } catch (SQLException e) {
            return handleException(e, "获取博客统计", new HashMap<>());
        }
    }
    
    /**
     * 封禁/解封博客
     */
    public boolean updateBlogStatus(Integer blogId, Integer status) {
        try {
            // 直接更新数据库，不先查询（因为findById可能因为博客被封禁而返回null）
            String sql = "UPDATE blogs SET is_shielded = ? WHERE blog_id = ?";
            java.sql.Connection conn = com.petblog.util.JdbcUtil.getConnection();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            pstmt.setInt(2, blogId);
            int rowsAffected = pstmt.executeUpdate();
            com.petblog.util.JdbcUtil.close(conn, pstmt, null);
            return rowsAffected > 0;
        } catch (SQLException e) {
            handleException(e, "更新博客状态");
            return false;
        }
    }
    
    // ==================== 评论管理 ====================
    
    /**
     * 获取所有评论（分页，包括已删除的）
     */
    public Map<String, Object> getAllReplies(int pageNum, int pageSize) {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            // 直接查询所有评论（包括已删除的），用于管理员后台
            String sql = "SELECT r.user_id, r.blog_id, r.reply_id, r.parentReply, r.reply_createdtime, r.reply_content, r.is_visible " +
                        "FROM replies r " +
                        "ORDER BY r.reply_createdtime DESC";
            
            conn = com.petblog.util.JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            List<Reply> allReplies = new ArrayList<>();
            while (rs.next()) {
                Reply reply = new Reply();
                reply.setUserId(rs.getInt("user_id"));
                reply.setBlogId(rs.getInt("blog_id"));
                reply.setReplyId(rs.getInt("reply_id"));
                reply.setParentReply(rs.getObject("parentReply") != null ? rs.getInt("parentReply") : null);
                reply.setReplyCreatedtime(rs.getDate("reply_createdtime"));
                reply.setReplyContent(rs.getString("reply_content"));
                reply.setIsVisible(rs.getInt("is_visible"));
                allReplies.add(reply);
            }
            
            // 分页处理
            int total = allReplies.size();
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<Reply> pagedReplies = start < total ? allReplies.subList(start, end) : new ArrayList<>();
            
            Map<String, Object> result = new HashMap<>();
            result.put("replies", pagedReplies);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            return result;
        } catch (SQLException e) {
            return handleException(e, "获取评论列表", new HashMap<>());
        } finally {
            // 确保连接总是被关闭
            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
        }
    }
    
    /**
     * 删除/禁用评论
     */
    public boolean updateReplyStatus(Integer replyId, Integer status) {
        try {
            return replyDAO.updateStatus(replyId, status) > 0;
        } catch (SQLException e) {
            handleException(e, "更新评论状态");
            return false;
        }
    }
    
    // ==================== 举报管理 ====================
    
    /**
     * 获取举报统计
     */
    public Map<String, Object> getReportStats() {
        try {
            // 使用ReportService的方法
            ReportService reportService = new ReportService();
            Map<String, Integer> stats = reportService.getReportStats();
            // 转换为 Map<String, Object>
            Map<String, Object> result = new HashMap<>();
            if (stats != null) {
                for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        } catch (Exception e) {
            return handleException(new SQLException(e), "获取举报统计", new HashMap<>());
        }
    }
    
    // ==================== 问诊管理 ====================
    
    /**
     * 获取所有问诊记录（分页）
     */
    public Map<String, Object> getAllConsultations(int pageNum, int pageSize) {
        try {
            List<Consultation> consultations = consultationDAO.findAll(pageNum, pageSize);
            int total = consultationDAO.countAll();
            
            Map<String, Object> result = new HashMap<>();
            result.put("consultations", consultations);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            return result;
        } catch (SQLException e) {
            return handleException(e, "获取问诊记录", new HashMap<>());
        }
    }
    
    /**
     * 获取问诊统计信息
     */
    public Map<String, Object> getConsultationStats() {
        try {
            List<Consultation> allConsultations = consultationDAO.findAll(1, 10000);
            int total = consultationDAO.countAll();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalConsultations", total);
            
            // 统计最常见的疾病
            Map<Integer, Integer> diseaseCount = new HashMap<>();
            for (Consultation consultation : allConsultations) {
                if (consultation.getResultDiseaseId() != null) {
                    diseaseCount.put(consultation.getResultDiseaseId(),
                        diseaseCount.getOrDefault(consultation.getResultDiseaseId(), 0) + 1);
                }
            }
            
            stats.put("diseaseDistribution", diseaseCount);
            
            return stats;
        } catch (SQLException e) {
            return handleException(e, "获取问诊统计", new HashMap<>());
        }
    }
    
    // ==================== 投票管理 ====================
    
    /**
     * 获取投票统计信息
     */
    public Map<String, Object> getVoteStats() {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            // 直接查询votes表，按blog_id统计投票数
            String sql = "SELECT blog_id, COUNT(*) as vote_count " +
                        "FROM votes " +
                        "GROUP BY blog_id " +
                        "ORDER BY vote_count DESC";
            
            conn = com.petblog.util.JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            List<Map<String, Object>> blogVotes = new ArrayList<>();
            int totalVotes = 0;
            
            while (rs.next()) {
                Integer blogId = rs.getInt("blog_id");
                Integer voteCount = rs.getInt("vote_count");
                totalVotes += voteCount;
                
                try {
                    // 获取博客信息
                    Blog blog = blogDAO.findById(blogId);
                    if (blog != null) {
                        Map<String, Object> blogVote = new HashMap<>();
                        blogVote.put("blogId", blogId);
                        blogVote.put("blogTitle", blog.getBlogTitle());
                        blogVote.put("voteCount", voteCount);
                        blogVote.put("userName", blog.getUserName());
                        blogVotes.add(blogVote);
                    }
                } catch (Exception e) {
                    // 如果博客不存在或已删除，仍然记录投票数
                    Map<String, Object> blogVote = new HashMap<>();
                    blogVote.put("blogId", blogId);
                    blogVote.put("blogTitle", "博客已删除");
                    blogVote.put("voteCount", voteCount);
                    blogVote.put("userName", "未知");
                    blogVotes.add(blogVote);
                }
            }
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("blogVotes", blogVotes);
            stats.put("totalVotes", totalVotes);
            stats.put("totalBlogs", blogVotes.size());
            
            return stats;
        } catch (SQLException e) {
            return handleException(e, "获取投票统计", new HashMap<>());
        } finally {
            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
        }
    }
    
    /**
     * 获取获胜狗狗（投票数最多的博客）
     */
    public List<Map<String, Object>> getWinningDogs(int limit) {
        try {
            Map<String, Object> voteStats = getVoteStats();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> blogVotes = (List<Map<String, Object>>) voteStats.get("blogVotes");
            
            if (blogVotes != null && !blogVotes.isEmpty()) {
                return blogVotes.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            handleException(new SQLException(e), "获取获胜狗狗");
            return new ArrayList<>();
        }
    }
    
    // ==================== 内容推荐系统管理 ====================
    
    /**
     * 获取推荐博客列表（按不同规则排序）
     */
    public Map<String, Object> getRecommendedBlogs(String rule, int limit) {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            String sql;
            String baseSelect = "SELECT b.blog_id, b.user_id, b.blog_title, b.blog_content, b.blog_create_time, " +
                               "b.is_shielded, u.user_name, u.user_avatar_path, " +
                               "COALESCE((SELECT COUNT(*) FROM likes WHERE likes.blog_id = b.blog_id), 0) as like_count, " +
                               "COALESCE((SELECT COUNT(*) FROM favorites WHERE favorites.blog_id = b.blog_id), 0) as favorite_count, " +
                               "COALESCE((SELECT COUNT(*) FROM replies WHERE replies.blog_id = b.blog_id AND replies.parentReply IS NULL), 0) as comment_count, " +
                               "COALESCE((SELECT COUNT(*) FROM reposts WHERE reposts.blog_Id = b.blog_id), 0) as repost_count ";
            String baseFrom = "FROM blogs b LEFT JOIN users u ON b.user_id = u.user_id WHERE b.is_shielded = 0 ";
            
            switch (rule) {
                case "likes":
                    sql = baseSelect + baseFrom + 
                          "ORDER BY like_count DESC LIMIT ?";
                    break;
                case "comments":
                    sql = baseSelect + baseFrom + 
                          "ORDER BY comment_count DESC LIMIT ?";
                    break;
                case "recent":
                    sql = baseSelect + baseFrom + 
                          "ORDER BY b.blog_create_time DESC LIMIT ?";
                    break;
                case "weight":
                default:
                    // 综合权重：点赞*2 + 评论*3 + 收藏*2 + 转发*1
                    sql = baseSelect + ", " +
                          "(COALESCE((SELECT COUNT(*) FROM likes WHERE likes.blog_id = b.blog_id), 0) * 2 + " +
                          "COALESCE((SELECT COUNT(*) FROM replies WHERE replies.blog_id = b.blog_id AND replies.parentReply IS NULL), 0) * 3 + " +
                          "COALESCE((SELECT COUNT(*) FROM favorites WHERE favorites.blog_id = b.blog_id), 0) * 2 + " +
                          "COALESCE((SELECT COUNT(*) FROM reposts WHERE reposts.blog_Id = b.blog_id), 0) * 1) as weight " +
                          baseFrom + 
                          "ORDER BY weight DESC LIMIT ?";
                    break;
            }
            
            conn = com.petblog.util.JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();
            
            List<Map<String, Object>> blogs = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> blog = new HashMap<>();
                blog.put("blogId", rs.getInt("blog_id"));
                blog.put("userId", rs.getInt("user_id"));
                blog.put("blogTitle", rs.getString("blog_title"));
                blog.put("blogContent", rs.getString("blog_content"));
                blog.put("blogCreateTime", rs.getTimestamp("blog_create_time"));
                blog.put("isShielded", rs.getInt("is_shielded"));
                blog.put("likeCount", rs.getInt("like_count"));
                blog.put("favoriteCount", rs.getInt("favorite_count"));
                blog.put("commentCount", rs.getInt("comment_count"));
                blog.put("repostCount", rs.getInt("repost_count"));
                blog.put("userName", rs.getString("user_name"));
                blog.put("userAvatarPath", rs.getString("user_avatar_path"));
                if (rule.equals("weight")) {
                    blog.put("weight", rs.getInt("weight"));
                }
                blogs.add(blog);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("blogs", blogs);
            result.put("rule", rule);
            result.put("total", blogs.size());
            return result;
        } catch (SQLException e) {
            return handleException(e, "获取推荐博客", new HashMap<>());
        } finally {
            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
        }
    }
    
    /**
     * 获取热门话题
     */
    public List<Map<String, Object>> getHotTopics(int limit) {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            // 统计每个话题下的博客数量（通过blogtopic中间表）
            String sql = "SELECT t.topic_id, t.topic_name, COUNT(bt.blog_id) as blog_count " +
                        "FROM topics t LEFT JOIN blogtopic bt ON t.topic_id = bt.topic_id " +
                        "LEFT JOIN blogs b ON bt.blog_id = b.blog_id AND b.is_shielded = 0 " +
                        "GROUP BY t.topic_id, t.topic_name " +
                        "ORDER BY blog_count DESC LIMIT ?";
            
            conn = com.petblog.util.JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();
            
            List<Map<String, Object>> topics = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> topic = new HashMap<>();
                topic.put("topicId", rs.getInt("topic_id"));
                topic.put("topicName", rs.getString("topic_name"));
                topic.put("blogCount", rs.getInt("blog_count"));
                topics.add(topic);
            }
            
            return topics;
        } catch (SQLException e) {
            return handleException(e, "获取热门话题", new ArrayList<>());
        } finally {
            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
        }
    }
    
    /**
     * 获取优质作者（按博客数、点赞数等综合排名）
     */
    public List<Map<String, Object>> getTopAuthors(int limit) {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            String sql = "SELECT u.user_id, u.user_name, u.user_avatar_path, " +
                        "COUNT(b.blog_id) as blog_count, " +
                        "SUM(COALESCE((SELECT COUNT(*) FROM likes WHERE likes.blog_id = b.blog_id), 0)) as total_likes, " +
                        "SUM(COALESCE((SELECT COUNT(*) FROM replies WHERE replies.blog_id = b.blog_id AND replies.parentReply IS NULL), 0)) as total_comments " +
                        "FROM users u " +
                        "LEFT JOIN blogs b ON u.user_id = b.user_id AND b.is_shielded = 0 " +
                        "WHERE u.is_ban = 0 " +
                        "GROUP BY u.user_id, u.user_name, u.user_avatar_path " +
                        "HAVING blog_count > 0 " +
                        "ORDER BY blog_count DESC, total_likes DESC " +
                        "LIMIT ?";
            
            conn = com.petblog.util.JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();
            
            List<Map<String, Object>> authors = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> author = new HashMap<>();
                author.put("userId", rs.getInt("user_id"));
                author.put("userName", rs.getString("user_name"));
                author.put("userAvatarPath", rs.getString("user_avatar_path"));
                author.put("blogCount", rs.getInt("blog_count"));
                author.put("totalLikes", rs.getInt("total_likes"));
                author.put("totalComments", rs.getInt("total_comments"));
                authors.add(author);
            }
            
            return authors;
        } catch (SQLException e) {
            return handleException(e, "获取优质作者", new ArrayList<>());
        } finally {
            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
        }
    }
    
    // ==================== 搜索与关键词热度分析 ====================
    
    /**
     * 获取热门搜索关键词（模拟数据，实际需要搜索日志表）
     */
    public List<Map<String, Object>> getHotSearchKeywords(int limit) {
        // 注意：这里需要实际的搜索日志表，暂时返回模拟数据
        // 实际实现应该从search_logs表查询
        List<Map<String, Object>> keywords = new ArrayList<>();
        
        // 模拟热门关键词（实际应该从数据库查询）
        String[] mockKeywords = {
            "金毛掉毛怎么办", "狗狗拉稀", "宠物疫苗", "狗粮推荐", 
            "训练狗狗", "狗狗不吃饭", "宠物医院", "狗狗皮肤病"
        };
        
        for (int i = 0; i < Math.min(limit, mockKeywords.length); i++) {
            Map<String, Object> keyword = new HashMap<>();
            keyword.put("keyword", mockKeywords[i]);
            keyword.put("searchCount", (int)(Math.random() * 1000) + 100);
            keyword.put("rank", i + 1);
            keywords.add(keyword);
        }
        
        return keywords;
    }
    
    /**
     * 获取搜索零结果统计
     */
    public Map<String, Object> getSearchZeroResultStats() {
        // 模拟数据，实际需要从搜索日志表查询
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalZeroResults", 156);
        stats.put("topZeroResultKeywords", Arrays.asList(
            "金毛掉毛怎么办", "狗狗拉稀", "宠物疫苗", "狗粮推荐"
        ));
        return stats;
    }
    
    // ==================== 数据可视化统计 ====================
    
    /**
     * 获取博客热度趋势（最近30天）
     */
    public Map<String, Object> getBlogHeatTrend(int days) {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            String sql = "SELECT DATE(b.blog_create_time) as date, " +
                        "COUNT(b.blog_id) as blog_count, " +
                        "SUM(COALESCE((SELECT COUNT(*) FROM likes WHERE likes.blog_id = b.blog_id), 0)) as total_likes, " +
                        "SUM(COALESCE((SELECT COUNT(*) FROM replies WHERE replies.blog_id = b.blog_id AND replies.parentReply IS NULL), 0)) as total_comments " +
                        "FROM blogs b " +
                        "WHERE b.blog_create_time >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                        "AND b.is_shielded = 0 " +
                        "GROUP BY DATE(b.blog_create_time) " +
                        "ORDER BY date ASC";
            
            conn = com.petblog.util.JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, days);
            rs = pstmt.executeQuery();
            
            List<String> dates = new ArrayList<>();
            List<Integer> blogCounts = new ArrayList<>();
            List<Integer> likeCounts = new ArrayList<>();
            List<Integer> commentCounts = new ArrayList<>();
            
            while (rs.next()) {
                dates.add(rs.getString("date"));
                blogCounts.add(rs.getInt("blog_count"));
                likeCounts.add(rs.getInt("total_likes"));
                commentCounts.add(rs.getInt("total_comments"));
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("dates", dates);
            result.put("blogCounts", blogCounts);
            result.put("likeCounts", likeCounts);
            result.put("commentCounts", commentCounts);
            return result;
        } catch (SQLException e) {
            return handleException(e, "获取博客热度趋势", new HashMap<>());
        } finally {
            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
        }
    }
    
    /**
     * 获取用户增长趋势（最近30天）
     */
    public Map<String, Object> getUserGrowthTrend(int days) {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            String sql = "SELECT DATE(registration_date) as date, COUNT(*) as user_count " +
                        "FROM users " +
                        "WHERE registration_date >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                        "GROUP BY DATE(registration_date) " +
                        "ORDER BY date ASC";
            
            conn = com.petblog.util.JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, days);
            rs = pstmt.executeQuery();
            
            List<String> dates = new ArrayList<>();
            List<Integer> userCounts = new ArrayList<>();
            
            while (rs.next()) {
                dates.add(rs.getString("date"));
                userCounts.add(rs.getInt("user_count"));
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("dates", dates);
            result.put("userCounts", userCounts);
            return result;
        } catch (SQLException e) {
            return handleException(e, "获取用户增长趋势", new HashMap<>());
        } finally {
            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
        }
    }
    
    /**
     * 获取问诊症状频率统计
     */
    public Map<String, Object> getSymptomFrequencyStats() {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            // 从问诊记录中提取症状关键词
            String sql = "SELECT selected_symptoms, COUNT(*) as count " +
                        "FROM consultations " +
                        "GROUP BY selected_symptoms " +
                        "ORDER BY count DESC " +
                        "LIMIT 20";
            
            conn = com.petblog.util.JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            List<String> symptoms = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();
            
            while (rs.next()) {
                String symptom = rs.getString("selected_symptoms");
                if (symptom != null && !symptom.isEmpty()) {
                    symptoms.add(symptom);
                    counts.add(rs.getInt("count"));
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("symptoms", symptoms);
            result.put("counts", counts);
            return result;
        } catch (SQLException e) {
            return handleException(e, "获取症状频率统计", new HashMap<>());
        } finally {
            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
        }
    }
    
    /**
     * 获取平台总体统计数据
     */
    public Map<String, Object> getPlatformStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 总用户数
            stats.put("totalUsers", userDAO.countTotalUsers());
            
            // 总博客数
            stats.put("totalBlogs", blogDAO.countAll());
            
            // 今日新增用户
            java.sql.Connection conn = com.petblog.util.JdbcUtil.getConnection();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE DATE(registration_date) = CURDATE()"
            );
            java.sql.ResultSet rs = pstmt.executeQuery();
            int todayUsers = 0;
            if (rs.next()) {
                todayUsers = rs.getInt(1);
            }
            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
            stats.put("todayUsers", todayUsers);
            
            // 活跃用户数（最近7天有登录）
            conn = com.petblog.util.JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(
                "SELECT COUNT(DISTINCT user_id) FROM users WHERE last_login >= DATE_SUB(NOW(), INTERVAL 7 DAY)"
            );
            rs = pstmt.executeQuery();
            int activeUsers = 0;
            if (rs.next()) {
                activeUsers = rs.getInt(1);
            }
            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
            stats.put("activeUsers", activeUsers);
            
            return stats;
        } catch (SQLException e) {
            return handleException(e, "获取平台统计", new HashMap<>());
        }
    }
}

