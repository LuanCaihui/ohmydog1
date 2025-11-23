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
        
        // 处理 /api/comments/{blogId} 请求（获取博客的评论列表，返回树结构）
        if (pathInfo != null && !pathInfo.equals("/")) {
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length >= 2) {
                    Integer blogId = Integer.valueOf(splits[1]);
                    // 使用ReplyServlet的逻辑来构建评论树结构
                    List<com.petblog.model.Reply> replies = replyService.getRepliesByBlogId(blogId);
                    if (replies == null) {
                        replies = new java.util.ArrayList<>();
                    }
                    
                    // 构建评论树结构
                    List<com.petblog.model.Reply> topLevelReplies = new java.util.ArrayList<>();
                    java.util.Map<Integer, com.petblog.model.Reply> replyMap = new java.util.HashMap<>();
                    
                    // 先收集所有回复
                    for (com.petblog.model.Reply reply : replies) {
                        replyMap.put(reply.getReplyId(), reply);
                    }
                    
                    // 获取所有子回复
                    for (com.petblog.model.Reply reply : replies) {
                        List<com.petblog.model.Reply> childReplies = replyService.getRepliesByCommentId(reply.getReplyId(), 1, 1000);
                        if (childReplies != null) {
                            for (com.petblog.model.Reply child : childReplies) {
                                if (!replyMap.containsKey(child.getReplyId())) {
                                    replyMap.put(child.getReplyId(), child);
                                }
                            }
                        }
                    }
                    
                    // 构建树结构 - 只添加一级评论
                    for (com.petblog.model.Reply reply : replyMap.values()) {
                        if (reply.getParentReply() == null) {
                            topLevelReplies.add(reply);
                        }
                    }
                    
                    // 为每个一级评论添加子评论
                    for (com.petblog.model.Reply topReply : topLevelReplies) {
                        addChildren(topReply, replyMap);
                    }
                    
                    // 转换为Map格式，包含用户信息和children
                    List<java.util.Map<String, Object>> replyMaps = new java.util.ArrayList<>();
                    for (com.petblog.model.Reply topReply : topLevelReplies) {
                        replyMaps.add(convertReplyToMap(topReply));
                    }
                    
                    out.print(objectMapper.writeValueAsString(replyMaps));
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

                // 解析JSON请求体
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> requestData = objectMapper.readValue(sb.toString(),
                    objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, Object.class));
                
                // 构建Reply对象
                com.petblog.model.Reply reply = new com.petblog.model.Reply();
                reply.setBlogId(requestData.get("blog_id") != null ? 
                    (requestData.get("blog_id") instanceof Integer ? (Integer) requestData.get("blog_id") : 
                     Integer.valueOf(requestData.get("blog_id").toString())) : null);
                reply.setUserId(requestData.get("user_id") != null ? 
                    (requestData.get("user_id") instanceof Integer ? (Integer) requestData.get("user_id") : 
                     Integer.valueOf(requestData.get("user_id").toString())) : null);
                reply.setReplyContent(requestData.get("reply_content") != null ? 
                    requestData.get("reply_content").toString() : 
                    (requestData.get("comment_content") != null ? requestData.get("comment_content").toString() : null));
                reply.setParentReply(requestData.get("parentReply") != null ? 
                    (requestData.get("parentReply") instanceof Integer ? (Integer) requestData.get("parentReply") : 
                     (requestData.get("parentReply") != null && !requestData.get("parentReply").toString().equals("null") ? 
                      Integer.valueOf(requestData.get("parentReply").toString()) : null)) : 
                    (requestData.get("parent_comment_id") != null ? 
                     (requestData.get("parent_comment_id") instanceof Integer ? (Integer) requestData.get("parent_comment_id") : 
                      (requestData.get("parent_comment_id") != null && !requestData.get("parent_comment_id").toString().equals("null") ? 
                       Integer.valueOf(requestData.get("parent_comment_id").toString()) : null)) : null));
                
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
                    java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                    responseData.put("success", true);
                    responseData.put("reply", reply);
                    out.print(objectMapper.writeValueAsString(responseData));
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

    /**
     * 递归添加子评论
     */
    private void addChildren(com.petblog.model.Reply parent, java.util.Map<Integer, com.petblog.model.Reply> replyMap) {
        java.util.List<com.petblog.model.Reply> children = new java.util.ArrayList<>();
        for (com.petblog.model.Reply reply : replyMap.values()) {
            if (reply.getParentReply() != null && reply.getParentReply().equals(parent.getReplyId())) {
                children.add(reply);
                addChildren(reply, replyMap);
            }
        }
        // 使用反射设置children字段
        try {
            java.lang.reflect.Field childrenField = com.petblog.model.Reply.class.getDeclaredField("children");
            childrenField.setAccessible(true);
            childrenField.set(parent, children);
        } catch (Exception e) {
            // 如果Reply类没有children字段，忽略
        }
    }

    /**
     * 将Reply对象转换为Map，包含用户信息和children
     */
    private java.util.Map<String, Object> convertReplyToMap(com.petblog.model.Reply reply) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("reply_id", reply.getReplyId());
        map.put("user_id", reply.getUserId());
        map.put("blog_id", reply.getBlogId());
        map.put("parentReply", reply.getParentReply());
        map.put("reply_createdtime", reply.getReplyCreatedtime());
        map.put("reply_content", reply.getReplyContent());
        map.put("is_visible", reply.getIsVisible());
        
        // 获取用户信息
        com.petblog.Service.UserService userService = new com.petblog.Service.UserService();
        com.petblog.model.User user = userService.getUserById(reply.getUserId());
        if (user != null) {
            map.put("user_name", user.getUserName());
            map.put("user_avatar_path", user.getUserAvatarPath());
        }
        
        // 获取父评论的用户名（如果有）
        if (reply.getParentReply() != null) {
            com.petblog.model.Reply parentReply = replyService.getReplyById(reply.getParentReply());
            if (parentReply != null) {
                com.petblog.model.User parentUser = userService.getUserById(parentReply.getUserId());
                if (parentUser != null) {
                    map.put("parent_user_name", parentUser.getUserName());
                }
            }
        }
        
        // 递归转换子评论
        try {
            java.lang.reflect.Field childrenField = com.petblog.model.Reply.class.getDeclaredField("children");
            childrenField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<com.petblog.model.Reply> children = (java.util.List<com.petblog.model.Reply>) childrenField.get(reply);
            if (children != null && !children.isEmpty()) {
                java.util.List<java.util.Map<String, Object>> childrenMaps = new java.util.ArrayList<>();
                for (com.petblog.model.Reply child : children) {
                    childrenMaps.add(convertReplyToMap(child));
                }
                map.put("children", childrenMaps);
            }
        } catch (Exception e) {
            // 如果Reply类没有children字段，忽略
        }
        
        return map;
    }
}

