package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.MedicineService;
import com.petblog.model.Medicine;
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

@WebServlet("/api/medicines/*")
public class MedicineServlet extends HttpServlet {
    private final MedicineService medicineService = new MedicineService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 根据宠物ID或名称搜索获取药品列表
            String petIdParam = request.getParameter("petId");
            String nameKeyword = request.getParameter("name");
            String categoryParam = request.getParameter("category");
            String pageNumParam = request.getParameter("pageNum");
            String pageSizeParam = request.getParameter("pageSize");

            try {
                int pageNum = pageNumParam != null ? Integer.parseInt(pageNumParam) : 1;
                int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;

                if (petIdParam != null) {
                    Integer petId = Integer.valueOf(petIdParam);
                    // 注意：根据现有DAO和Service，没有根据petId查询药品的方法
                    // 这里保持原逻辑，返回未实现错误
                    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    out.print("{\"error\":\"功能未实现\"}");
                } else if (nameKeyword != null && !nameKeyword.isEmpty()) {
                    // 调用MedicineService根据名称搜索药品的方法
                    List<Medicine> medicines = medicineService.searchMedicinesByName(nameKeyword, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(medicines));
                } else if (categoryParam != null && !categoryParam.isEmpty()) {
                    // 调用MedicineService根据类别查询药品的方法
                    List<Medicine> medicines = medicineService.getMedicinesByCategory(categoryParam, pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(medicines));
                } else {
                    // 调用MedicineService获取所有药品的方法
                    List<Medicine> medicines = medicineService.getAllMedicines(pageNum, pageSize);
                    out.print(objectMapper.writeValueAsString(medicines));
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
                    out.print("{\"error\":\"Invalid medicine ID\"}");
                    return;
                }
                Integer medicineId = Integer.valueOf(splits[1]);

                // 调用MedicineService获取药品详情的方法
                Medicine medicine = medicineService.getMedicineById(medicineId);
                if (medicine != null) {
                    out.print(objectMapper.writeValueAsString(medicine));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"药品未找到\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid medicine ID format\"}");
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
            // 将JSON转换为Medicine对象
            Medicine medicine = objectMapper.readValue(sb.toString(), Medicine.class);

            // 设置默认发布时间
            if (medicine.getReleaseTime() == null) {
                medicine.setReleaseTime(new Date());
            }

            // 调用MedicineService创建药品记录的方法
            Integer result = medicineService.createMedicine(medicine);
            if (result > 0) {
                medicine.setMedicineId(result);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(medicine));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建药品记录失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid medicine data\"}");
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
            out.print("{\"error\":\"需要指定药品ID\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid medicine ID\"}");
                return;
            }
            Integer medicineId = Integer.valueOf(splits[1]);

            // 读取请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 将JSON转换为Medicine对象
            Medicine medicine = objectMapper.readValue(sb.toString(), Medicine.class);
            medicine.setMedicineId(medicineId); // 确保ID一致

            // 调用MedicineService更新药品记录的方法
            boolean result = medicineService.updateMedicine(medicine);
            if (result) {
                out.print(objectMapper.writeValueAsString(medicine));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"更新药品记录失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid medicine ID format\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid medicine data\"}");
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
            out.print("{\"error\":\"需要指定药品ID\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid medicine ID\"}");
                return;
            }
            Integer medicineId = Integer.valueOf(splits[1]);

            // 调用MedicineService删除药品记录的方法
            boolean result = medicineService.deleteMedicine(medicineId);
            if (result) {
                out.print("{\"message\":\"药品删除成功\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"药品未找到或删除失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid medicine ID format\"}");
        }
    }
}
