package com.petblog.Service;

import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;

public abstract class BaseService {

    /**
     * 统一异常处理方法
     */
    protected <T> T handleException(SQLException e, String operationDescription, T defaultValue) {
        return SQLExceptionHandler.handleSQLExceptionWithDefault(e, operationDescription, defaultValue);
    }

    /**
     * 统一异常处理方法（无返回值）
     */
    protected void handleException(SQLException e, String operationDescription) {
        SQLExceptionHandler.handleSQLException(e, operationDescription);
    }
}