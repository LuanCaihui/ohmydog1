package com.petblog.Service;
import com.petblog.dao.FavoriteDAO;
import com.petblog.dao.impl.FavoriteDAOImpl;
import com.petblog.model.Favorite;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class FavoriteService extends BaseService {

    private FavoriteDAO favoriteDAO = new FavoriteDAOImpl();

    /**
     * 根据用户ID查询其收藏的所有博客ID
     */
    public List<Integer> getFavoriteBlogIdsByUserId(Integer userId, int pageNum, int pageSize) {
        try {
            return favoriteDAO.findBlogIdsByUserId(userId, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据用户ID查询收藏博客列表");
            return null;
        }
    }

    /**
     * 根据博客ID查询所有收藏该博客的用户ID
     */
    public List<Integer> getUserIdsByFavoriteBlogId(Integer blogId) {
        try {
            return favoriteDAO.findUserIdsByBlogId(blogId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据博客ID查询收藏用户列表");
            return null;
        }
    }

    /**
     * 统计用户的收藏总数
     */
    public int countUserFavorites(Integer userId) {
        try {
            return favoriteDAO.countByUserId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计用户收藏数量");
            return 0;
        }
    }

    /**
     * 统计博客的被收藏次数
     */
    public int countBlogFavorites(Integer blogId) {
        try {
            return favoriteDAO.countByBlogId(blogId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计博客被收藏次数");
            return 0;
        }
    }

    /**
     * 检查用户是否已收藏指定博客
     */
    public boolean isBlogFavorite(Integer userId, Integer blogId) {
        try {
            return favoriteDAO.isFavorite(userId, blogId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "检查博客收藏状态");
            return false;
        }
    }

    /**
     * 新增博客收藏
     */
    public boolean addFavorite(Favorite favorite) {
        try {
            int result = favoriteDAO.insert(favorite);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增博客收藏");
            return false;
        }
    }

    /**
     * 取消博客收藏
     */
    public boolean removeFavorite(Integer userId, Integer blogId) {
        try {
            int result = favoriteDAO.delete(userId, blogId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "取消博客收藏");
            return false;
        }
    }

    /**
     * 删除用户的所有收藏
     */
    public boolean removeAllFavoritesOfUser(Integer userId) {
        try {
            int result = favoriteDAO.deleteByUserId(userId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除用户所有收藏");
            return false;
        }
    }

    /**
     * 移除所有用户对指定博客的收藏
     */
    public boolean removeAllFavoritesOfBlog(Integer blogId) {
        try {
            int result = favoriteDAO.deleteByBlogId(blogId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "移除博客所有收藏");
            return false;
        }
    }
}
