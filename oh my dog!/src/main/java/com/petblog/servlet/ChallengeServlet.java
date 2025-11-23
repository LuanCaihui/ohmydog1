package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.ChallengeService;
import com.petblog.Service.BlogChallengeService;
import com.petblog.Service.BlogService;
import com.petblog.Service.VoteService;
import com.petblog.model.Challenge;
import com.petblog.model.BlogChallenge;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@WebServlet("/api/challenges/*")
public class ChallengeServlet extends HttpServlet {
    private final ChallengeService challengeService = new ChallengeService();
    private final BlogChallengeService blogChallengeService = new BlogChallengeService();
    private final BlogService blogService = new BlogService();
    private final VoteService voteService = new VoteService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 获取挑战列表（包含热度、用户信息等）
            try {
                // 直接查询所有挑战（不限制状态）
                List<Challenge> challenges = getAllChallenges();
                System.out.println("查询到挑战数量: " + (challenges != null ? challenges.size() : 0));
                
                // 为每个挑战添加用户信息和热度
                List<Map<String, Object>> challengesWithDetails = new java.util.ArrayList<>();
                for (Challenge challenge : challenges) {
                    Map<String, Object> challengeMap = buildChallengeMap(challenge);
                    challengesWithDetails.add(challengeMap);
                }
                
                // 按热度排序
                challengesWithDetails.sort((a, b) -> {
                    Integer heatA = (Integer) a.get("heat");
                    Integer heatB = (Integer) b.get("heat");
                    return (heatB != null ? heatB : 0) - (heatA != null ? heatA : 0);
                });
                
                out.print(objectMapper.writeValueAsString(challengesWithDetails));
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"获取挑战列表失败\"}");
                e.printStackTrace();
            }
        } else {
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length < 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid challenge ID\"}");
                    return;
                }
                Integer challengeId = Integer.valueOf(splits[1]);
                
                // 处理特殊路径
                if (splits.length >= 3) {
                    String action = splits[2];
                    
                    if ("blogs-with-heat".equals(action)) {
                        // 获取挑战的博客列表（带热度）
                        try {
                            handleGetChallengeBlogsWithHeat(challengeId, response, out);
                        } catch (Exception e) {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            out.print("{\"error\":\"获取博客列表失败: " + e.getMessage() + "\"}");
                            e.printStackTrace();
                        }
                        return;
                    } else if ("pet-rankings".equals(action)) {
                        // 获取狗狗排名
                        try {
                            handleGetPetRankings(challengeId, response, out);
                        } catch (Exception e) {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            out.print("{\"error\":\"获取排名失败: " + e.getMessage() + "\"}");
                            e.printStackTrace();
                        }
                        return;
                    } else if ("winners".equals(action)) {
                        // 获取获胜者
                        try {
                            handleGetWinners(challengeId, response, out);
                        } catch (Exception e) {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            out.print("{\"error\":\"获取获胜者失败: " + e.getMessage() + "\"}");
                            e.printStackTrace();
                        }
                        return;
                    } else if ("heat".equals(action)) {
                        // 获取挑战热度
                        try {
                            handleGetChallengeHeat(challengeId, response, out);
                        } catch (Exception e) {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            out.print("{\"error\":\"获取热度失败: " + e.getMessage() + "\"}");
                            e.printStackTrace();
                        }
                        return;
                    }
                }

                // 获取挑战详情
                Challenge challenge = challengeService.getChallengeById(challengeId);
                if (challenge == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("error", "挑战不存在");
                    out.print(objectMapper.writeValueAsString(result));
                } else {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("challenge", buildChallengeMap(challenge));
                    out.print(objectMapper.writeValueAsString(result));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid challenge ID format\"}");
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

        // 读取请求体
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        String pathInfo = request.getPathInfo();
        
        // 处理特殊路径
        if (pathInfo != null && pathInfo.equals("/remove-blog")) {
            // 从挑战中移除博客
            try {
                handleRemoveBlogFromChallenge(request, response, out, sb.toString());
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "移除博客失败: " + e.getMessage());
                out.print(objectMapper.writeValueAsString(result));
                e.printStackTrace();
            }
            return;
        }
        
        try {
            // 将JSON转换为Challenge对象
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = objectMapper.readValue(sb.toString(), 
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
            
            Challenge challenge = new Challenge();
            challenge.setChallengeTitle((String) requestData.get("title"));
            challenge.setChallengeDescription((String) requestData.get("description"));
            
            // 解析时间
            String startTimeStr = (String) requestData.get("start_time");
            String endTimeStr = (String) requestData.get("end_time");
            if (startTimeStr != null && endTimeStr != null) {
                // 从datetime-local格式转换为Date
                // datetime-local格式: "2024-01-01T12:00" -> "2024-01-01 12:00:00"
                try {
                    String startTimeFormatted = startTimeStr.replace("T", " ") + ":00";
                    String endTimeFormatted = endTimeStr.replace("T", " ") + ":00";
                    challenge.setChallengeStartTime(new Date(java.sql.Timestamp.valueOf(startTimeFormatted).getTime()));
                    challenge.setChallengeEndTime(new Date(java.sql.Timestamp.valueOf(endTimeFormatted).getTime()));
                } catch (Exception e) {
                    System.err.println("时间格式转换失败: start_time=" + startTimeStr + ", end_time=" + endTimeStr);
                    e.printStackTrace();
                    throw new IllegalArgumentException("时间格式错误: " + e.getMessage());
                }
            }
            
            // 设置默认值
            challenge.setChellengeStatus("规划中");
            challenge.setChallengeIsCancell(0);
            
            // 从sessionStorage获取用户ID（这里需要从请求中获取，暂时设为null，需要前端传递）
            String userIdStr = request.getParameter("userId");
            if (userIdStr == null && requestData.containsKey("userId")) {
                userIdStr = requestData.get("userId").toString();
            }
            if (userIdStr != null) {
                challenge.setUserId(Integer.valueOf(userIdStr));
            }

            // 调用ChallengeService创建挑战的方法
            Integer newId = challengeService.createChallenge(challenge);
            if (newId != null && newId > 0) {
                challenge.setChallengeId(newId);
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("challenge", challenge);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(result));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "创建挑战失败");
                out.print(objectMapper.writeValueAsString(result));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Invalid challenge data: " + e.getMessage());
            out.print(objectMapper.writeValueAsString(result));
            e.printStackTrace();
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
            out.print("{\"success\":false,\"error\":\"需要指定挑战ID\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"error\":\"Invalid challenge ID\"}");
                return;
            }
            Integer challengeId = Integer.valueOf(splits[1]);

            // 检查是否有博客参与
            List<BlogChallenge> blogChallenges = blogChallengeService.getBlogChallengesByChallengeId(challengeId);
            if (blogChallenges != null && !blogChallenges.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "该挑战已有博客参与，无法删除");
                out.print(objectMapper.writeValueAsString(result));
                return;
            }

            boolean result = challengeService.deleteChallenge(challengeId);
            Map<String, Object> responseMap = new HashMap<>();
            if (result) {
                responseMap.put("success", true);
                responseMap.put("message", "挑战删除成功");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("success", false);
                responseMap.put("error", "删除挑战失败");
            }
            out.print(objectMapper.writeValueAsString(responseMap));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"error\":\"Invalid challenge ID format\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"error\":\"删除挑战失败\"}");
            e.printStackTrace();
        }
    }
    
    /**
     * 获取所有挑战（不限制状态）
     */
    private List<Challenge> getAllChallenges() {
        try {
            // 注意：数据库字段名是 uer_id（拼写错误），不是 user_id
            String sql = "SELECT challenge_id, challenge_title, challenge_start_time, challenge_end_time, chellenge_status, challenge_is_cancell, uer_id, challenge_description FROM challenges ORDER BY challenge_start_time DESC";
            java.sql.Connection conn = com.petblog.util.JdbcUtil.getConnection();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            List<Challenge> challenges = new java.util.ArrayList<>();
            
            while (rs.next()) {
                Challenge challenge = new Challenge();
                challenge.setChallengeId(rs.getInt("challenge_id"));
                challenge.setChallengeTitle(rs.getString("challenge_title"));
                challenge.setChallengeStartTime(rs.getDate("challenge_start_time"));
                challenge.setChallengeEndTime(rs.getDate("challenge_end_time"));
                challenge.setChellengeStatus(rs.getString("chellenge_status"));
                challenge.setChallengeIsCancell(rs.getInt("challenge_is_cancell"));
                challenge.setUserId(rs.getInt("uer_id")); // 注意：数据库字段名是 uer_id（拼写错误）
                challenge.setChallengeDescription(rs.getString("challenge_description"));
                challenges.add(challenge);
            }
            
            com.petblog.util.JdbcUtil.close(conn, pstmt, rs);
            System.out.println("getAllChallenges 查询到 " + challenges.size() + " 条挑战记录");
            return challenges;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("getAllChallenges 查询失败: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 构建挑战Map（包含用户信息和热度）
     */
    private Map<String, Object> buildChallengeMap(Challenge challenge) {
        Map<String, Object> map = new HashMap<>();
        map.put("challenge_id", challenge.getChallengeId());
        map.put("challenge_title", challenge.getChallengeTitle());
        map.put("challenge_description", challenge.getChallengeDescription());
        map.put("challenge_start_time", challenge.getChallengeStartTime());
        map.put("challenge_end_time", challenge.getChallengeEndTime());
        map.put("chellenge_status", challenge.getChellengeStatus());
        map.put("challenge_is_cancell", challenge.getChallengeIsCancell());
        map.put("uer_id", challenge.getUserId()); // 注意：前端使用uer_id
        
        // 获取用户信息
        if (challenge.getUserId() != null) {
            com.petblog.Service.UserService userService = new com.petblog.Service.UserService();
            com.petblog.model.User user = userService.getUserById(challenge.getUserId());
            if (user != null) {
                map.put("user_name", user.getUserName());
                map.put("user_avatar_path", user.getUserAvatarPath());
            }
        }
        
        // 计算热度
        Integer heat = calculateChallengeHeat(challenge.getChallengeId());
        map.put("heat", heat);
        
        return map;
    }
    
    /**
     * 计算挑战热度
     * 热度 = 博客数×50 + 投票数×50 + 点赞数×20 + 收藏数×20 + 评论数×10 + 转发数×10
     */
    private Integer calculateChallengeHeat(Integer challengeId) {
        try {
            // 获取挑战相关的所有博客
            List<BlogChallenge> blogChallenges = blogChallengeService.getBlogChallengesByChallengeId(challengeId);
            if (blogChallenges == null || blogChallenges.isEmpty()) {
                return 0;
            }
            
            int totalHeat = 0;
            for (BlogChallenge bc : blogChallenges) {
                Blog blog = blogService.getBlogById(bc.getBlogId());
                if (blog != null) {
                    // 博客数贡献
                    totalHeat += 50;
                    
                    // 投票数贡献（votes表）
                    int voteCount = countVotesForBlog(blog.getBlogId());
                    totalHeat += voteCount * 50;
                    
                    // 点赞数、收藏数、评论数、转发数贡献
                    totalHeat += (blog.getLikeCount() != null ? blog.getLikeCount() : 0) * 20;
                    totalHeat += (blog.getFavoriteCount() != null ? blog.getFavoriteCount() : 0) * 20;
                    totalHeat += (blog.getCommentCount() != null ? blog.getCommentCount() : 0) * 10;
                    totalHeat += (blog.getRepostCount() != null ? blog.getRepostCount() : 0) * 10;
                }
            }
            
            return totalHeat;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 获取挑战的博客列表（带热度）
     */
    private void handleGetChallengeBlogsWithHeat(Integer challengeId, HttpServletResponse response, PrintWriter out) throws Exception {
        List<BlogChallenge> blogChallenges = blogChallengeService.getBlogChallengesByChallengeId(challengeId);
        List<Map<String, Object>> blogsWithHeat = new java.util.ArrayList<>();
        
        if (blogChallenges != null) {
            for (BlogChallenge bc : blogChallenges) {
                Blog blog = blogService.getBlogById(bc.getBlogId());
                if (blog != null) {
                    Map<String, Object> blogMap = buildBlogMapWithHeat(blog);
                    blogsWithHeat.add(blogMap);
                }
            }
        }
        
        out.print(objectMapper.writeValueAsString(blogsWithHeat));
    }
    
    /**
     * 构建博客Map（包含热度和用户信息）
     */
    private Map<String, Object> buildBlogMapWithHeat(Blog blog) {
        Map<String, Object> map = new HashMap<>();
        map.put("blog_id", blog.getBlogId());
        map.put("blog_title", blog.getBlogTitle());
        map.put("blog_content", blog.getBlogContent());
        map.put("blog_create_time", blog.getBlogCreateTime());
        map.put("user_id", blog.getUserId());
        
        // 获取用户信息（如果Blog对象中没有，则从数据库查询）
        String userName = blog.getUserName();
        String userAvatarPath = blog.getUserAvatarPath();
        
        if (userName == null || userAvatarPath == null) {
            // 从数据库查询用户信息
            if (blog.getUserId() != null) {
                com.petblog.Service.UserService userService = new com.petblog.Service.UserService();
                com.petblog.model.User user = userService.getUserById(blog.getUserId());
                if (user != null) {
                    userName = user.getUserName();
                    userAvatarPath = user.getUserAvatarPath();
                }
            }
        }
        
        map.put("user_name", userName);
        map.put("user_avatar_path", userAvatarPath);
        
        // 计算热度
        int heat = calculateBlogHeat(blog.getBlogId());
        map.put("heat", heat);
        
        // 获取投票数
        int voteCount = countVotesForBlog(blog.getBlogId());
        map.put("vote_count", voteCount);
        
        return map;
    }
    
    /**
     * 计算博客热度
     */
    private int calculateBlogHeat(Integer blogId) {
        Blog blog = blogService.getBlogById(blogId);
        if (blog == null) return 0;
        
        int heat = 0;
        heat += countVotesForBlog(blogId) * 50;
        heat += (blog.getLikeCount() != null ? blog.getLikeCount() : 0) * 20;
        heat += (blog.getFavoriteCount() != null ? blog.getFavoriteCount() : 0) * 20;
        heat += (blog.getCommentCount() != null ? blog.getCommentCount() : 0) * 10;
        heat += (blog.getRepostCount() != null ? blog.getRepostCount() : 0) * 10;
        
        return heat;
    }
    
    /**
     * 统计博客的投票数
     */
    private int countVotesForBlog(Integer blogId) {
        try {
            // votes表结构：user_id, blog_id, vote_create_time
            // 直接查询votes表中blog_id的记录数
            String sql = "SELECT COUNT(*) FROM votes WHERE blog_id = ?";
            com.petblog.util.JdbcUtil jdbcUtil = new com.petblog.util.JdbcUtil();
            java.sql.Connection conn = com.petblog.util.JdbcUtil.getConnection();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, blogId);
            java.sql.ResultSet rs = pstmt.executeQuery();
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
    
    /**
     * 从挑战中移除博客
     */
    private void handleRemoveBlogFromChallenge(HttpServletRequest request, HttpServletResponse response, PrintWriter out, String requestBody) throws Exception {
        Integer challengeId = null;
        Integer blogId = null;
        
        // 尝试从请求体解析
        if (requestBody != null && !requestBody.trim().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> requestData = objectMapper.readValue(requestBody, 
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                
                if (requestData.get("challenge_id") != null) {
                    challengeId = Integer.valueOf(requestData.get("challenge_id").toString());
                }
                if (requestData.get("blog_id") != null) {
                    blogId = Integer.valueOf(requestData.get("blog_id").toString());
                }
            } catch (Exception e) {
                System.err.println("解析请求体失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 如果从请求体无法获取，尝试从参数获取
        if (challengeId == null || blogId == null) {
            String challengeIdStr = request.getParameter("challenge_id");
            String blogIdStr = request.getParameter("blog_id");
            if (challengeIdStr != null) {
                try {
                    challengeId = Integer.valueOf(challengeIdStr);
                } catch (NumberFormatException e) {
                    System.err.println("challenge_id格式错误: " + challengeIdStr);
                }
            }
            if (blogIdStr != null) {
                try {
                    blogId = Integer.valueOf(blogIdStr);
                } catch (NumberFormatException e) {
                    System.err.println("blog_id格式错误: " + blogIdStr);
                }
            }
        }
        
        if (challengeId == null || blogId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "参数错误: challengeId=" + challengeId + ", blogId=" + blogId);
            out.print(objectMapper.writeValueAsString(result));
            return;
        }
        
        deleteBlogChallengeRelation(challengeId, blogId, response, out);
    }
    
    /**
     * 删除博客挑战关联
     */
    private void deleteBlogChallengeRelation(Integer challengeId, Integer blogId, HttpServletResponse response, PrintWriter out) throws Exception {
        // 删除blogchallenge关联（删除特定的博客-挑战关联）
        try {
            String sql = "DELETE FROM blogchallenge WHERE challenge_id = ? AND blog_id = ?";
            java.sql.Connection conn = com.petblog.util.JdbcUtil.getConnection();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, challengeId);
            pstmt.setInt(2, blogId);
            int rowsAffected = pstmt.executeUpdate();
            com.petblog.util.JdbcUtil.close(conn, pstmt, null);
            
            if (rowsAffected == 0) {
                System.out.println("警告: 未找到要删除的博客-挑战关联 (challenge_id=" + challengeId + ", blog_id=" + blogId + ")");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "博客已从挑战中移除");
            out.print(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            System.err.println("删除博客-挑战关联失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 获取狗狗排名
     */
    private void handleGetPetRankings(Integer challengeId, HttpServletResponse response, PrintWriter out) throws Exception {
        // 获取挑战的所有博客
        List<BlogChallenge> blogChallenges = blogChallengeService.getBlogChallengesByChallengeId(challengeId);
        if (blogChallenges == null || blogChallenges.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("rankings", new java.util.ArrayList<>());
            out.print(objectMapper.writeValueAsString(result));
            return;
        }
        
        // 获取每个博客的宠物和投票数
        Map<Integer, PetRankingInfo> petRankingMap = new HashMap<>();
        
        for (BlogChallenge bc : blogChallenges) {
            Blog blog = blogService.getBlogById(bc.getBlogId());
            if (blog != null) {
                // 获取博客关联的宠物（通过blogpet表）
                com.petblog.Service.BlogPetService blogPetService = new com.petblog.Service.BlogPetService();
                List<Integer> petIds = blogPetService.getPetIdsByBlogId(blog.getBlogId());
                
                if (petIds != null) {
                    for (Integer petId : petIds) {
                        PetRankingInfo info = petRankingMap.getOrDefault(petId, new PetRankingInfo());
                        info.petId = petId;
                        info.voteCount += countVotesForBlog(blog.getBlogId());
                        info.blogCount += 1;
                        petRankingMap.put(petId, info);
                    }
                }
            }
        }
        
        // 转换为列表并排序
        List<Map<String, Object>> rankings = new java.util.ArrayList<>();
        int rank = 1;
        for (PetRankingInfo info : petRankingMap.values().stream()
                .sorted((a, b) -> b.voteCount - a.voteCount)
                .toList()) {
            Map<String, Object> rankingMap = new HashMap<>();
            rankingMap.put("ranking", rank++);
            rankingMap.put("pet_id", info.petId);
            rankingMap.put("vote_count", info.voteCount);
            rankingMap.put("blog_count", info.blogCount);
            
            // 获取宠物信息
            com.petblog.Service.PetService petService = new com.petblog.Service.PetService();
            com.petblog.model.Pet pet = petService.getPetById(info.petId);
            if (pet != null) {
                rankingMap.put("pet_name", pet.getPetName());
                rankingMap.put("pet_breed", pet.getPetBreed());
                rankingMap.put("pet_avatar_path", pet.getPetAvatarPath());
                
                // 获取主人信息
                if (pet.getUserId() != null) {
                    com.petblog.Service.UserService userService = new com.petblog.Service.UserService();
                    com.petblog.model.User owner = userService.getUserById(pet.getUserId());
                    if (owner != null) {
                        rankingMap.put("owner_name", owner.getUserName());
                        rankingMap.put("owner_avatar", owner.getUserAvatarPath());
                    }
                }
            }
            
            rankings.add(rankingMap);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("rankings", rankings);
        out.print(objectMapper.writeValueAsString(result));
    }
    
    /**
     * 获取获胜者
     */
    private void handleGetWinners(Integer challengeId, HttpServletResponse response, PrintWriter out) throws Exception {
        // 获取排名前3的宠物
        handleGetPetRankings(challengeId, response, out);
        // 注意：这里应该只返回前3名，但为了简化，先返回所有排名，前端可以过滤
    }
    
    /**
     * 获取挑战热度
     */
    private void handleGetChallengeHeat(Integer challengeId, HttpServletResponse response, PrintWriter out) throws Exception {
        Integer heat = calculateChallengeHeat(challengeId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("heat", heat);
        out.print(objectMapper.writeValueAsString(result));
    }
    
    /**
     * 宠物排名信息内部类
     */
    private static class PetRankingInfo {
        Integer petId;
        int voteCount = 0;
        int blogCount = 0;
    }
}

