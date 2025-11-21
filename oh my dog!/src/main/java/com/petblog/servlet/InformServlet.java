package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.InformService;
import com.petblog.model.Inform;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/api/informs/*")
public class InformServlet extends HttpServlet {
    private final InformService informService = new InformService();
    private final ObjectMapper objectMapper = com.petblog.util.JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 根据用户ID获取通知列表
            String userIdParam = request.getParameter("userId");
            String unreadParam = request.getParameter("unread");
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");

            try {
                if (userIdParam != null) {
                    Integer userId = Integer.valueOf(userIdParam);
                    int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                    int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

                    if ("true".equals(unreadParam)) {
                        List<Inform> informs = informService.getUnreadInformsByUserId(userId);
                        out.print(objectMapper.writeValueAsString(informs));
                    } else {
                        List<Inform> informs = informService.getInformsByUserId(userId, pageNum, pageSize);
                        out.print(objectMapper.writeValueAsString(informs));
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定userId参数\"}");
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
                    out.print("{\"error\":\"Invalid inform ID\"}");
                    return;
                }
                Integer informId = Integer.valueOf(splits[1]);

                Inform inform = informService.getInformById(informId);
                if (inform == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Inform not found\"}");
                } else {
                    out.print(objectMapper.writeValueAsString(inform));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid inform ID format\"}");
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
            // 将JSON转换为Inform对象
            Inform inform = objectMapper.readValue(sb.toString(), Inform.class);

            // 设置默认值
            if (inform.getInformTime() == null) {
                inform.setInformTime(new Date());
            }
            if (inform.getIsReaded() == null) {
                inform.setIsReaded(0); // 默认未读
            }

            // 调用InformService创建通知的方法
            Integer result = informService.createInform(inform);
            if (result > 0) {
                inform.setInformId(result);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(inform));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建通知失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid inform data\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 批量标记为已读
            String userIdParam = request.getParameter("userId");
            String markAllReadParam = request.getParameter("markAllRead");

            if (userIdParam != null && "true".equals(markAllReadParam)) {
                try {
                    Integer userId = Integer.valueOf(userIdParam);
                    boolean result = informService.markAllInformsAsRead(userId);
                    if (result) {
                        out.print("{\"message\":\"所有通知已标记为已读\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"标记通知为已读失败\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"用户ID格式错误\"}");
                }
                return;
            }

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"需要指定userId参数和markAllRead=true\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid inform ID\"}");
                return;
            }
            Integer informId = Integer.valueOf(splits[1]);

            // 调用InformService将通知标记为已读的方法
            boolean result = informService.markInformAsRead(informId);
            if (result) {
                out.print("{\"message\":\"通知已标记为已读\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"通知未找到或标记失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid inform ID format\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 批量删除通知或清空用户所有通知
            String userIdParam = request.getParameter("userId");
            String clearAllParam = request.getParameter("clearAll");
            String informIdsParam = request.getParameter("informIds");

            try {
                if (userIdParam != null && "true".equals(clearAllParam)) {
                    // 清空用户所有通知
                    Integer userId = Integer.valueOf(userIdParam);
                    boolean result = informService.clearAllInformsByUserId(userId);
                    if (result) {
                        out.print("{\"message\":\"用户所有通知已清空\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"清空通知失败\"}");
                    }
                } else if (informIdsParam != null) {
                    // 批量删除通知
                    List<Integer> informIds = Arrays.stream(informIdsParam.split(","))
                            .map(Integer::valueOf)
                            .collect(Collectors.toList());
                    boolean result = informService.batchDeleteInforms(informIds);
                    if (result) {
                        out.print("{\"message\":\"批量删除成功\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"批量删除失败\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定userId和clearAll=true，或指定informIds参数\"}");
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
                    out.print("{\"error\":\"Invalid inform ID\"}");
                    return;
                }
                Integer informId = Integer.valueOf(splits[1]);

                // 调用InformService删除通知的方法
                boolean result = informService.deleteInform(informId);
                if (result) {
                    out.print("{\"message\":\"通知删除成功\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"通知未找到或删除失败\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid inform ID format\"}");
            }
        }
    }
}
