package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.PetChallengeService;
import com.petblog.model.PetChallenge;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/pet-challenges/*")
public class PetChallengeServlet extends HttpServlet {
    private final PetChallengeService petChallengeService = new PetChallengeService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 根据挑战ID或宠物ID获取关联信息
            String challengeIdParam = request.getParameter("challengeId");
            String petIdParam = request.getParameter("petId");
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");

            try {
                int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

                if (challengeIdParam != null) {
                    Integer challengeId = Integer.valueOf(challengeIdParam);
                    // 调用PetChallengeService根据挑战ID获取宠物列表的方法
                    List<Integer> petIds = petChallengeService.getPetIdsByChallengeId(challengeId, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(petIds));
                } else if (petIdParam != null) {
                    Integer petId = Integer.valueOf(petIdParam);
                    // 调用PetChallengeService根据宠物ID获取挑战信息的方法
                    List<Integer> challengeIds = petChallengeService.getChallengeIdsByPetId(petId);
                    out.print(objectMapper.writeValueAsString(challengeIds));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定challengeId或petId参数\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
            }
        } else {
            // 检查特定宠物是否参与了特定挑战 /api/pet-challenges/{petId}/{challengeId}
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length < 3) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"路径格式错误，应为 /api/pet-challenges/{petId}/{challengeId}\"}");
                    return;
                }
                Integer petId = Integer.valueOf(splits[1]);
                Integer challengeId = Integer.valueOf(splits[2]);

                // 检查宠物是否已参与某挑战
                boolean hasParticipated = petChallengeService.hasPetParticipated(petId, challengeId);
                out.print(objectMapper.writeValueAsString(hasParticipated));
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
            // 将JSON转换为PetChallenge对象
            PetChallenge petChallenge = objectMapper.readValue(sb.toString(), PetChallenge.class);

            // 调用PetChallengeService创建宠物挑战关联的方法
            boolean result = petChallengeService.addPetToChallenge(petChallenge);
            if (result) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(petChallenge));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建宠物挑战关联失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid pet-challenge data\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 批量操作：取消宠物参与的所有挑战或移除参与某挑战的所有宠物
            String petIdParam = request.getParameter("petId");
            String challengeIdParam = request.getParameter("challengeId");

            try {
                if (petIdParam != null) {
                    Integer petId = Integer.valueOf(petIdParam);
                    // 取消宠物参与的所有挑战
                    boolean result = petChallengeService.removePetFromAllChallenges(petId);
                    if (result) {
                        out.print("{\"message\":\"宠物所有挑战参与已取消\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"取消宠物所有挑战参与失败\"}");
                    }
                } else if (challengeIdParam != null) {
                    Integer challengeId = Integer.valueOf(challengeIdParam);
                    // 移除参与某挑战的所有宠物
                    boolean result = petChallengeService.removeAllPetsFromChallenge(challengeId);
                    if (result) {
                        out.print("{\"message\":\"挑战所有参与宠物已移除\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"移除挑战所有参与宠物失败\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定petId或challengeId参数\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
            }
        } else {
            try {
                // 解析路径 /api/pet-challenges/{petId}/{challengeId}
                String[] splits = pathInfo.split("/");
                if (splits.length < 3) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid pet-challenge path\"}");
                    return;
                }
                Integer petId = Integer.valueOf(splits[1]);
                Integer challengeId = Integer.valueOf(splits[2]);

                // 调用PetChallengeService删除宠物挑战关联的方法
                boolean result = petChallengeService.removePetFromChallenge(petId, challengeId);
                if (result) {
                    out.print("{\"message\":\"宠物挑战关联已删除\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"宠物挑战关联未找到或删除失败\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid pet or challenge ID format\"}");
            }
        }
    }
}
