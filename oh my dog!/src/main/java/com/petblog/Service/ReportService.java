package com.petblog.Service;

import com.petblog.dao.ReportDAO;
import com.petblog.dao.impl.ReportDAOImpl;
import com.petblog.model.Report;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class ReportService extends BaseService {

    private final ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAOImpl();
    }

    /**
     * 根据举报ID查询举报详情
     */
    public Report getReportById(Integer reportId) {
        try {
            return reportDAO.findById(reportId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询举报详情");
            return null;
        }
    }

    /**
     * 查询待处理的举报信息
     */
    public List<Report> getPendingReports(int pageNum, int pageSize) {
        try {
            return reportDAO.findPendingReports(pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询待处理举报信息");
            return null;
        }
    }

    /**
     * 查询已处理的举报信息
     */
    public List<Report> getProcessedReports(int pageNum, int pageSize) {
        try {
            return reportDAO.findProcessedReports(pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询已处理举报信息");
            return null;
        }
    }

    /**
     * 根据被举报内容的类型和ID查询举报记录
     */
    public List<Report> getReportsByTarget(Integer targetType, Integer targetId) {
        try {
            return reportDAO.findByTarget(targetType, targetId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据目标查询举报记录");
            return null;
        }
    }

    /**
     * 统计不同状态的举报数量
     */
    public int countReportsByStatus(Integer status) {
        try {
            return reportDAO.countByStatus(status);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计举报数量");
            return 0;
        }
    }

    /**
     * 新增举报信息
     */
    public Integer createReport(Report report) {
        try {
            return reportDAO.insert(report);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增举报信息");
            return 0;
        }
    }

    /**
     * 更新举报处理结果
     */
    public boolean updateReportHandleResult(Integer reportId, Integer status, String handleResult, Integer handlerId) {
        try {
            int result = reportDAO.updateHandleResult(reportId, status, handleResult, handlerId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新举报处理结果");
            return false;
        }
    }

    /**
     * 删除举报记录（仅用于清理过期数据）
     */
    public boolean deleteReport(Integer reportId) {
        try {
            int result = reportDAO.delete(reportId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除举报记录");
            return false;
        }
    }

    /**
     * 检查用户是否已举报过同一内容
     */
    public boolean hasUserReported(Integer userId, Integer targetType, Integer targetId) {
        try {
            return reportDAO.hasReported(userId, targetType, targetId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "检查用户是否已举报");
            return false;
        }
    }

    /**
     * 获取所有举报信息
     */
    public List<Report> getAllReports() {
        try {
            return reportDAO.findAll();
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询所有举报信息");
            return null;
        }
    }

    /**
     * 获取举报统计信息
     */
    public java.util.Map<String, Integer> getReportStats() {
        try {
            int pending = reportDAO.countByStatus(0);
            int handled = reportDAO.countByStatus(1) + reportDAO.countByStatus(2) + reportDAO.countByStatus(3);
            int total = pending + handled;
            java.util.Map<String, Integer> stats = new java.util.HashMap<>();
            stats.put("pending", pending);
            stats.put("handled", handled);
            stats.put("total", total);
            return stats;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "获取举报统计信息");
            java.util.Map<String, Integer> stats = new java.util.HashMap<>();
            stats.put("pending", 0);
            stats.put("handled", 0);
            stats.put("total", 0);
            return stats;
        }
    }
}
