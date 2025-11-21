package com.petblog.dao;



import com.petblog.util.JdbcUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseJdbcDAO<T> {
    
    /**
     * 插入操作
     * @param sql SQL语句
     * @param params 参数
     * @return 影响行数
     */
    protected int insert(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            setParameters(pstmt, params);
            int result = pstmt.executeUpdate();
            
            // 获取生成的主键
            if (result > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return result;
        } finally {
            JdbcUtil.close(conn, pstmt);
        }
    }
    
    /**
     * 更新操作
     * @param sql SQL语句
     * @param params 参数
     * @return 影响行数
     */
    protected int update(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            setParameters(pstmt, params);
            return pstmt.executeUpdate();
        } finally {
            JdbcUtil.close(conn, pstmt);
        }
    }
    
    /**
     * 删除操作
     * @param sql SQL语句
     * @param params 参数
     * @return 影响行数
     */
    protected int delete(String sql, Object... params) throws SQLException {
        return update(sql, params);
    }
    
    /**
     * 查询单个对象
     * @param sql SQL语句
     * @param mapper 结果映射器
     * @param params 参数
     * @return 查询结果对象
     */
    protected T queryForObject(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            setParameters(pstmt, params);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapper.mapRow(rs);
            }
            return null;
        } finally {
            JdbcUtil.close(conn, pstmt, rs);
        }
    }
    
    /**
     * 查询对象列表
     * @param sql SQL语句
     * @param mapper 结果映射器
     * @param params 参数
     * @return 查询结果列表
     */
    protected List<T> queryForList(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            setParameters(pstmt, params);
            rs = pstmt.executeQuery();
            List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapper.mapRow(rs));
            }
            return list;
        } finally {
            JdbcUtil.close(conn, pstmt, rs);
        }
    }
    
    /**
     * 查询单个值
     * @param sql SQL语句
     * @param params 参数
     * @return 查询结果值
     */
    protected Object queryForSingleValue(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = JdbcUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            setParameters(pstmt, params);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getObject(1);
            }
            return null;
        } finally {
            JdbcUtil.close(conn, pstmt, rs);
        }
    }
    
    /**
     * 设置参数
     */
    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }
    }
    
    /**
     * 结果集映射接口
     */
    public interface RowMapper<T> {
        T mapRow(ResultSet rs) throws SQLException;
    }
}
