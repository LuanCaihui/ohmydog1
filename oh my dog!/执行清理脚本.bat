@echo off
chcp 65001 >nul
echo ========================================
echo 清理重复数据脚本执行工具
echo ========================================
echo.
echo ⚠️  警告：执行前请确保已备份数据库！
echo.
pause

REM 根据你的数据库配置修改以下参数
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=db
set DB_USER=root
set DB_PASSWORD=123456
set SQL_FILE=src\main\resources\clean_duplicate_data.sql

echo 正在连接到数据库...
echo 数据库: %DB_NAME%
echo 用户: %DB_USER%
echo.

REM 执行SQL脚本
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% < %SQL_FILE%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ 脚本执行成功！
) else (
    echo.
    echo ❌ 脚本执行失败，请检查错误信息
)

echo.
pause

