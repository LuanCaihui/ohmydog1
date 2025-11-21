package com.petblog.servlet;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.BlogChallengeService;
import com.petblog.model.BlogChallenge;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/blog-challenges/*")
public class BlogChallengeServlet extends HttpServlet {
    private final BlogChallengeService blogChallengeService = new BlogChallengeService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    response.setContentType("application/json;charset=UTF-8");
    PrintWriter out = response.getWriter();

    String pathInfo = request.getPathInfo();
    if (pathInfo == null || pathInfo.equals("/")) {
        // 根据挑战ID或博客ID获取关联信息
        String challengeIdParam = request.getParameter("challengeId");
        String blogIdParam = request.getParameter("blogId");

        try {
            if (challengeIdParam != null) {
                Integer challengeId = Integer.valueOf(challengeIdParam);
                // 根据挑战ID获取所有关联记录
                List<BlogChallenge> list = blogChallengeService.getBlogChallengesByChallengeId(challengeId);
                out.print(objectMapper.writeValueAsString(list));
            } else if (blogIdParam != null) {
                Integer blogId = Integer.valueOf(blogIdParam);
                // 根据博客ID获取所有关联记录
                List<BlogChallenge> list = blogChallengeService.getBlogChallengesByBlogId(blogId);
                out.print(objectMapper.writeValueAsString(list));
            } else {
                // 无筛选参数则返回全部
                List<BlogChallenge> list = blogChallengeService.getAllBlogChallenges();
                out.print(objectMapper.writeValueAsString(list));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"参数格式错误\"}");
        }
    } else {
        // /{id} 获取单条记录
        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2 || splits[1].isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"无效的路径\"}");
                return;
            }
            Integer id = Integer.valueOf(splits[1]);
            BlogChallenge item = blogChallengeService.getBlogChallengeById(id);
            if (item == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"记录不存在\"}");
            } else {
                out.print(objectMapper.writeValueAsString(item));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"ID格式错误\"}");
        }
    }
}

@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    response.setContentType("application/json;charset=UTF-8");
    PrintWriter out = response.getWriter();

    // 读取请求体
    StringBuilder sb = new StringBuilder();
    BufferedReader reader = request.getReader();
    String line;
    while ((line = reader.readLine()) != null) {
        sb.append(line);
    }

    try {
        // 将JSON转换为BlogChallenge对象
        BlogChallenge blogChallenge = objectMapper.readValue(sb.toString(), BlogChallenge.class);

        // 调用BlogChallengeService创建博客挑战关联的方法
        boolean result = blogChallengeService.createBlogChallenge(blogChallenge);
        if (result) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(objectMapper.writeValueAsString(blogChallenge));
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"创建博客挑战关联失败\"}");
        }
    } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"error\":\"Invalid blog-challenge data\"}");
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
        out.print("{\"error\":\"需要在路径中提供要删除的ID，例如 /api/blog-challenges/{id}\"}");
        return;
    }

    try {
        // 解析路径 /api/blog-challenges/{id}
        String[] splits = pathInfo.split("/");
        if (splits.length < 2 || splits[1].isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"无效的路径\"}");
            return;
        }
        Integer id = Integer.valueOf(splits[1]);

        // 调用BlogChallengeService删除博客挑战关联的方法
        boolean result = blogChallengeService.deleteBlogChallenge(id);
        if (result) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"删除博客挑战关联失败\"}");
        }
    } catch (NumberFormatException e) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"error\":\"ID格式错误\"}");
    }
}

}

