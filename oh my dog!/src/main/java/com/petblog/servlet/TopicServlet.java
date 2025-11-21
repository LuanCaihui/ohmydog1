package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.TopicService;
import com.petblog.model.Topic;
import com.petblog.util.JsonUtil;
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

@WebServlet("/api/topics/*")
public class TopicServlet extends HttpServlet {
    private final TopicService topicService = new TopicService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        
        // 处理 /api/topics/all 请求
        if (pathInfo != null && pathInfo.equals("/all")) {
            try {
                List<Topic> topics = topicService.searchTopicsByName("", 1, 1000);
                if (topics == null) {
                    topics = new java.util.ArrayList<>();
                }
                out.print(objectMapper.writeValueAsString(topics));
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"获取话题列表失败\"}");
            }
            return;
        }
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // 搜索话题或获取话题列表
            String keyword = request.getParameter("keyword");
            String type = request.getParameter("type"); // popular, latest, all
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");
            String limitParam = request.getParameter("limit");

            try {
                int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;
                int limit = limitParam != null ? Integer.parseInt(limitParam) : 10;

                if (keyword != null && !keyword.isEmpty()) {
                    // 根据关键词搜索话题
                    List<Topic> topics = topicService.searchTopicsByName(keyword, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(topics));
                } else if ("popular".equals(type)) {
                    // 获取热门话题
                    List<Topic> topics = topicService.getPopularTopics(limit);
                    out.print(objectMapper.writeValueAsString(topics));
                } else if ("latest".equals(type)) {
                    // 获取最新话题
                    List<Topic> topics = topicService.getLatestTopics(limit);
                    out.print(objectMapper.writeValueAsString(topics));
                } else if ("all".equals(type) || type == null) {
                    // 获取所有话题（分页）
                    List<Topic> topics = topicService.searchTopicsByName("", pageNum, pageSize);
                    if (topics == null) {
                        topics = new java.util.ArrayList<>();
                    }
                    out.print(objectMapper.writeValueAsString(topics));
                } else {
                    // 默认获取热门话题
                    List<Topic> topics = topicService.getPopularTopics(limit);
                    out.print(objectMapper.writeValueAsString(topics));
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
                    out.print("{\"error\":\"Invalid topic ID\"}");
                    return;
                }
                Integer topicId = Integer.valueOf(splits[1]);

                Topic topic = topicService.getTopicById(topicId);
                if (topic == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Topic not found\"}");
                } else {
                    out.print(objectMapper.writeValueAsString(topic));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid topic ID format\"}");
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
            // 将JSON转换为Topic对象
            Topic topic = objectMapper.readValue(sb.toString(), Topic.class);

            // 设置默认创建时间
            if (topic.getTopicCreateTime() == null) {
                topic.setTopicCreateTime(LocalDateTime.now());
            }

            // 调用TopicService创建话题的方法
            Integer result = topicService.createTopic(topic);
            if (result > 0) {
                topic.setTopicId(result);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(topic));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建话题失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid topic data\"}");
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
            out.print("{\"error\":\"需要指定话题ID\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid topic ID\"}");
                return;
            }
            Integer topicId = Integer.valueOf(splits[1]);

            // 读取请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 将JSON转换为Topic对象
            Topic topic = objectMapper.readValue(sb.toString(), Topic.class);
            topic.setTopicId(topicId); // 确保ID一致

            // 调用TopicService更新话题的方法
            boolean result = topicService.updateTopic(topic);
            if (result) {
                out.print(objectMapper.writeValueAsString(topic));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"更新话题失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid topic ID format\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid topic data\"}");
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
            out.print("{\"error\":\"需要指定话题ID\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid topic ID\"}");
                return;
            }
            Integer topicId = Integer.valueOf(splits[1]);

            // 调用TopicService删除话题的方法
            boolean result = topicService.deleteTopic(topicId);
            if (result) {
                out.print("{\"message\":\"话题删除成功\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"话题未找到或删除失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid topic ID format\"}");
        }
    }
}
