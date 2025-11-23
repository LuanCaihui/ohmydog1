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
        
        // 处理 /api/replies/{blogId} 请求（兼容 /api/comments/{blogId}）
        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length >= 2 && splits[1] != null && !splits[1].isEmpty()) {
                    Integer blogId = Integer.valueOf(splits[1]);
                    List<Reply> replies = replyService.getRepliesByBlogId(blogId);
                    if (replies == null) {
                        replies = new java.util.ArrayList<>();
                    }
                    // 构建评论树结构
                    List<Reply> topLevelReplies = new java.util.ArrayList<>();
                    java.util.Map<Integer, Reply> replyMap = new java.util.HashMap<>();
                    
                    // 先收集所有回复
                    for (Reply reply : replies) {
                        replyMap.put(reply.getReplyId(), reply);
                    }
                    
                    // 获取所有一级评论
                    List<Reply> allReplies = replyService.getRepliesByBlogId(blogId);
                    if (allReplies == null) {
                        allReplies = new java.util.ArrayList<>();
                    }
                    
                    // 获取所有子回复
                    List<Reply> allChildReplies = new java.util.ArrayList<>();
                    for (Reply reply : allReplies) {
                        List<Reply> childReplies = replyService.getRepliesByCommentId(reply.getReplyId(), 1, 1000);
                        if (childReplies != null) {
                            allChildReplies.addAll(childReplies);
                        }
                    }
                    allReplies.addAll(allChildReplies);
                    
                    // 构建map
                    replyMap.clear();
                    for (Reply reply : allReplies) {
                        replyMap.put(reply.getReplyId(), reply);
                    }
                    
                    // 构建树结构 - 只添加一级评论
                    for (Reply reply : allReplies) {
                        if (reply.getParentReply() == null) {
                            topLevelReplies.add(reply);
                        }
                    }
                    
                    // 为每个一级评论添加子评论
                    for (Reply topReply : topLevelReplies) {
                        addChildren(topReply, replyMap);
                    }
                    
                    // 转换为Map格式，包含用户信息
                    List<java.util.Map<String, Object>> replyMaps = new java.util.ArrayList<>();
                    for (Reply topReply : topLevelReplies) {
                        replyMaps.add(convertReplyToMap(topReply));
                    }
                    
                    out.print(objectMapper.writeValueAsString(replyMaps));
                    return;
                }
            } catch (NumberFormatException e) {
                // 如果不是数字，继续处理其他路径
            }
        }
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // 可以根据查询参数获取特定评论的回复列表
            String blogIdParam = request.getParameter("blogId");
            String commentIdParam = request.getParameter("commentId");
            String userIdParam = request.getParameter("userId");
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");

            try {
                int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

                if (blogIdParam != null) {
                    Integer blogId = Integer.valueOf(blogIdParam);
                    List<Reply> replies = replyService.getRepliesByBlogId(blogId);
                    if (replies == null) {
                        replies = new java.util.ArrayList<>();
                    }
                    out.print(objectMapper.writeValueAsString(replies));
                } else if (commentIdParam != null) {
                    Integer commentId = Integer.valueOf(commentIdParam);
                    List<Reply> replies = replyService.getRepliesByCommentId(commentId, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(replies));
                } else if (userIdParam != null) {
                    Integer userId = Integer.valueOf(userIdParam);
                    List<Reply> replies = replyService.getRepliesByUserId(userId, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(replies));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"blogId, commentId or userId is required\"}");
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

    /**
     * 递归添加子评论
     */
    private void addChildren(Reply parent, java.util.Map<Integer, Reply> replyMap) {
        java.util.List<Reply> children = new java.util.ArrayList<>();
        for (Reply reply : replyMap.values()) {
            if (reply.getParentReply() != null && reply.getParentReply().equals(parent.getReplyId())) {
                children.add(reply);
                addChildren(reply, replyMap);
            }
        }
        // 使用反射设置children字段（如果Reply类有children字段）
        try {
            java.lang.reflect.Field childrenField = Reply.class.getDeclaredField("children");
            childrenField.setAccessible(true);
            childrenField.set(parent, children);
        } catch (Exception e) {
            // 如果Reply类没有children字段，忽略
        }
    }

    /**
     * 将Reply对象转换为Map，包含用户信息，使用下划线命名
     */
    private java.util.Map<String, Object> convertReplyToMap(Reply reply) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("reply_id", reply.getReplyId());
        map.put("user_id", reply.getUserId());
        map.put("blog_id", reply.getBlogId());
        map.put("parentReply", reply.getParentReply());
        map.put("reply_createdtime", reply.getReplyCreatedtime());
        map.put("reply_content", reply.getReplyContent());
        map.put("is_visible", reply.getIsVisible());
        
        // 获取用户信息
        try {
            java.lang.reflect.Field userNameField = Reply.class.getDeclaredField("userName");
            userNameField.setAccessible(true);
            Object userName = userNameField.get(reply);
            if (userName != null) {
                map.put("user_name", userName);
            } else {
                // 如果Reply对象没有用户信息，从UserService获取
                com.petblog.Service.UserService userService = new com.petblog.Service.UserService();
                com.petblog.model.User user = userService.getUserById(reply.getUserId());
                if (user != null) {
                    map.put("user_name", user.getUserName());
                    map.put("user_avatar_path", user.getUserAvatarPath());
                }
            }
        } catch (Exception e) {
            // 如果获取失败，从UserService获取
            com.petblog.Service.UserService userService = new com.petblog.Service.UserService();
            com.petblog.model.User user = userService.getUserById(reply.getUserId());
            if (user != null) {
                map.put("user_name", user.getUserName());
                map.put("user_avatar_path", user.getUserAvatarPath());
            }
        }
        
        try {
            java.lang.reflect.Field userAvatarPathField = Reply.class.getDeclaredField("userAvatarPath");
            userAvatarPathField.setAccessible(true);
            Object userAvatarPath = userAvatarPathField.get(reply);
            if (userAvatarPath != null) {
                map.put("user_avatar_path", userAvatarPath);
            }
        } catch (Exception e) {
            // 忽略
        }
        
        // 获取父评论的用户名（如果有）
        if (reply.getParentReply() != null) {
            Reply parentReply = replyService.getReplyById(reply.getParentReply());
            if (parentReply != null) {
                com.petblog.Service.UserService userService = new com.petblog.Service.UserService();
                com.petblog.model.User parentUser = userService.getUserById(parentReply.getUserId());
                if (parentUser != null) {
                    map.put("parent_user_name", parentUser.getUserName());
                }
            }
        }
        
        // 递归转换子评论
        try {
            java.lang.reflect.Field childrenField = Reply.class.getDeclaredField("children");
            childrenField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<Reply> children = (java.util.List<Reply>) childrenField.get(reply);
            if (children != null && !children.isEmpty()) {
                java.util.List<java.util.Map<String, Object>> childrenMaps = new java.util.ArrayList<>();
                for (Reply child : children) {
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
