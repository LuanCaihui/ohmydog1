package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.ChallengeService;
import com.petblog.model.Challenge;
import com.petblog.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/challenges/*")
public class ChallengeServlet extends HttpServlet {
    private final ChallengeService challengeService = new ChallengeService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 获取挑战列表（分页）
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");
            String statusParam = request.getParameter("status"); // active|completed

            try {
                int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

                java.util.List<Challenge> challenges;
                if ("completed".equalsIgnoreCase(statusParam)) {
                    challenges = challengeService.getCompletedChallenges(pageNum, pageSize);
                } else { // 默认为进行中
                    challenges = challengeService.getActiveChallenges(pageNum, pageSize);
                }
                out.print(objectMapper.writeValueAsString(challenges));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
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

                // 调用ChallengeService获取挑战详情的方法
                Challenge challenge = challengeService.getChallengeById(challengeId);
                if (challenge == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"挑战不存在\"}");
                } else {
                    out.print(objectMapper.writeValueAsString(challenge));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid challenge ID format\"}");
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
            // 将JSON转换为Challenge对象
            Challenge challenge = objectMapper.readValue(sb.toString(), Challenge.class);

            // 调用ChallengeService创建挑战的方法
            Integer newId = challengeService.createChallenge(challenge);
            if (newId != null && newId > 0) {
                challenge.setChallengeId(newId);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(challenge));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建挑战失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid challenge data\"}");
        }
    }
}

