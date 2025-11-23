@echo off
chcp 65001 >nul
echo ========================================
echo 验证清理结果
echo ========================================
echo.

mysql -h localhost -P 3306 -u root -p123456 db -e "SELECT name, category, COUNT(*) as count FROM symptoms GROUP BY name, category HAVING count > 1;"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo 如果上面没有显示任何结果，说明 symptoms 表没有重复数据了！
) else (
    echo.
    echo 执行失败
)

echo.
echo ========================================
echo 检查 disease_symptoms 表
echo ========================================
echo.

mysql -h localhost -P 3306 -u root -p123456 db -e "SELECT disease_id, symptom_id, COUNT(*) as count FROM disease_symptoms GROUP BY disease_id, symptom_id HAVING count > 1;"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo 如果上面没有显示任何结果，说明 disease_symptoms 表没有重复数据了！
) else (
    echo.
    echo 执行失败
)

echo.
echo ========================================
echo 数据统计
echo ========================================
echo.

mysql -h localhost -P 3306 -u root -p123456 db -e "SELECT (SELECT COUNT(*) FROM symptoms) as total_symptoms, (SELECT COUNT(DISTINCT name, category) FROM symptoms) as unique_symptoms, (SELECT COUNT(*) FROM disease_symptoms) as total_relations, (SELECT COUNT(DISTINCT disease_id, symptom_id) FROM disease_symptoms) as unique_relations;"

echo.
pause

