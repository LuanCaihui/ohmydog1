package com.petblog.Service;

import com.petblog.dao.LikeDAO;
import com.petblog.dao.impl.LikeDAOImpl;
import com.petblog.model.Like;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

import static com.petblog.util.JdbcUtil.getConnection;

public class LikeService extends BaseService {

    private final LikeDAO likeDAO;

    public LikeService() {
        try {
            this.likeDAO = new LikeDAOImpl(getConnection());
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "初始化LikeDAO失败");
            throw new RuntimeException("无法建立数据库连接", e);
        }
    }

    /**
     * 根据点赞记录ID查询详情
     */
    public Like getLikeById(Integer likeId) {
        try {
            return likeDAO.findById(likeId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询点赞详情");
            return null;
        }
    }

    /**
     * 根据用户ID查询其点赞的所有博客ID
     */
    public List<Integer> getLikedBlogIdsByUserId(Integer userId, int pageNum, int pageSize) {
        try {
            return likeDAO.findBlogIdsByUserId(userId, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据用户ID查询点赞博客列表");
            return null;
        }
    }

    /**
     * 根据博客ID查询所有点赞该博客的用户ID
     */
    public List<Integer> getUserIdsByLikedBlogId(Integer blogId) {
        try {
            return likeDAO.findUserIdsByBlogId(blogId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据博客ID查询点赞用户列表");
            return null;
        }
    }

    /**
     * 统计用户的点赞总数
     */
    public int countUserLikes(Integer userId) {
        try {
            return likeDAO.countByUserId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计用户点赞数量");
            return 0;
        }
    }

    /**
     * 统计博客的获赞总数
     */
    public int countBlogLikes(Integer blogId) {
        try {
            return likeDAO.countByBlogId(blogId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计博客获赞数量");
            return 0;
        }
    }

    /**
     * 检查用户是否已点赞指定博客
     */
    public boolean isBlogLiked(Integer userId, Integer blogId) {
        try {
            return likeDAO.isLiked(userId, blogId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "检查博客点赞状态");
            return false;
        }
    }

    /**
     * 新增点赞记录
     */
    public boolean addLike(Like like) {
        try {
            int result = likeDAO.insert(like);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增点赞记录");
            return false;
        }
    }

    /**
     * 取消点赞
     */
    public boolean removeLike(Integer userId, Integer blogId) {
        try {
            int result = likeDAO.delete(userId, blogId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "取消点赞");
            return false;
        }
    }

    /**
     * 删除用户的所有点赞记录
     */
    public boolean removeAllLikesOfUser(Integer userId) {
        try {
            int result = likeDAO.deleteByUserId(userId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除用户所有点赞记录");
            return false;
        }
    }

    /**
     * 移除所有用户对指定博客的点赞记录
     */
    public boolean removeAllLikesOfBlog(Integer blogId) {
        try {
            int result = likeDAO.deleteByBlogId(blogId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "移除博客所有点赞记录");
            return false;
        }
    }
}
