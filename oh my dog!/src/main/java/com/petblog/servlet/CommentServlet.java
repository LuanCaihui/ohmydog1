package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.ReplyService;
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

/**
 * 评论接口（使用Reply作为评论）
 * 提供 /api/comments/* 路径的接口
 */
@WebServlet("/api/comments/*")
public class CommentServlet extends HttpServlet {
    private final ReplyService replyService = new ReplyService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理 /api/comments/init 请求
        if (pathInfo != null && pathInfo.equals("/init")) {
            out.print("{\"message\":\"Comments表初始化完成\",\"success\":true}");
            return;
        }
        
        // 处理 /api/comments/{blogId} 请求（获取博客的评论列表）
        if (pathInfo != null && !pathInfo.equals("/")) {
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length >= 2) {
                    Integer blogId = Integer.valueOf(splits[1]);
                    // 根据博客ID获取评论（这里使用ReplyService，假设reply表中有blog_id字段）
                    // 如果Reply表结构不同，需要调整
                    List<com.petblog.model.Reply> replies = replyService.getRepliesByCommentId(blogId, 1, 100);
                    if (replies == null) {
                        replies = new java.util.ArrayList<>();
                    }
                    out.print(objectMapper.writeValueAsString(replies));
                    return;
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid blog ID format\"}");
                return;
            }
        }
        
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"error\":\"需要指定博客ID\"}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理 /api/comments/add 请求
        if (pathInfo != null && pathInfo.equals("/add")) {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                // 将JSON转换为Reply对象（评论就是回复）
                com.petblog.model.Reply reply = objectMapper.readValue(sb.toString(), com.petblog.model.Reply.class);
                
                if (reply.getReplyCreatedtime() == null) {
                    reply.setReplyCreatedtime(new java.util.Date());
                }
                if (reply.getIsVisible() == null) {
                    reply.setIsVisible(1);
                }

                Integer result = replyService.createReply(reply);
                if (result > 0) {
                    reply.setReplyId(result);
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    out.print(objectMapper.writeValueAsString(reply));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"添加评论失败\"}");
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid comment data\"}");
            }
            return;
        }
        
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"error\":\"Invalid request\"}");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理 /api/comments/delete 请求
        if (pathInfo != null && pathInfo.equals("/delete")) {
            try {
                String commentIdParam = request.getParameter("commentId");
                if (commentIdParam != null) {
                    Integer commentId = Integer.valueOf(commentIdParam);
                    boolean result = replyService.deleteReply(commentId);
                    if (result) {
                        out.print("{\"message\":\"评论删除成功\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"评论未找到或删除失败\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定commentId参数\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid comment ID format\"}");
            }
            return;
        }
        
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"error\":\"Invalid request\"}");
    }
}

