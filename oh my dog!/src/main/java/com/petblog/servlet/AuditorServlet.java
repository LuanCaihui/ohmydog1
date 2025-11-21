package com.petblog.servlet;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.AuditorService;
import com.petblog.model.Auditor;
import com.petblog.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

// src/main/java/com/petblog/servlet/AuditorServlet.java
@WebServlet("/api/auditors/*")
public class AuditorServlet extends HttpServlet {
    private final AuditorService auditorService = new AuditorService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 获取审核员列表或审核记录
            String statusParam = request.getParameter("status");
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");

            try {
                int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

                // 调用AuditorService获取审核记录的方法
                List<Auditor> auditors = auditorService.getAuditorsByStatus(statusParam);
                out.print(objectMapper.writeValueAsString(auditors));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
            }
        } else {
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length < 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid auditor ID\"}");
                    return;
                }
                Integer auditorId = Integer.valueOf(splits[1]);

                // 调用AuditorService获取审核员详情的方法
                Auditor auditor = auditorService.getAuditorById(auditorId);
                if (auditor == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Auditor not found\"}");
                } else {
                    out.print(objectMapper.writeValueAsString(auditor));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid auditor ID format\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理登录请求
        if (pathInfo != null && pathInfo.equals("/login")) {
            try {
                // 读取请求体
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                
                String requestBody = sb.toString();
                if (requestBody == null || requestBody.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"请求体不能为空\"}");
                    return;
                }

                // 解析JSON（期望格式：{"name":"xxx","password":"xxx"}）
                @SuppressWarnings("unchecked")
                java.util.Map<String, String> loginData = objectMapper.readValue(requestBody, 
                    objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, String.class));
                
                String name = loginData != null ? loginData.get("name") : null;
                String password = loginData != null ? loginData.get("password") : null;

                if (name == null || password == null || name.trim().isEmpty() || password.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"账号和密码不能为空\"}");
                    return;
                }

                // 调用登录验证
                Auditor auditor = auditorService.login(name, password);
                if (auditor != null) {
                    // 登录成功
                    out.print(objectMapper.writeValueAsString(auditor));
                } else {
                    // 登录失败
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.print("{\"error\":\"账号或密码错误\"}");
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"登录请求格式错误\"}");
                e.printStackTrace();
            }
            return;
        }

        // 读取请求体
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        try {
            // 将JSON转换为Auditor对象
            Auditor auditor = objectMapper.readValue(sb.toString(), Auditor.class);

            // 调用AuditorService创建审核员的方法
            boolean result = auditorService.createAuditor(auditor);
            if (result) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(auditor));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建审核员失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid auditor data\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Auditor ID is required\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid auditor ID\"}");
                return;
            }
            Integer auditorId = Integer.valueOf(splits[1]);

            // 读取请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 将JSON转换为Auditor对象
            Auditor auditor = objectMapper.readValue(sb.toString(), Auditor.class);
            auditor.setId(auditorId); // 确保ID一致

            // 调用AuditorService更新审核员的方法
            boolean result = auditorService.updateAuditor(auditor);
            if (result) {
                out.print(objectMapper.writeValueAsString(auditor));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"更新审核员失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid auditor ID format\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid auditor data\"}");
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
            out.print("{\"error\":\"Auditor ID is required\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid auditor ID\"}");
                return;
            }
            Integer auditorId = Integer.valueOf(splits[1]);

            // 调用AuditorService删除审核员的方法
            boolean result = auditorService.deleteAuditor(auditorId);
            if (result) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                out.print("{\"message\":\"审核员删除成功\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"删除审核员失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid auditor ID format\"}");
        }
    }
}

