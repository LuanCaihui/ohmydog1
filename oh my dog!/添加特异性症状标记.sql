/*
 * 添加特异性症状标记 (is_exclusive=1)
 * 
 * 特异性症状是指某些疾病独有的、具有高度诊断价值的症状。
 * 当这些症状出现时，对应疾病的概率应该直接提升到95%（接近确诊）。
 * 
 * 更新策略：将以下疾病的关键特征性症状标记为 is_exclusive=1
 */

-- 1. 犬细小病毒肠炎 (disease_id=2) - 特征性症状：便血（腥臭血便是该病的典型表现）
-- 症状ID 72 = "便血"
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 2 AND symptom_id = 72;

-- 2. 犬耳螨感染 (disease_id=43) - 特征性症状：耳道内黑褐色分泌物
-- 症状ID 117 = "耳朵流脓"，但耳螨的典型表现是黑褐色分泌物
-- 我们标记"耳朵有分泌物" (symptom_id=120) 和"频繁抓挠耳朵" (symptom_id=43) 的组合
-- 为了简化，先标记"耳朵有分泌物" (120) 和"摇头频繁" (123)
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 43 AND symptom_id IN (120, 123); -- 耳朵有分泌物、摇头频繁

-- 3. 犬膀胱炎 (disease_id=13) - 特征性症状：尿血 + 频繁排尿 + 排尿疼痛
-- 症状ID 31 = "尿血"，症状ID 30 = "频繁排尿"，症状ID 195 = "排尿疼痛"
-- 尿血是最具特征性的
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 13 AND symptom_id = 31; -- 尿血

-- 4. 犬尿结石 (disease_id=14) - 特征性症状：尿血 + 排尿困难
-- 症状ID 31 = "尿血"，症状ID 32 = "排尿困难"
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 14 AND symptom_id = 31; -- 尿血

-- 5. 犬椎间盘突出 (disease_id=26) - 特征性症状：后肢瘫痪 + 头部倾斜
-- 症状ID 86 = "无法站立"（表示瘫痪），症状ID 26 = "头部倾斜"
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 26 AND symptom_id = 86; -- 无法站立（瘫痪）

-- 6. 犬癫痫 (disease_id=27) - 特征性症状：癫痫发作
-- 症状ID 29 = "癫痫发作"
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 27 AND symptom_id = 29; -- 癫痫发作

-- 7. 犬子宫蓄脓 (disease_id=25) - 特征性症状：腹部包块 + 阴道分泌物
-- 症状ID 49 = "腹部包块"
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 25 AND symptom_id = 49; -- 腹部包块

-- 8. 犬胃扩张与扭转 (disease_id=16) - 特征性症状：腹胀 + 焦虑 + 呼吸困难
-- 症状ID 15 = "腹胀"，症状ID 41 = "焦虑"，症状ID 8 = "呼吸困难"
-- 腹胀是最具特征性的，特别是急性腹胀
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 16 AND symptom_id = 15; -- 腹胀

-- 9. 犬角膜溃疡 (disease_id=30) - 特征性症状：眼睛疼痛 + 流泪增多
-- 症状ID 37 = "眼睛红肿"，症状ID 38 = "流泪增多"
-- 但更特征的是"眼睛疼痛"或"眼部分泌物"
-- 我们标记"流泪增多" (38)
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 30 AND symptom_id = 38; -- 流泪增多

-- 10. 犬骨折 (disease_id=88) - 特征性症状：无法站立 + 关节疼痛
-- 症状ID 86 = "无法站立"，症状ID 23 = "关节痛"
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 88 AND symptom_id = 86; -- 无法站立

-- 11. 犬心丝虫病 (disease_id=8) - 特征性症状：运动不耐受 + 呼吸困难
-- 症状ID 261 = "运动不耐受"，症状ID 8 = "呼吸困难"
-- 运动不耐受是心丝虫病的典型表现
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 8 AND symptom_id = 261; -- 运动不耐受

-- 12. 犬青光眼 (disease_id=76) - 特征性症状：眼痛 + 瞳孔散大
-- 症状ID 37 = "眼睛红肿"，但更特征的是眼压升高导致的症状
-- 我们标记"眼睛疼痛"相关的症状，但数据库中可能没有精确匹配
-- 先用"眼睛红肿" (37) 和"视力下降" (39) 的组合
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 76 AND symptom_id = 39; -- 视力下降

-- 13. 犬白内障 (disease_id=77) - 特征性症状：视力下降 + 眼睛浑浊
-- 症状ID 39 = "视力下降"，症状ID 103 = "眼睛浑浊"
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 77 AND symptom_id = 103; -- 眼睛浑浊

-- 14. 犬胰腺炎 (disease_id=17) - 特征性症状：剧烈腹痛 + 呕吐
-- 症状ID 13 = "腹痛"，症状ID 11 = "呕吐"
-- 剧烈腹痛是胰腺炎的特征
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 17 AND symptom_id = 13; -- 腹痛

-- 15. 犬耳血肿 (disease_id=44) - 特征性症状：耳朵肿胀
-- 症状ID 115 = "耳朵肿胀"
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 44 AND symptom_id = 115; -- 耳朵肿胀

-- 16. 犬前庭综合征 (disease_id=28) - 特征性症状：失去平衡 + 头部倾斜
-- 症状ID 28 = "失去平衡"，症状ID 26 = "头部倾斜"
UPDATE disease_symptoms 
SET is_exclusive = 1 
WHERE disease_id = 28 AND symptom_id = 28; -- 失去平衡

-- 验证更新结果
SELECT 
    d.name AS disease_name,
    s.name AS symptom_name,
    ds.weight,
    ds.is_required,
    ds.is_exclusive
FROM disease_symptoms ds
JOIN diseases d ON ds.disease_id = d.id
JOIN symptoms s ON ds.symptom_id = s.id
WHERE ds.is_exclusive = 1
ORDER BY d.id, s.id;

