package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.InvestigateService;
import com.petblog.model.Investigate;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/investigates/*")
public class InvestigateServlet extends HttpServlet {
    private final InvestigateService investigateService = new InvestigateService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 获取调查列表
            String userIdParam = request.getParameter("userId");
            String medicineIdParam = request.getParameter("medicineId");

            try {
                if (userIdParam != null) {
                    // 根据用户ID查询其参与的所有药品调查记录
                    Integer userId = Integer.valueOf(userIdParam);
                    List<Investigate> investigates = investigateService.getInvestigatesByUserId(userId);
                    out.print(objectMapper.writeValueAsString(investigates));
                } else if (medicineIdParam != null) {
                    // 根据药品ID查询所有参与该药品调查的用户ID
                    Integer medicineId = Integer.valueOf(medicineIdParam);
                    List<Integer> userIds = investigateService.getUserIdsByMedicineId(medicineId);
                    out.print(objectMapper.writeValueAsString(userIds));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定userId或medicineId参数\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
            }
        } else {
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length < 3) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"路径格式错误，应为 /api/investigates/{medicineId}/{userId}\"}");
                    return;
                }
                Integer medicineId = Integer.valueOf(splits[1]);
                Integer userId = Integer.valueOf(splits[2]);

                // 检查用户是否已参与某药品的调查
                boolean hasParticipated = investigateService.hasUserParticipated(userId, medicineId);
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
            // 将JSON转换为Investigate对象
            Investigate investigate = objectMapper.readValue(sb.toString(), Investigate.class);

            // 调用InvestigateService创建调查的方法
            Integer result = investigateService.createInvestigate(investigate);
            if (result > 0) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(investigate));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"创建调查记录失败\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid investigate data\"}");
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
            out.print("{\"error\":\"需要提供药品ID和用户ID路径\"}");
            return;
        }

        try {
            String[] splits = pathInfo.split("/");
            if (splits.length < 3) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"路径格式错误，应为 /api/investigates/{medicineId}/{userId}\"}");
                return;
            }
            Integer medicineId = Integer.valueOf(splits[1]);
            Integer userId = Integer.valueOf(splits[2]);

            // 读取请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 将JSON转换为Investigate对象
            Investigate investigate = objectMapper.readValue(sb.toString(), Investigate.class);
            investigate.setMedicineId(medicineId);
            investigate.setUserId(userId);

            // 调用InvestigateService更新调查反馈内容的方法
            boolean result = investigateService.updateInvestigateFeedback(investigate);
            if (result) {
                out.print(objectMapper.writeValueAsString(investigate));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"更新调查反馈内容失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"ID格式错误\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid investigate data\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 删除用户参与的所有调查记录
            String userIdParam = request.getParameter("userId");
            String medicineIdParam = request.getParameter("medicineId");

            try {
                if (userIdParam != null) {
                    Integer userId = Integer.valueOf(userIdParam);
                    boolean result = investigateService.deleteInvestigatesByUserId(userId);
                    if (result) {
                        out.print("{\"message\":\"用户所有调查记录已删除\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"删除用户调查记录失败\"}");
                    }
                } else if (medicineIdParam != null) {
                    Integer medicineId = Integer.valueOf(medicineIdParam);
                    boolean result = investigateService.deleteInvestigatesByMedicineId(medicineId);
                    if (result) {
                        out.print("{\"message\":\"药品所有调查记录已删除\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"删除药品调查记录失败\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"需要指定userId或medicineId参数\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"参数格式错误\"}");
            }
        } else {
            try {
                String[] splits = pathInfo.split("/");
                if (splits.length < 3) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"路径格式错误，应为 /api/investigates/{medicineId}/{userId}\"}");
                    return;
                }
                Integer medicineId = Integer.valueOf(splits[1]);
                Integer userId = Integer.valueOf(splits[2]);

                // 创建一个Investigate对象用于删除
                Investigate investigate = new Investigate();
                investigate.setMedicineId(medicineId);
                investigate.setUserId(userId);

                // 调用InvestigateService删除调查记录的方法
                // 注意：由于是联合主键，这里简化处理直接使用medicineId作为参数
                boolean result = investigateService.deleteInvestigate(medicineId);
                if (result) {
                    out.print("{\"message\":\"调查记录已删除\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"调查记录未找到或删除失败\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"ID格式错误\"}");
            }
        }
    }
}
