package com.petblog.Service;

import com.petblog.dao.AuditorDAO;
import com.petblog.dao.impl.AuditorDAOImpl;
import com.petblog.model.Auditor;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class AuditorService extends BaseService {

    private AuditorDAO auditorDAO = new AuditorDAOImpl();

    /**
     * 创建新的审计员
     */
    public boolean createAuditor(Auditor auditor) {
        try {
            auditorDAO.insert(auditor);
            return true;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "创建审计员");
            return false;
        }
    }

    /**
     * 更新审计员信息
     */
    public boolean updateAuditor(Auditor auditor) {
        try {
            auditorDAO.update(auditor);
            return true;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新审计员");
            return false;
        }
    }

    /**
     * 删除审计员
     */
    public boolean deleteAuditor(int id) {
        try {
            auditorDAO.delete(id);
            return true;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除审计员");
            return false;
        }
    }

    /**
     * 根据ID查找审计员
     */
    public Auditor getAuditorById(int id) {
        try {
            return auditorDAO.findById(id);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询审计员");
            return null;
        }
    }

    /**
     * 查找所有审计员
     */
    public List<Auditor> getAllAuditors() {
        try {
            return auditorDAO.findAll();
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询所有审计员");
            return null;
        }
    }

    /**
     * 根据状态查找审计员
     */
    public List<Auditor> getAuditorsByStatus(String status) {
        try {
            return auditorDAO.findByStatus(status);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据状态查询审计员");
            return null;
        }
    }

    /**
     * 审核员登录
     */
    public Auditor login(String name, String password) {
        try {
            System.out.println("审核员登录尝试: name=" + name + ", password=" + password);
            Auditor auditor = auditorDAO.findByName(name);
            System.out.println("查询结果: " + (auditor != null ? "找到审核员 id=" + auditor.getAuditorId() : "未找到审核员"));
            if (auditor != null) {
                // 验证密码
                if (auditor.getAuditorPassword() != null && auditor.getAuditorPassword().equals(password)) {
                    System.out.println("密码验证成功");
                    return auditor;
                } else {
                    System.out.println("密码验证失败: 数据库密码=" + auditor.getAuditorPassword() + ", 输入密码=" + password);
                    return null;
                }
            }
            return null;
        } catch (SQLException e) {
            System.err.println("审核员登录SQL异常: " + e.getMessage());
            e.printStackTrace();
            SQLExceptionHandler.handleSQLException(e, "审核员登录");
            return null;
        }
    }
}