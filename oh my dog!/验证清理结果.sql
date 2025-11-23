-- 验证清理结果脚本
-- 执行清理脚本后，运行此脚本验证是否还有重复数据

-- 1. 检查 symptoms 表中是否还有重复的症状
SELECT '检查 symptoms 表重复数据:' as check_type;
SELECT name, category, COUNT(*) as count, GROUP_CONCAT(id ORDER BY id) as symptom_ids
FROM symptoms
GROUP BY name, category
HAVING count > 1;

-- 2. 检查 disease_symptoms 表中是否还有重复的关系
SELECT '检查 disease_symptoms 表重复数据:' as check_type;
SELECT disease_id, symptom_id, COUNT(*) as count, GROUP_CONCAT(id ORDER BY id) as relation_ids
FROM disease_symptoms
GROUP BY disease_id, symptom_id
HAVING count > 1;

-- 3. 统计清理后的数据量
SELECT '数据统计:' as check_type;
SELECT 
    (SELECT COUNT(*) FROM symptoms) as total_symptoms,
    (SELECT COUNT(DISTINCT name, category) FROM symptoms) as unique_symptoms,
    (SELECT COUNT(*) FROM disease_symptoms) as total_disease_symptom_relations,
    (SELECT COUNT(DISTINCT disease_id, symptom_id) FROM disease_symptoms) as unique_disease_symptom_relations,
    (SELECT COUNT(*) FROM consultations) as total_consultations;

-- 如果上面的查询结果为空（除了数据统计），说明清理成功！

