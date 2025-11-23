package com.petblog.servlet;

import com.petblog.Service.LikeService;
import com.petblog.model.Like;
import com.petblog.util.JsonUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

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

@WebServlet("/api/likes/*")
public class LikeServlet extends HttpServlet {
    private final LikeService likeService = new LikeService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理 /api/likes/toggle 请求
        if (pathInfo != null && pathInfo.equals("/toggle")) {
            try {
                // 读取请求体
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                // 解析请求体
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> requestData = objectMapper.readValue(sb.toString(), 
                    objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, Object.class));
                
                Integer blogId = requestData.get("blog_id") != null ? 
                    Integer.valueOf(requestData.get("blog_id").toString()) : null;
                Integer userId = requestData.get("userId") != null ? 
                    Integer.valueOf(requestData.get("userId").toString()) : null;
                
                if (blogId == null || userId == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"error\":\"缺少必要参数: blog_id 或 userId\"}");
                    return;
                }

                // 检查是否已点赞
                boolean isLiked = likeService.isBlogLiked(userId, blogId);
                
                java.util.Map<String, Object> result = new java.util.HashMap<>();
                if (isLiked) {
                    // 取消点赞
                    boolean removed = likeService.removeLike(userId, blogId);
                    result.put("success", removed);
                    result.put("liked", false);
                    result.put("message", removed ? "取消点赞成功" : "取消点赞失败");
                } else {
                    // 添加点赞
                    Like like = new Like();
                    like.setUserId(userId);
                    like.setBlogId(blogId);
                    like.setLikeTime(new Date());
                    boolean added = likeService.addLike(like);
                    result.put("success", added);
                    result.put("liked", added);
                    result.put("message", added ? "点赞成功" : "点赞失败");
                }
                
