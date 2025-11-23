package com.petblog.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 诊断结果解释服务
 * 可以集成DeepSeek API或其他AI服务来生成疾病解释和护理建议
 */
public class DiagnosisExplanationService extends BaseService {

    // DeepSeek API配置（可选，如果不需要可以设为null）
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String DEEPSEEK_API_KEY = System.getProperty("deepseek.api.key", ""); // 从环境变量或配置读取

    /**
     * 获取疾病解释（使用本地模板或AI）
     * @param diseaseName 疾病名称
     * @param probability 诊断概率
     * @return 疾病解释文本
     */
    public String getDiseaseExplanation(String diseaseName, double probability) {
        // 首先尝试使用本地模板
        String localExplanation = getLocalExplanation(diseaseName, probability);
        
        // 如果配置了DeepSeek API，可以调用AI生成更详细的解释
        if (DEEPSEEK_API_KEY != null && !DEEPSEEK_API_KEY.isEmpty()) {
            try {
                String aiExplanation = getAIExplanation(diseaseName, probability);
                if (aiExplanation != null && !aiExplanation.isEmpty()) {
                    return aiExplanation;
                }
            } catch (Exception e) {
                System.err.println("调用AI解释服务失败，使用本地模板: " + e.getMessage());
            }
        }
        
        return localExplanation;
    }

    /**
     * 本地疾病解释模板
     */
    private String getLocalExplanation(String diseaseName, double probability) {
        StringBuilder explanation = new StringBuilder();
        
        explanation.append("根据您提供的症状，系统诊断您的宠物可能患有：").append(diseaseName).append("\n\n");
        explanation.append("诊断置信度：").append(String.format("%.1f%%", probability * 100)).append("\n\n");
        
        // 根据疾病名称提供不同的解释
        switch (diseaseName) {
            case "犬瘟热":
                explanation.append("【疾病说明】\n");
                explanation.append("犬瘟热是一种高度传染性的病毒性疾病，主要影响幼犬。\n\n");
                explanation.append("【常见症状】\n");
                explanation.append("• 发热\n");
                explanation.append("• 咳嗽\n");
                explanation.append("• 呕吐\n");
                explanation.append("• 眼屎增多\n");
                explanation.append("• 食欲下降\n\n");
                explanation.append("【护理建议】\n");
                explanation.append("1. 立即隔离患病宠物，避免传染给其他动物\n");
                explanation.append("2. 保持环境清洁，定期消毒\n");
                explanation.append("3. 确保宠物有充足的休息和水分摄入\n");
                explanation.append("4. 尽快联系兽医进行专业治疗\n");
                explanation.append("5. 按照兽医建议进行疫苗接种\n\n");
                explanation.append("⚠️ 注意：犬瘟热是严重疾病，需要及时就医！");
                break;
                
            case "细小病毒":
                explanation.append("【疾病说明】\n");
                explanation.append("细小病毒是一种高度传染性的病毒性疾病，主要影响幼犬的消化系统。\n\n");
                explanation.append("【常见症状】\n");
                explanation.append("• 呕吐\n");
                explanation.append("• 腹泻（可能带血）\n");
                explanation.append("• 食欲下降\n");
                explanation.append("• 脱水\n");
                explanation.append("• 精神萎靡\n\n");
                explanation.append("【护理建议】\n");
                explanation.append("1. 立即隔离，防止病毒传播\n");
                explanation.append("2. 保持环境干燥清洁，彻底消毒\n");
                explanation.append("3. 禁食禁水（按兽医建议），避免加重肠胃负担\n");
                explanation.append("4. 及时补充电解质和水分（可能需要静脉输液）\n");
                explanation.append("5. 尽快就医，细小病毒进展迅速，需要专业治疗\n\n");
                explanation.append("⚠️ 注意：细小病毒死亡率较高，必须立即就医！");
                break;
                
            case "感冒":
                explanation.append("【疾病说明】\n");
                explanation.append("感冒是常见的上呼吸道感染，通常由病毒引起。\n\n");
                explanation.append("【常见症状】\n");
                explanation.append("• 发热\n");
                explanation.append("• 咳嗽\n");
                explanation.append("• 流鼻涕\n");
                explanation.append("• 打喷嚏\n");
                explanation.append("• 精神不振\n\n");
                explanation.append("【护理建议】\n");
                explanation.append("1. 保持宠物温暖，避免受凉\n");
                explanation.append("2. 提供充足的清洁饮水\n");
                explanation.append("3. 保证充足的休息，减少活动\n");
                explanation.append("4. 可以适当补充维生素C（按兽医建议）\n");
                explanation.append("5. 如果症状持续或加重，及时就医\n\n");
                explanation.append("💡 提示：大多数感冒可以自愈，但需要密切观察宠物状态。");
                break;
                
            default:
                explanation.append("【疾病说明】\n");
                explanation.append("根据症状分析，您的宠物可能患有：").append(diseaseName).append("\n\n");
                explanation.append("【建议】\n");
                explanation.append("1. 密切观察宠物的症状变化\n");
                explanation.append("2. 记录症状的严重程度和持续时间\n");
                explanation.append("3. 如果症状持续或加重，请及时咨询兽医\n");
                explanation.append("4. 保持宠物的生活环境清洁卫生\n");
                explanation.append("5. 确保宠物有充足的休息和营养\n\n");
                explanation.append("⚠️ 注意：本诊断仅供参考，不能替代专业兽医诊断。");
        }
        
        return explanation.toString();
    }

    /**
     * 调用DeepSeek API获取AI生成的解释
     * 需要配置API Key才能使用
     */
    private String getAIExplanation(String diseaseName, double probability) throws Exception {
        if (DEEPSEEK_API_KEY == null || DEEPSEEK_API_KEY.isEmpty()) {
            return null;
        }

        String prompt = String.format(
            "请为宠物疾病诊断系统生成一段专业的疾病解释。\n" +
            "疾病名称：%s\n" +
            "诊断置信度：%.1f%%\n\n" +
            "请提供以下内容：\n" +
            "1. 疾病的基本说明（2-3句话）\n" +
            "2. 常见症状列表\n" +
            "3. 护理建议（5-7条实用建议）\n" +
            "4. 注意事项\n\n" +
            "请用中文回答，语言要专业但易懂，适合宠物主人阅读。",
            diseaseName, probability * 100
        );

        try {
            URL url = new URL(DEEPSEEK_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + DEEPSEEK_API_KEY);
            conn.setDoOutput(true);

            // 构建请求体
            ObjectMapper mapper = new ObjectMapper();
            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("model", "deepseek-chat");
            java.util.List<java.util.Map<String, String>> messages = new java.util.ArrayList<>();
            java.util.Map<String, String> message = new java.util.HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            String jsonBody = mapper.writeValueAsString(requestBody);

            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 读取响应
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    // 解析JSON响应
                    java.util.Map<String, Object> jsonResponse = mapper.readValue(
                        response.toString(),
                        mapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, Object.class)
                    );

                    java.util.List<java.util.Map<String, Object>> choices = 
                        (java.util.List<java.util.Map<String, Object>>) jsonResponse.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        java.util.Map<String, Object> firstChoice = choices.get(0);
                        java.util.Map<String, String> messageObj = 
                            (java.util.Map<String, String>) firstChoice.get("message");
                        if (messageObj != null) {
                            return messageObj.get("content");
                        }
                    }
                }
            } else {
                System.err.println("DeepSeek API调用失败，状态码: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("调用DeepSeek API时出错: " + e.getMessage());
            throw e;
        }

        return null;
    }
}

