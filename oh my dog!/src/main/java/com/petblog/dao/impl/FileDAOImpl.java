// src/main/java/com/petblog/dao/impl/FileDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.FileDAO;
import com.petblog.model.File;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class FileDAOImpl extends BaseJdbcDAO<File> implements FileDAO {

    @Override
    public File findById(Integer fileId) {
        String sql = "SELECT file_id, file_name, file_path, file_type, file_size, file_uploaded_time, blog_id FROM files WHERE file_id = ?";
        try {
            return queryForObject(sql, this::mapRowToFile, fileId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询文件信息", null);
        }
    }

    @Override
    public List<File> findByBlogId(Integer blogId) {
        String sql = "SELECT file_id, file_name, file_path, file_type, file_size, file_uploaded_time, blog_id FROM files WHERE blog_id = ? ORDER BY file_uploaded_time ASC";
        try {
            return queryForList(sql, this::mapRowToFile, blogId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询博客关联的文件列表", null);
        }
    }

    @Override
    public File findByPath(String filePath) {
        String sql = "SELECT file_id, file_name, file_path, file_type, file_size, file_uploaded_time, blog_id FROM files WHERE file_path = ?";
        try {
            return queryForObject(sql, this::mapRowToFile, filePath);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据文件路径查询文件信息", null);
        }
    }

    @Override
    public int countByBlogId(Integer blogId) {
        String sql = "SELECT COUNT(*) FROM files WHERE blog_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, blogId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计博客关联的文件数量", 0);
        }
    }

    @Override
    public int insert(File file) {
        String sql = "INSERT INTO files (file_name, file_path, file_type, file_size, file_uploaded_time, blog_id) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            return insert(sql, file.getFileName(), file.getFilePath(), file.getFileType(),
                         file.getFileSize(), file.getFileUploadedTime(), file.getBlogId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "添加文件记录", 0);
        }
    }

    @Override
    public int update(File file) {
        String sql = "UPDATE files SET file_name = ?, file_path = ?, file_type = ?, file_size = ? WHERE file_id = ?";
        try {
            return update(sql, file.getFileName(), file.getFilePath(), file.getFileType(),
                         file.getFileSize(), file.getFileId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新文件信息", 0);
        }
    }

    @Override
    public int delete(Integer fileId) {
        String sql = "DELETE FROM files WHERE file_id = ?";
        try {
            return delete(sql, fileId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除文件记录", 0);
        }
    }

    @Override
    public int deleteByBlogId(Integer blogId) {
        String sql = "DELETE FROM files WHERE blog_id = ?";
        try {
            return delete(sql, blogId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除博客关联的所有文件", 0);
        }
    }

    @Override
    public int batchDelete(List<Integer> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return 0;
        }

        StringBuilder sql = new StringBuilder("DELETE FROM files WHERE file_id IN (");
        for (int i = 0; i < fileIds.size(); i++) {
            sql.append("?");
            if (i < fileIds.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");

        try {
            Object[] params = fileIds.toArray();
            return update(sql.toString(), params);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "批量删除文件记录", 0);
        }
    }

    private File mapRowToFile(ResultSet rs) throws SQLException {
        File file = new File();
        file.setFileId(rs.getInt("file_id"));
        file.setFileName(rs.getString("file_name"));
        file.setFilePath(rs.getString("file_path"));
        file.setFileType(rs.getString("file_type"));
        file.setFileSize(rs.getFloat("file_size"));
        file.setFileUploadedTime(rs.getDate("file_uploaded_time"));
        file.setBlogId(rs.getInt("blog_id"));
        return file;
    }
}