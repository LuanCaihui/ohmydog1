package com.petblog.util;

import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * SQL异常处理类，用于集中处理和封装SQL异常
 * 提供友好的错误信息、异常日志记录和恢复策略
 */
public class SQLExceptionHandler {
    private static final Logger logger = Logger.getLogger(SQLExceptionHandler.class.getName());

    /**
     * 处理SQL异常
     * @param e 原始的SQL异常
     * @param operationDescription 操作描述，用于提供更具体的错误信息
     * @return 包装后的运行时异常
     */
    public static RuntimeException handleSQLException(SQLException e, String operationDescription) {
        // 记录异常信息
        logSQLException(e, operationDescription);

        // 根据不同的SQL异常类型提供更具体的错误信息
        String errorMessage = generateErrorMessage(e, operationDescription);

        // 返回包装后的运行时异常，避免在业务层处理checked异常
        return new RuntimeException(errorMessage, e);
    }

    /**
     * 记录SQL异常到日志
     */
    private static void logSQLException(SQLException e, String operationDescription) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("SQL操作失败: ").append(operationDescription).append("\n");
        logMessage.append("错误代码: ").append(e.getErrorCode()).append("\n");
        logMessage.append("SQL状态: ").append(e.getSQLState()).append("\n");
        logMessage.append("错误信息: ").append(e.getMessage()).append("\n");

        // 记录异常堆栈
        logger.log(Level.SEVERE, logMessage.toString(), e);

        // 记录链式异常
        SQLException nextException = e.getNextException();
        while (nextException != null) {
            logger.log(Level.SEVERE, "链式异常: " + nextException.getMessage(), nextException);
            nextException = nextException.getNextException();
        }
    }

    /**
     * 根据SQL异常类型生成友好的错误信息
     */
    private static String generateErrorMessage(SQLException e, String operationDescription) {
        String errorCode = String.valueOf(e.getErrorCode());
        String sqlState = e.getSQLState();

        // 根据常见的SQL错误代码提供更具体的错误信息
        switch (errorCode) {
            case "1062":  // 唯一约束冲突
                return "操作失败：" + operationDescription + "，数据已存在，请检查后重试。";
            case "1045":  // 访问被拒绝
                return "数据库连接失败：用户名或密码错误。";
            case "1049":  // 数据库不存在
                return "数据库连接失败：指定的数据库不存在。";
            case "1146":  // 表不存在
                return "操作失败：数据库表不存在，请检查系统配置。";
            case "1216":  // 外键约束 - 找不到匹配的行
            case "1217":  // 外键约束 - 不能删除被引用的行
                return "操作失败：" + operationDescription + "，数据存在关联关系，请先处理相关数据。";
            case "1406":  // 数据太长
                return "操作失败：数据长度超过限制，请检查输入数据。";
            case "1451":  // 无法删除或更新父行
            case "1452":  // 无法添加或更新子行
                return "操作失败：" + operationDescription + "，存在外键约束，请先处理相关数据。";
            case "1064":  // SQL语法错误
                return "操作失败：SQL语法错误，请联系系统管理员。";
            default:
                // 通用错误信息
                if (sqlState != null && sqlState.startsWith("23")) {
                    // 约束违反类错误
                    return "操作失败：" + operationDescription + "，数据约束违反，请检查输入数据。";
                } else if (sqlState != null && sqlState.startsWith("08")) {
                    // 连接类错误
                    return "操作失败：数据库连接异常，请检查数据库服务是否正常。";
                }
                return "操作失败：" + operationDescription + "，请稍后重试。如有问题，请联系系统管理员。";
        }
    }

    /**
     * 处理SQL异常并返回默认值
     * 适用于非关键操作，可以优雅地失败并返回默认值
     */
    public static <T> T handleSQLExceptionWithDefault(SQLException e, String operationDescription, T defaultValue) {
        handleSQLException(e, operationDescription);
        return defaultValue;
    }

    /**
     * 检查是否是数据库连接相关的异常
     */
    public static boolean isConnectionException(SQLException e) {
        String sqlState = e.getSQLState();
        return sqlState != null && sqlState.startsWith("08");
    }

    /**
     * 检查是否是约束违反相关的异常
     */
    public static boolean isConstraintViolationException(SQLException e) {
        String sqlState = e.getSQLState();
        return sqlState != null && sqlState.startsWith("23");
    }

    /**
     * 检查是否是语法错误相关的异常
     */
    public static boolean isSyntaxErrorException(SQLException e) {
        String sqlState = e.getSQLState();
        return sqlState != null && (sqlState.startsWith("42") || "1064".equals(String.valueOf(e.getErrorCode())));
    }
}

