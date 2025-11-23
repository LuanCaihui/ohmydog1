package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.VoteService;
import com.petblog.model.Vote;
import com.petblog.util.JsonUtil;
import com.petblog.util.JdbcUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/votes/*")
public class VoteServlet extends HttpServlet {
    private final VoteService voteService = new VoteService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

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
            // 检查用户对指定博客的投票状态 /api/votes/status/{blogId}
            String[] splits = pathInfo.split("/");
            if (splits.length >= 3 && "status".equals(splits[1])) {
                try {
                    Integer blogId = Integer.valueOf(splits[2]);
                    
                    // 从请求参数或session获取userId
                    String userIdStr = request.getParameter("userId");
                    if (userIdStr == null) {
                        // 尝试从session获取
                        Object userIdObj = request.getSession().getAttribute("userId");
                        if (userIdObj != null) {
                            userIdStr = userIdObj.toString();
                        }
                    }
                    
                    if (userIdStr == null) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        Map<String, Object> result = new HashMap<>();
                        result.put("success", false);
                        result.put("error", "需要用户ID");
                        out.print(objectMapper.writeValueAsString(result));
                        return;
                    }
                    
                    Integer userId = Integer.valueOf(userIdStr);
                    
                    // 检查用户是否已投票（votes表：user_id, blog_id）
                    boolean userVoted = checkUserVotedForBlog(userId, blogId);
                    int voteCount = countVotesForBlog(blogId);
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("userVoted", userVoted);
                    result.put("voteCount", voteCount);
                    out.print(objectMapper.writeValueAsString(result));
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("error", "ID格式错误");
                    out.print(objectMapper.writeValueAsString(result));
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

        String pathInfo = request.getPathInfo();
        
        // 处理切换投票状态 /api/votes/toggle
        if (pathInfo != null && pathInfo.equals("/toggle")) {
            try {
                handleToggleVote(request, response, out, sb.toString());
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "切换投票状态失败: " + e.getMessage());
                out.print(objectMapper.writeValueAsString(result));
                e.printStackTrace();
            }
            return;
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
    
    /**
     * 切换投票状态（投票/取消投票）
     */
    private void handleToggleVote(HttpServletRequest request, HttpServletResponse response, PrintWriter out, String requestBody) throws Exception {
        // 如果请求体为空，尝试从参数获取
        if (requestBody == null || requestBody.trim().isEmpty()) {
            String blogIdStr = request.getParameter("blog_id");
            String userIdStr = request.getParameter("userId");
            if (blogIdStr != null && userIdStr != null) {
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("blog_id", Integer.valueOf(blogIdStr));
                requestData.put("userId", Integer.valueOf(userIdStr));
                processToggleVote(requestData, request, response, out);
                return;
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "请求体为空");
            out.print(objectMapper.writeValueAsString(result));
            return;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> requestData = objectMapper.readValue(requestBody, 
            objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
        
        processToggleVote(requestData, request, response, out);
    }
    
    /**
     * 处理投票切换逻辑
     */
    private void processToggleVote(Map<String, Object> requestData, HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws Exception {
        
        Integer blogId = requestData.get("blog_id") != null ? 
            Integer.valueOf(requestData.get("blog_id").toString()) : null;
        
        // 从请求参数或session获取userId
        String userIdStr = request.getParameter("userId");
        if (userIdStr == null && requestData.containsKey("userId")) {
            userIdStr = requestData.get("userId").toString();
        }
        if (userIdStr == null) {
            Object userIdObj = request.getSession().getAttribute("userId");
            if (userIdObj != null) {
                userIdStr = userIdObj.toString();
            }
        }
        
        if (blogId == null || userIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "参数错误: blogId=" + blogId + ", userId=" + userIdStr);
            out.print(objectMapper.writeValueAsString(result));
            return;
        }
        
        Integer userId = Integer.valueOf(userIdStr);
        
        // 检查是否已投票
        boolean userVoted = checkUserVotedForBlog(userId, blogId);
        
        Map<String, Object> result = new HashMap<>();
        
        if (userVoted) {
            // 取消投票
            deleteVote(userId, blogId);
            result.put("success", true);
            result.put("voted", false);
        } else {
            // 添加投票
            Vote vote = new Vote();
            vote.setUserId(userId);
            vote.setBlogId(blogId);
            vote.setVoteCreateTime(LocalDateTime.now());
            voteService.createVote(vote);
            result.put("success", true);
            result.put("voted", true);
        }
        
        // 获取更新后的投票数
        int voteCount = countVotesForBlog(blogId);
        result.put("voteCount", voteCount);
        
        out.print(objectMapper.writeValueAsString(result));
    }
    
    /**
     * 检查用户是否已投票
     */
    private boolean checkUserVotedForBlog(Integer userId, Integer blogId) {
        try {
            String sql = "SELECT COUNT(*) FROM votes WHERE user_id = ? AND blog_id = ?";
            Connection conn = JdbcUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, blogId);
            ResultSet rs = pstmt.executeQuery();
            boolean voted = false;
            if (rs.next()) {
                voted = rs.getInt(1) > 0;
            }
            JdbcUtil.close(conn, pstmt, rs);
            return voted;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 删除投票
     */
    private void deleteVote(Integer userId, Integer blogId) {
        try {
            String sql = "DELETE FROM votes WHERE user_id = ? AND blog_id = ?";
            Connection conn = JdbcUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, blogId);
            pstmt.executeUpdate();
            JdbcUtil.close(conn, pstmt, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 统计博客的投票数
     */
    private int countVotesForBlog(Integer blogId) {
        try {
            String sql = "SELECT COUNT(*) FROM votes WHERE blog_id = ?";
            Connection conn = JdbcUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, blogId);
            ResultSet rs = pstmt.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            JdbcUtil.close(conn, pstmt, rs);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
