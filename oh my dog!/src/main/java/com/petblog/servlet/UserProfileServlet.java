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

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 用户资料接口
 * 提供 /api/user/profile 路径
 */
@WebServlet("/api/user/profile")
public class UserProfileServlet extends HttpServlet {
    private final UserService userService = new UserService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

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
                // 如果没有userId参数，尝试从sessionStorage获取（前端处理）
                // 这里返回空对象，让前端处理
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"需要指定userId参数\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"获取用户资料失败\"}");
        }
    }
}

