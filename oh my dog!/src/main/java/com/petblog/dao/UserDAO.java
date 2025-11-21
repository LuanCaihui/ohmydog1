package com.petblog.dao;

import com.petblog.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * 用户数据访问接口
 * 定义用户信息表(users)的所有操作方法，支持用户全生命周期管理
 */
public interface UserDAO {

    /**
     * 根据用户ID查询完整用户信息
     * @param userId 用户ID
     * @return 包含完整信息的User对象，不存在则返回null
     */
    User selectById(Integer userId) throws SQLException;

    /**
     * 根据用户名查询用户（用于登录验证）
     * @param username 用户名
     * @return 包含密码信息的User对象，不存在则返回null
     */
    User selectByUsername(String username) throws SQLException;

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return User对象，不存在则返回null
     */
    User selectByPhone(String phone) throws SQLException;

    /**
     * 根据邮箱查询用户
     * @param email 邮箱地址
     * @return User对象，不存在则返回null
     */
    User selectByEmail(String email) throws SQLException;

    /**
     * 分页查询所有用户（管理员功能）
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页条数
     * @return 用户列表（不含密码信息）
     */
    List<User> selectAll(int pageNum, int pageSize)throws SQLException;

    /**
     * 根据关键词搜索用户（按用户名/昵称模糊匹配）
     * @param keyword 搜索关键词
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 符合条件的用户列表（公开信息）
     */
    List<User> searchUsers(String keyword, int pageNum, int pageSize) throws SQLException;

    /**
     * 统计用户总数
     * @return 系统总用户数量
     */
    int countTotalUsers();

    /**
     * 新增用户（注册功能）
     * @param user 包含用户名、密码、手机号等信息的User对象
     * @return 新增用户的ID，失败返回0
     */
    int insert(User user);

    /**
     * 更新用户基本信息（昵称、头像、简介等）
     * @param user 包含用户ID和待更新字段的User对象
     * @return 受影响的行数
     */
    int updateBaseInfo(User user);

    /**
     * 更新用户密码
     * @param userId 用户ID
     * @param newPasswordHash 加密后的新密码
     * @return 受影响的行数
     */
    int updatePassword(Integer userId, String newPasswordHash);

    /**
     * 更新用户账号状态
     * @param userId 用户ID
     * @param status 状态值（0-禁用，1-正常，2-待验证）
     * @return 受影响的行数
     */
    int updateStatus(Integer userId, Integer status);

    /**
     * 逻辑删除用户（标记删除状态）
     * @param userId 用户ID
     * @return 受影响的行数
     */
    int deleteLogical(Integer userId);

    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 存在返回true，否则返回false
     */
    boolean existsUsername(String username);

    /**
     * 检查手机号是否已被注册
     * @param phone 手机号
     * @return 已注册返回true，否则返回false
     */
    boolean existsPhone(String phone);

    /**
     * 检查邮箱是否已被注册
     * @param email 邮箱
     * @return 已注册返回true，否则返回false
     */
    boolean existsEmail(String email);
}