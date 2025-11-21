package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.PetService;
import com.petblog.model.Pet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/api/pets/*")
public class PetServlet extends HttpServlet {
    private final PetService petService = new PetService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 根据用户ID获取宠物列表
            String userIdParam = request.getParameter("userId");
            String nameKeyword = request.getParameter("name");
            String typeParam = request.getParameter("type");
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");

            try {
                if (userIdParam != null) {
                    Integer userId = Integer.valueOf(userIdParam);
                    if (nameKeyword != null && !nameKeyword.isEmpty()) {
                        // 根据名称搜索宠物
                        List<Pet> pets = petService.searchPetsByName(userId, nameKeyword);
                        out.print(objectMapper.writeValueAsString(pets));
                    } else {
                        // 获取用户所有宠物
                        List<Pet> pets = petService.getPetsByUserId(userId);
                        out.print(objectMapper.writeValueAsString(pets));
                    }
                } else if (typeParam != null && !typeParam.isEmpty()) {
                    // 根据类型查询宠物
                    int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                    int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;
                    List<Pet> pets = petService.getPetsByType(typeParam, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(pets));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定用户ID或类型参数\"}");
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
                    out.print("{\"error\":\"Invalid pet ID\"}");
                    return;
                }
                Integer petId = Integer.valueOf(splits[1]);

                Pet pet = petService.getPetById(petId);
                if (pet == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Pet not found\"}");
                } else {
                    out.print(objectMapper.writeValueAsString(pet));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid pet ID format\"}");
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
            // 将JSON转换为Pet对象
            Pet pet = objectMapper.readValue(sb.toString(), Pet.class);

            // 调用PetService创建宠物的方法
            Integer result = petService.createPet(pet);
            if (result > 0) {
                pet.setPetId(result);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(pet));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建宠物失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid pet data\"}");
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
            out.print("{\"error\":\"需要指定宠物ID\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid pet ID\"}");
                return;
            }
            Integer petId = Integer.valueOf(splits[1]);

            // 读取请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 将JSON转换为Pet对象
            Pet pet = objectMapper.readValue(sb.toString(), Pet.class);
            pet.setPetId(petId); // 确保ID一致

            // 调用PetService更新宠物的方法
            boolean result = petService.updatePet(pet);
            if (result) {
                out.print(objectMapper.writeValueAsString(pet));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"更新宠物失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid pet ID format\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid pet data\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 批量删除宠物
            String petIdsParam = request.getParameter("petIds");
            if (petIdsParam != null) {
                try {
                    List<Integer> petIds = Arrays.stream(petIdsParam.split(","))
                            .map(Integer::valueOf)
                            .collect(Collectors.toList());

                    boolean result = petService.batchDeletePets(petIds);
                    if (result) {
                        out.print("{\"message\":\"批量删除成功\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"批量删除失败\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"宠物ID格式错误\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"需要指定petIds参数\"}");
            }
        } else {
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length < 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid pet ID\"}");
                    return;
                }
                Integer petId = Integer.valueOf(splits[1]);

                // 调用PetService删除宠物的方法
                boolean result = petService.deletePet(petId);
                if (result) {
                    out.print("{\"message\":\"宠物删除成功\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"宠物未找到或删除失败\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid pet ID format\"}");
            }
        }
    }
}
