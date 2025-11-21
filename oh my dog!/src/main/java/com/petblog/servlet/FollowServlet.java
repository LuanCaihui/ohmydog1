package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.FollowService;
import com.petblog.model.Follow;
import com.petblog.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

@WebServlet("/api/follows/*")
public class FollowServlet extends HttpServlet {
    private final FollowService followService = new FollowService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 从请求体获取关注信息
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 解析关注信息
            Follow follow = objectMapper.readValue(sb.toString(), Follow.class);

            // 如果没有设置关注时间，则使用当前时间
            if (follow.getFollowTime() == null) {
                follow.setFollowTime(new Date());
            }

            // 调用FollowService创建关注关系的方法
            boolean result = followService.followUser(follow);
            if (result) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(follow));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"关注失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"关注失败\"}");
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
            out.print("{\"error\":\"需要指定关注者ID和被关注者ID\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 3) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"路径格式错误，应为 /api/follows/{followerId}/{followeeId}\"}");
                return;
            }
            Integer followerId = Integer.valueOf(splits[1]);
            Integer followeeId = Integer.valueOf(splits[2]);

            // 调用FollowService删除关注关系的方法
            boolean result = followService.unfollowUser(followerId, followeeId);
            if (result) {
                out.print("{\"message\":\"取消关注成功\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"关注关系未找到或取消关注失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"ID格式错误\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理 /api/follows/init 请求
        if (pathInfo != null && pathInfo.equals("/init")) {
            out.print("{\"message\":\"Follows表初始化完成\",\"success\":true}");
            return;
        }

        // 获取查询参数
        String userIdParam = request.getParameter("userId");
        String typeParam = request.getParameter("type"); // following 或 follower
        String pageNumParam = request.getParameter("pageNum");
        String pageSizeParam = request.getParameter("pageSize");

        try {
            if (userIdParam != null) {
                Integer userId = Integer.valueOf(userIdParam);
                int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

                if ("follower".equals(typeParam)) {
                    // 获取粉丝列表
                    java.util.List<Integer> followerIds = followService.getFollowerIds(userId, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(followerIds));
                } else {
                    // 获取关注列表
                    java.util.List<Integer> followingIds = followService.getFollowingIds(userId, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(followingIds));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"需要指定用户ID\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"参数格式错误\"}");
        }
    }
}
