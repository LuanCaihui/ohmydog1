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
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {
    private final UserService userService = new UserService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 解析URL中的用户ID
        String pathInfo = request.getPathInfo();
        
        // 处理 /api/users/profile 请求（获取当前用户资料）
        if (pathInfo != null && pathInfo.equals("/profile")) {
            try {
                // 从session或请求参数获取当前用户ID（这里简化处理）
                // 实际应该从session中获取
                String userIdParam = request.getParameter("userId");
                if (userIdParam != null) {
                    Integer userId = Integer.valueOf(userIdParam);
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
                } else {
                    // 如果没有userId参数，返回错误
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定userId参数\"}");
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"获取用户资料失败\"}");
            }
            return;
        }
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // 获取所有用户（分页查询）
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");

            try {
                int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

                List<User> users = userService.getAllUsers(pageNum, pageSize);
                out.print(objectMapper.writeValueAsString(users));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid page parameters\"}");
            }
        } else {
            try {
                // 提取用户ID
                String[] splits = pathInfo.split("/");
                if (splits.length < 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid user ID\"}");
                    return;
                }
                Integer userId = Integer.valueOf(splits[1]);

                // 根据ID获取用户
                User user = userService.getUserById(userId);
                if (user == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"User not found\"}");
                } else {
                    out.print(objectMapper.writeValueAsString(user));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid user ID format\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 判断是登录请求还是注册请求
        if (pathInfo != null && pathInfo.equals("/login")) {
            // 登录请求
            try {
                // 读取请求体
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                
                String requestBody = sb.toString();
                
                // 检查请求体是否为空
                if (requestBody == null || requestBody.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"请求体不能为空\"}");
                    return;
                }

                // 解析JSON（期望格式：{"username":"xxx","password":"xxx"}）
                @SuppressWarnings("unchecked")
                java.util.Map<String, String> loginData = objectMapper.readValue(requestBody, 
                    objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, String.class));
                
                String username = loginData != null ? loginData.get("username") : null;
                String password = loginData != null ? loginData.get("password") : null;

                if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"用户名和密码不能为空\"}");
                    return;
                }

                // 调用登录验证
                User user = userService.login(username, password);
                if (user != null) {
                    // 登录成功，设置session
                    request.getSession().setAttribute("userId", user.getUserId());
                    request.getSession().setAttribute("userName", user.getUserName());
                    // 登录成功
                    out.print(objectMapper.writeValueAsString(user));
                } else {
                    // 登录失败
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.print("{\"error\":\"用户名或密码错误\"}");
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                // JSON解析错误
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"JSON格式错误: " + e.getMessage() + "\"}");
                e.printStackTrace(); // 打印到控制台便于调试
            } catch (Exception e) {
                // 其他错误
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"登录请求处理失败: " + e.getMessage() + "\"}");
                e.printStackTrace(); // 打印到控制台便于调试
            }
        } else {
            // 注册请求（创建用户）
            // 读取请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            String requestBody = sb.toString();
            
            // 检查请求体是否为空
            if (requestBody == null || requestBody.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"请求体不能为空\"}");
                return;
            }

            try {
                // 将JSON转换为User对象
                User user = objectMapper.readValue(requestBody, User.class);

                // 验证必填字段
                if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"用户名不能为空\"}");
                    return;
                }
                
                if (user.getUserPassword() == null || user.getUserPassword().trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"密码不能为空\"}");
                    return;
                }
                
                if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"邮箱不能为空\"}");
                    return;
                }

                // 检查用户名是否已存在
                User existingUser = userService.getUserByUsername(user.getUserName());
                if (existingUser != null) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print("{\"error\":\"用户名已存在\"}");
                    return;
                }

                // 检查邮箱是否已存在
                existingUser = userService.getUserByEmail(user.getEmail());
                if (existingUser != null) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print("{\"error\":\"邮箱已被注册\"}");
                    return;
                }

                // 设置默认值
                if (user.getRegistrationDate() == null) {
                    user.setRegistrationDate(LocalDateTime.now());
                }
                if (user.getLastLogin() == null) {
                    user.setLastLogin(LocalDateTime.now());
                }
                if (user.getIsBan() == null) {
                    user.setIsBan(0); // 默认未封禁
                }
                if (user.getUserAvatarPath() == null || user.getUserAvatarPath().trim().isEmpty()) {
                    user.setUserAvatarPath("/images/default-avatar.png");
                }

                // 调用UserService创建用户的方法
                User createdUser = userService.createUser(user);
                if (createdUser != null) {
                    // 创建成功，返回用户信息（不包含密码）
                    User result = new User();
                    result.setUserId(createdUser.getUserId());
                    result.setUserName(createdUser.getUserName());
                    result.setEmail(createdUser.getEmail());
                    result.setUserAvatarPath(createdUser.getUserAvatarPath());
                    result.setRegistrationDate(createdUser.getRegistrationDate());
                    result.setLastLogin(createdUser.getLastLogin());
                    result.setIsBan(createdUser.getIsBan());
                    
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    out.print(objectMapper.writeValueAsString(result));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"注册失败，请稍后重试\"}");
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"JSON格式错误: " + e.getMessage() + "\"}");
                e.printStackTrace();
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"注册失败: " + e.getMessage() + "\"}");
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"User ID is required\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid user ID\"}");
                return;
            }
            Integer userId = Integer.valueOf(splits[1]);

            // 读取请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 将JSON转换为User对象
            User user = objectMapper.readValue(sb.toString(), User.class);
            user.setUserId(userId); // 确保ID一致

            // 调用UserService更新用户的方法
            User updatedUser = userService.updateUser(user);
            if (updatedUser != null) {
                out.print(objectMapper.writeValueAsString(updatedUser));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"User not found\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid user ID format\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid user data\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"User ID is required\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid user ID\"}");
                return;
            }
            Integer userId = Integer.valueOf(splits[1]);

            // 调用UserService删除用户的方法
            boolean result = userService.deleteUser(userId);
            if (result) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                out.print("{\"message\":\"User deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"User not found\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid user ID format\"}");
        }
    }
}
