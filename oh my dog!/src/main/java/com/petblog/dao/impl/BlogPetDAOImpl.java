
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.BlogPetDAO;
import com.petblog.model.BlogPet;
import com.petblog.util.JdbcUtil;
import com.petblog.util.SQLExceptionHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BlogPetDAOImpl extends BaseJdbcDAO<Integer> implements BlogPetDAO {

    @Override
    public List<Integer> findPetIdsByBlogId(Integer blogId) {
        String sql = "SELECT pet_id FROM blogpet WHERE blog_id = ?";
        try {
            return queryForList(sql, rs -> {
                try {
                    return rs.getInt("pet_id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, blogId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据博客ID查询宠物ID列表", null);
        }
    }

    @Override
    public List<Integer> findBlogIdsByPetId(Integer petId) {
        String sql = "SELECT blog_id FROM blogpet WHERE pet_id = ?";
        try {
            return queryForList(sql, rs -> {
                try {
                    return rs.getInt("blog_id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, petId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据宠物ID查询博客ID列表", null);
        }
    }

    @Override
    public boolean exists(Integer blogId, Integer petId) {
        String sql = "SELECT COUNT(*) FROM blogpet WHERE blog_id = ? AND pet_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, blogId, petId);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查博客宠物关联是否存在", false);
        }
    }

    @Override
    public int insert(BlogPet blogPet) {
        String sql = "INSERT INTO blogpet (blog_id, pet_id) VALUES (?, ?)";
        try {
            return insert(sql, blogPet.getBlogId(), blogPet.getPetId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "插入博客宠物关联数据", 0);
        }
    }

    @Override
    public int batchInsert(List<BlogPet> blogPets) {
        String sql = "INSERT INTO blogpet (blog_id, pet_id) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        int count = 0;

        try {
            conn = JdbcUtil.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);

            for (BlogPet blogPet : blogPets) {
                pstmt.setInt(1, blogPet.getBlogId());
                pstmt.setInt(2, blogPet.getPetId());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            for (int result : results) {
                if (result > 0) count++;
            }

            conn.commit();
            return count;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                throw SQLExceptionHandler.handleSQLException(ex, "回滚批量插入操作");
            }
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "批量插入博客宠物关联数据", 0);
        }  finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                throw SQLExceptionHandler.handleSQLException(e, "恢复自动提交模式");
            }
            JdbcUtil.close(conn, pstmt);
        }
    }

    @Override
    public int deleteByBlogId(Integer blogId) {
        String sql = "DELETE FROM blogpet WHERE blog_id = ?";
        try {
            return delete(sql, blogId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据博客ID删除宠物关联数据", 0);
        }
    }

    @Override
    public int deleteByPetId(Integer petId) {
        String sql = "DELETE FROM blogpet WHERE pet_id = ?";
        try {
            return delete(sql, petId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据宠物ID删除博客关联数据", 0);
        }
    }

    @Override
    public int delete(Integer blogId, Integer petId) {
        String sql = "DELETE FROM blogpet WHERE blog_id = ? AND pet_id = ?";
        try {
            return delete(sql, blogId, petId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除指定的博客宠物关联数据", 0);
        }
    }
}
