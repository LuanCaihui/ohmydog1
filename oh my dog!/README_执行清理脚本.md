# 如何执行清理重复数据脚本

## ⚠️ 重要提示
**执行前请务必备份数据库！**

## 方法一：使用 Navicat Premium（最简单）

1. **备份数据库**
   - 打开 Navicat Premium
   - 连接到数据库 `db`（localhost:3306）
   - 右键点击数据库 `db` → 选择"转储SQL文件" → "结构和数据"
   - 保存备份文件到安全位置

2. **执行脚本**
   - 在 Navicat 中，点击顶部菜单"查询" → "新建查询"
   - 点击"文件" → "打开文件"，选择 `src/main/resources/clean_duplicate_data.sql`
   - 点击"运行"按钮（或按 F5）
   - 查看执行结果

3. **验证结果**
   - 脚本执行完成后，会显示验证查询的结果
   - 如果查询结果为空，说明没有重复数据了

## 方法二：使用 MySQL 命令行

### Windows 系统：

1. **打开命令提示符（CMD）或 PowerShell**
   - 按 `Win + R`，输入 `cmd`，回车

2. **切换到项目目录**
   ```cmd
   cd "C:\Users\刘雪婷\Desktop\ohmydog\oh my dog!"
   ```

3. **执行脚本**
   ```cmd
   mysql -h localhost -P 3306 -u root -p123456 db < src\main\resources\clean_duplicate_data.sql
   ```
   
   或者使用批处理文件：
   ```cmd
   执行清理脚本.bat
   ```

### Linux/Mac 系统：

1. **打开终端**

2. **切换到项目目录**
   ```bash
   cd "/path/to/oh my dog!"
   ```

3. **执行脚本**
   ```bash
   mysql -h localhost -P 3306 -u root -p123456 db < src/main/resources/clean_duplicate_data.sql
   ```
   
   或者使用 shell 脚本：
   ```bash
   chmod +x 执行清理脚本.sh
   ./执行清理脚本.sh
   ```

## 方法三：使用 MySQL Workbench

1. **打开 MySQL Workbench**
2. **连接到数据库**
3. **打开脚本文件**
   - File → Open SQL Script
   - 选择 `src/main/resources/clean_duplicate_data.sql`
4. **执行脚本**
   - 点击执行按钮（⚡图标）或按 `Ctrl + Shift + Enter`

## 方法四：使用命令行（需要 MySQL 客户端在 PATH 中）

如果 MySQL 客户端不在系统 PATH 中，需要找到 MySQL 的安装路径：

### Windows（默认路径）：
```cmd
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -h localhost -u root -p123456 db < src\main\resources\clean_duplicate_data.sql
```

### Linux/Mac：
```bash
/usr/local/mysql/bin/mysql -h localhost -u root -p123456 db < src/main/resources/clean_duplicate_data.sql
```

## 执行后验证

脚本执行完成后，会自动运行验证查询。如果看到：

1. **第一个查询结果为空** → 说明 symptoms 表没有重复数据了 ✅
2. **第二个查询结果为空** → 说明 disease_symptoms 表没有重复数据了 ✅

如果还有结果，说明清理不完整，需要检查原因。

## 常见问题

### 1. 提示"找不到 mysql 命令"
- **解决方法**：需要将 MySQL 的 bin 目录添加到系统 PATH 环境变量中
- 或者使用完整路径执行 mysql 命令

### 2. 提示"Access denied"
- **解决方法**：检查数据库用户名和密码是否正确
- 根据 `src/main/resources/db.properties` 中的配置修改

### 3. 提示"Unknown database 'db'"
- **解决方法**：确认数据库名称是否正确
- 根据实际情况修改数据库名称

### 4. 执行失败，提示外键约束错误
- **解决方法**：脚本已经设置了 `SET FOREIGN_KEY_CHECKS = 0`，如果还有问题，可能是脚本执行顺序问题
- 可以尝试分段执行脚本

## 数据库配置信息

根据 `src/main/resources/db.properties`：
- **主机**: localhost
- **端口**: 3306
- **数据库名**: db
- **用户名**: root
- **密码**: 123456

如果配置不同，请修改相应的参数。

