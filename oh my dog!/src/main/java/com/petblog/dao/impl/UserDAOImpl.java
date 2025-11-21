// src/main/java/com/petblog/dao/impl/UserDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.UserDAO;
import com.petblog.model.User;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class UserDAOImpl extends BaseJdbcDAO<User> implements UserDAO {

    @Override
    public User selectById(Integer userId) {
        String sql = "SELECT user_id, user_name, user_password, registration_date, last_login, is_ban, email, user_avatar_path FROM users WHERE user_id = ?";
        try {
            return queryForObject(sql, this::mapRowToUser, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询用户", null);
        }
    }

    @Override
    public User selectByUsername(String username) {
        String sql = "SELECT user_id, user_name, user_password, registration_date, last_login, is_ban, email, user_avatar_path FROM users WHERE user_name = ?";
        try {
            return queryForObject(sql, this::mapRowToUser, username);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据用户名查询用户", null);
        }
    }

    @Override
    public User selectByPhone(String phone) {
        // 假设phone字段在users表中
        String sql = "SELECT user_id, user_name, user_password, registration_date, last_login, is_ban, email, user_avatar_path FROM users WHERE phone = ?";
        try {
            return queryForObject(sql, this::mapRowToUser, phone);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据手机号查询用户", null);
        }
    }

    @Override
    public User selectByEmail(String email) {
        String sql = "SELECT user_id, user_name, user_password, registration_date, last_login, is_ban, email, user_avatar_path FROM users WHERE email = ?";
        try {
            return queryForObject(sql, this::mapRowToUser, email);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据邮箱查询用户", null);
        }
    }

    @Override
    public List<User> selectAll(int pageNum, int pageSize) {
        String sql = "SELECT user_id, user_name, registration_date, last_login, is_ban, email, user_avatar_path FROM users LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToUserWithoutPassword, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "分页查询所有用户", null);
        }
    }

    @Override
    public List<User> searchUsers(String keyword, int pageNum, int pageSize) {
        String sql = "SELECT user_id, user_name, registration_date, last_login, is_ban, email, user_avatar_path FROM users WHERE user_name LIKE ? OR email LIKE ? LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToUserWithoutPassword,
                               "%" + keyword + "%", "%" + keyword + "%", pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "搜索用户", null);
        }
    }

    @Override
    public int countTotalUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        try {
            Number count = (Number) queryForSingleValue(sql);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计用户总数", 0);
        }
    }

    @Override
    public int insert(User user) {
        String sql = "INSERT INTO users (user_name, user_password, registration_date, last_login, is_ban, email, user_avatar_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            return insert(sql, user.getUserName(), user.getUserPassword(), user.getRegistrationDate(),
                         user.getLastLogin(), user.getIsBan(), user.getEmail(), user.getUserAvatarPath());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "添加用户", 0);
        }
    }

    @Override
    public int updateBaseInfo(User user) {
        String sql = "UPDATE users SET user_name = ?, email = ?, user_avatar_path = ? WHERE user_id = ?";
        try {
            return update(sql, user.getUserName(), user.getEmail(), user.getUserAvatarPath(), user.getUserId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新用户基本信息", 0);
        }
    }

    @Override
    public int updatePassword(Integer userId, String newPasswordHash) {
        String sql = "UPDATE users SET user_password = ? WHERE user_id = ?";
        try {
            return update(sql, newPasswordHash, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新用户密码", 0);
        }
    }

    @Override
    public int updateStatus(Integer userId, Integer status) {
        String sql = "UPDATE users SET is_ban = ? WHERE user_id = ?";
        try {
            return update(sql, status, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新用户状态", 0);
        }
    }

    @Override
    public int deleteLogical(Integer userId) {
        String sql = "UPDATE users SET is_ban = 1 WHERE user_id = ?";
        try {
            return update(sql, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "逻辑删除用户", 0);
        }
    }

    @Override
    public boolean existsUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_name = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, username);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查用户名是否存在", false);
        }
    }

    @Override
    public boolean existsPhone(String phone) {
        String sql = "SELECT COUNT(*) FROM users WHERE phone = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, phone);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查手机号是否存在", false);
        }
    }

    @Override
    public boolean existsEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, email);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查邮箱是否存在", false);
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUserName(rs.getString("user_name"));
        user.setUserPassword(rs.getString("user_password"));
        user.setRegistrationDate(rs.getObject("registration_date", LocalDateTime.class));
        user.setLastLogin(rs.getObject("last_login", LocalDateTime.class));
        user.setIsBan(rs.getInt("is_ban"));
        user.setEmail(rs.getString("email"));
        user.setUserAvatarPath(rs.getString("user_avatar_path"));
        return user;
    }

    private User mapRowToUserWithoutPassword(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUserName(rs.getString("user_name"));
        user.setRegistrationDate(rs.getObject("registration_date", LocalDateTime.class));
        user.setLastLogin(rs.getObject("last_login", LocalDateTime.class));
        user.setIsBan(rs.getInt("is_ban"));
        user.setEmail(rs.getString("email"));
        user.setUserAvatarPath(rs.getString("user_avatar_path"));
        return user;
    }
}