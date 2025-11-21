package com.petblog.servlet;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.BlogColumnService;
import com.petblog.model.BlogColumn;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/blog-columns/*")
public class BlogColumnServlet extends HttpServlet {
    private final BlogColumnService blogColumnService = new BlogColumnService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 根据专栏ID或博客ID获取关联信息
            String columnIdParam = request.getParameter("columnId");
            String blogIdParam = request.getParameter("blogId");

            try {
                if (columnIdParam != null) {
                    Integer columnId = Integer.valueOf(columnIdParam);
                    // 根据专栏ID获取所有关联记录
                    java.util.List<BlogColumn> list = blogColumnService.getBlogColumnsByColumnId(columnId);
                    out.print(objectMapper.writeValueAsString(list));
                } else if (blogIdParam != null) {
                    Integer blogId = Integer.valueOf(blogIdParam);
                    // 根据博客ID获取所有关联记录
                    java.util.List<BlogColumn> list = blogColumnService.getBlogColumnsByBlogId(blogId);
                    out.print(objectMapper.writeValueAsString(list));
                } else {
                    // 无筛选参数则返回全部
                    java.util.List<BlogColumn> list = blogColumnService.getAllBlogColumns();
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
                BlogColumn item = blogColumnService.getBlogColumnById(id);
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
            // 将JSON转换为BlogColumn对象
            BlogColumn blogColumn = objectMapper.readValue(sb.toString(), BlogColumn.class);

            // 调用BlogColumnService创建博客专栏关联的方法
            boolean result = blogColumnService.createBlogColumn(blogColumn);
            if (result) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(blogColumn));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建博客专栏关联失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid blog-column data\"}");
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
            out.print("{\"error\":\"需要在路径中提供要删除的ID，例如 /api/blog-columns/{id}\"}");
            return;
        }

        try {
            // 解析路径 /api/blog-columns/{id}
            String[] splits = pathInfo.split("/");
            if (splits.length < 2 || splits[1].isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"无效的路径\"}");
                return;
            }
            Integer id = Integer.valueOf(splits[1]);

            // 调用BlogColumnService删除博客专栏关联的方法
            boolean result = blogColumnService.deleteBlogColumn(id);
            if (result) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"删除博客专栏关联失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"ID格式错误\"}");
        }
    }
}

