package com.petblog.Service;

import com.petblog.dao.*;
import com.petblog.dao.impl.*;
import com.petblog.util.UserLevelUtil;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户经验值服务
 * 计算用户的活跃度积分（XP）
 */
public class UserXpService extends BaseService {
    
    private BlogDAO blogDAO = new BlogDAOImpl();
    private LikeDAO likeDAO;
    private FavoriteDAO favoriteDAO = new FavoriteDAOImpl();
    private ReplyDAO replyDAO = new ReplyDAOImpl();
    private UserDAO userDAO = new UserDAOImpl();
    
    // XP权重配置
    private static final int XP_BLOG_POST = 5;           // 发一篇博客
    private static final int XP_BLOG_LIKED = 1;          // 博客被点赞
    private static final int XP_BLOG_FAVORITED = 3;     // 博客被收藏
    private static final int XP_COMMENT = 2;             // 评论
    private static final int XP_COMMENTED = 2;           // 被评论
    private static final int XP_DAILY_LOGIN = 3;         // 每天登录
    private static final int XP_WEEKLY_BONUS = 20;       // 连续7天登录奖励
    
    public UserXpService() {
        try {
            this.likeDAO = new LikeDAOImpl(com.petblog.util.JdbcUtil.getConnection());
        } catch (SQLException e) {
            handleException(e, "初始化LikeDAO失败");
        }
    }
    
    /**
     * 计算用户的总XP
     * @param userId 用户ID
     * @return 用户总XP
     */
    public int calculateUserXp(Integer userId) {
        try {
            int totalXp = 0;
            
            // 1. 发博客的XP
            int blogCount = countUserBlogs(userId);
            totalXp += blogCount * XP_BLOG_POST;
            
            // 2. 博客被点赞的XP
            int blogLikedCount = countBlogLikes(userId);
            totalXp += blogLikedCount * XP_BLOG_LIKED;
            
            // 3. 博客被收藏的XP
            int blogFavoritedCount = countBlogFavorites(userId);
            totalXp += blogFavoritedCount * XP_BLOG_FAVORITED;
            
            // 4. 评论的XP
            int commentCount = countUserComments(userId);
            totalXp += commentCount * XP_COMMENT;
            
            // 5. 被评论的XP（用户博客收到的评论）
            int commentedCount = countCommentsOnUserBlogs(userId);
            totalXp += commentedCount * XP_COMMENTED;
            
            // 6. 登录活跃度XP
            int loginXp = calculateLoginXp(userId);
            totalXp += loginXp;
            
            return totalXp;
        } catch (Exception e) {
            handleException(new SQLException(e), "计算用户XP");
            return 0;
        }
    }
    
    /**
     * 获取用户等级信息
     * @param userId 用户ID
     * @return 包含等级、名称、图标、XP的Map
     */
    public Map<String, Object> getUserLevelInfo(Integer userId) {
        Map<String, Object> info = new HashMap<>();
        
        int xp = calculateUserXp(userId);
        int level = UserLevelUtil.calculateLevel(xp);
        UserLevelUtil.LevelInfo levelInfo = UserLevelUtil.getLevelInfo(level);
        int nextLevelXp = UserLevelUtil.getNextLevelXp(level);
        
        info.put("xp", xp);
        info.put("level", level);
        info.put("levelName", levelInfo.name);
        info.put("levelIcon", levelInfo.icon);
        info.put("nextLevelXp", nextLevelXp);
        info.put("currentLevelXp", levelInfo.requiredXp);
        
        return info;
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 统计用户发布的博客数
     */
    private int countUserBlogs(Integer userId) {
        try {
            java.util.List<com.petblog.model.Blog> blogs = blogDAO.findByAuthorId(userId);
            return blogs != null ? blogs.size() : 0;
        } catch (SQLException e) {
            return 0;
        }
    }
    
    /**
     * 统计用户博客被点赞的总数
     */
    private int countBlogLikes(Integer userId) {
        try {
            java.util.List<com.petblog.model.Blog> blogs = blogDAO.findByAuthorId(userId);
            if (blogs == null || blogs.isEmpty()) {
                return 0;
            }
            
            int totalLikes = 0;
            for (com.petblog.model.Blog blog : blogs) {
                try {
                    totalLikes += likeDAO.countByBlogId(blog.getBlogId());
                } catch (Exception e) {
                    // 忽略单个博客的错误
                }
            }
            return totalLikes;
        } catch (SQLException e) {
            return 0;
        }
    }
    
    /**
     * 统计用户博客被收藏的总数
     */
    private int countBlogFavorites(Integer userId) {
        try {
            java.util.List<com.petblog.model.Blog> blogs = blogDAO.findByAuthorId(userId);
            if (blogs == null || blogs.isEmpty()) {
                return 0;
            }
            
            int totalFavorites = 0;
            for (com.petblog.model.Blog blog : blogs) {
                try {
                    totalFavorites += favoriteDAO.countByBlogId(blog.getBlogId());
                } catch (Exception e) {
                    // 忽略单个博客的错误
                }
            }
            return totalFavorites;
        } catch (SQLException e) {
            return 0;
        }
    }
    
    /**
     * 统计用户发布的评论数
     */
    private int countUserComments(Integer userId) {
        try {
            return replyDAO.countByUserId(userId);
        } catch (SQLException e) {
            return 0;
        }
    }
    
    /**
     * 统计用户博客收到的评论数
     */
    private int countCommentsOnUserBlogs(Integer userId) {
        try {
            java.util.List<com.petblog.model.Blog> blogs = blogDAO.findByAuthorId(userId);
            if (blogs == null || blogs.isEmpty()) {
                return 0;
            }
            
            int totalComments = 0;
            for (com.petblog.model.Blog blog : blogs) {
                try {
                    java.util.List<com.petblog.model.Reply> replies = replyDAO.findByBlogId(blog.getBlogId());
                    totalComments += replies != null ? replies.size() : 0;
                } catch (Exception e) {
                    // 忽略单个博客的错误
                }
            }
            return totalComments;
        } catch (SQLException e) {
            return 0;
        }
    }
    
    /**
     * 计算登录活跃度XP
     * 每天登录 +3 XP，连续7天额外 +20 XP
     */
    private int calculateLoginXp(Integer userId) {
        try {
            com.petblog.model.User user = userDAO.selectById(userId);
            if (user == null || user.getRegistrationDate() == null) {
                return 0;
            }
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime registrationDate = user.getRegistrationDate();
            LocalDateTime lastLogin = user.getLastLogin();
            
            // 计算注册天数
            long daysSinceRegistration = ChronoUnit.DAYS.between(registrationDate, now);
            
            // 每天登录 +3 XP，最多计算30天（避免无限增长）
            int dailyLoginXp = (int) Math.min(daysSinceRegistration, 30) * XP_DAILY_LOGIN;
            
            // 连续7天登录奖励（简化处理：每7天额外奖励）
            int weeklyBonus = (int) (daysSinceRegistration / 7) * XP_WEEKLY_BONUS;
            
            return dailyLoginXp + weeklyBonus;
        } catch (SQLException e) {
            return 0;
        }
    }
}

