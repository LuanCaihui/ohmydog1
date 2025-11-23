package com.petblog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petblog.Service.ConsultationService;
import com.petblog.Service.DiagnosisExplanationService;
import com.petblog.Service.DiseaseService;
import com.petblog.Service.DiseaseSymptomService;
import com.petblog.Service.SymptomService;
import com.petblog.Service.StructuredQuestionService;
import com.petblog.model.Consultation;
import com.petblog.model.Disease;
import com.petblog.model.DiseaseSymptom;
import com.petblog.model.Symptom;
import com.petblog.model.SymptomQuestion;
import com.petblog.util.DecisionTree;
import com.petblog.util.JsonUtil;
import com.petblog.util.NaiveBayes;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 诊断问诊控制器
 * 提供动态问诊功能，包括：
 * - 获取第一个问题
 * - 获取下一个问题
 * - 执行诊断
 * - 保存问诊记录
 */
@WebServlet("/api/diagnosis/*")
public class DiagnosisServlet extends HttpServlet {
    private final SymptomService symptomService = new SymptomService();
    private final DiseaseService diseaseService = new DiseaseService();
    private final DiseaseSymptomService diseaseSymptomService = new DiseaseSymptomService();
    private final ConsultationService consultationService = new ConsultationService();
    private final DiagnosisExplanationService explanationService = new DiagnosisExplanationService();
    private final StructuredQuestionService structuredQuestionService = new StructuredQuestionService();
    private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    /**
     * 构建疾病-症状关系映射
     */
    private Map<Integer, List<DiseaseSymptom>> buildDiseaseSymptomMap() {
        Map<Integer, List<DiseaseSymptom>> map = new HashMap<>();
        List<DiseaseSymptom> allRelations = diseaseSymptomService.getAllRelations();
        
        if (allRelations != null) {
            for (DiseaseSymptom ds : allRelations) {
                Integer diseaseId = ds.getDiseaseId();
                if (!map.containsKey(diseaseId)) {
                    map.put(diseaseId, new ArrayList<>());
                }
                map.get(diseaseId).add(ds);
            }
        }
        
        return map;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.equals("/history")) {
                // GET /api/diagnosis/history?userId=xxx - 获取用户的问诊历史
                String userIdStr = request.getParameter("userId");
                if (userIdStr == null || userIdStr.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"userId参数不能为空\"}");
                    return;
                }
                
