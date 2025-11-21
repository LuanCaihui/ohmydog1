package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.CreateColumnService;
import com.petblog.model.CreateColumn;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/create-columns/*")
public class CreateColumnServlet extends HttpServlet {
    private final CreateColumnService createColumnService = new CreateColumnService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 获取创建专栏申请列表
            String userIdParam = request.getParameter("userId");
            String columnIdParam = request.getParameter("columnId");
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");

            try {
                int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

                if (userIdParam != null) {
                    // 根据用户ID查询其创建的所有专栏ID
                    Integer userId = Integer.valueOf(userIdParam);
                    List<Integer> columnIds = createColumnService.getColumnIdsByCreatorId(userId);
                    out.print(objectMapper.writeValueAsString(columnIds));
                } else if (columnIdParam != null) {
                    // 根据专栏ID查询所有创建者ID
                    Integer columnId = Integer.valueOf(columnIdParam);
                    List<Integer> creatorIds = createColumnService.getCreatorIdsByColumnId(columnId);
                    out.print(objectMapper.writeValueAsString(creatorIds));
                } else {
                    // 返回默认信息，因为没有合适的查询方法
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"必须提供userId或columnId参数\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
            }
        } else {
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length < 3) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"路径格式错误，应为 /api/create-columns/{userId}/{columnId}\"}");
                    return;
                }
                Integer userId = Integer.valueOf(splits[1]);
                Integer columnId = Integer.valueOf(splits[2]);

                // 检查用户是否为专栏的创建者
                boolean isCreator = createColumnService.isUserCreatorOfColumn(userId, columnId);
                out.print(objectMapper.writeValueAsString(isCreator));
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
            // 将JSON转换为CreateColumn对象
            CreateColumn createColumn = objectMapper.readValue(sb.toString(), CreateColumn.class);

            // 调用CreateColumnService创建专栏申请的方法
            boolean result = createColumnService.createColumnCreator(createColumn);
            if (result) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(createColumn));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建专栏关联失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid create-column data\"}");
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
            out.print("{\"error\":\"需要提供用户ID和专栏ID路径\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 3) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"路径格式错误，应为 /api/create-columns/{userId}/{columnId}\"}");
                return;
            }
            Integer userId = Integer.valueOf(splits[1]);
            Integer columnId = Integer.valueOf(splits[2]);

            // 创建CreateColumn对象
            CreateColumn createColumn = new CreateColumn();
            createColumn.setUserId(userId);
            createColumn.setColumnId(columnId);

            // 调用CreateColumnService更新专栏申请的方法（这里实现为添加关联）
            boolean result = createColumnService.createColumnCreator(createColumn);
            if (result) {
                out.print(objectMapper.writeValueAsString(createColumn));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"更新专栏关联失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"ID格式错误\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid create-column data\"}");
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
            out.print("{\"error\":\"需要提供用户ID和专栏ID路径\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 3) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"路径格式错误，应为 /api/create-columns/{userId}/{columnId}\"}");
                return;
            }
            Integer userId = Integer.valueOf(splits[1]);
            Integer columnId = Integer.valueOf(splits[2]);

            // 调用CreateColumnService移除用户与专栏的创建关联关系
            boolean result = createColumnService.removeColumnCreator(userId, columnId);
            if (result) {
                out.print("{\"message\":\"专栏创建者关联已移除\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"未找到对应的专栏创建者关联或移除失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"ID格式错误\"}");
        }
    }
}
