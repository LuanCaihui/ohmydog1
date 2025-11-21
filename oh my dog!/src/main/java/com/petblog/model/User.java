package com.petblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：users
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 用户ID（主键，自增）
     */
    private Integer userId;

    /**
     * 用户名（非空）
     */
    private String userName;

    /**
     * 用户密码（非空，存储加密后的密码）
     */
    private String userPassword;

    /**
     * 注册日期（非空）
     */
    private LocalDateTime registrationDate;

    /**
     * 最后登录日期（非空）
     */
    private LocalDateTime lastLogin;

    /**
     * 是否被封禁（非空，0：未封禁，1：已封禁）
     */
    private Integer isBan;

    /**
     * 邮箱（非空，用于登录或找回密码）
     */
    private String email;

    /**
     * 用户头像路径（非空，存储头像图片在服务器的路径）
     */
    private String userAvatarPath;
}