                try {
                    Integer userId = Integer.parseInt(userIdStr);
                    List<Consultation> consultations = consultationService.getConsultationsByUserId(userId);
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("consultations", consultations != null ? consultations : new ArrayList<>());
                    
                    out.print(objectMapper.writeValueAsString(result));
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"userId参数格式错误\"}");
                }
            } else if (pathInfo != null && pathInfo.equals("/start")) {
                // GET /api/diagnosis/start - 获取主诉问题列表
                List<SymptomQuestion> mainComplaints = structuredQuestionService.getMainComplaintQuestions();
                
                if (mainComplaints == null || mainComplaints.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"没有可用的主诉数据\"}");
                    return;
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("questions", mainComplaints);
                result.put("type", "main_complaint");
                
                out.print(objectMapper.writeValueAsString(result));
                
            } else if (pathInfo != null && pathInfo.equals("/symptoms")) {
                // GET /api/diagnosis/symptoms - 获取所有症状列表
                List<Symptom> allSymptoms = symptomService.getAllSymptoms();
                
                if (allSymptoms == null) {
                    allSymptoms = new ArrayList<>();
                }
                
                out.print(objectMapper.writeValueAsString(allSymptoms));
                
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid path\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"服务器错误: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();

        // 读取请求体
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        try {
            if (pathInfo != null && pathInfo.equals("/next")) {
                // POST /api/diagnosis/next - 获取下一个问题（支持结构化问诊）
                @SuppressWarnings("unchecked")
                Map<String, Object> requestData = objectMapper.readValue(sb.toString(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));

                @SuppressWarnings("unchecked")
                List<Integer> selectedSymptoms = (List<Integer>) requestData.get("selectedSymptoms");
                
                // 允许selectedSymptoms为空数组（第一次调用或跳过问题时）
                if (selectedSymptoms == null) {
                    selectedSymptoms = new ArrayList<>();
                }
                
                // 获取所有已问过的症状（包括选择"是"和"否"的）
                @SuppressWarnings("unchecked")
                List<Integer> askedSymptoms = (List<Integer>) requestData.get("askedSymptoms");
                if (askedSymptoms == null) {
                    askedSymptoms = new ArrayList<>(selectedSymptoms);
                }
                
                // 获取已回答的问题总数
                Integer totalQuestions = (Integer) requestData.get("questionCount");
                if (totalQuestions == null) {
                    totalQuestions = askedSymptoms.size();
                }
                
                // 获取主诉ID（如果已选择主诉）
                Integer mainComplaintId = (Integer) requestData.get("mainComplaintId");
                
                // 获取已回答的问题维度（用于动态追问）
                String answeredDimensionStr = (String) requestData.get("answeredDimension");
                SymptomQuestion.QuestionDimension answeredDimension = null;
                if (answeredDimensionStr != null) {
                    try {
                        answeredDimension = SymptomQuestion.QuestionDimension.valueOf(answeredDimensionStr);
                    } catch (IllegalArgumentException e) {
                        // 忽略无效的维度值
                    }
                }

                // 如果已选择主诉，生成相关追问
                if (mainComplaintId != null && answeredDimension != null) {
                    SymptomQuestion nextQuestion = structuredQuestionService.getNextFollowUpQuestion(
                        mainComplaintId, answeredDimension, selectedSymptoms);
                    
                    if (nextQuestion != null) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("symptomId", nextQuestion.getSymptomId());
                        result.put("name", nextQuestion.getSymptomName());
                        result.put("questionText", nextQuestion.getQuestionText());
                        result.put("options", nextQuestion.getOptions());
                        result.put("isMultiple", nextQuestion.getIsMultiple());
                        result.put("questionType", nextQuestion.getQuestionType().name());
                        result.put("dimension", nextQuestion.getDimension() != null ? nextQuestion.getDimension().name() : null);
                        result.put("parentSymptomId", nextQuestion.getParentSymptomId());
                        result.put("finished", false);
                        result.put("structured", true);
                        
                        out.print(objectMapper.writeValueAsString(result));
                        return;
                    }
                }

                // 获取所有症状和疾病
                List<Symptom> allSymptoms = symptomService.getAllSymptoms();
                List<Disease> allDiseases = diseaseService.getAllDiseases();
                Map<Integer, List<DiseaseSymptom>> diseaseSymMap = buildDiseaseSymptomMap();

                // 新的结束逻辑：不强制结束，只有当某个病症达到一定概率时才结束
                // 至少问5个问题才能检查是否结束
                int minQuestions = 5;
                boolean hasEnoughQuestions = totalQuestions >= minQuestions;
                
                // 结束条件：已回答至少5个问题 AND 某个病症的概率>=45%
                boolean shouldStop = false;
                if (hasEnoughQuestions) {
                    // 检查是否有病症达到45%的概率
                    shouldStop = DecisionTree.shouldStop(selectedSymptoms, allDiseases, diseaseSymMap, 0.45);
                }
                
                if (shouldStop) {
                    // 执行诊断 - 返回多个可能的疾病（前5个）
                    List<NaiveBayes.Result> diagnoses = NaiveBayes.diagnoseMultiple(selectedSymptoms, allDiseases, diseaseSymMap, 5);
                    
                    if (diagnoses == null || diagnoses.isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"无法诊断，请选择更多症状\"}");
                        return;
                    }

                    Map<String, Object> result = new HashMap<>();
                    result.put("finished", true);
                    
                    // 转换为前端需要的格式
                    List<Map<String, Object>> diseasesList = new ArrayList<>();
                    for (NaiveBayes.Result d : diagnoses) {
                        Map<String, Object> diseaseMap = new HashMap<>();
                        diseaseMap.put("disease", d.disease);
                        diseaseMap.put("diseaseId", d.diseaseId);
                        diseaseMap.put("probability", d.probability);
                        diseasesList.add(diseaseMap);
                    }
                    result.put("diseases", diseasesList);
                    
                    out.print(objectMapper.writeValueAsString(result));
                } else {
                    // 获取下一个问题（传入已问过的症状列表，避免重复）
                    Integer nextSymptomId = DecisionTree.getNextQuestion(selectedSymptoms, askedSymptoms, allSymptoms, allDiseases, diseaseSymMap);
                    
                    if (nextSymptomId == null) {
                        // 没有更多问题了，执行诊断 - 返回多个可能的疾病（前5个）
                        List<NaiveBayes.Result> diagnoses = NaiveBayes.diagnoseMultiple(selectedSymptoms, allDiseases, diseaseSymMap, 5);
                        
                        Map<String, Object> result = new HashMap<>();
                        result.put("finished", true);
                        
                        if (diagnoses != null && !diagnoses.isEmpty()) {
                            // 转换为前端需要的格式
                            List<Map<String, Object>> diseasesList = new ArrayList<>();
                            for (NaiveBayes.Result d : diagnoses) {
                                Map<String, Object> diseaseMap = new HashMap<>();
                                diseaseMap.put("disease", d.disease);
                                diseaseMap.put("diseaseId", d.diseaseId);
                                diseaseMap.put("probability", d.probability);
                                diseasesList.add(diseaseMap);
                            }
                            result.put("diseases", diseasesList);
                        } else {
                            // 如果没有诊断结果，返回一个默认值
                            List<Map<String, Object>> diseasesList = new ArrayList<>();
                            Map<String, Object> diseaseMap = new HashMap<>();
                            diseaseMap.put("disease", "未知");
                            diseaseMap.put("diseaseId", null);
                            diseaseMap.put("probability", 0.0);
                            diseasesList.add(diseaseMap);
                            result.put("diseases", diseasesList);
                        }
                        
                        out.print(objectMapper.writeValueAsString(result));
                    } else {
                        Symptom nextSymptom = symptomService.getSymptomById(nextSymptomId);
                        
                        Map<String, Object> result = new HashMap<>();
                        result.put("finished", false);
                        result.put("nextSymptomId", nextSymptomId);
                        result.put("name", nextSymptom != null ? nextSymptom.getName() : "");
                        result.put("description", nextSymptom != null ? nextSymptom.getCategory() : ""); // 使用category作为description
                        
                        out.print(objectMapper.writeValueAsString(result));
                    }
                }
                
            } else if (pathInfo != null && pathInfo.equals("/followup")) {
                // POST /api/diagnosis/followup - 根据主诉生成相关追问
                @SuppressWarnings("unchecked")
                Map<String, Object> requestData = objectMapper.readValue(sb.toString(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                
                Integer mainComplaintId = (Integer) requestData.get("mainComplaintId");
                @SuppressWarnings("unchecked")
                List<Integer> selectedSymptoms = (List<Integer>) requestData.get("selectedSymptoms");
                if (selectedSymptoms == null) {
                    selectedSymptoms = new ArrayList<>();
                }
                
                if (mainComplaintId == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"主诉ID不能为空\"}");
                    return;
                }
                
                // 生成相关追问
                List<SymptomQuestion> followUpQuestions = structuredQuestionService.getFollowUpQuestions(
                    mainComplaintId, selectedSymptoms);
                
                Map<String, Object> result = new HashMap<>();
                result.put("questions", followUpQuestions);
                result.put("type", "follow_up");
                
                out.print(objectMapper.writeValueAsString(result));
                
            } else if (pathInfo != null && pathInfo.equals("/diagnose")) {
                // POST /api/diagnosis/diagnose - 直接执行诊断（不继续提问）
                @SuppressWarnings("unchecked")
                Map<String, Object> requestData = objectMapper.readValue(sb.toString(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));

                @SuppressWarnings("unchecked")
                List<Integer> selectedSymptoms = (List<Integer>) requestData.get("selectedSymptoms");
                
                if (selectedSymptoms == null || selectedSymptoms.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"selectedSymptoms不能为空\"}");
                    return;
                }

                // 获取所有疾病和关系
                List<Disease> allDiseases = diseaseService.getAllDiseases();
                Map<Integer, List<DiseaseSymptom>> diseaseSymMap = buildDiseaseSymptomMap();

                // 执行诊断
                NaiveBayes.Result diagnosis = NaiveBayes.diagnose(selectedSymptoms, allDiseases, diseaseSymMap);
                
                Map<String, Object> result = new HashMap<>();
                if (diagnosis != null) {
                    result.put("success", true);
                    result.put("disease", diagnosis.disease);
                    result.put("diseaseId", diagnosis.diseaseId);
                    result.put("probability", diagnosis.probability);
                } else {
                    result.put("success", false);
                    result.put("error", "无法诊断，请选择更多症状");
                }
                
                out.print(objectMapper.writeValueAsString(result));
                
            } else if (pathInfo != null && pathInfo.equals("/save")) {
                // POST /api/diagnosis/save - 保存问诊记录
                @SuppressWarnings("unchecked")
                Map<String, Object> requestData = objectMapper.readValue(sb.toString(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));

                Integer userId = requestData.get("userId") != null ? 
                    (requestData.get("userId") instanceof Integer ? (Integer) requestData.get("userId") : 
                     Integer.valueOf(requestData.get("userId").toString())) : null;
                
                @SuppressWarnings("unchecked")
                List<Integer> selectedSymptoms = (List<Integer>) requestData.get("selectedSymptoms");
                Integer resultDiseaseId = requestData.get("resultDiseaseId") != null ? 
                    (requestData.get("resultDiseaseId") instanceof Integer ? (Integer) requestData.get("resultDiseaseId") : 
                     Integer.valueOf(requestData.get("resultDiseaseId").toString())) : null;
                Double probability = requestData.get("probability") != null ? 
                    (requestData.get("probability") instanceof Double ? (Double) requestData.get("probability") : 
                     Double.valueOf(requestData.get("probability").toString())) : null;

                if (userId == null || selectedSymptoms == null || selectedSymptoms.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"缺少必要参数\"}");
                    return;
                }

                // 将症状列表转换为JSON字符串
                String selectedSymptomsJson = objectMapper.writeValueAsString(selectedSymptoms);

                Consultation consultation = new Consultation();
                consultation.setUserId(userId);
                consultation.setSelectedSymptoms(selectedSymptomsJson);
                consultation.setResultDiseaseId(resultDiseaseId);
                consultation.setProbability(probability != null ? probability.floatValue() : null);
                consultation.setCreatedAt(LocalDateTime.now());

                Integer consultationId = consultationService.createConsultation(consultation);
                
                Map<String, Object> result = new HashMap<>();
                if (consultationId > 0) {
                    result.put("success", true);
                    result.put("consultationId", consultationId);
                    result.put("message", "问诊记录保存成功");
                } else {
                    result.put("success", false);
                    result.put("error", "保存问诊记录失败");
                }
                
                out.print(objectMapper.writeValueAsString(result));
                
            } else if (pathInfo != null && pathInfo.equals("/explain")) {
                // POST /api/diagnosis/explain - 获取疾病解释
                @SuppressWarnings("unchecked")
                Map<String, Object> requestData = objectMapper.readValue(sb.toString(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));

                String diseaseName = requestData.get("disease") != null ? 
                    requestData.get("disease").toString() : null;
                Double probability = requestData.get("probability") != null ? 
                    (requestData.get("probability") instanceof Double ? (Double) requestData.get("probability") : 
                     Double.valueOf(requestData.get("probability").toString())) : 0.0;

                if (diseaseName == null || diseaseName.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"disease参数不能为空\"}");
                    return;
                }

                // 获取疾病解释
                String explanation = explanationService.getDiseaseExplanation(diseaseName, probability);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("disease", diseaseName);
                result.put("explanation", explanation);
                
                out.print(objectMapper.writeValueAsString(result));
                
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid path\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"服务器错误: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}

