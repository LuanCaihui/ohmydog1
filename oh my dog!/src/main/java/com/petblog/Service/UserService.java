package com.petblog.Service;

import com.petblog.dao.UserDAO;
import com.petblog.dao.impl.UserDAOImpl;
import com.petblog.model.User;
import java.sql.SQLException;
import java.util.List;

public class UserService extends BaseService {

    private UserDAO userDAO = new UserDAOImpl();

    /**
     * 根据用户ID获取用户信息
     */
    public User getUserById(Integer userId) {
        try {
            return userDAO.selectById(userId);
        } catch (SQLException e) {
            return handleException(e, "根据ID查询用户信息", null);
        }
    }
    /**
 * 创建用户
 */
public User createUser(User user) {
    try {
        int userId = userDAO.insert(user);
        if (userId > 0) {
            return userDAO.selectById(userId);
        }
        return null;
    } catch (SQLException e) {
        return handleException(e, "创建用户", null);
    }
}

/**
 * 更新用户信息
 */
public User updateUser(User user) {
    try {
        int result = userDAO.updateBaseInfo(user);
        if (result > 0) {
            return userDAO.selectById(user.getUserId());
        }
        return null;
    } catch (SQLException e) {
        return handleException(e, "更新用户信息", null);
    }
}

/**
 * 删除用户（逻辑删除）
 */
public boolean deleteUser(Integer userId) {
    try {
        int result = userDAO.deleteLogical(userId);
        return result > 0;
    } catch (Exception e) {  // 改为捕获通用异常
        handleException(new SQLException(e), "删除用户");  // 将Exception包装为SQLException
        return false;
    }
}


    /**
     * 根据用户名获取用户信息
     */
    public User getUserByUsername(String username) {
        try {
            return userDAO.selectByUsername(username);
        } catch (SQLException e) {
            return handleException(e, "根据用户名查询用户信息", null);
        }
    }

    /**
     * 根据手机号获取用户信息
     */
    public User getUserByPhone(String phone) {
        try {
            return userDAO.selectByPhone(phone);
        } catch (SQLException e) {
            return handleException(e, "根据手机号查询用户信息", null);
        }
    }

    /**
     * 根据邮箱获取用户信息
     */
    public User getUserByEmail(String email) {
        try {
            return userDAO.selectByEmail(email);
        } catch (SQLException e) {
            return handleException(e, "根据邮箱查询用户信息", null);
        }
    }

    /**
     * 分页获取所有用户
     */
    public List<User> getAllUsers(int pageNum, int pageSize) {
        try {
            return userDAO.selectAll(pageNum, pageSize);
        } catch (SQLException e) {
            return handleException(e, "分页查询所有用户", null);
        }
    }

    /**
     * 用户登录验证
     * @param username 用户名
     * @param password 密码（明文）
     * @return 登录成功返回User对象（不包含密码），失败返回null
     */
    public User login(String username, String password) {
        try {
            // 根据用户名查询用户
            User user = userDAO.selectByUsername(username);
            if (user == null) {
                return null; // 用户不存在
            }

            // 验证密码（简单字符串比较，实际项目中应使用BCrypt等加密算法）
            if (user.getUserPassword() != null && user.getUserPassword().equals(password)) {
                // 登录成功，更新最后登录时间
                user.setLastLogin(java.time.LocalDateTime.now());
                userDAO.updateBaseInfo(user);
                
                // 返回用户信息（不包含密码）
                User result = new User();
                result.setUserId(user.getUserId());
                result.setUserName(user.getUserName());
                result.setEmail(user.getEmail());
                result.setUserAvatarPath(user.getUserAvatarPath());
                result.setRegistrationDate(user.getRegistrationDate());
                result.setLastLogin(user.getLastLogin());
                result.setIsBan(user.getIsBan());
                return result;
            } else {
                return null; // 密码错误
            }
        } catch (SQLException e) {
            return handleException(e, "用户登录验证", null);
        }
    }
}