                out.print(objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                java.util.Map<String, Object> errorResult = new java.util.HashMap<>();
                errorResult.put("success", false);
                errorResult.put("error", "切换点赞状态失败: " + e.getMessage());
                out.print(objectMapper.writeValueAsString(errorResult));
                e.printStackTrace();
            }
            return;
        }

        try {
            // 读取请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 解析点赞信息
            Like like = objectMapper.readValue(sb.toString(), Like.class);

            // 设置点赞时间
            if (like.getLikeTime() == null) {
                like.setLikeTime(new Date());
            }

            // 调用LikeService创建点赞的方法
            boolean result = likeService.addLike(like);
            if (result) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(like));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"点赞失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"点赞失败\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 通过参数取消点赞
            String userIdParam = request.getParameter("userId");
            String blogIdParam = request.getParameter("blogId");

            try {
                if (userIdParam != null && blogIdParam != null) {
                    Integer userId = Integer.valueOf(userIdParam);
                    Integer blogId = Integer.valueOf(blogIdParam);

                    // 调用LikeService删除点赞的方法
                    boolean result = likeService.removeLike(userId, blogId);
                    if (result) {
                        out.print("{\"message\":\"取消点赞成功\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"点赞记录未找到或取消点赞失败\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定userId和blogId参数\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
            }
            return;
        }

        // 通过路径参数取消点赞 /api/likes/{userId}/{blogId}
        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 3) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"路径格式错误，应为 /api/likes/{userId}/{blogId}\"}");
                return;
            }
            Integer userId = Integer.valueOf(splits[1]);
            Integer blogId = Integer.valueOf(splits[2]);

            // 调用LikeService删除点赞的方法
            boolean result = likeService.removeLike(userId, blogId);
            if (result) {
                out.print("{\"message\":\"取消点赞成功\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"点赞记录未找到或取消点赞失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"ID格式错误\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理 /api/likes/init 请求
        if (pathInfo != null && pathInfo.equals("/init")) {
            // 初始化接口，返回成功消息
            out.print("{\"message\":\"Likes表初始化完成\",\"success\":true}");
            return;
        }
        
        // 处理 /api/likes/my 请求 - 获取当前用户点赞的博客详情列表
        if (pathInfo != null && pathInfo.equals("/my")) {
            try {
                // 从session获取当前用户ID
                Object userIdObj = request.getSession().getAttribute("userId");
                String userIdParam = request.getParameter("userId");
                
                Integer userId = null;
                if (userIdParam != null) {
                    userId = Integer.valueOf(userIdParam);
                } else if (userIdObj != null) {
                    userId = userIdObj instanceof Integer ? (Integer) userIdObj : Integer.valueOf(userIdObj.toString());
                }
                
                if (userId == null) {
                    // 如果没有userId，返回空数组而不是错误
                    out.print("[]");
                    return;
                }
                
                // 获取用户点赞的博客ID列表
                List<Integer> blogIds = likeService.getLikedBlogIdsByUserId(userId, 1, 1000);
                if (blogIds == null || blogIds.isEmpty()) {
                    out.print("[]");
                    return;
                }
                
                // 获取博客详情
                com.petblog.Service.BlogService blogService = new com.petblog.Service.BlogService();
                java.util.List<java.util.Map<String, Object>> blogsWithDetails = new java.util.ArrayList<>();
                
                for (Integer blogId : blogIds) {
                    com.petblog.model.Blog blog = blogService.getBlogById(blogId);
                    if (blog != null) {
                        java.util.Map<String, Object> blogMap = new java.util.HashMap<>();
                        blogMap.put("blog_id", blog.getBlogId());
                        blogMap.put("blog_title", blog.getBlogTitle());
                        blogMap.put("blog_content", blog.getBlogContent());
                        blogMap.put("blog_create_time", blog.getBlogCreateTime());
                        blogMap.put("user_id", blog.getUserId());
                        blogMap.put("user_name", blog.getUserName());
                        blogMap.put("user_avatar_path", blog.getUserAvatarPath());
                        
                        // 获取点赞时间
                        try {
                            java.sql.Connection conn = com.petblog.util.JdbcUtil.getConnection();
                            java.sql.PreparedStatement pstmt = conn.prepareStatement(
                                "SELECT like_time FROM likes WHERE user_id = ? AND blog_id = ? ORDER BY like_time DESC LIMIT 1");
                            pstmt.setInt(1, userId);
                            pstmt.setInt(2, blogId);
                            java.sql.ResultSet rs = pstmt.executeQuery();
                            if (rs.next()) {
                                blogMap.put("like_time", rs.getTimestamp("like_time"));
                            }
                            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
                        } catch (Exception e) {
                            // 忽略错误
                        }
                        
                        blogsWithDetails.add(blogMap);
                    }
                }
                
                out.print(objectMapper.writeValueAsString(blogsWithDetails));
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"获取点赞列表失败: " + e.getMessage() + "\"}");
                e.printStackTrace();
            }
            return;
        }
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // 根据用户ID获取点赞的博客列表
            String userIdParam = request.getParameter("userId");
            String blogIdParam = request.getParameter("blogId"); // 新增：根据博客ID查询点赞用户
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");

            try {
                if (userIdParam != null) {
                    Integer userId = Integer.valueOf(userIdParam);
                    int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                    int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

                    List<Integer> blogIds = likeService.getLikedBlogIdsByUserId(userId, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(blogIds));
                } else if (blogIdParam != null) {
                    // 根据博客ID查询所有点赞该博客的用户ID
                    Integer blogId = Integer.valueOf(blogIdParam);
                    List<Integer> userIds = likeService.getUserIdsByLikedBlogId(blogId);
                    out.print(objectMapper.writeValueAsString(userIds));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定用户ID或博客ID\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
            }
        } else {
            // 检查特定用户是否点赞了特定博客 /api/likes/{userId}/{blogId}
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length < 3) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"路径格式错误，应为 /api/likes/{userId}/{blogId}\"}");
                    return;
                }
                Integer userId = Integer.valueOf(splits[1]);
                Integer blogId = Integer.valueOf(splits[2]);

                boolean isLiked = likeService.isBlogLiked(userId, blogId);
                out.print(objectMapper.writeValueAsString(isLiked));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"ID格式错误\"}");
            }
        }
    }
}
