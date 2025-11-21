package com.petblog.dao;
import com.petblog.model.File;

import java.sql.SQLException;
import java.util.List;

/**
 * 文件DAO接口
 * 定义对files表的所有数据操作方法
 * 该表用于管理博客相关的附件文件（图片、文档等）
 */
public interface FileDAO {

    /**
     * 根据文件ID查询文件详情
     * @param fileId 文件ID
     * @return 文件实体对象，包含存储路径、大小等信息
     */
    File findById(Integer fileId) throws SQLException;

    /**
     * 根据博客ID查询关联的所有文件
     * @param blogId 博客ID
     * @return 该博客的所有附件文件列表（按上传时间正序）
     */
    List<File> findByBlogId(Integer blogId) throws SQLException;

    /**
     * 根据文件存储路径查询文件
     * @param filePath 文件存储路径（相对路径或URL）
     * @return 文件实体对象，不存在则返回null
     */
    File findByPath(String filePath) throws SQLException;

    /**
     * 统计指定博客的附件数量
     * @param blogId 博客ID
     * @return 附件文件数量
     */
    int countByBlogId(Integer blogId) throws SQLException;

    /**
     * 新增文件记录
     * @param file 文件实体（包含博客ID、文件名、路径、大小、类型等信息）
     * @return 新增文件的ID（自增主键），失败返回0
     */
    int insert(File file) throws SQLException;

    /**
     * 更新文件信息（通常用于修改文件名或备注）
     * @param file 文件实体（需包含文件ID）
     * @return 影响行数（1表示成功，0表示失败）
     */
    int update(File file) throws SQLException;

    /**
     * 根据文件ID删除文件记录
     * @param fileId 文件ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer fileId) throws SQLException;

    /**
     * 删除指定博客的所有附件文件记录
     * @param blogId 博客ID
     * @return 影响行数
     */
    int deleteByBlogId(Integer blogId) throws SQLException;

    /**
     * 批量删除文件记录
     * @param fileIds 文件ID列表
     * @return 成功删除的数量
     */
    int batchDelete(List<Integer> fileIds) throws SQLException;
}