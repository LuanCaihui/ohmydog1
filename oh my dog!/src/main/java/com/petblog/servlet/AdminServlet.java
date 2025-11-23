package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.AdminService;
import com.petblog.model.Blog;
import com.petblog.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员后台Servlet
 * 处理所有管理员相关的API请求
 */
@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {
    private final AdminService adminService = new AdminService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid path\"}");
            return;
        }

        try {
            // 用户管理
            if (pathInfo.startsWith("/users")) {
                handleUserManagement(request, response, pathInfo);
            }
            // 博客管理
            else if (pathInfo.startsWith("/blogs")) {
                handleBlogManagement(request, response, pathInfo);
            }
            // 评论管理
            else if (pathInfo.startsWith("/replies")) {
                handleReplyManagement(request, response, pathInfo);
            }
            // 举报管理
            else if (pathInfo.startsWith("/reports")) {
                handleReportManagement(request, response, pathInfo);
            }
            // 问诊管理
            else if (pathInfo.startsWith("/consultations")) {
                handleConsultationManagement(request, response, pathInfo);
            }
            // 投票管理
            else if (pathInfo.startsWith("/votes")) {
                handleVoteManagement(request, response, pathInfo);
            }
            // 内容推荐管理
            else if (pathInfo.startsWith("/recommendations")) {
                handleRecommendationManagement(request, response, pathInfo);
            }
            // 数据可视化
            else if (pathInfo.startsWith("/analytics")) {
                handleAnalytics(request, response, pathInfo);
            }
            else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Resource not found\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid path\"}");
            return;
        }

        try {
            BufferedReader reader = request.getReader();
            StringBuilder jsonBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
            Map<String, Object> requestData = objectMapper.readValue(jsonBody.toString(), Map.class);

            // 用户管理
            if (pathInfo.startsWith("/users")) {
                handleUserPost(request, response, pathInfo, requestData);
            }
            // 博客管理
            else if (pathInfo.startsWith("/blogs")) {
                handleBlogPost(request, response, pathInfo, requestData);
            }
            // 评论管理
            else if (pathInfo.startsWith("/replies")) {
                handleReplyPost(request, response, pathInfo, requestData);
            }
            else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Resource not found\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // ==================== 用户管理 ====================
    private void handleUserManagement(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        PrintWriter out = response.getWriter();
        
        if (pathInfo.equals("/users") || pathInfo.equals("/users/")) {
            // 获取用户列表
            int pageNum = getIntParameter(request, "pageNum", 1);
            int pageSize = getIntParameter(request, "pageSize", 10);
            String keyword = request.getParameter("keyword");
            
            Map<String, Object> result;
            if (keyword != null && !keyword.isEmpty()) {
                result = adminService.searchUsers(keyword, pageNum, pageSize);
            } else {
                result = adminService.getAllUsers(pageNum, pageSize);
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", result);
            out.print(objectMapper.writeValueAsString(responseData));
        } else if (pathInfo.startsWith("/users/") && pathInfo.endsWith("/history")) {
            // 获取用户历史记录
            String[] parts = pathInfo.split("/");
            if (parts.length >= 3) {
                try {
                    Integer userId = Integer.valueOf(parts[2]);
                    Map<String, Object> history = adminService.getUserHistory(userId);
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("success", true);
                    responseData.put("data", history);
                    out.print(objectMapper.writeValueAsString(responseData));
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid user ID\"}");
                }
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Resource not found\"}");
        }
    }

    private void handleUserPost(HttpServletRequest request, HttpServletResponse response, 
                                String pathInfo, Map<String, Object> requestData) throws IOException {
        PrintWriter out = response.getWriter();
        // 用户等级现在由系统自动计算，不再支持手动设置
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        out.print("{\"error\":\"Resource not found\"}");
    }

    // ==================== 博客管理 ====================
    private void handleBlogManagement(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        PrintWriter out = response.getWriter();
        
        if (pathInfo.equals("/blogs") || pathInfo.equals("/blogs/")) {
            // 获取博客列表
            int pageNum = getIntParameter(request, "pageNum", 1);
            int pageSize = getIntParameter(request, "pageSize", 10);
            
            Map<String, Object> result = adminService.getAllBlogs(pageNum, pageSize);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", result);
            out.print(objectMapper.writeValueAsString(responseData));
        } else if (pathInfo.matches("/blogs/\\d+")) {
            // 获取单个博客详情（包括已封禁的）
            String[] parts = pathInfo.split("/");
            if (parts.length >= 3) {
                try {
                    Integer blogId = Integer.valueOf(parts[2]);
                    Blog blog = adminService.getBlogById(blogId);
                    Map<String, Object> responseData = new HashMap<>();
                    if (blog != null) {
                        responseData.put("success", true);
                        responseData.put("data", blog);
                    } else {
                        responseData.put("success", false);
                        responseData.put("message", "博客不存在");
                    }
                    out.print(objectMapper.writeValueAsString(responseData));
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid blog ID\"}");
                }
            }
        } else if (pathInfo.equals("/blogs/stats")) {
            // 获取博客统计
            Map<String, Object> stats = adminService.getBlogStats();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", stats);
            out.print(objectMapper.writeValueAsString(responseData));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Resource not found\"}");
        }
    }

    private void handleBlogPost(HttpServletRequest request, HttpServletResponse response,
                               String pathInfo, Map<String, Object> requestData) throws IOException {
        PrintWriter out = response.getWriter();
        
        if (pathInfo.equals("/blogs/update-status")) {
            // 更新博客状态（封禁/解封）
            Integer blogId = getIntegerFromMap(requestData, "blogId");
            Integer status = getIntegerFromMap(requestData, "status");
            
            if (blogId == null || status == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Missing blogId or status\"}");
                return;
            }
            
            boolean success = adminService.updateBlogStatus(blogId, status);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", success);
            out.print(objectMapper.writeValueAsString(responseData));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Resource not found\"}");
        }
    }

    // ==================== 评论管理 ====================
    private void handleReplyManagement(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        PrintWriter out = response.getWriter();
        
        if (pathInfo.equals("/replies") || pathInfo.equals("/replies/")) {
            // 获取评论列表
            int pageNum = getIntParameter(request, "pageNum", 1);
            int pageSize = getIntParameter(request, "pageSize", 10);
            
            Map<String, Object> result = adminService.getAllReplies(pageNum, pageSize);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", result);
            out.print(objectMapper.writeValueAsString(responseData));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Resource not found\"}");
        }
    }

    private void handleReplyPost(HttpServletRequest request, HttpServletResponse response,
                                 String pathInfo, Map<String, Object> requestData) throws IOException {
        PrintWriter out = response.getWriter();
        
        if (pathInfo.equals("/replies/update-status")) {
            // 更新评论状态（删除/禁用）
            Integer replyId = getIntegerFromMap(requestData, "replyId");
            Integer status = getIntegerFromMap(requestData, "status");
            
            if (replyId == null || status == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Missing replyId or status\"}");
                return;
            }
            
            boolean success = adminService.updateReplyStatus(replyId, status);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", success);
            out.print(objectMapper.writeValueAsString(responseData));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Resource not found\"}");
        }
    }

    // ==================== 举报管理 ====================
    private void handleReportManagement(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        PrintWriter out = response.getWriter();
        
        if (pathInfo.equals("/reports/stats")) {
            // 获取举报统计
            Map<String, Object> stats = adminService.getReportStats();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", stats);
            out.print(objectMapper.writeValueAsString(responseData));
        } else {
            // 其他举报相关请求由ReportServlet处理
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Resource not found\"}");
        }
    }

    // ==================== 问诊管理 ====================
    private void handleConsultationManagement(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        PrintWriter out = response.getWriter();
        
        if (pathInfo.equals("/consultations") || pathInfo.equals("/consultations/")) {
            // 获取问诊记录列表
            int pageNum = getIntParameter(request, "pageNum", 1);
            int pageSize = getIntParameter(request, "pageSize", 10);
            
            Map<String, Object> result = adminService.getAllConsultations(pageNum, pageSize);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", result);
            out.print(objectMapper.writeValueAsString(responseData));
        } else if (pathInfo.equals("/consultations/stats")) {
            // 获取问诊统计
            Map<String, Object> stats = adminService.getConsultationStats();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", stats);
            out.print(objectMapper.writeValueAsString(responseData));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Resource not found\"}");
        }
    }

    // ==================== 投票管理 ====================
    private void handleVoteManagement(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        PrintWriter out = response.getWriter();
        
        if (pathInfo.equals("/votes/stats")) {
            // 获取投票统计
            Map<String, Object> stats = adminService.getVoteStats();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", stats);
            out.print(objectMapper.writeValueAsString(responseData));
        } else if (pathInfo.equals("/votes/winners")) {
            // 获取获胜狗狗
            int limit = getIntParameter(request, "limit", 10);
            java.util.List<Map<String, Object>> winners = adminService.getWinningDogs(limit);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", winners);
            out.print(objectMapper.writeValueAsString(responseData));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Resource not found\"}");
        }
    }

    // ==================== 工具方法 ====================
    private int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private Integer getIntegerFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.valueOf((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    // ==================== 内容推荐管理 ====================
    private void handleRecommendationManagement(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        PrintWriter out = response.getWriter();
        if (pathInfo.equals("/recommendations/blogs") || pathInfo.equals("/recommendations/blogs/")) {
            String rule = request.getParameter("rule");
            if (rule == null) rule = "weight";
            int limit = getIntParameter(request, "limit", 10);
            
            Map<String, Object> result = adminService.getRecommendedBlogs(rule, limit);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", result);
            out.print(objectMapper.writeValueAsString(responseData));
        } else if (pathInfo.equals("/recommendations/topics") || pathInfo.equals("/recommendations/topics/")) {
            int limit = getIntParameter(request, "limit", 10);
            java.util.List<Map<String, Object>> topics = adminService.getHotTopics(limit);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", topics);
            out.print(objectMapper.writeValueAsString(responseData));
        } else if (pathInfo.equals("/recommendations/authors") || pathInfo.equals("/recommendations/authors/")) {
            int limit = getIntParameter(request, "limit", 10);
            java.util.List<Map<String, Object>> authors = adminService.getTopAuthors(limit);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", authors);
            out.print(objectMapper.writeValueAsString(responseData));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Resource not found\"}");
        }
    }
    
    // ==================== 数据可视化 ====================
    private void handleAnalytics(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        PrintWriter out = response.getWriter();
        if (pathInfo.equals("/analytics/blog-trend") || pathInfo.equals("/analytics/blog-trend/")) {
            int days = getIntParameter(request, "days", 30);
            Map<String, Object> trend = adminService.getBlogHeatTrend(days);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", trend);
            out.print(objectMapper.writeValueAsString(responseData));
        } else if (pathInfo.equals("/analytics/user-growth") || pathInfo.equals("/analytics/user-growth/")) {
            int days = getIntParameter(request, "days", 30);
            Map<String, Object> trend = adminService.getUserGrowthTrend(days);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", trend);
            out.print(objectMapper.writeValueAsString(responseData));
        } else if (pathInfo.equals("/analytics/symptoms") || pathInfo.equals("/analytics/symptoms/")) {
            Map<String, Object> stats = adminService.getSymptomFrequencyStats();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", stats);
            out.print(objectMapper.writeValueAsString(responseData));
        } else if (pathInfo.equals("/analytics/platform-stats") || pathInfo.equals("/analytics/platform-stats/")) {
            Map<String, Object> stats = adminService.getPlatformStats();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("data", stats);
            out.print(objectMapper.writeValueAsString(responseData));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Resource not found\"}");
        }
    }
}

