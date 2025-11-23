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
import java.util.Arrays;
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
                    
                    // 转换为前端需要的格式：显示诊断结果和诊断时间，而不是症状
                    List<Map<String, Object>> consultationList = new ArrayList<>();
                    if (consultations != null) {
                        DiseaseService diseaseService = new DiseaseService();
                        for (Consultation consultation : consultations) {
                            Map<String, Object> consultationMap = new HashMap<>();
                            consultationMap.put("id", consultation.getId());
                            consultationMap.put("userId", consultation.getUserId());
                            consultationMap.put("createdAt", consultation.getCreatedAt());
                            consultationMap.put("probability", consultation.getProbability());
                            
                            // 获取疾病名称
                            if (consultation.getResultDiseaseId() != null) {
                                Disease disease = diseaseService.getDiseaseById(consultation.getResultDiseaseId());
                                consultationMap.put("diseaseName", disease != null ? disease.getName() : "未知疾病");
                                consultationMap.put("diseaseId", consultation.getResultDiseaseId());
                            } else {
                                consultationMap.put("diseaseName", "未诊断");
                                consultationMap.put("diseaseId", null);
                            }
                            
                            consultationList.add(consultationMap);
                        }
                    }
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("consultations", consultationList);
                    
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
                
            } else if (pathInfo != null && pathInfo.startsWith("/evidence/")) {
                // GET /api/diagnosis/evidence/{diseaseId}?selectedSymptoms=[1,2,3] - 获取疾病的证据症状
                try {
                    String[] splits = pathInfo.split("/");
                    if (splits.length < 3) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"需要提供疾病ID\"}");
                        return;
                    }
                    
                    Integer diseaseId = Integer.valueOf(splits[2]);
                    String selectedSymptomsStr = request.getParameter("selectedSymptoms");
                    
                    List<Integer> selectedSymptoms = new ArrayList<>();
                    if (selectedSymptomsStr != null && !selectedSymptomsStr.isEmpty()) {
                        try {
                            @SuppressWarnings("unchecked")
                            List<Integer> symptoms = objectMapper.readValue(selectedSymptomsStr,
                                objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class));
                            if (symptoms != null) {
                                selectedSymptoms = symptoms;
                            }
                        } catch (Exception e) {
                            // 忽略解析错误，使用空列表
                        }
                    }
                    
                    Map<Integer, List<DiseaseSymptom>> diseaseSymMap = buildDiseaseSymptomMap();
                    List<Map<String, Object>> evidenceSymptoms = getEvidenceSymptoms(
                        diseaseId, selectedSymptoms, diseaseSymMap, symptomService);
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("diseaseId", diseaseId);
                    result.put("evidenceSymptoms", evidenceSymptoms);
                    
                    out.print(objectMapper.writeValueAsString(result));
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"疾病ID格式错误\"}");
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"error\":\"获取证据症状失败: " + e.getMessage() + "\"}");
                    e.printStackTrace();
                }
                
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

                // 获取所有症状和疾病（提前获取，用于智能切换和跳过判断）
                List<Symptom> allSymptoms = symptomService.getAllSymptoms();
                List<Disease> allDiseases = diseaseService.getAllDiseases();
                Map<Integer, List<DiseaseSymptom>> diseaseSymMap = buildDiseaseSymptomMap();
                
                // 计算当前疾病概率分布（用于智能切换和跳过判断）
                Map<Integer, Double> diseaseProbs = null;
                if (!selectedSymptoms.isEmpty() && allDiseases != null && diseaseSymMap != null) {
                    diseaseProbs = DecisionTree.calculateDiseaseProbabilities(selectedSymptoms, allDiseases, diseaseSymMap);
                }

                // 用于存储过渡提示信息（从结构化问题切换到决策树时使用）
                String transitionMessage = null;

                // 如果已选择主诉，生成相关追问（支持智能跳过）
                if (mainComplaintId != null && answeredDimension != null) {
                    // 计算已问的结构化问题数量（用于判断是否切换到决策树）
                    int structuredQuestionCount = 0;
                    if (answeredDimension != null) {
                        SymptomQuestion.QuestionDimension[] dimensions = SymptomQuestion.QuestionDimension.values();
                        for (int i = 0; i < dimensions.length; i++) {
                            if (dimensions[i] == answeredDimension) {
                                structuredQuestionCount = i + 1; // 已问的问题数
                                break;
                            }
                        }
                    }
                    
                    // 智能切换策略：
                    // 1. 如果已问3个以上结构化问题，且某个疾病概率>30%，切换到决策树
                    // 2. 如果已问5个以上结构化问题，强制切换到决策树
                    boolean shouldSwitchToDecisionTree = false;
                    
                    if (structuredQuestionCount >= 5) {
                        shouldSwitchToDecisionTree = true;
                        transitionMessage = "已完成主要症状的详细询问，现在将根据当前症状智能选择后续问题。";
                    } else if (structuredQuestionCount >= 3 && diseaseProbs != null) {
                        double maxProb = diseaseProbs.values().stream()
                            .mapToDouble(Double::doubleValue)
                            .max()
                            .orElse(0.0);
                        if (maxProb > 0.3) {
                            shouldSwitchToDecisionTree = true;
                            transitionMessage = "根据当前症状，系统已初步判断可能的疾病方向，现在将针对性地询问相关问题。";
                        }
                    }
                    
                    if (!shouldSwitchToDecisionTree) {
                        // 继续使用结构化问题（支持智能跳过）
                        SymptomQuestion nextQuestion = structuredQuestionService.getNextFollowUpQuestion(
                            mainComplaintId, answeredDimension, selectedSymptoms, diseaseProbs, diseaseSymMap);
                        
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
                            result.put("transitionMessage", null); // 继续结构化问题，无过渡提示
                            
                            out.print(objectMapper.writeValueAsString(result));
                            return;
                        } else {
                            // 结构化问题已问完，切换到决策树
                            transitionMessage = "已完成主要症状的详细询问，现在将根据当前症状智能选择后续问题。";
                        }
                    }
                }

                // 新的结束逻辑：不强制结束，只有当某个病症达到一定概率时才结束
                // 至少问5个问题才能检查是否结束
                int minQuestions = 5;
                boolean hasEnoughQuestions = totalQuestions >= minQuestions;
                
                // 结束条件：已回答至少5个问题 AND 某个病症的概率>=45%
                // 使用NaiveBayes计算概率，确保与诊断结果一致
                boolean shouldStop = false;
                if (hasEnoughQuestions) {
                    List<NaiveBayes.Result> tempDiagnoses = NaiveBayes.diagnoseMultiple(selectedSymptoms, allDiseases, diseaseSymMap, 1);
                    if (tempDiagnoses != null && !tempDiagnoses.isEmpty()) {
                        double maxProb = tempDiagnoses.get(0).probability;
                        shouldStop = maxProb >= 0.45;
                    }
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
                    
                    // 转换为前端需要的格式，包含证据和诊疗建议
                    List<Map<String, Object>> diseasesList = new ArrayList<>();
                    for (NaiveBayes.Result d : diagnoses) {
                        Map<String, Object> diseaseMap = new HashMap<>();
                        diseaseMap.put("disease", d.disease);
                        diseaseMap.put("diseaseId", d.diseaseId);
                        diseaseMap.put("probability", d.probability);
                        
                        // 获取支持该疾病的症状（证据）
                        List<Map<String, Object>> evidenceSymptoms = getEvidenceSymptoms(
                            d.diseaseId, selectedSymptoms, diseaseSymMap, symptomService);
                        diseaseMap.put("evidenceSymptoms", evidenceSymptoms);
                        
                        // 根据概率和疾病严重程度生成诊疗建议
                        String recommendation = generateRecommendation(d.disease, d.probability, d.diseaseId);
                        diseaseMap.put("recommendation", recommendation);
                        
                        diseasesList.add(diseaseMap);
                    }
                    result.put("diseases", diseasesList);
                    
                    out.print(objectMapper.writeValueAsString(result));
                } else {
                    // 使用决策树获取下一个问题（带解释）
                    DecisionTree.QuestionResult questionResult = DecisionTree.getNextQuestionWithExplanation(
                        selectedSymptoms, askedSymptoms, allSymptoms, allDiseases, diseaseSymMap);
                    
                    if (questionResult == null || questionResult.getSymptomId() == null) {
                        // 没有更多问题了，执行诊断 - 返回多个可能的疾病（前5个）
                        List<NaiveBayes.Result> diagnoses = NaiveBayes.diagnoseMultiple(selectedSymptoms, allDiseases, diseaseSymMap, 5);
                        
                        Map<String, Object> result = new HashMap<>();
                        result.put("finished", true);
                        
                        if (diagnoses != null && !diagnoses.isEmpty()) {
                            // 转换为前端需要的格式，包含证据和诊疗建议
                            List<Map<String, Object>> diseasesList = new ArrayList<>();
                            for (NaiveBayes.Result d : diagnoses) {
                                Map<String, Object> diseaseMap = new HashMap<>();
                                diseaseMap.put("disease", d.disease);
                                diseaseMap.put("diseaseId", d.diseaseId);
                                diseaseMap.put("probability", d.probability);
                                
                                // 获取支持该疾病的症状（证据）
                                List<Map<String, Object>> evidenceSymptoms = getEvidenceSymptoms(
                                    d.diseaseId, selectedSymptoms, diseaseSymMap, symptomService);
                                diseaseMap.put("evidenceSymptoms", evidenceSymptoms);
                                
                                // 根据概率和疾病严重程度生成诊疗建议
                                String recommendation = generateRecommendation(d.disease, d.probability, d.diseaseId);
                                diseaseMap.put("recommendation", recommendation);
                                
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
                            diseaseMap.put("evidenceSymptoms", new ArrayList<>());
                            diseaseMap.put("recommendation", "无法诊断，请选择更多症状或咨询专业兽医。");
                            diseasesList.add(diseaseMap);
                            result.put("diseases", diseasesList);
                        }
                        
                        out.print(objectMapper.writeValueAsString(result));
                    } else {
                        Symptom nextSymptom = symptomService.getSymptomById(questionResult.getSymptomId());
                        
                        Map<String, Object> result = new HashMap<>();
                        result.put("finished", false);
                        result.put("nextSymptomId", questionResult.getSymptomId());
                        result.put("name", nextSymptom != null ? nextSymptom.getName() : "");
                        result.put("description", nextSymptom != null ? nextSymptom.getCategory() : ""); // 使用category作为description
                        result.put("structured", false); // 决策树问题
                        result.put("explanation", questionResult.getExplanation()); // 问题解释
                        
                        // 添加过渡提示（从结构化问题切换到决策树时）
                        if (transitionMessage != null && !transitionMessage.isEmpty()) {
                            result.put("transitionMessage", transitionMessage);
                        }
                        
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

                // 执行诊断 - 返回多个可能的疾病（前5个）
                List<NaiveBayes.Result> diagnoses = NaiveBayes.diagnoseMultiple(selectedSymptoms, allDiseases, diseaseSymMap, 5);
                
                Map<String, Object> result = new HashMap<>();
                if (diagnoses != null && !diagnoses.isEmpty()) {
                    result.put("finished", true);
                    
                    // 转换为前端需要的格式，包含证据和诊疗建议
                    List<Map<String, Object>> diseasesList = new ArrayList<>();
                    for (NaiveBayes.Result d : diagnoses) {
                        Map<String, Object> diseaseMap = new HashMap<>();
                        diseaseMap.put("disease", d.disease);
                        diseaseMap.put("diseaseId", d.diseaseId);
                        diseaseMap.put("probability", d.probability);
                        
                        // 获取支持该疾病的症状（证据）
                        List<Map<String, Object>> evidenceSymptoms = getEvidenceSymptoms(
                            d.diseaseId, selectedSymptoms, diseaseSymMap, symptomService);
                        diseaseMap.put("evidenceSymptoms", evidenceSymptoms);
                        
                        // 根据概率和疾病严重程度生成诊疗建议
                        String recommendation = generateRecommendation(d.disease, d.probability, d.diseaseId);
                        diseaseMap.put("recommendation", recommendation);
                        
                        diseasesList.add(diseaseMap);
                    }
                    result.put("diseases", diseasesList);
                } else {
                    result.put("finished", true);
                    result.put("diseases", new ArrayList<>());
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
    
    /**
     * 获取支持某个疾病的症状列表（证据）
     * @param diseaseId 疾病ID
     * @param selectedSymptoms 用户已选择的症状ID列表
     * @param diseaseSymMap 疾病-症状关系映射
     * @param symptomService 症状服务
     * @return 支持该疾病的症状列表（包含症状ID和名称）
     */
    private List<Map<String, Object>> getEvidenceSymptoms(Integer diseaseId, 
                                                          List<Integer> selectedSymptoms,
                                                          Map<Integer, List<DiseaseSymptom>> diseaseSymMap,
                                                          SymptomService symptomService) {
        List<Map<String, Object>> evidenceList = new ArrayList<>();
        
        if (diseaseId == null || selectedSymptoms == null || selectedSymptoms.isEmpty()) {
            System.out.println("getEvidenceSymptoms: 参数为空 - diseaseId=" + diseaseId + ", selectedSymptoms=" + selectedSymptoms);
            return evidenceList;
        }
        
        List<DiseaseSymptom> diseaseSymptoms = diseaseSymMap.get(diseaseId);
        if (diseaseSymptoms == null || diseaseSymptoms.isEmpty()) {
            System.out.println("getEvidenceSymptoms: 疾病症状列表为空 - diseaseId=" + diseaseId);
            return evidenceList;
        }
        
        System.out.println("getEvidenceSymptoms: 开始计算证据 - diseaseId=" + diseaseId + ", selectedSymptoms=" + selectedSymptoms + ", diseaseSymptoms数量=" + diseaseSymptoms.size());
        
        // 找出用户已选择的症状中，哪些支持该疾病
        for (Integer symptomId : selectedSymptoms) {
            for (DiseaseSymptom ds : diseaseSymptoms) {
                if (ds.getSymptomId() != null && ds.getSymptomId().equals(symptomId)) {
                    // 检查是否互斥，如果互斥则不作为证据
                    if (ds.getIsExclusive() != null && ds.getIsExclusive()) {
                        System.out.println("getEvidenceSymptoms: 症状 " + symptomId + " 是互斥的，跳过");
                        continue;
                    }
                    
                    Symptom symptom = symptomService.getSymptomById(symptomId);
                    if (symptom != null) {
                        Map<String, Object> evidence = new HashMap<>();
                        evidence.put("symptomId", symptomId);
                        evidence.put("symptomName", symptom.getName());
                        evidence.put("weight", ds.getWeight() != null ? ds.getWeight() : 0.5);
                        evidence.put("isRequired", ds.getIsRequired() != null && ds.getIsRequired());
                        evidenceList.add(evidence);
                        System.out.println("getEvidenceSymptoms: 添加证据 - symptomId=" + symptomId + ", symptomName=" + symptom.getName());
                    } else {
                        System.out.println("getEvidenceSymptoms: 症状不存在 - symptomId=" + symptomId);
                    }
                    break;
                }
            }
        }
        
        System.out.println("getEvidenceSymptoms: 最终证据数量=" + evidenceList.size());
        return evidenceList;
    }
    
    /**
     * 根据疾病和概率生成诊疗建议
     * @param diseaseName 疾病名称
     * @param probability 概率
     * @param diseaseId 疾病ID
     * @return 诊疗建议
     */
    private String generateRecommendation(String diseaseName, double probability, Integer diseaseId) {
        // 定义严重疾病列表（需要立即就医）
        List<String> criticalDiseases = Arrays.asList(
            "犬瘟热", "犬细小病毒肠炎", "狂犬病", "犬传染性肝炎", 
            "急性中毒", "严重外伤", "呼吸困难", "休克"
        );
        
        // 定义中等严重疾病列表（建议尽快就医）
        List<String> moderateDiseases = Arrays.asList(
            "犬冠状病毒肠炎", "犬传染性气管炎", "皮肤病", "耳部感染",
            "消化系统疾病", "泌尿系统疾病"
        );
        
        boolean isCritical = criticalDiseases.stream()
            .anyMatch(name -> diseaseName != null && diseaseName.contains(name));
        boolean isModerate = moderateDiseases.stream()
            .anyMatch(name -> diseaseName != null && diseaseName.contains(name));
        
        StringBuilder recommendation = new StringBuilder();
        
        if (probability >= 0.7) {
            // 高概率
            if (isCritical) {
                recommendation.append("⚠️ 紧急情况！建议立即就医。该疾病具有较高的严重性，且根据症状分析，患病可能性很高。请尽快带宠物前往最近的宠物医院进行专业诊断和治疗。");
            } else if (isModerate) {
                recommendation.append("建议尽快就医。根据症状分析，患病可能性较高，建议在24小时内带宠物前往宠物医院进行专业诊断。");
            } else {
                recommendation.append("建议就医检查。根据症状分析，患病可能性较高，建议带宠物前往宠物医院进行专业诊断，以确认病情并获得适当的治疗。");
            }
        } else if (probability >= 0.45) {
            // 中等概率
            if (isCritical) {
                recommendation.append("⚠️ 建议尽快就医。该疾病具有较高的严重性，虽然概率不是特别高，但为了宠物的健康，建议尽快带宠物前往宠物医院进行专业诊断。");
            } else if (isModerate) {
                recommendation.append("建议就医检查。根据症状分析，存在一定的患病可能性，建议带宠物前往宠物医院进行专业诊断，以排除或确认病情。");
            } else {
                recommendation.append("建议观察并就医检查。根据症状分析，存在一定的患病可能性，建议密切观察宠物状况，如有加重请及时就医。");
            }
        } else if (probability >= 0.2) {
            // 低概率但值得关注
            recommendation.append("建议观察。根据症状分析，患病可能性较低，但建议密切观察宠物状况。如果症状持续或加重，请及时就医。");
        } else {
            // 很低概率
            recommendation.append("建议继续观察。根据症状分析，患病可能性很低，但建议继续观察宠物状况，如有异常请及时就医。");
        }
        
        return recommendation.toString();
    }
}

