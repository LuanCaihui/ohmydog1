package com.petblog.Service;

import com.petblog.dao.ConsultationDAO;
import com.petblog.dao.impl.ConsultationDAOImpl;
import com.petblog.model.Consultation;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问诊记录服务类
 */
public class ConsultationService extends BaseService {

    private final ConsultationDAO consultationDAO;

    public ConsultationService() {
        this.consultationDAO = new ConsultationDAOImpl();
    }

    /**
     * 根据ID获取问诊记录
     */
    public Consultation getConsultationById(Integer id) {
        try {
            return consultationDAO.findById(id);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询问诊记录");
            return null;
        }
    }

    /**
     * 根据用户ID获取所有问诊记录
     */
    public List<Consultation> getConsultationsByUserId(Integer userId) {
        try {
            return consultationDAO.findByUserId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据用户ID查询问诊记录");
            return null;
        }
    }

    /**
     * 根据用户ID分页获取问诊记录
     */
    public List<Consultation> getConsultationsByUserId(Integer userId, int pageNum, int pageSize) {
        try {
            return consultationDAO.findByUserId(userId, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "分页查询用户问诊记录");
            return null;
        }
    }

    /**
     * 分页获取所有问诊记录（管理员功能）
     */
    public List<Consultation> getAllConsultations(int pageNum, int pageSize) {
        try {
            return consultationDAO.findAll(pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "分页查询所有问诊记录");
            return null;
        }
    }

    /**
     * 统计用户问诊记录数
     */
    public int countConsultationsByUserId(Integer userId) {
        try {
            return consultationDAO.countByUserId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计用户问诊记录数");
            return 0;
        }
    }

    /**
     * 统计所有问诊记录数
     */
    public int countAllConsultations() {
        try {
            return consultationDAO.countAll();
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计所有问诊记录数");
            return 0;
        }
    }

    /**
     * 创建问诊记录
     */
    public Integer createConsultation(Consultation consultation) {
        try {
            // 如果创建时间为空，设置为当前时间
            if (consultation.getCreatedAt() == null) {
                consultation.setCreatedAt(LocalDateTime.now());
            }
            return consultationDAO.insert(consultation);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "创建问诊记录");
            return 0;
        }
    }

    /**
     * 更新问诊记录
     */
    public boolean updateConsultation(Consultation consultation) {
        try {
            int result = consultationDAO.update(consultation);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新问诊记录");
            return false;
        }
    }

    /**
     * 删除问诊记录
     */
    public boolean deleteConsultation(Integer id) {
        try {
            int result = consultationDAO.delete(id);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除问诊记录");
            return false;
        }
    }
}

