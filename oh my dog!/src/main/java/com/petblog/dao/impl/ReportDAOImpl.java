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
        // report_status: 0=未处理, 1=用户已封禁, 2=博客已封禁, 3=举报已驳回
        String sql = "SELECT blog_id, user_id, report_id, reason, report_status, report_createdtime, report_handled_time FROM reports WHERE report_status IN (1, 2, 3) ORDER BY report_handled_time DESC LIMIT ? OFFSET ?";
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
        // reports表只有report_status和report_handled_time字段，没有handle_result和handler_id字段
        // 注意：handleResult 和 handlerId 参数虽然传入，但不存储到数据库
        String sql = "UPDATE reports SET report_status = ?, report_handled_time = ? WHERE report_id = ?";
        try {
            LocalDateTime handledTime = LocalDateTime.now();
            System.out.println("=== 调试信息：更新举报状态 - reportId=" + reportId + ", status=" + status + ", handledTime=" + handledTime);
            int result = update(sql, status, handledTime, reportId);
            System.out.println("=== 调试信息：更新举报状态结果 - 影响行数=" + result);
            return result;
        } catch (SQLException e) {
            System.err.println("=== 错误信息：更新举报状态失败 - " + e.getMessage());
            System.err.println("=== SQL语句: " + sql);
            System.err.println("=== 参数: reportId=" + reportId + ", status=" + status);
            e.printStackTrace();
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
        String sql = "SELECT r.blog_id, r.user_id, r.report_id, r.reason, r.report_status, r.report_createdtime, r.report_handled_time FROM reports r ORDER BY r.report_createdtime DESC";
        try {
            return queryForList(sql, this::mapRowToReport);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询所有举报列表", null);
        }
    }

    /**
     * 查询所有举报信息（包含用户和博客详情）
     * 返回Map列表，包含举报信息、举报人信息、博客信息
     */
    public java.util.List<java.util.Map<String, Object>> findAllWithDetails() {
        // 先尝试查询基础数据，看看是否有数据
        String testSql = "SELECT COUNT(*) as cnt FROM reports";
        try {
            java.sql.Connection testConn = com.petblog.util.JdbcUtil.getConnection();
            java.sql.PreparedStatement testPstmt = testConn.prepareStatement(testSql);
            java.sql.ResultSet testRs = testPstmt.executeQuery();
            if (testRs.next()) {
                int count = testRs.getInt("cnt");
                System.out.println("=== 调试信息：数据库中举报总数: " + count);
            }
            com.petblog.util.JdbcUtil.close(testConn, testPstmt, testRs);
        } catch (SQLException e) {
            System.err.println("查询举报总数失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 测试简单查询，不进行JOIN
        String simpleSql = "SELECT report_id, user_id, blog_id, reason, report_status FROM reports LIMIT 5";
        try {
            java.sql.Connection testConn2 = com.petblog.util.JdbcUtil.getConnection();
            java.sql.PreparedStatement testPstmt2 = testConn2.prepareStatement(simpleSql);
            java.sql.ResultSet testRs2 = testPstmt2.executeQuery();
            int simpleCount = 0;
            while (testRs2.next()) {
                simpleCount++;
                System.out.println("=== 调试信息：简单查询第" + simpleCount + "条 - report_id=" + testRs2.getInt("report_id") + ", status=" + testRs2.getInt("report_status"));
            }
            System.out.println("=== 调试信息：简单查询返回 " + simpleCount + " 条记录");
            com.petblog.util.JdbcUtil.close(testConn2, testPstmt2, testRs2);
        } catch (SQLException e) {
            System.err.println("简单查询失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        String sql = "SELECT r.blog_id, r.user_id, r.report_id, r.reason, r.report_status, r.report_createdtime, r.report_handled_time, " +
                     "u.user_name, u.email, u.registration_date, u.is_ban, " +
                     "b.blog_title, b.blog_content, b.blog_create_time, b.is_shielded, " +
                     "b.user_id as blog_user_id " +
                     "FROM reports r " +
                     "LEFT JOIN users u ON r.user_id = u.user_id " +
                     "LEFT JOIN blogs b ON r.blog_id = b.blog_id " +
                     "ORDER BY r.report_createdtime DESC";
        System.out.println("=== 调试信息：执行SQL查询: " + sql);
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            conn = com.petblog.util.JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
            int count = 0;
            int successCount = 0;
            while (rs.next()) {
                count++;
                try {
                    java.util.Map<String, Object> reportMap = mapRowToReportMap(rs);
                    list.add(reportMap);
                    successCount++;
                    if (successCount == 1) {
                        System.out.println("第一条举报记录映射成功: report_id=" + reportMap.get("report_id"));
                    }
                } catch (Exception e) {
                    System.err.println("映射第 " + count + " 条举报记录失败: " + e.getMessage());
                    System.err.println("错误详情: " + e.getClass().getName());
                    e.printStackTrace();
                    // 继续处理下一条记录，不中断整个查询
                }
            }
            System.out.println("=== 调试信息：查询举报列表（含详情）: 总共 " + count + " 行，成功映射 " + successCount + " 条记录");
            if (count == 0) {
                System.err.println("=== 警告: 查询返回0条记录，可能的原因：1. 数据库中没有举报数据 2. SQL查询有问题 3. JOIN条件不匹配");
            } else if (successCount == 0 && count > 0) {
                System.err.println("=== 严重警告: 查询到 " + count + " 行数据，但所有记录映射都失败了！请检查mapRowToReportMap方法");
            }
            System.out.println("=== 调试信息：返回列表大小: " + list.size());
            return list;
        } catch (SQLException e) {
            System.err.println("查询举报列表（含详情）失败: " + e.getMessage());
            e.printStackTrace();
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询所有举报列表（含详情）", null);
        } finally {
            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
        }
    }

    private java.util.Map<String, Object> mapRowToReportMap(ResultSet rs) throws SQLException {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        try {
            // 举报信息 - 这些字段应该总是存在的
            map.put("report_id", rs.getInt("report_id"));
            map.put("user_id", rs.getInt("user_id"));
            map.put("blog_id", rs.getInt("blog_id"));
            map.put("reason", rs.getString("reason"));
            map.put("report_status", rs.getInt("report_status"));
            
            // 安全处理时间字段，可能为NULL
            java.sql.Timestamp reportCreatedTime = rs.getTimestamp("report_createdtime");
            map.put("report_createdtime", reportCreatedTime != null ? reportCreatedTime.toLocalDateTime() : null);
            
            java.sql.Timestamp reportHandledTime = rs.getTimestamp("report_handled_time");
            map.put("report_handled_time", reportHandledTime != null ? reportHandledTime.toLocalDateTime() : null);
            
            // 举报人信息 - LEFT JOIN可能为NULL，需要安全处理
            String userName = rs.getString("user_name");
            map.put("user_name", userName);
            map.put("user_email", rs.getString("email")); // 使用email字段，兼容前端
            map.put("email", rs.getString("email")); // 同时提供email字段
            java.sql.Timestamp userCreatedTime = rs.getTimestamp("registration_date");
            map.put("user_createdtime", userCreatedTime != null ? userCreatedTime.toLocalDateTime() : null);
            map.put("registration_date", userCreatedTime != null ? userCreatedTime.toLocalDateTime() : null); // 同时提供registration_date字段
            Integer isBan = rs.getObject("is_ban") != null ? rs.getInt("is_ban") : 0;
            map.put("is_ban", isBan);
            map.put("user_is_ban", isBan); // 添加别名，方便前端使用
            
            // 博客信息 - LEFT JOIN可能为NULL，需要安全处理
            map.put("blog_title", rs.getString("blog_title"));
            map.put("blog_content", rs.getString("blog_content"));
            java.sql.Timestamp blogCreateTime = rs.getTimestamp("blog_create_time");
            map.put("blog_create_time", blogCreateTime != null ? blogCreateTime.toLocalDateTime() : null);
            Integer isShielded = rs.getObject("is_shielded") != null ? rs.getInt("is_shielded") : 0;
            map.put("blog_is_shielded", isShielded);
            map.put("is_shielded", isShielded); // 添加别名，方便前端使用
            // 安全处理blog_user_id，可能为NULL
            Object blogUserIdObj = rs.getObject("blog_user_id");
            Integer blogUserId = null;
            if (blogUserIdObj != null) {
                if (blogUserIdObj instanceof Integer) {
                    blogUserId = (Integer) blogUserIdObj;
                } else if (blogUserIdObj instanceof Number) {
                    blogUserId = ((Number) blogUserIdObj).intValue();
                } else {
                    blogUserId = rs.getInt("blog_user_id");
                }
            }
            map.put("blog_user_id", blogUserId);
            
            return map;
        } catch (SQLException e) {
            System.err.println("映射举报记录时出错: " + e.getMessage());
            System.err.println("当前记录 report_id: " + (rs.getObject("report_id") != null ? rs.getInt("report_id") : "NULL"));
            throw e;
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