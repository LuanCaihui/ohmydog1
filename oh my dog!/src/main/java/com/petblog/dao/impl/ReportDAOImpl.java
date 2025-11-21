// src/main/java/com/petblog/dao/impl/ReportDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.ReportDAO;
import com.petblog.model.Report;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ReportDAOImpl extends BaseJdbcDAO<Report> implements ReportDAO {

    @Override
    public Report findById(Integer reportId) {
        String sql = "SELECT blog_id, user_id, report_id, reason, report_status, report_createdtime, report_handled_time FROM reports WHERE report_id = ?";
        try {
            return queryForObject(sql, this::mapRowToReport, reportId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据举报ID查询举报信息", null);
        }
    }

    @Override
    public List<Report> findPendingReports(int pageNum, int pageSize) {
        String sql = "SELECT blog_id, user_id, report_id, reason, report_status, report_createdtime, report_handled_time FROM reports WHERE report_status = 0 ORDER BY report_createdtime DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToReport, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询待处理举报列表", null);
        }
    }

    @Override
    public List<Report> findProcessedReports(int pageNum, int pageSize) {
        String sql = "SELECT blog_id, user_id, report_id, reason, report_status, report_createdtime, report_handled_time FROM reports WHERE report_status IN (1, 2) ORDER BY report_handled_time DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToReport, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询已处理举报列表", null);
        }
    }

    @Override
    public List<Report> findByTarget(Integer targetType, Integer targetId) {
        String sql = "SELECT blog_id, user_id, report_id, reason, report_status, report_createdtime, report_handled_time FROM reports WHERE target_type = ? AND target_id = ?";
        try {
            return queryForList(sql, this::mapRowToReport, targetType, targetId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据目标类型和ID查询举报", null);
        }
    }

    @Override
    public int countByStatus(Integer status) {
        String sql = "SELECT COUNT(*) FROM reports WHERE report_status = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, status);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计指定状态的举报数量", 0);
        }
    }

    @Override
    public int insert(Report report) {
        String sql = "INSERT INTO reports (blog_id, user_id, reason, report_status, report_createdtime) VALUES (?, ?, ?, ?, ?)";
        try {
            return insert(sql, report.getBlogId(), report.getUserId(), report.getReason(),
                         report.getReportStatus(), report.getReportCreatedTime());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "插入举报信息", 0);
        }
    }

    @Override
    public int updateHandleResult(Integer reportId, Integer status, String handleResult, Integer handlerId) {
        String sql = "UPDATE reports SET report_status = ?, handle_result = ?, handler_id = ?, report_handled_time = ? WHERE report_id = ?";
        try {
            return update(sql, status, handleResult, handlerId, LocalDateTime.now(), reportId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新举报处理结果", 0);
        }
    }

    @Override
    public int delete(Integer reportId) {
        String sql = "DELETE FROM reports WHERE report_id = ?";
        try {
            return delete(sql, reportId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除举报信息", 0);
        }
    }

    @Override
    public boolean hasReported(Integer userId, Integer targetType, Integer targetId) {
        String sql = "SELECT COUNT(*) FROM reports WHERE user_id = ? AND blog_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, userId, targetId);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查用户是否已举报过目标", false);
        }
    }

    @Override
    public List<Report> findAll() {
        String sql = "SELECT blog_id, user_id, report_id, reason, report_status, report_createdtime, report_handled_time FROM reports ORDER BY report_createdtime DESC";
        try {
            return queryForList(sql, this::mapRowToReport);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询所有举报列表", null);
        }
    }

    private Report mapRowToReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setBlogId(rs.getInt("blog_id"));
        report.setUserId(rs.getInt("user_id"));
        report.setReportId(rs.getInt("report_id"));
        report.setReason(rs.getString("reason"));
        report.setReportStatus(rs.getInt("report_status"));
        report.setReportCreatedTime(rs.getObject("report_createdtime", LocalDateTime.class));
        report.setReportHandledTime(rs.getObject("report_handled_time", LocalDateTime.class));
        return report;
    }
}