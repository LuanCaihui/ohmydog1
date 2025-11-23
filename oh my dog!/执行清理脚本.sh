#!/bin/bash

echo "========================================"
echo "清理重复数据脚本执行工具"
echo "========================================"
echo ""
echo "⚠️  警告：执行前请确保已备份数据库！"
echo ""
read -p "按回车键继续..."

# 根据你的数据库配置修改以下参数
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="db"
DB_USER="root"
DB_PASSWORD="123456"
SQL_FILE="src/main/resources/clean_duplicate_data.sql"

echo "正在连接到数据库..."
echo "数据库: $DB_NAME"
echo "用户: $DB_USER"
echo ""

# 执行SQL脚本
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME < $SQL_FILE

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 脚本执行成功！"
else
    echo ""
    echo "❌ 脚本执行失败，请检查错误信息"
fi

echo ""
read -p "按回车键退出..."

