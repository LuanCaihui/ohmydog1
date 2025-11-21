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
            // 获取所有举报
            try {
                List<Report> reports = reportService.getAllReports();
                java.util.Map<String, Object> result = new java.util.HashMap<>();
                result.put("success", true);
                result.put("reports", reports != null ? reports : new java.util.ArrayList<>());
                out.print(objectMapper.writeValueAsString(result));
            } catch (Exception e) {
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
                        out.print("{\"error\":\"Report not found\"}");
                    } else {
                        out.print(objectMapper.writeValueAsString(report));
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
            if (pathInfo != null && pathInfo.equals("/banUser")) {
                // 封禁用户
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
                    userDAO.updateStatus(userId, 1); // 1表示封禁
                    // 更新举报状态为已处理（封禁用户）
                    reportService.updateReportHandleResult(reportId, 1, "用户已被封禁", null);
                    java.util.Map<String, Object> result = new java.util.HashMap<>();
                    result.put("success", true);
                    result.put("message", "用户封禁成功");
                    out.print(objectMapper.writeValueAsString(result));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"error\":\"参数错误\"}");
                }
            } else if (pathInfo != null && pathInfo.equals("/banBlog")) {
                // 封禁博客
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
                    com.petblog.Service.BlogService blogService = new com.petblog.Service.BlogService();
                    com.petblog.model.Blog blog = blogService.getBlogById(blogId);
                    if (blog != null) {
                        blog.setIsShielded(1);
                        blogService.updateBlog(blog);
                    }
                    // 更新举报状态为已处理（封禁博客）
                    reportService.updateReportHandleResult(reportId, 2, "博客已被封禁", null);
                    java.util.Map<String, Object> result = new java.util.HashMap<>();
                    result.put("success", true);
                    result.put("message", "博客封禁成功");
                    out.print(objectMapper.writeValueAsString(result));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"error\":\"参数错误\"}");
                }
            } else if (pathInfo != null && pathInfo.equals("/reject")) {
                // 驳回举报
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> requestData = objectMapper.readValue(sb.toString(), 
                    objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, Object.class));
                Integer reportId = requestData.get("report_id") != null ? 
                    (requestData.get("report_id") instanceof Integer ? (Integer) requestData.get("report_id") : 
                     Integer.valueOf(requestData.get("report_id").toString())) : null;
                String reason = requestData.get("reason") != null ? requestData.get("reason").toString() : "理由不充分";
                
                if (reportId != null) {
                    // 更新举报状态为已处理（驳回）
                    reportService.updateReportHandleResult(reportId, 3, reason, null);
                    java.util.Map<String, Object> result = new java.util.HashMap<>();
                    result.put("success", true);
                    result.put("message", "举报驳回成功");
                    out.print(objectMapper.writeValueAsString(result));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"error\":\"参数错误\"}");
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
