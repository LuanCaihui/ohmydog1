package com.petblog.Service;

import com.petblog.dao.AuthenticationDAO;
import com.petblog.dao.impl.AuthenticationDAOImpl;
import com.petblog.model.Authentication;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class AuthenticationService extends BaseService {

    private AuthenticationDAO authenticationDAO = new AuthenticationDAOImpl();

    /**
     * 创建认证信息
     */
    public boolean createAuthentication(Authentication auth) {
        try {
            authenticationDAO.insert(auth);
            return true;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "创建认证信息");
            return false;
        }
    }

    /**
     * 更新认证信息
     */
    public boolean updateAuthentication(Authentication auth) {
        try {
            authenticationDAO.update(auth);
            return true;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新认证信息");
            return false;
        }
    }

    /**
     * 删除认证信息
     */
    public boolean deleteAuthentication(int id) {
        try {
            authenticationDAO.delete(id);
            return true;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除认证信息");
            return false;
        }
    }

    /**
     * 根据ID查找认证信息
     */
    public Authentication getAuthenticationById(int id) {
        try {
            return authenticationDAO.findById(id);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询认证信息");
            return null;
        }
    }

    /**
     * 根据用户ID查找认证信息
     */
    public Authentication getAuthenticationByUserId(int userId) {
        try {
            return authenticationDAO.findByUserId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据用户ID查询认证信息");
            return null;
        }
    }

    /**
     * 查找所有认证信息
     */
    public List<Authentication> getAllAuthentications() {
        try {
            return authenticationDAO.findAll();
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询所有认证信息");
            return null;
        }
    }
}
