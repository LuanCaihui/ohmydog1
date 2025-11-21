package com.petblog.servlet;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.BlogTopicService;
import com.petblog.model.BlogTopic;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/blog-topics/*")
public class BlogTopicServlet extends HttpServlet {
    private final BlogTopicService blogTopicService = new BlogTopicService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 根据话题ID或博客ID获取关联信息
            String topicIdParam = request.getParameter("topicId");
            String blogIdParam = request.getParameter("blogId");
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");

            try {
                if (topicIdParam != null) {
                    Integer topicId = Integer.valueOf(topicIdParam);
                    int pageNum = pageNumParam == null ? 1 : Integer.parseInt(pageNumParam);
                    int pageSize = pageSizeParam == null ? 10 : Integer.parseInt(pageSizeParam);
                    java.util.List<Integer> blogIds = blogTopicService.getBlogIdsByTopicId(topicId, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(blogIds));
                } else if (blogIdParam != null) {
                    Integer blogId = Integer.valueOf(blogIdParam);
                    java.util.List<Integer> topicIds = blogTopicService.getTopicIdsByBlogId(blogId);
                    out.print(objectMapper.writeValueAsString(topicIds));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定topicId或blogId参数\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"该接口不支持根据ID获取单个记录\"}");
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
            // 将JSON转换为BlogTopic对象
            BlogTopic blogTopic = objectMapper.readValue(sb.toString(), BlogTopic.class);

            // 调用BlogTopicService创建博客话题关联的方法
            boolean result = blogTopicService.createBlogTopic(blogTopic);
            if (result) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(blogTopic));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建博客话题关联失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid blog-topic data\"}");
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
            out.print("{\"error\":\"需要指定博客和话题的关联信息\"}");
            return;
        }

        try {
            // 解析路径 /api/blog-topics/{blogId}/{topicId}
            String[] splits = pathInfo.split("/");
            if (splits.length < 3) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid blog-topic path\"}");
                return;
            }
            Integer blogId = Integer.valueOf(splits[1]);
            Integer topicId = Integer.valueOf(splits[2]);

            // 调用BlogTopicService删除博客话题关联的方法
            boolean result = blogTopicService.deleteBlogTopic(blogId, topicId);
            if (result) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"删除博客话题关联失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid blog or topic ID format\"}");
        }
    }
}

