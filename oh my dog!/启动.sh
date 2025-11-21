#!/bin/bash

echo "========================================"
echo "  宠物博客系统 - 快速启动脚本"
echo "========================================"
echo ""

echo "[1/3] 检查Java环境..."
if ! command -v java &> /dev/null; then
    echo "❌ 错误：未找到Java，请先安装JDK 21"
    exit 1
fi
echo "✅ Java环境正常"

echo ""
echo "[2/3] 检查Maven环境..."
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误：未找到Maven，请先安装Maven 3.6+"
    exit 1
fi
echo "✅ Maven环境正常"

echo ""
echo "[3/3] 启动Jetty服务器..."
echo ""
echo "提示：服务器启动后，访问 http://localhost:8080/petblog/"
echo "按 Ctrl+C 停止服务器"
echo ""
echo "========================================"
echo ""

mvn jetty:run

