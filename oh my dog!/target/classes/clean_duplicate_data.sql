-- 清理重复数据脚本
-- ⚠️ 执行前请备份数据库！
-- 此脚本会删除重复的症状和疾病-症状关系数据

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 清理 symptoms 表中的重复症状
-- ============================================
-- 步骤1: 创建临时表，找出需要保留的症状（ID最小的）
CREATE TEMPORARY TABLE temp_symptom_keep AS
SELECT MIN(id) as keep_id, name, category
FROM symptoms
GROUP BY name, category;

-- 步骤2: 创建映射表，将重复症状ID映射到保留的ID
CREATE TEMPORARY TABLE temp_symptom_mapping AS
SELECT s.id as old_id, tk.keep_id as new_id
FROM symptoms s
INNER JOIN temp_symptom_keep tk ON s.name = tk.name AND s.category = tk.category
WHERE s.id != tk.keep_id;

-- 步骤3: 更新 disease_symptoms 表，将重复的症状ID替换为保留的ID
-- 先处理需要更新的记录（避免外键冲突）
UPDATE disease_symptoms ds
INNER JOIN temp_symptom_mapping tsm ON ds.symptom_id = tsm.old_id
LEFT JOIN disease_symptoms ds_existing ON ds_existing.disease_id = ds.disease_id 
    AND ds_existing.symptom_id = tsm.new_id
SET ds.symptom_id = tsm.new_id
WHERE ds_existing.id IS NULL; -- 只有当新ID的关系不存在时才更新

-- 步骤4: 删除更新后剩余的重复关系（现在有相同的disease_id和symptom_id）
DELETE ds1 FROM disease_symptoms ds1
INNER JOIN disease_symptoms ds2
WHERE ds1.id > ds2.id
AND ds1.disease_id = ds2.disease_id
AND ds1.symptom_id = ds2.symptom_id;

-- 步骤5: 删除重复的症状记录
DELETE s FROM symptoms s
INNER JOIN temp_symptom_mapping tsm ON s.id = tsm.old_id;

-- 清理临时表
DROP TEMPORARY TABLE IF EXISTS temp_symptom_keep;
DROP TEMPORARY TABLE IF EXISTS temp_symptom_mapping;

-- ============================================
-- 2. 清理 disease_symptoms 表中的重复关系
-- ============================================
-- 找出重复的疾病-症状关系（保留ID最小的）
DELETE ds1 FROM disease_symptoms ds1
INNER JOIN disease_symptoms ds2
WHERE ds1.id > ds2.id
AND ds1.disease_id = ds2.disease_id
AND ds1.symptom_id = ds2.symptom_id;

-- ============================================
-- 3. 清理 consultations 表中的重复问诊记录
-- ============================================
-- 删除完全相同的问诊记录（保留ID最小的，5秒内创建的视为重复）
-- 注意：这个操作比较激进，如果用户确实在短时间内进行了多次相同的问诊，也会被删除
-- 如果不想删除，可以注释掉这部分
DELETE c1 FROM consultations c1
INNER JOIN consultations c2
WHERE c1.id > c2.id
AND c1.user_id = c2.user_id
AND c1.selected_symptoms = c2.selected_symptoms
AND c1.diagnosis = c2.diagnosis
AND ABS(TIMESTAMPDIFF(SECOND, c1.created_at, c2.created_at)) < 5; -- 5秒内的重复记录

-- ============================================
-- 4. 重新设置 AUTO_INCREMENT 值
-- ============================================
-- 获取当前最大ID并设置AUTO_INCREMENT
SET @max_symptom_id = (SELECT IFNULL(MAX(id), 0) FROM symptoms);
SET @sql = CONCAT('ALTER TABLE symptoms AUTO_INCREMENT = ', @max_symptom_id + 1);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @max_disease_symptom_id = (SELECT IFNULL(MAX(id), 0) FROM disease_symptoms);
SET @sql = CONCAT('ALTER TABLE disease_symptoms AUTO_INCREMENT = ', @max_disease_symptom_id + 1);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @max_consultation_id = (SELECT IFNULL(MAX(id), 0) FROM consultations);
SET @sql = CONCAT('ALTER TABLE consultations AUTO_INCREMENT = ', @max_consultation_id + 1);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 5. 验证清理结果
-- ============================================
-- 检查是否还有重复的症状
SELECT name, category, COUNT(*) as count
FROM symptoms
GROUP BY name, category
HAVING count > 1;

-- 检查是否还有重复的疾病-症状关系
SELECT disease_id, symptom_id, COUNT(*) as count
FROM disease_symptoms
GROUP BY disease_id, symptom_id
HAVING count > 1;

