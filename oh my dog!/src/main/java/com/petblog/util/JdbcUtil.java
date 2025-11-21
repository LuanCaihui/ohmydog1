package com.petblog.util;

import java.sql.*;
import java.util.Properties;
import java.io.InputStream;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * 数据库工具类，使用HikariCP连接池管理数据库连接
 */
public class JdbcUtil {
    private static HikariDataSource dataSource;

    static {
        try {
            // 从配置文件加载数据库连接信息
            Properties props = new Properties();
            InputStream input = JdbcUtil.class.getClassLoader().getResourceAsStream("db.properties");

            HikariConfig config = new HikariConfig();

            if (input != null) {
                props.load(input);
                // 从db.properties读取配置
                config.setJdbcUrl(props.getProperty("jdbc.url", "jdbc:mysql://localhost:3306/db?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"));
                config.setUsername(props.getProperty("jdbc.username", "root"));
                config.setPassword(props.getProperty("jdbc.password", "123456"));
                config.setDriverClassName(props.getProperty("jdbc.driver", "com.mysql.cj.jdbc.Driver"));
                
                // 连接池参数（从配置文件读取，如果没有则使用默认值）
                config.setMaximumPoolSize(Integer.parseInt(props.getProperty("jdbc.maxPoolSize", "10")));
                config.setMinimumIdle(Integer.parseInt(props.getProperty("jdbc.minIdle", "5")));
                config.setIdleTimeout(Long.parseLong(props.getProperty("jdbc.idleTimeout", "300000")));
                config.setMaxLifetime(Long.parseLong(props.getProperty("jdbc.maxLifetime", "1800000")));
                config.setConnectionTimeout(Long.parseLong(props.getProperty("jdbc.connectionTimeout", "30000")));
                config.setValidationTimeout(Long.parseLong(props.getProperty("jdbc.validationTimeout", "5000")));
                config.setLeakDetectionThreshold(Long.parseLong(props.getProperty("jdbc.leakDetectionThreshold", "60000")));
            } else {
                // 默认配置（如果配置文件不存在）
                config.setJdbcUrl("jdbc:mysql://localhost:3306/db?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true");
                config.setUsername("root");
                config.setPassword("123456");
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                
                // 默认连接池参数
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(5);
                config.setIdleTimeout(300000);
                config.setMaxLifetime(1800000);
                config.setConnectionTimeout(30000);
                config.setValidationTimeout(5000);
                config.setLeakDetectionThreshold(60000);
            }

            // 设置连接测试SQL
            config.setConnectionTestQuery("SELECT 1");

            // 初始化数据源
            dataSource = new HikariDataSource(config);

            System.out.println("HikariCP连接池初始化成功");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("数据库连接池初始化失败", e);
        }
    }

    /**
     * 获取数据库连接
     * @return 数据库连接对象
     * @throws SQLException SQL异常
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("数据库连接池未初始化");
        }
        return dataSource.getConnection();
    }

    /**
     * 关闭资源
     */
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close(); // 对于连接池，close()会将连接归还到池中
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "关闭数据库资源");
        }
    }

    /**
     * 关闭资源
     */
    public static void close(Connection conn, PreparedStatement pstmt) {
        close(conn, pstmt, null);
    }

    /**
     * 关闭数据源（应用程序关闭时调用）
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("HikariCP连接池已关闭");
        }
    }

    /**
     * 获取连接池状态信息
     * @return 连接池状态信息
     */
    public static String getPoolStatus() {
        if (dataSource == null) {
            return "连接池未初始化";
        }

        StringBuilder status = new StringBuilder();
        status.append("HikariCP连接池状态：\n");
        status.append("- 活动连接数：").append(dataSource.getHikariPoolMXBean().getActiveConnections()).append("\n");
        status.append("- 空闲连接数：").append(dataSource.getHikariPoolMXBean().getIdleConnections()).append("\n");
        status.append("- 总连接数：").append(dataSource.getHikariPoolMXBean().getTotalConnections()).append("\n");
        status.append("- 等待连接数：").append(dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()).append("\n");

        return status.toString();
    }
}
