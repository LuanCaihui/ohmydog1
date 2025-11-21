package com.petblog.Service;

import com.petblog.dao.InvestigateDAO;
import com.petblog.dao.impl.InvestigateDAOImpl;
import com.petblog.model.Investigate;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class InvestigateService extends BaseService {

    private InvestigateDAO investigateDAO = new InvestigateDAOImpl();

    /**
     * 根据调查记录ID查询详情
     */
    public Investigate getInvestigateById(Integer investigateId) {
        try {
            return investigateDAO.findById(investigateId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询调查记录详情");
            return null;
        }
    }

    /**
     * 根据用户ID查询其参与的所有药品调查记录
     */
    public List<Investigate> getInvestigatesByUserId(Integer userId) {
        try {
            return investigateDAO.findByUserId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据用户ID查询调查记录列表");
            return null;
        }
    }

    /**
     * 根据药品ID查询所有参与该药品调查的用户ID
     */
    public List<Integer> getUserIdsByMedicineId(Integer medicineId) {
        try {
            return investigateDAO.findUserIdsByMedicineId(medicineId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据药品ID查询参与用户列表");
            return null;
        }
    }

    /**
     * 统计某药品的调查参与人数
     */
    public int countInvestigatesByMedicineId(Integer medicineId) {
        try {
            return investigateDAO.countByMedicineId(medicineId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计药品调查参与人数");
            return 0;
        }
    }

    /**
     * 检查用户是否已参与某药品的调查
     */
    public boolean hasUserParticipated(Integer userId, Integer medicineId) {
        try {
            return investigateDAO.hasParticipated(userId, medicineId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "检查用户是否已参与调查");
            return false;
        }
    }

    /**
     * 新增调查参与记录
     */
    public Integer createInvestigate(Investigate investigate) {
        try {
            return investigateDAO.insert(investigate);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增调查参与记录");
            return 0;
        }
    }

    /**
     * 更新用户的调查反馈内容
     */
    public boolean updateInvestigateFeedback(Investigate investigate) {
        try {
            int result = investigateDAO.updateFeedback(investigate);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新调查反馈内容");
            return false;
        }
    }

    /**
     * 删除用户的调查参与记录
     */
    public boolean deleteInvestigate(Integer investigateId) {
        try {
            int result = investigateDAO.delete(investigateId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除调查参与记录");
            return false;
        }
    }

    /**
     * 删除用户参与的所有调查记录
     */
    public boolean deleteInvestigatesByUserId(Integer userId) {
        try {
            int result = investigateDAO.deleteByUserId(userId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除用户所有调查记录");
            return false;
        }
    }

    /**
     * 删除某药品的所有调查记录
     */
    public boolean deleteInvestigatesByMedicineId(Integer medicineId) {
        try {
            int result = investigateDAO.deleteByMedicineId(medicineId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除药品所有调查记录");
            return false;
        }
    }
}
