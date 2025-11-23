package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.ReportService;
import com.petblog.model.Report;
import com.petblog.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/api/reports/*")
public class ReportServlet extends HttpServlet {
    private final ReportService reportService = new ReportService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 获取举报列表（分页）
            String statusParam = request.getParameter("status"); // pending 或 processed
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");

            try {
                int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

                if ("processed".equals(statusParam)) {
                    List<Report> reports = reportService.getProcessedReports(pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(reports));
                } else {
                    List<Report> reports = reportService.getPendingReports(pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(reports));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
            }
        } else if (pathInfo.equals("/all")) {
            // 获取所有举报（包含用户和博客详情）
            try {
                System.out.println("=== ReportServlet: 开始调用 getAllReportsWithDetails()");
                java.util.List<java.util.Map<String, Object>> reports = reportService.getAllReportsWithDetails();
                System.out.println("=== ReportServlet: getAllReportsWithDetails() 返回结果: " + (reports != null ? "非空" : "NULL"));
                System.out.println("=== ReportServlet: 获取举报列表，数量: " + (reports != null ? reports.size() : 0));
                if (reports != null && reports.size() > 0) {
                    System.out.println("=== ReportServlet: 第一条记录: " + reports.get(0));
                }
                java.util.Map<String, Object> result = new java.util.HashMap<>();
                result.put("success", true);
                result.put("reports", reports != null ? reports : new java.util.ArrayList<>());
                String jsonResult = objectMapper.writeValueAsString(result);
                System.out.println("=== ReportServlet: 返回JSON长度: " + jsonResult.length());
                System.out.println("=== ReportServlet: 返回JSON前100字符: " + (jsonResult.length() > 100 ? jsonResult.substring(0, 100) : jsonResult));
                out.print(jsonResult);
            } catch (Exception e) {
                System.err.println("=== ReportServlet: 获取举报列表失败: " + e.getMessage());
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"error\":\"获取举报列表失败\"}");
            }
        } else if (pathInfo.equals("/stats")) {
            // 获取统计信息
            try {
                java.util.Map<String, Integer> stats = reportService.getReportStats();
                java.util.Map<String, Object> result = new java.util.HashMap<>();
                result.put("success", true);
                result.put("stats", stats);
                out.print(objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"error\":\"获取统计信息失败\"}");
            }
        } else {
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length < 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid report ID\"}");
                    return;
                }
                
                // 处理 /{id}/detail 路径
                if (splits.length >= 3 && splits[2].equals("detail")) {
                    Integer reportId = Integer.valueOf(splits[1]);
                    Report report = reportService.getReportById(reportId);
                    if (report == null) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"success\":false,\"error\":\"Report not found\"}");
                    } else {
                        // 获取举报人信息
                        com.petblog.Service.UserService userService = new com.petblog.Service.UserService();
                        com.petblog.model.User reporter = userService.getUserById(report.getUserId());
                        
                        // 获取博客信息
                        com.petblog.Service.BlogService blogService = new com.petblog.Service.BlogService();
                        com.petblog.model.Blog blog = blogService.getBlogById(report.getBlogId());
                        
                        // 获取博客作者信息
                        com.petblog.model.User blogAuthor = null;
                        if (blog != null && blog.getUserId() != null) {
                            blogAuthor = userService.getUserById(blog.getUserId());
                        }
                        
                        // 构建返回结果
                        java.util.Map<String, Object> result = new java.util.HashMap<>();
                        result.put("success", true);
                        result.put("report", report);
                        result.put("reporter", reporter);
                        result.put("blog", blog);
                        result.put("blogAuthor", blogAuthor);
                        out.print(objectMapper.writeValueAsString(result));
                    }
                } else {
                    Integer reportId = Integer.valueOf(splits[1]);
                    Report report = reportService.getReportById(reportId);
                    if (report == null) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Report not found\"}");
                    } else {
                        out.print(objectMapper.writeValueAsString(report));
                    }
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid report ID format\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 读取请求体
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        try {
            // 处理不同的POST路径
            if (pathInfo != null && pathInfo.equals("/add")) {
                // 创建新举报（从首页举报按钮）
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> requestData = objectMapper.readValue(sb.toString(), 
                    objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, Object.class));
                
                // 获取请求参数
                Integer blogId = requestData.get("blog_id") != null ? 
                    (requestData.get("blog_id") instanceof Integer ? (Integer) requestData.get("blog_id") : 
                     Integer.valueOf(requestData.get("blog_id").toString())) : null;
                String reason = requestData.get("reason") != null ? requestData.get("reason").toString() : null;
                
                // 从session获取当前用户ID
                Integer userId = (Integer) request.getSession().getAttribute("userId");
                
                // 如果session中没有，尝试从请求参数中获取
                if (userId == null && requestData.get("userId") != null) {
                    userId = requestData.get("userId") instanceof Integer ? 
                        (Integer) requestData.get("userId") : 
                        Integer.valueOf(requestData.get("userId").toString());
                }
                
                if (blogId == null || reason == null || reason.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"error\":\"缺少必要参数：blog_id或reason\"}");
                    return;
                }
                
                if (userId == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.print("{\"success\":false,\"error\":\"未登录，请先登录\"}");
                    return;
                }
                
                // 检查用户是否已经举报过该博客
                if (reportService.hasUserReported(userId, 1, blogId)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"error\":\"您已经举报过该博客\"}");
                    return;
                }
                
                // 创建Report对象
                Report report = new Report();
                report.setBlogId(blogId);
                report.setUserId(userId);
                report.setReason(reason.trim());
                report.setReportStatus(0); // 未处理状态
                report.setReportCreatedTime(LocalDateTime.now());
                
                // 调用ReportService创建举报
                Integer result = reportService.createReport(report);
                if (result > 0) {
                    report.setReportId(result);
                    java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                    responseData.put("success", true);
                    responseData.put("message", "举报成功，感谢您的反馈！");
                    responseData.put("report", report);
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    out.print(objectMapper.writeValueAsString(responseData));
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"error\":\"创建举报失败\"}");
                }
            } else if (pathInfo != null && pathInfo.equals("/banUser")) {
                // 封禁用户
                try {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> requestData = objectMapper.readValue(sb.toString(), 
                        objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, Object.class));
                    Integer userId = requestData.get("user_id") != null ? 
                        (requestData.get("user_id") instanceof Integer ? (Integer) requestData.get("user_id") : 
                         Integer.valueOf(requestData.get("user_id").toString())) : null;
                    Integer reportId = requestData.get("report_id") != null ? 
                        (requestData.get("report_id") instanceof Integer ? (Integer) requestData.get("report_id") : 
                         Integer.valueOf(requestData.get("report_id").toString())) : null;
                    
                    if (userId != null && reportId != null) {
                        // 更新用户状态为封禁
                        com.petblog.dao.UserDAO userDAO = new com.petblog.dao.impl.UserDAOImpl();
                        int updateResult = userDAO.updateStatus(userId, 1); // 1表示封禁
                        if (updateResult > 0) {
                            // 更新举报状态为已处理（封禁用户）- status=1表示用户已封禁
                            boolean reportUpdateResult = reportService.updateReportHandleResult(reportId, 1, "用户已被封禁", null);
                            System.out.println("=== 调试信息：封禁用户后更新举报状态 - reportId=" + reportId + ", status=1, result=" + reportUpdateResult);
                            java.util.Map<String, Object> result = new java.util.HashMap<>();
                            result.put("success", true);
                            result.put("message", "用户封禁成功，该用户的所有博客将不再显示");
                            out.print(objectMapper.writeValueAsString(result));
                        } else {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            out.print("{\"success\":false,\"error\":\"封禁用户失败\"}");
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"success\":false,\"error\":\"参数错误：缺少user_id或report_id\"}");
                    }
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"error\":\"封禁用户失败: " + e.getMessage() + "\"}");
                    e.printStackTrace();
                }
            } else if (pathInfo != null && pathInfo.equals("/banBlog")) {
                // 封禁博客
                try {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> requestData = objectMapper.readValue(sb.toString(), 
                        objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, Object.class));
                    Integer blogId = requestData.get("blog_id") != null ? 
                        (requestData.get("blog_id") instanceof Integer ? (Integer) requestData.get("blog_id") : 
                         Integer.valueOf(requestData.get("blog_id").toString())) : null;
                    Integer reportId = requestData.get("report_id") != null ? 
                        (requestData.get("report_id") instanceof Integer ? (Integer) requestData.get("report_id") : 
                         Integer.valueOf(requestData.get("report_id").toString())) : null;
                    
                    if (blogId != null && reportId != null) {
                        // 更新博客状态为封禁
                        // 直接查询数据库，不检查是否被封禁（因为可能已经被封禁了）
                        try {
                            String directSql = "SELECT b.blog_id, b.user_id, b.blog_title, b.blog_content, b.blog_update_time, b.blog_create_time, b.is_shielded " +
                                             "FROM blogs b " +
                                             "WHERE b.blog_id = ?";
                            java.sql.Connection conn = com.petblog.util.JdbcUtil.getConnection();
                            java.sql.PreparedStatement pstmt = conn.prepareStatement(directSql);
                            pstmt.setInt(1, blogId);
                            java.sql.ResultSet rs = pstmt.executeQuery();
                            
                            com.petblog.model.Blog blog = null;
                            if (rs.next()) {
                                blog = new com.petblog.model.Blog();
                                blog.setBlogId(rs.getInt("blog_id"));
                                blog.setUserId(rs.getInt("user_id"));
                                blog.setBlogTitle(rs.getString("blog_title"));
                                blog.setBlogContent(rs.getString("blog_content"));
                                blog.setBlogUpdateTime(rs.getDate("blog_update_time"));
                                blog.setBlogCreateTime(rs.getDate("blog_create_time"));
                                blog.setIsShielded(rs.getInt("is_shielded"));
                            }
                            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
                            
                            if (blog != null) {
                                // 直接更新is_shielded字段
                                String updateSql = "UPDATE blogs SET is_shielded = 1 WHERE blog_id = ?";
                                java.sql.Connection updateConn = com.petblog.util.JdbcUtil.getConnection();
                                java.sql.PreparedStatement updatePstmt = updateConn.prepareStatement(updateSql);
                                updatePstmt.setInt(1, blogId);
                                int updateCount = updatePstmt.executeUpdate();
                                com.petblog.util.JdbcUtil.close(updateConn, updatePstmt, null);
                                
                                if (updateCount > 0) {
                                    // 更新举报状态为已处理（封禁博客）- status=2表示博客已封禁
                                    boolean reportUpdateResult = reportService.updateReportHandleResult(reportId, 2, "博客已被封禁", null);
                                    System.out.println("=== 调试信息：封禁博客后更新举报状态 - reportId=" + reportId + ", status=2, result=" + reportUpdateResult);
                                    java.util.Map<String, Object> result = new java.util.HashMap<>();
                                    result.put("success", true);
                                    result.put("message", "博客封禁成功，该博客将不再显示");
                                    out.print(objectMapper.writeValueAsString(result));
                                } else {
                                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                    out.print("{\"success\":false,\"error\":\"更新博客状态失败\"}");
                                }
                            } else {
                                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                out.print("{\"success\":false,\"error\":\"博客不存在\"}");
                            }
                        } catch (Exception e) {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            out.print("{\"success\":false,\"error\":\"封禁博客失败: " + e.getMessage() + "\"}");
                            e.printStackTrace();
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"success\":false,\"error\":\"参数错误：缺少blog_id或report_id\"}");
                    }
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"error\":\"封禁博客失败: " + e.getMessage() + "\"}");
                    e.printStackTrace();
                }
            } else if (pathInfo != null && pathInfo.equals("/reject")) {
                // 驳回举报
                try {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> requestData = objectMapper.readValue(sb.toString(), 
                        objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, Object.class));
                    Integer reportId = requestData.get("report_id") != null ? 
                        (requestData.get("report_id") instanceof Integer ? (Integer) requestData.get("report_id") : 
                         Integer.valueOf(requestData.get("report_id").toString())) : null;
                    String reason = requestData.get("reason") != null ? requestData.get("reason").toString() : "理由不充分";
                    
                    System.out.println("=== 调试信息：驳回举报 - reportId=" + reportId + ", reason=" + reason);
                    
                    if (reportId != null) {
                        // 更新举报状态为已处理（驳回）- status=3表示举报已驳回
                        boolean updateResult = reportService.updateReportHandleResult(reportId, 3, reason, null);
                        System.out.println("=== 调试信息：驳回举报更新结果 - " + updateResult);
                        if (updateResult) {
                            java.util.Map<String, Object> result = new java.util.HashMap<>();
                            result.put("success", true);
                            result.put("message", "举报驳回成功");
                            out.print(objectMapper.writeValueAsString(result));
                        } else {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            out.print("{\"success\":false,\"error\":\"驳回举报失败\"}");
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"success\":false,\"error\":\"参数错误：缺少report_id\"}");
                    }
                } catch (Exception e) {
                    System.err.println("=== 错误信息：驳回举报异常 - " + e.getMessage());
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"error\":\"驳回举报失败: " + e.getMessage() + "\"}");
                }
            } else {
                // 创建新举报
                Report report = objectMapper.readValue(sb.toString(), Report.class);

                // 设置默认值
                if (report.getReportCreatedTime() == null) {
                    report.setReportCreatedTime(LocalDateTime.now());
                }
                if (report.getReportStatus() == null) {
                    report.setReportStatus(0); // 默认为未处理状态
                }

                // 调用ReportService创建举报的方法
                Integer result = reportService.createReport(report);
                if (result > 0) {
                    report.setReportId(result);
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    out.print(objectMapper.writeValueAsString(report));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"创建举报失败\"}");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"error\":\"Invalid request data\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"需要指定举报ID\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid report ID\"}");
                return;
            }
            Integer reportId = Integer.valueOf(splits[1]);

            // 读取请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 解析请求体中的处理信息
            Report updateRequest = objectMapper.readValue(sb.toString(), Report.class);

            // 调用ReportService更新举报处理结果的方法
            boolean result = reportService.updateReportHandleResult(
                reportId,
                updateRequest.getReportStatus(),
                updateRequest.getReason(),
                updateRequest.getUserId() // 这里用userId字段表示处理人ID
            );

            if (result) {
                out.print("{\"message\":\"举报处理结果更新成功\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"更新举报处理结果失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid report ID format\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid request data\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"需要指定举报ID\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid report ID\"}");
                return;
            }
            Integer reportId = Integer.valueOf(splits[1]);

            // 调用ReportService删除举报记录的方法
            boolean result = reportService.deleteReport(reportId);
            if (result) {
                out.print("{\"message\":\"举报记录删除成功\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"举报记录未找到或删除失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid report ID format\"}");
        }
    }
}
