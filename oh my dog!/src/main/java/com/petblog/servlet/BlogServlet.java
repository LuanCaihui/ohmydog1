package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.BlogService;
import com.petblog.model.Blog;
import com.petblog.util.JsonUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/blogs/*")
@jakarta.servlet.annotation.MultipartConfig(
    maxFileSize = 10485760, // 10MB
    maxRequestSize = 52428800, // 50MB
    fileSizeThreshold = 2097152 // 2MB
)
public class BlogServlet extends HttpServlet {
    private final BlogService blogService = new BlogService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理 /api/blogs/my/not-in-column/{columnId} 路径
        if (pathInfo != null && pathInfo.startsWith("/my/not-in-column/")) {
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length < 4) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid column ID\"}");
                    return;
                }
                Integer columnId = Integer.valueOf(splits[3]);
                
                // 从session获取userId
                Object userIdObj = request.getSession().getAttribute("userId");
                String userIdParam = request.getParameter("userId");
                
                Integer userId = null;
                if (userIdParam != null) {
                    userId = Integer.valueOf(userIdParam);
                } else if (userIdObj != null) {
                    userId = userIdObj instanceof Integer ? (Integer) userIdObj : Integer.valueOf(userIdObj.toString());
                }
                
                if (userId == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.print("{\"error\":\"需要用户ID，请先登录\"}");
                    return;
                }
                
                // 获取用户的所有博客
                List<Blog> allBlogs = blogService.getBlogsByAuthorId(userId);
                if (allBlogs == null) {
                    allBlogs = new java.util.ArrayList<>();
                }
                
                // 获取专栏中的博客ID列表
                com.petblog.Service.BlogColumnService blogColumnService = new com.petblog.Service.BlogColumnService();
                java.util.List<com.petblog.model.BlogColumn> blogColumns = blogColumnService.getBlogColumnsByColumnId(columnId);
                java.util.Set<Integer> columnBlogIds = new java.util.HashSet<>();
                if (blogColumns != null) {
                    for (com.petblog.model.BlogColumn blogColumn : blogColumns) {
                        columnBlogIds.add(blogColumn.getBlogId());
                    }
                }
                
                // 过滤出不在专栏中的博客
                List<Blog> blogsNotInColumn = new java.util.ArrayList<>();
                for (Blog blog : allBlogs) {
                    if (!columnBlogIds.contains(blog.getBlogId())) {
                        blogsNotInColumn.add(blog);
                    }
                }
                
                out.print(objectMapper.writeValueAsString(blogsNotInColumn));
                return;
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
                return;
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"获取博客列表失败: " + e.getMessage() + "\"}");
                e.printStackTrace();
                return;
            }
        }
        
        // 处理 /api/blogs/by-topic 路径
        if (pathInfo != null && pathInfo.equals("/by-topic")) {
            String topicParam2 = request.getParameter("topic");
            if (topicParam2 != null && !topicParam2.isEmpty()) {
                try {
                    // 根据话题名称查找话题ID
                    com.petblog.Service.TopicService topicService = new com.petblog.Service.TopicService();
                    com.petblog.model.Topic topic = topicService.getTopicByName(topicParam2);
                    if (topic == null || topic.getTopicId() == null) {
                        out.print(objectMapper.writeValueAsString(new java.util.ArrayList<>()));
                        return;
                    }
                    
                    // 根据话题ID获取博客ID列表
                    com.petblog.Service.BlogTopicService blogTopicService = new com.petblog.Service.BlogTopicService();
                    List<Integer> blogIds = blogTopicService.getBlogIdsByTopicId(topic.getTopicId(), 1, 1000);
                    if (blogIds == null || blogIds.isEmpty()) {
                        out.print(objectMapper.writeValueAsString(new java.util.ArrayList<>()));
                        return;
                    }
                    
                    // 根据博客ID列表获取博客详情
                    List<Blog> blogs = new java.util.ArrayList<>();
                    for (Integer blogId : blogIds) {
                        Blog blog = blogService.getBlogById(blogId);
                        if (blog != null) {
                            blogs.add(blog);
                        }
                    }
                    
                    out.print(objectMapper.writeValueAsString(blogs));
                    return;
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"error\":\"根据话题查询博客失败: " + e.getMessage() + "\"}");
                    e.printStackTrace();
                    return;
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"缺少topic参数\"}");
                return;
            }
        }
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // 获取博客列表
            List<Blog> blogs = blogService.getAllBlogs();
            if (blogs == null) {
                blogs = new java.util.ArrayList<>(); // 如果为null，返回空列表
            }
            out.print(objectMapper.writeValueAsString(blogs));
        } else {
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length < 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid blog ID\"}");
                    return;
                }
                Integer blogId = Integer.valueOf(splits[1]);
                
                // 处理特殊路径 /api/blogs/{blogId}/heat-details
                if (splits.length >= 3 && "heat-details".equals(splits[2])) {
                    handleGetBlogHeatDetails(blogId, response, out);
                    return;
                }

                Blog blog = blogService.getBlogById(blogId);
                if (blog == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Blog not found\"}");
                } else {
                    out.print(objectMapper.writeValueAsString(blog));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid blog ID format\"}");
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"处理请求失败\"}");
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理 /api/blogs/add - 支持文件上传的博客创建
        if (pathInfo != null && pathInfo.equals("/add")) {
            try {
                handleAddBlogWithFiles(request, response, out);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "创建博客失败: " + e.getMessage());
                out.print(objectMapper.writeValueAsString(result));
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
            // 将JSON转换为Blog对象
            Blog blog = objectMapper.readValue(sb.toString(), Blog.class);

            // 调用BlogService创建博客
            blogService.createBlog(blog);
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(objectMapper.writeValueAsString(blog));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid blog data\"}");
        }
    }
    
    /**
     * 处理带文件上传的博客创建
     */
    private void handleAddBlogWithFiles(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws Exception {
        // 检查是否是multipart请求
        String contentType = request.getContentType();
        boolean isMultipart = contentType != null && contentType.toLowerCase().contains("multipart/form-data");
        
        if (!isMultipart) {
            // 如果不是multipart，尝试作为JSON处理
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            Blog blog = objectMapper.readValue(sb.toString(), Blog.class);
            blogService.createBlog(blog);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("blog", blog);
            out.print(objectMapper.writeValueAsString(result));
            return;
        }
        
        // 处理multipart/form-data
        jakarta.servlet.http.Part blogTitlePart = request.getPart("blog_title");
        jakarta.servlet.http.Part blogContentPart = request.getPart("blog_content");
        jakarta.servlet.http.Part userIdPart = request.getPart("user_id");
        jakarta.servlet.http.Part challengeIdPart = request.getPart("challenge_id");
        jakarta.servlet.http.Part columnIdPart = request.getPart("column_id");
        jakarta.servlet.http.Part petIdsPart = request.getPart("pet_ids");
        
        if (blogTitlePart == null || blogContentPart == null || userIdPart == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "缺少必要参数: blog_title=" + (blogTitlePart != null) + 
                     ", blog_content=" + (blogContentPart != null) + 
                     ", user_id=" + (userIdPart != null));
            out.print(objectMapper.writeValueAsString(result));
            return;
        }
        
        // 读取表单字段
        String blogTitle = readPartAsString(blogTitlePart);
        String blogContent = readPartAsString(blogContentPart);
        String userIdStr = readPartAsString(userIdPart);
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "用户ID不能为空");
            out.print(objectMapper.writeValueAsString(result));
            return;
        }
        Integer userId = Integer.valueOf(userIdStr.trim());
        Integer challengeId = challengeIdPart != null ? 
            (readPartAsString(challengeIdPart).trim().isEmpty() ? null : Integer.valueOf(readPartAsString(challengeIdPart).trim())) : null;
        Integer columnId = columnIdPart != null && !readPartAsString(columnIdPart).trim().isEmpty() ? 
            Integer.valueOf(readPartAsString(columnIdPart).trim()) : null;
        
        // 创建博客对象
        Blog blog = new Blog();
        blog.setUserId(userId);
        blog.setBlogTitle(blogTitle);
        blog.setBlogContent(blogContent);
        blog.setBlogCreateTime(new java.util.Date());
        blog.setBlogUpdateTime(new java.util.Date());
        blog.setIsShielded(0);
        
        // 创建博客
        blogService.createBlog(blog);
        Integer blogId = blog.getBlogId();
        
        if (blogId == null || blogId <= 0) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "创建博客失败：无法获取博客ID");
            out.print(objectMapper.writeValueAsString(result));
            return;
        }
        
        System.out.println("博客创建成功，blogId=" + blogId);
        
        // 处理文件上传
        java.util.List<jakarta.servlet.http.Part> fileParts = request.getParts().stream()
            .filter(part -> "files".equals(part.getName()) && part.getSize() > 0)
            .collect(java.util.stream.Collectors.toList());
        
        if (!fileParts.isEmpty()) {
            com.petblog.Service.FileService fileService = new com.petblog.Service.FileService();
            
            // 获取应用部署路径
            String appPath = request.getServletContext().getRealPath("/");
            if (appPath == null) {
                // 如果getRealPath返回null（某些服务器环境），使用用户目录
                appPath = System.getProperty("user.dir");
            }
            
            // 构建上传目录的文件系统路径
            String uploadDir = appPath + "images" + java.io.File.separator + "uploads" + java.io.File.separator;
            java.io.File uploadDirFile = new java.io.File(uploadDir);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs();
            }
            
            for (jakarta.servlet.http.Part filePart : fileParts) {
                String fileName = getFileName(filePart);
                if (fileName != null && !fileName.isEmpty()) {
                    try {
                        // 生成唯一文件名
                        String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                        
                        // 文件系统路径（用于保存文件）
                        String fileSystemPath = uploadDir + uniqueFileName;
                        
                        // URL路径（用于数据库存储和前端访问）
                        String urlPath = "images/uploads/" + uniqueFileName;
                        
                        // 保存文件到文件系统
                        filePart.write(fileSystemPath);
                        
                        // 创建文件记录（存储URL路径）
                        com.petblog.model.File file = new com.petblog.model.File();
                        file.setFileName(fileName);
                        file.setFilePath(urlPath);
                        file.setFileType(filePart.getContentType());
                        file.setFileSize((float) filePart.getSize());
                        file.setFileUploadedTime(new java.util.Date());
                        file.setBlogId(blogId);
                        fileService.createFile(file);
                    } catch (Exception e) {
                        System.err.println("保存文件失败: " + e.getMessage());
                        e.printStackTrace();
                        // 继续处理其他文件，不中断整个流程
                    }
                }
            }
        }
        
        // 处理宠物关联
        if (petIdsPart != null) {
            String petIdsJson = readPartAsString(petIdsPart);
            if (petIdsJson != null && !petIdsJson.trim().isEmpty()) {
                try {
                    @SuppressWarnings("unchecked")
                    List<Integer> petIds = objectMapper.readValue(petIdsJson.trim(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class));
                    if (petIds != null && !petIds.isEmpty()) {
                        com.petblog.Service.BlogPetService blogPetService = new com.petblog.Service.BlogPetService();
                        for (Integer petId : petIds) {
                            if (petId != null) {
                                com.petblog.model.BlogPet blogPet = new com.petblog.model.BlogPet();
                                blogPet.setBlogId(blogId);
                                blogPet.setPetId(petId);
                                blogPetService.createBlogPet(blogPet);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("解析宠物ID列表失败: " + e.getMessage());
                    e.printStackTrace();
                    // 不抛出异常，继续处理
                }
            }
        }
        
        // 处理挑战关联
        if (challengeId != null && blogId != null) {
            try {
                // 检查是否已经存在该关联（避免重复插入）
                com.petblog.Service.BlogChallengeService blogChallengeService = new com.petblog.Service.BlogChallengeService();
                java.util.List<com.petblog.model.BlogChallenge> existing = 
                    blogChallengeService.getBlogChallengesByBlogId(blogId);
                boolean alreadyExists = false;
                if (existing != null && !existing.isEmpty()) {
                    for (com.petblog.model.BlogChallenge bc : existing) {
                        if (bc != null && bc.getChallengeId() != null && bc.getChallengeId().equals(challengeId)) {
                            alreadyExists = true;
                            System.out.println("博客挑战关联已存在: blogId=" + blogId + ", challengeId=" + challengeId);
                            break;
                        }
                    }
                }
                
                if (!alreadyExists) {
                    com.petblog.model.BlogChallenge blogChallenge = new com.petblog.model.BlogChallenge();
                    blogChallenge.setBlogId(blogId);
                    blogChallenge.setChallengeId(challengeId);
                    boolean result = blogChallengeService.createBlogChallenge(blogChallenge);
                    if (result) {
                        System.out.println("博客挑战关联创建成功: blogId=" + blogId + ", challengeId=" + challengeId);
                    } else {
                        System.err.println("创建博客挑战关联失败: blogId=" + blogId + ", challengeId=" + challengeId);
                    }
                }
            } catch (Exception e) {
                System.err.println("处理挑战关联失败: " + e.getMessage());
                e.printStackTrace();
                // 不抛出异常，继续处理其他关联，但记录错误
            }
        } else {
            System.err.println("挑战关联参数无效: blogId=" + blogId + ", challengeId=" + challengeId);
        }
        
        // 处理专栏关联
        if (columnId != null) {
            com.petblog.model.BlogColumn blogColumn = new com.petblog.model.BlogColumn();
            blogColumn.setBlogId(blogId);
            blogColumn.setColumnId(columnId);
            com.petblog.Service.BlogColumnService blogColumnService = new com.petblog.Service.BlogColumnService();
            blogColumnService.createBlogColumn(blogColumn);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("blog_id", blogId);
        result.put("message", "博客创建成功");
        out.print(objectMapper.writeValueAsString(result));
    }
    
    private String readPartAsString(jakarta.servlet.http.Part part) throws IOException {
        if (part == null) {
            return null;
        }
        java.io.InputStream is = part.getInputStream();
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
    
    private String getFileName(jakarta.servlet.http.Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] tokens = contentDisposition.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return null;
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Blog ID is required\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid blog ID\"}");
                return;
            }
            Integer blogId = Integer.valueOf(splits[1]);

            // 读取请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 将JSON转换为Blog对象
            Blog blog = objectMapper.readValue(sb.toString(), Blog.class);
            blog.setBlogId(blogId); // 确保ID一致

            // 调用BlogService更新博客
            blogService.updateBlog(blog);
            out.print(objectMapper.writeValueAsString(blog));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid blog ID format\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid blog data\"}");
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
            out.print("{\"error\":\"Blog ID is required\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid blog ID\"}");
                return;
            }
            Integer blogId = Integer.valueOf(splits[1]);

            // 调用BlogService删除博客
            boolean result = blogService.deleteBlog(blogId);
            if (result) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Blog not found\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid blog ID format\"}");
        }
    }
    
    /**
     * 获取博客热度详情
     */
    private void handleGetBlogHeatDetails(Integer blogId, HttpServletResponse response, PrintWriter out) throws Exception {
        Blog blog = blogService.getBlogById(blogId);
        if (blog == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "博客不存在");
            out.print(objectMapper.writeValueAsString(result));
            return;
        }
        
        // 获取各项统计数据
        int voteCount = countVotesForBlog(blogId);
        int likeCount = blog.getLikeCount() != null ? blog.getLikeCount() : 0;
        int favoriteCount = blog.getFavoriteCount() != null ? blog.getFavoriteCount() : 0;
        int replyCount = blog.getCommentCount() != null ? blog.getCommentCount() : 0;
        int repostCount = blog.getRepostCount() != null ? blog.getRepostCount() : 0;
        
        // 计算总热度
        int totalHeat = voteCount * 50 + likeCount * 20 + favoriteCount * 20 + replyCount * 10 + repostCount * 10;
        
        // 构建详情
        Map<String, Object> details = new HashMap<>();
        details.put("votes", voteCount);
        details.put("likes", likeCount);
        details.put("favorites", favoriteCount);
        details.put("replies", replyCount);
        details.put("reposts", repostCount);
        
        // 构建计算过程
        Map<String, String> calculation = new HashMap<>();
        calculation.put("votes", voteCount + " × 50 = " + (voteCount * 50));
        calculation.put("likes", likeCount + " × 20 = " + (likeCount * 20));
        calculation.put("favorites", favoriteCount + " × 20 = " + (favoriteCount * 20));
        calculation.put("replies", replyCount + " × 10 = " + (replyCount * 10));
        calculation.put("reposts", repostCount + " × 10 = " + (repostCount * 10));
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("heat", totalHeat);
        result.put("details", details);
        result.put("calculation", calculation);
        
        out.print(objectMapper.writeValueAsString(result));
    }
    
    /**
     * 统计博客的投票数
     */
    private int countVotesForBlog(Integer blogId) {
        try {
            String sql = "SELECT COUNT(*) FROM votes WHERE blog_id = ?";
            Connection conn = com.petblog.util.JdbcUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, blogId);
            ResultSet rs = pstmt.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}

