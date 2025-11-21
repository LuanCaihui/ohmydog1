package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.VoteService;
import com.petblog.model.Vote;
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

@WebServlet("/api/votes/*")
public class VoteServlet extends HttpServlet {
    private final VoteService voteService = new VoteService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 根据目标内容获取投票记录
            String targetTypeParam = request.getParameter("targetType");
            String targetIdParam = request.getParameter("targetId");
            String userIdParam = request.getParameter("userId");

            try {
                if (targetTypeParam != null && targetIdParam != null) {
                    Integer targetType = Integer.valueOf(targetTypeParam);
                    Integer targetId = Integer.valueOf(targetIdParam);
                    List<Vote> votes = voteService.getVotesByTarget(targetType, targetId);
                    out.print(objectMapper.writeValueAsString(votes));
                } else if (userIdParam != null) {
                    Integer userId = Integer.valueOf(userIdParam);
                    String targetTypeStr = request.getParameter("targetType");
                    if (targetTypeStr != null) {
                        Integer targetType = Integer.valueOf(targetTypeStr);
                        List<Vote> votes = voteService.getVotesByUserAndType(userId, targetType);
                        out.print(objectMapper.writeValueAsString(votes));
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"需要指定targetType参数\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定查询参数\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
            }
        } else {
            // 检查用户对指定内容的投票状态 /api/votes/status/{userId}/{targetType}/{targetId}
            String[] splits = pathInfo.split("/");
            if (splits.length >= 4 && "status".equals(splits[1])) {
                try {
                    if (splits.length < 5) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"路径格式错误，应为 /api/votes/status/{userId}/{targetType}/{targetId}\"}");
                        return;
                    }
                    Integer userId = Integer.valueOf(splits[2]);
                    Integer targetType = Integer.valueOf(splits[3]);
                    Integer targetId = Integer.valueOf(splits[4]);

                    int voteStatus = voteService.getUserVoteStatus(userId, targetType, targetId);
                    out.print("{\"voteStatus\":" + voteStatus + "}");
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"ID格式错误\"}");
                }
                return;
            }

            // 根据投票ID获取投票详情
            try {
                if (splits.length < 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid vote ID\"}");
                    return;
                }
                Integer voteId = Integer.valueOf(splits[1]);

                Vote vote = voteService.getVoteById(voteId);
                if (vote == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Vote not found\"}");
                } else {
                    out.print(objectMapper.writeValueAsString(vote));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid vote ID format\"}");
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
            // 将JSON转换为Vote对象
            Vote vote = objectMapper.readValue(sb.toString(), Vote.class);

            // 设置默认投票时间
            if (vote.getVoteCreateTime() == null) {
                vote.setVoteCreateTime(LocalDateTime.now());
            }

            // 调用VoteService创建投票的方法
            Integer result = voteService.createVote(vote);
            if (result > 0) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(vote));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建投票失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid vote data\"}");
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
            out.print("{\"error\":\"需要指定投票ID\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid vote ID\"}");
                return;
            }
            Integer voteId = Integer.valueOf(splits[1]);

            // 读取请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 解析请求体中的投票类型
            Vote updateRequest = objectMapper.readValue(sb.toString(), Vote.class);

            // 调用VoteService更新投票类型的方法
            boolean result = voteService.updateVoteType(voteId, updateRequest.getUserId()); // 这里用userId字段表示投票类型
            if (result) {
                out.print("{\"message\":\"投票类型更新成功\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"更新投票类型失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid vote ID format\"}");
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
            // 取消用户对指定内容的投票
            String userIdParam = request.getParameter("userId");
            String targetTypeParam = request.getParameter("targetType");
            String targetIdParam = request.getParameter("targetId");

            try {
                if (userIdParam != null && targetTypeParam != null && targetIdParam != null) {
                    Integer userId = Integer.valueOf(userIdParam);
                    Integer targetType = Integer.valueOf(targetTypeParam);
                    Integer targetId = Integer.valueOf(targetIdParam);

                    boolean result = voteService.cancelVote(userId, targetType, targetId);
                    if (result) {
                        out.print("{\"message\":\"投票已取消\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"取消投票失败\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定userId、targetType和targetId参数\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
            }
        } else {
            // 删除指定内容的所有投票记录 /api/votes/target/{targetType}/{targetId}
            String[] splits = pathInfo.split("/");
            if (splits.length >= 3 && "target".equals(splits[1])) {
                try {
                    if (splits.length < 4) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"路径格式错误，应为 /api/votes/target/{targetType}/{targetId}\"}");
                        return;
                    }
                    Integer targetType = Integer.valueOf(splits[2]);
                    Integer targetId = Integer.valueOf(splits[3]);

                    boolean result = voteService.deleteVotesByTarget(targetType, targetId);
                    if (result) {
                        out.print("{\"message\":\"目标投票记录已删除\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"删除目标投票记录失败\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"ID格式错误\"}");
                }
                return;
            }

            // 删除用户的所有投票记录 /api/votes/user/{userId}
            if (splits.length >= 3 && "user".equals(splits[1])) {
                try {
                    Integer userId = Integer.valueOf(splits[2]);
                    boolean result = voteService.deleteVotesByUserId(userId);
                    if (result) {
                        out.print("{\"message\":\"用户投票记录已删除\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"删除用户投票记录失败\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"ID格式错误\"}");
                }
                return;
            }

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"不支持的操作\"}");
        }
    }
}
