package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.RepostService;
import com.petblog.model.Repost;
import com.petblog.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/api/reposts/*")
public class RepostServlet extends HttpServlet {
    private final RepostService repostService = new RepostService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理 /api/reposts/init 请求
        if (pathInfo != null && pathInfo.equals("/init")) {
            out.print("{\"message\":\"Reposts表初始化完成\",\"success\":true}");
            return;
        }
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // 根据原博客ID或用户ID获取转发列表
            String originalBlogIdParam = request.getParameter("originalBlogId");
            String userIdParam = request.getParameter("userId");
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");

            try {
                int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

                if (originalBlogIdParam != null) {
                    Integer originalBlogId = Integer.valueOf(originalBlogIdParam);
                    List<Repost> reposts = repostService.getRepostsByOriginalBlogId(originalBlogId, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(reposts));
                } else if (userIdParam != null) {
                    Integer userId = Integer.valueOf(userIdParam);
                    List<Repost> reposts = repostService.getRepostsByUserId(userId, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(reposts));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定originalBlogId或userId参数\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
            }
        } else {
            // 检查用户是否已转发过指定博客 /api/reposts/check/{userId}/{originalBlogId}
            String[] splits = pathInfo.split("/");
            if (splits.length >= 3 && "check".equals(splits[1])) {
                try {
                    if (splits.length < 4) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"路径格式错误，应为 /api/reposts/check/{userId}/{originalBlogId}\"}");
                        return;
                    }
                    Integer userId = Integer.valueOf(splits[2]);
                    Integer originalBlogId = Integer.valueOf(splits[3]);

                    boolean hasReposted = repostService.hasUserReposted(userId, originalBlogId);
                    out.print(objectMapper.writeValueAsString(hasReposted));
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"ID格式错误\"}");
                }
                return;
            }

            // 根据转发ID获取转发详情
            try {
                if (splits.length < 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid repost ID\"}");
                    return;
                }
                Integer repostId = Integer.valueOf(splits[1]);

                Repost repost = repostService.getRepostById(repostId);
                if (repost == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Repost not found\"}");
                } else {
                    out.print(objectMapper.writeValueAsString(repost));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid repost ID format\"}");
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
            // 将JSON转换为Repost对象
            Repost repost = objectMapper.readValue(sb.toString(), Repost.class);

            // 设置默认转发时间
            if (repost.getRepostsTime() == null) {
                repost.setRepostsTime(LocalDateTime.now());
            }

            // 调用RepostService创建转发的方法
            Integer result = repostService.createRepost(repost);
            if (result > 0) {
                repost.setRepostId(result);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(repost));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建转发失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid repost data\"}");
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
            out.print("{\"error\":\"需要指定转发ID\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid repost ID\"}");
                return;
            }
            Integer repostId = Integer.valueOf(splits[1]);

            // 读取请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 解析请求体中的转发说明内容
            Repost updateRequest = objectMapper.readValue(sb.toString(), Repost.class);

            // 调用RepostService更新转发说明内容的方法
            boolean result = repostService.updateRepostContent(repostId, updateRequest.getRepostId().toString());
            if (result) {
                out.print("{\"message\":\"转发说明内容更新成功\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"更新转发说明内容失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid repost ID format\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid request data\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 批量删除操作
            String userIdParam = request.getParameter("userId");
            String originalBlogIdParam = request.getParameter("originalBlogId");

            try {
                if (userIdParam != null) {
                    Integer userId = Integer.valueOf(userIdParam);
                    boolean result = repostService.deleteRepostsByUserId(userId);
                    if (result) {
                        out.print("{\"message\":\"用户所有转发记录已删除\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"删除用户所有转发记录失败\"}");
                    }
                } else if (originalBlogIdParam != null) {
                    Integer originalBlogId = Integer.valueOf(originalBlogIdParam);
                    boolean result = repostService.deleteRepostsByOriginalBlogId(originalBlogId);
                    if (result) {
                        out.print("{\"message\":\"博客所有转发记录已删除\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"删除博客所有转发记录失败\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定userId或originalBlogId参数\"}");
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
                    out.print("{\"error\":\"Invalid repost ID\"}");
                    return;
                }
                Integer repostId = Integer.valueOf(splits[1]);

                // 调用RepostService删除转发记录的方法
                boolean result = repostService.deleteRepost(repostId);
                if (result) {
                    out.print("{\"message\":\"转发记录删除成功\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"转发记录未找到或删除失败\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid repost ID format\"}");
            }
        }
    }
}
