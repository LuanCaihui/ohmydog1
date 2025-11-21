package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.MessageService;
import com.petblog.model.Message;
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

@WebServlet("/api/messages/*")
public class MessageServlet extends HttpServlet {
    private final MessageService messageService = new MessageService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 获取当前用户的所有私信对话列表
            // 需要从session或token中获取当前用户ID
            Integer userId = (Integer) request.getSession().getAttribute("userId");
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\":\"未登录\"}");
                return;
            }

            List<Message> conversations = messageService.getConversationList(userId);
            out.print(objectMapper.writeValueAsString(conversations));
        } else {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid message path\"}");
                return;
            }

            // 检查是否是获取聊天记录的请求 /api/messages/chat/{fromUserId}/{toUserId}
            if ("chat".equals(splits[1]) && splits.length >= 4) {
                try {
                    Integer fromUserId = Integer.valueOf(splits[2]);
                    Integer toUserId = Integer.valueOf(splits[3]);
                    String pageNumParam = request.getParameter("pageNum");
                    String pageSizeParam = request.getParameter("pageSize");

                    int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                    int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

                    List<Message> chatRecords = messageService.getChatRecords(fromUserId, toUserId, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(chatRecords));
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid user ID format\"}");
                }
                return;
            }

            // 获取单条消息详情
            try {
                Integer messageId = Integer.valueOf(splits[1]);

                Message message = messageService.getMessageById(messageId);
                if (message == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Message not found\"}");
                } else {
                    out.print(objectMapper.writeValueAsString(message));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid message ID format\"}");
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
            // 将JSON转换为Message对象
            Message message = objectMapper.readValue(sb.toString(), Message.class);

            // 设置默认值
            if (message.getCreationTime() == null) {
                message.setCreationTime(new Date());
            }
            if (message.getUpdateTime() == null) {
                message.setUpdateTime(new Date());
            }
            if (message.getIsWithdraw() == null) {
                message.setIsWithdraw(0); // 默认未撤回
            }
            if (message.getIsRead() == null) {
                message.setIsRead(0); // 默认未读
            }

            // 调用MessageService创建消息的方法
            Integer result = messageService.sendMessage(message);
            if (result > 0) {
                message.setMessageId(result);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(message));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"发送消息失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid message data\"}");
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
            out.print("{\"error\":\"需要指定操作类型\"}");
            return;
        }

        String[] splits = pathInfo.split("/");
        if (splits.length < 2) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid path\"}");
            return;
        }

        // 标记对话为已读 /api/messages/read/{fromUserId}/{toUserId}
        if ("read".equals(splits[1]) && splits.length >= 4) {
            try {
                Integer fromUserId = Integer.valueOf(splits[2]);
                Integer toUserId = Integer.valueOf(splits[3]);

                boolean result = messageService.markConversationAsRead(fromUserId, toUserId);
                if (result) {
                    out.print("{\"message\":\"对话已标记为已读\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"标记对话为已读失败\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid user ID format\"}");
            }
            return;
        }

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"error\":\"不支持的操作\"}");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 清空对话 /api/messages?userId1={userId1}&userId2={userId2}
            String userId1Param = request.getParameter("userId1");
            String userId2Param = request.getParameter("userId2");

            try {
                if (userId1Param != null && userId2Param != null) {
                    Integer userId1 = Integer.valueOf(userId1Param);
                    Integer userId2 = Integer.valueOf(userId2Param);

                    boolean result = messageService.clearConversation(userId1, userId2);
                    if (result) {
                        out.print("{\"message\":\"对话已清空\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"清空对话失败\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定userId1和userId2参数\"}");
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
                    out.print("{\"error\":\"Invalid message ID\"}");
                    return;
                }
                Integer messageId = Integer.valueOf(splits[1]);

                // 调用MessageService删除消息的方法
                boolean result = messageService.deleteMessage(messageId);
                if (result) {
                    out.print("{\"message\":\"消息已删除\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"消息未找到或删除失败\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid message ID format\"}");
            }
        }
    }
}
