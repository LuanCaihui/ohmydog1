package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态广场接口
 */
@WebServlet("/api/dynamic-square")
public class DynamicSquareServlet extends HttpServlet {
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 返回动态广场数据（示例数据）
            // 实际应该从数据库获取热门动态
            List<Map<String, Object>> data = new ArrayList<>();
            
            // 示例数据
            Map<String, Object> item1 = new HashMap<>();
            item1.put("id", 1);
            item1.put("type", "blog");
            item1.put("title", "热门动态1");
            item1.put("content", "这是一条热门动态");
            data.add(item1);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", data);
            
            out.print(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"error\":\"获取动态广场失败\"}");
        }
    }
}

