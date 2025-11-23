package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.FavoriteService;
import com.petblog.model.Favorite;
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

@WebServlet("/api/favorites/*")
public class FavoriteServlet extends HttpServlet {
    private final FavoriteService favoriteService = new FavoriteService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理 /api/favorites/init 请求
        if (pathInfo != null && pathInfo.equals("/init")) {
            out.print("{\"message\":\"Favorites表初始化完成\",\"success\":true}");
            return;
        }
        
        // 处理 /api/favorites/my 请求 - 获取当前用户收藏的博客详情列表
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
                
                // 获取用户收藏的博客ID列表
                List<Integer> blogIds = favoriteService.getFavoriteBlogIdsByUserId(userId, 1, 1000);
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
                        
                        // 获取收藏时间
                        try {
                            java.sql.Connection conn = com.petblog.util.JdbcUtil.getConnection();
                            java.sql.PreparedStatement pstmt = conn.prepareStatement(
                                "SELECT favorite_time FROM favorites WHERE user_id = ? AND blog_id = ? ORDER BY favorite_time DESC LIMIT 1");
                            pstmt.setInt(1, userId);
                            pstmt.setInt(2, blogId);
                            java.sql.ResultSet rs = pstmt.executeQuery();
                            if (rs.next()) {
                                blogMap.put("favorite_time", rs.getTimestamp("favorite_time"));
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
                out.print("{\"error\":\"获取收藏列表失败: " + e.getMessage() + "\"}");
                e.printStackTrace();
            }
            return;
        }
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // 根据用户ID获取收藏列表
            String userIdParam = request.getParameter("userId");
            String blogIdParam = request.getParameter("blogId");
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");

            try {
                if (userIdParam != null) {
                    Integer userId = Integer.valueOf(userIdParam);
                    int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                    int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

                    // 调用FavoriteService获取用户收藏的博客ID列表
                    List<Integer> favoriteBlogIds = favoriteService.getFavoriteBlogIdsByUserId(userId, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(favoriteBlogIds));
                } else if (blogIdParam != null) {
                    // 根据博客ID获取收藏该博客的用户列表
                    Integer blogId = Integer.valueOf(blogIdParam);
                    List<Integer> userIds = favoriteService.getUserIdsByFavoriteBlogId(blogId);
                    out.print(objectMapper.writeValueAsString(userIds));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定userId或blogId参数\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
            }
        } else {
            // 检查特定用户是否收藏了特定博客
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length < 3) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"路径格式错误，应为 /api/favorites/{userId}/{blogId}\"}");
                    return;
                }
                Integer userId = Integer.valueOf(splits[1]);
                Integer blogId = Integer.valueOf(splits[2]);

                // 调用FavoriteService检查用户是否已收藏指定博客
                boolean isFavorite = favoriteService.isBlogFavorite(userId, blogId);
                out.print(objectMapper.writeValueAsString(isFavorite));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"ID格式错误\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理 /api/favorites/toggle 请求
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

                // 检查是否已收藏
                boolean isFavorite = favoriteService.isBlogFavorite(userId, blogId);
                
                java.util.Map<String, Object> result = new java.util.HashMap<>();
                if (isFavorite) {
                    // 取消收藏
                    boolean removed = favoriteService.removeFavorite(userId, blogId);
                    result.put("success", removed);
                    result.put("favorited", false);
                    result.put("message", removed ? "取消收藏成功" : "取消收藏失败");
                } else {
                    // 添加收藏
                    Favorite favorite = new Favorite();
                    favorite.setUserId(userId);
                    favorite.setBlogId(blogId);
                    favorite.setFavoriteTime(new Date());
                    boolean added = favoriteService.addFavorite(favorite);
                    result.put("success", added);
                    result.put("favorited", added);
                    result.put("message", added ? "收藏成功" : "收藏失败");
                }
                
                out.print(objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                java.util.Map<String, Object> errorResult = new java.util.HashMap<>();
                errorResult.put("success", false);
                errorResult.put("error", "切换收藏状态失败: " + e.getMessage());
                out.print(objectMapper.writeValueAsString(errorResult));
                e.printStackTrace();
            }
            return;
        }

        // 读取请求体
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        try {
            // 将JSON转换为Favorite对象
            Favorite favorite = objectMapper.readValue(sb.toString(), Favorite.class);

            // 如果没有设置收藏时间，则使用当前时间
            if (favorite.getFavoriteTime() == null) {
                favorite.setFavoriteTime(new Date());
            }

            // 调用FavoriteService创建收藏的方法
            boolean result = favoriteService.addFavorite(favorite);
            if (result) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(favorite));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建收藏失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid favorite data\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"需要提供用户ID和博客ID路径\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 3) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"路径格式错误，应为 /api/favorites/{userId}/{blogId}\"}");
                return;
            }
            Integer userId = Integer.valueOf(splits[1]);
            Integer blogId = Integer.valueOf(splits[2]);

            // 调用FavoriteService删除收藏的方法
            boolean result = favoriteService.removeFavorite(userId, blogId);
            if (result) {
                out.print("{\"message\":\"取消收藏成功\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"未找到收藏记录或取消收藏失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"ID格式错误\"}");
        }
    }
}
