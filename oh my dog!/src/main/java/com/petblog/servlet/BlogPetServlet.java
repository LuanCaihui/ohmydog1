package com.petblog.servlet;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.BlogPetService;
import com.petblog.model.BlogPet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/blog-pets/*")
public class BlogPetServlet extends HttpServlet {
    private final BlogPetService blogPetService = new BlogPetService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 根据宠物ID或博客ID获取关联信息
            String petIdParam = request.getParameter("petId");
            String blogIdParam = request.getParameter("blogId");

            try {
                if (petIdParam != null) {
                    Integer petId = Integer.valueOf(petIdParam);
                    List<Integer> blogIds = blogPetService.getBlogIdsByPetId(petId);
                    out.print(objectMapper.writeValueAsString(blogIds));
                } else if (blogIdParam != null) {
                    Integer blogId = Integer.valueOf(blogIdParam);
                    List<Integer> petIds = blogPetService.getPetIdsByBlogId(blogId);
                    out.print(objectMapper.writeValueAsString(petIds));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定petId或blogId参数\"}");
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
            // 将JSON转换为BlogPet对象
            BlogPet blogPet = objectMapper.readValue(sb.toString(), BlogPet.class);

            // 调用BlogPetService创建博客宠物关联的方法
            boolean result = blogPetService.createBlogPet(blogPet);
            if (result) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(blogPet));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建博客宠物关联失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid blog-pet data\"}");
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
            out.print("{\"error\":\"需要指定博客和宠物的关联信息\"}");
            return;
        }

        try {
            // 解析路径 /api/blog-pets/{blogId}/{petId}
            String[] splits = pathInfo.split("/");
            if (splits.length < 3) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid blog-pet path\"}");
                return;
            }
            Integer blogId = Integer.valueOf(splits[1]);
            Integer petId = Integer.valueOf(splits[2]);

            // 调用BlogPetService删除博客宠物关联的方法
            boolean result = blogPetService.deleteBlogPet(blogId, petId);
            if (result) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"删除博客宠物关联失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid blog or pet ID format\"}");
        }
    }
}

