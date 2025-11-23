package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.UserService;
import com.petblog.model.User;
import com.petblog.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/user/*")
public class UserProfileServlet extends HttpServlet {
    private final UserService userService = new UserService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理 /api/user/profile 请求（获取当前用户资料）
        if (pathInfo != null && pathInfo.equals("/profile")) {
            try {
                // 从session获取当前用户ID
                Object userIdObj = request.getSession().getAttribute("userId");
                String userIdParam = request.getParameter("userId");
                
                Integer userId = null;
                if (userIdParam != null) {
                    userId = Integer.valueOf(userIdParam);
                } else if (userIdObj != null) {
                    userId = userIdObj instanceof Integer ? (Integer) userIdObj : Integer.valueOf(userIdObj.toString());
                }
                
                if (userId == null) {
                    // 如果没有userId，返回null或空对象，而不是错误
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.print("{\"error\":\"未登录，请先登录\"}");
                    return;
                }
                
                User user = userService.getUserById(userId);
                if (user != null) {
                    // 返回用户信息（不包含密码）
                    User result = new User();
                    result.setUserId(user.getUserId());
                    result.setUserName(user.getUserName());
                    result.setEmail(user.getEmail());
                    result.setUserAvatarPath(user.getUserAvatarPath());
                    result.setRegistrationDate(user.getRegistrationDate());
                    result.setLastLogin(user.getLastLogin());
                    result.setIsBan(user.getIsBan());
                    out.print(objectMapper.writeValueAsString(result));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"用户未找到\"}");
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"获取用户资料失败: " + e.getMessage() + "\"}");
                e.printStackTrace();
            }
            return;
        }
        
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        out.print("{\"error\":\"路径不存在\"}");
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理 /api/user/update 请求
        if (pathInfo != null && pathInfo.equals("/update")) {
            try {
                // 优先从session获取当前用户ID
                Object userIdObj = request.getSession().getAttribute("userId");
                String userIdParam = request.getParameter("userId");
                
                Integer userId = null;
                if (userIdParam != null) {
                    userId = Integer.valueOf(userIdParam);
                } else if (userIdObj != null) {
                    userId = userIdObj instanceof Integer ? (Integer) userIdObj : Integer.valueOf(userIdObj.toString());
                }
                
                // 读取请求体（只读取一次）
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                
                String requestBody = sb.toString();
                
                // 如果仍然没有userId，尝试从请求体中获取
                if (userId == null && requestBody.length() > 0) {
                    try {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> requestData = objectMapper.readValue(requestBody, 
                            objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, Object.class));
                        
                        Object userIdFromBody = requestData.get("userId");
                        if (userIdFromBody != null) {
                            userId = userIdFromBody instanceof Integer ? (Integer) userIdFromBody : Integer.valueOf(userIdFromBody.toString());
                        }
                    } catch (Exception e) {
                        // 如果解析失败，忽略
                    }
                }
                
                if (userId == null) {
                    System.err.println("=== UserProfileServlet: userId is null. Session userId: " + userIdObj + ", Param userId: " + userIdParam + ", Request body: " + requestBody.substring(0, Math.min(200, requestBody.length())));
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.print("{\"success\":false,\"error\":\"未登录，请先登录\"}");
                    return;
                }
                
                System.out.println("=== UserProfileServlet: Updating user with userId: " + userId);

                // 解析请求体
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> requestData = requestBody.length() > 0 ? 
                    objectMapper.readValue(requestBody, 
                        objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, Object.class)) :
                    new java.util.HashMap<>();
                
                // 获取用户信息
                User user = userService.getUserById(userId);
                if (user == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"success\":false,\"error\":\"用户未找到\"}");
                    return;
                }
                
                // 更新用户信息
                if (requestData.containsKey("user_name")) {
                    user.setUserName(requestData.get("user_name").toString());
                }
                if (requestData.containsKey("email")) {
                    user.setEmail(requestData.get("email").toString());
                }
                // 支持 newPassword 和 new_password 两种字段名
                Object passwordObj = requestData.get("newPassword");
                if (passwordObj == null) {
                    passwordObj = requestData.get("new_password");
                }
                if (passwordObj != null && passwordObj.toString().trim().length() > 0) {
                    String newPassword = passwordObj.toString();
                    if (!newPassword.trim().isEmpty()) {
                        // 更新密码
                        userService.updatePassword(userId, newPassword);
                    }
                }
                
                // 更新基本信息
                User updatedUser = userService.updateUser(user);
                if (updatedUser != null) {
                    java.util.Map<String, Object> result = new java.util.HashMap<>();
                    result.put("success", true);
                    result.put("message", "更新成功");
                    out.print(objectMapper.writeValueAsString(result));
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"error\":\"更新失败\"}");
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"error\":\"更新失败: " + e.getMessage() + "\"}");
                e.printStackTrace();
            }
            return;
        }
        
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        out.print("{\"error\":\"路径不存在\"}");
    }
}
