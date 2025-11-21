package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.ReplyService;
import com.petblog.model.Reply;
import com.petblog.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

@WebServlet("/api/replies/*")
public class ReplyServlet extends HttpServlet {
    private final ReplyService replyService = new ReplyService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 可以根据查询参数获取特定评论的回复列表
            String commentIdParam = request.getParameter("commentId");
            String userIdParam = request.getParameter("userId");
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");

            try {
                int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

                if (commentIdParam != null) {
                    Integer commentId = Integer.valueOf(commentIdParam);
                    List<Reply> replies = replyService.getRepliesByCommentId(commentId, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(replies));
                } else if (userIdParam != null) {
                    Integer userId = Integer.valueOf(userIdParam);
                    List<Reply> replies = replyService.getRepliesByUserId(userId, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(replies));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"commentId or userId is required\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid parameter format\"}");
            }
        } else {
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length < 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid reply ID\"}");
                    return;
                }
                Integer replyId = Integer.valueOf(splits[1]);

                Reply reply = replyService.getReplyById(replyId);
                if (reply == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Reply not found\"}");
                } else {
                    out.print(objectMapper.writeValueAsString(reply));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid reply ID format\"}");
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
            // 将JSON转换为Reply对象
            Reply reply = objectMapper.readValue(sb.toString(), Reply.class);

            // 设置默认值
            if (reply.getReplyCreatedtime() == null) {
                reply.setReplyCreatedtime(new Date());
            }
            if (reply.getIsVisible() == null) {
                reply.setIsVisible(1); // 默认可见
            }

            // 调用ReplyService创建回复的方法
            Integer result = replyService.createReply(reply);
            if (result > 0) {
                reply.setReplyId(result);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(reply));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建回复失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid reply data\"}");
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
            out.print("{\"error\":\"需要指定回复ID\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid reply ID\"}");
                return;
            }
            Integer replyId = Integer.valueOf(splits[1]);

            // 读取请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 将JSON转换为Reply对象
            Reply reply = objectMapper.readValue(sb.toString(), Reply.class);
            reply.setReplyId(replyId); // 确保ID一致

            // 调用ReplyService更新回复内容的方法
            boolean result = replyService.updateReplyContent(reply);
            if (result) {
                out.print(objectMapper.writeValueAsString(reply));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"更新回复内容失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid reply ID format\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid reply data\"}");
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
            String commentIdParam = request.getParameter("commentId");
            String userIdParam = request.getParameter("userId");

            try {
                if (commentIdParam != null) {
                    Integer commentId = Integer.valueOf(commentIdParam);
                    boolean result = replyService.deleteRepliesByCommentId(commentId);
                    if (result) {
                        out.print("{\"message\":\"评论下所有回复已删除\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"删除评论下所有回复失败\"}");
                    }
                } else if (userIdParam != null) {
                    Integer userId = Integer.valueOf(userIdParam);
                    boolean result = replyService.deleteRepliesByUserId(userId);
                    if (result) {
                        out.print("{\"message\":\"用户所有回复已删除\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"删除用户所有回复失败\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定commentId或userId参数\"}");
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
                    out.print("{\"error\":\"Invalid reply ID\"}");
                    return;
                }
                Integer replyId = Integer.valueOf(splits[1]);

                // 调用ReplyService删除回复的方法
                boolean result = replyService.deleteReply(replyId);
                if (result) {
                    out.print("{\"message\":\"回复已删除\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"回复未找到或删除失败\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid reply ID format\"}");
            }
        }
    }
}
