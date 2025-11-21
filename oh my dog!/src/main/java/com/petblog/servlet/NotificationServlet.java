package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.InformService;
import com.petblog.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知接口
 */
@WebServlet("/api/notifications/*")
public class NotificationServlet extends HttpServlet {
    private final InformService informService = new InformService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理 /api/notifications/unread-count 请求
        if (pathInfo != null && pathInfo.equals("/unread-count")) {
            try {
                String userIdParam = request.getParameter("userId");
                if (userIdParam != null) {
                    Integer userId = Integer.valueOf(userIdParam);
                    java.util.List<com.petblog.model.Inform> unreadInforms = informService.getUnreadInformsByUserId(userId);
                    int count = unreadInforms != null ? unreadInforms.size() : 0;
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("count", count);
                    out.print(objectMapper.writeValueAsString(result));
                } else {
                    // 如果没有userId参数，返回0
                    Map<String, Object> result = new HashMap<>();
                    result.put("count", 0);
                    out.print(objectMapper.writeValueAsString(result));
                }
            } catch (Exception e) {
                Map<String, Object> result = new HashMap<>();
                result.put("count", 0);
                out.print(objectMapper.writeValueAsString(result));
            }
            return;
        }
        
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"error\":\"Invalid request\"}");
    }
}

