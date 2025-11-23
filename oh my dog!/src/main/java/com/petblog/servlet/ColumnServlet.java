package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.ColumnService;
import com.petblog.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.petblog.model.Column;
import java.util.List;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/columns/*")
public class ColumnServlet extends HttpServlet {
    private final ColumnService columnService = new ColumnService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    response.setContentType("application/json;charset=UTF-8");
    PrintWriter out = response.getWriter();

    String pathInfo = request.getPathInfo();
    
    // 处理 /api/columns/my 请求（获取当前用户的专栏）
    if (pathInfo != null && pathInfo.equals("/my")) {
        try {
            // 从sessionStorage获取当前用户ID（这里简化处理，实际应该从session获取）
            String userIdParam = request.getParameter("userId");
            if (userIdParam != null) {
                Integer userId = Integer.valueOf(userIdParam);
                List<Column> columns = columnService.getColumnsByCreatorId(userId);
                if (columns == null) {
                    columns = new java.util.ArrayList<>();
                }
                out.print(objectMapper.writeValueAsString(columns));
            } else {
                // 如果没有userId参数，返回空数组
                out.print("[]");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"获取用户专栏失败\"}");
        }
        return;
    }
    
    if (pathInfo == null || pathInfo.equals("/")) {
        // 获取专栏列表
        String userIdParam = request.getParameter("userId");
        String pageNumParam = request.getParameter("pageNum");
        String pageSizeParam = request.getParameter("pageSize");

        try {
            int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
            int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

            if (userIdParam != null) {
                Integer userId = Integer.valueOf(userIdParam);
                // 调用ColumnService获取用户专栏列表的方法
                List<Column> columns = columnService.getColumnsByCreatorId(userId);
                out.print(objectMapper.writeValueAsString(columns));
            } else {
                // 调用ColumnService获取所有专栏列表的方法
                List<Column> columns = columnService.searchColumnsByName("", pageNum, pageSize);
                out.print(objectMapper.writeValueAsString(columns));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"参数格式错误\"}");
        }
    } else {
        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid column ID\"}");
                return;
            }
            Integer columnId = Integer.valueOf(splits[1]);
            
            // 处理 /api/columns/{columnId}/blogs 路径
            if (splits.length >= 3 && "blogs".equals(splits[2])) {
                // 获取专栏中的博客列表
                com.petblog.Service.BlogColumnService blogColumnService = new com.petblog.Service.BlogColumnService();
                java.util.List<com.petblog.model.BlogColumn> blogColumns = blogColumnService.getBlogColumnsByColumnId(columnId);
                
                if (blogColumns == null || blogColumns.isEmpty()) {
                    out.print("[]");
                    return;
                }
                
                // 获取博客详情
                com.petblog.Service.BlogService blogService = new com.petblog.Service.BlogService();
                java.util.List<java.util.Map<String, Object>> blogsWithDetails = new java.util.ArrayList<>();
                
                for (com.petblog.model.BlogColumn blogColumn : blogColumns) {
                    com.petblog.model.Blog blog = blogService.getBlogById(blogColumn.getBlogId());
                    if (blog != null) {
                        java.util.Map<String, Object> blogMap = new java.util.HashMap<>();
                        blogMap.put("blog_id", blog.getBlogId());
                        blogMap.put("blog_title", blog.getBlogTitle());
                        blogMap.put("blog_content", blog.getBlogContent());
                        blogMap.put("blog_create_time", blog.getBlogCreateTime());
                        blogMap.put("user_id", blog.getUserId());
                        blogMap.put("user_name", blog.getUserName());
                        blogsWithDetails.add(blogMap);
                    }
                }
                
                out.print(objectMapper.writeValueAsString(blogsWithDetails));
                return;
            }

            // 调用ColumnService获取专栏详情的方法
            Column column = columnService.getColumnById(columnId);
            if (column != null) {
                out.print(objectMapper.writeValueAsString(column));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"专栏未找到\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid column ID format\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"获取专栏博客失败: " + e.getMessage() + "\"}");
            e.printStackTrace();
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
        // 将JSON转换为Column对象
        Column column = objectMapper.readValue(sb.toString(), Column.class);

        // 调用ColumnService创建专栏的方法
        Integer result = columnService.createColumn(column);
        if (result > 0) {
            column.setColumnId(result);
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(objectMapper.writeValueAsString(column));
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"创建专栏失败\"}");
        }
    } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"error\":\"Invalid column data\"}");
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
        out.print("{\"error\":\"Column ID is required\"}");
        return;
    }

    try {
        String[] splits = pathInfo.split("/");
        if (splits.length < 2) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid column ID\"}");
            return;
        }
        Integer columnId = Integer.valueOf(splits[1]);

        // 读取请求体
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        // 将JSON转换为Column对象
        Column column = objectMapper.readValue(sb.toString(), Column.class);
        column.setColumnId(columnId); // 确保ID一致

        // 调用ColumnService更新专栏的方法
        boolean result = columnService.updateColumn(column);
        if (result) {
            out.print(objectMapper.writeValueAsString(column));
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"更新专栏失败\"}");
        }
    } catch (NumberFormatException e) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"error\":\"Invalid column ID format\"}");
    } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"error\":\"Invalid column data\"}");
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
        out.print("{\"error\":\"Column ID is required\"}");
        return;
    }

    try {
        String[] splits = pathInfo.split("/");
        if (splits.length < 2) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid column ID\"}");
            return;
        }
        Integer columnId = Integer.valueOf(splits[1]);

        // 调用ColumnService删除专栏的方法
        boolean result = columnService.deleteColumn(columnId);
        if (result) {
            out.print("{\"message\":\"专栏删除成功\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"专栏未找到或删除失败\"}");
        }
    } catch (NumberFormatException e) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"error\":\"Invalid column ID format\"}");
    }
}

}

