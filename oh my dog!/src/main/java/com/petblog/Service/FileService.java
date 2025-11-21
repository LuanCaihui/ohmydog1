package com.petblog.Service;

import com.petblog.dao.FileDAO;
import com.petblog.dao.impl.FileDAOImpl;
import com.petblog.model.File;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class FileService extends BaseService {

    private FileDAO fileDAO = new FileDAOImpl();

    /**
     * 根据文件ID查询文件详情
     */
    public File getFileById(Integer fileId) {
        try {
            return fileDAO.findById(fileId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询文件详情");
            return null;
        }
    }

    /**
     * 根据博客ID查询关联的所有文件
     */
    public List<File> getFilesByBlogId(Integer blogId) {
        try {
            return fileDAO.findByBlogId(blogId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据博客ID查询文件列表");
            return null;
        }
    }

    /**
     * 根据文件存储路径查询文件
     */
    public File getFileByPath(String filePath) {
        try {
            return fileDAO.findByPath(filePath);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据路径查询文件");
            return null;
        }
    }

    /**
     * 统计指定博客的附件数量
     */
    public int countFilesByBlogId(Integer blogId) {
        try {
            return fileDAO.countByBlogId(blogId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计博客附件数量");
            return 0;
        }
    }

    /**
     * 新增文件记录
     */
    public Integer createFile(File file) {
        try {
            return fileDAO.insert(file);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增文件记录");
            return 0;
        }
    }

    /**
     * 更新文件信息
     */
    public boolean updateFile(File file) {
        try {
            int result = fileDAO.update(file);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新文件信息");
            return false;
        }
    }

    /**
     * 根据文件ID删除文件记录
     */
    public boolean deleteFile(Integer fileId) {
        try {
            int result = fileDAO.delete(fileId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除文件记录");
            return false;
        }
    }

    /**
     * 删除指定博客的所有附件文件记录
     */
    public boolean deleteFilesByBlogId(Integer blogId) {
        try {
            int result = fileDAO.deleteByBlogId(blogId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除博客所有附件文件");
            return false;
        }
    }

    /**
     * 批量删除文件记录
     */
    public boolean batchDeleteFiles(List<Integer> fileIds) {
        try {
            int result = fileDAO.batchDelete(fileIds);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "批量删除文件记录");
            return false;
        }
    }
}
