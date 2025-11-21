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
