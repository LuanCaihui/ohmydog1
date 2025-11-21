package com.petblog.dao;
import com.petblog.model.Report;

import java.sql.SQLException;
import java.util.List;

/**
 * 内容举报DAO接口
 * 定义对reports表的所有数据操作方法
 * 该表用于管理用户对违规内容（博客、评论、回复等）的举报信息
 */
public interface ReportDAO {

    /**
     * 根据举报ID查询举报详情
     * @param reportId 举报ID
     * @return 举报实体对象，包含完整信息
     */
    Report findById(Integer reportId) throws SQLException;

    /**
     * 查询待处理的举报信息
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 待处理举报列表（按举报时间倒序）
     */
    List<Report> findPendingReports(int pageNum, int pageSize) throws SQLException;

    /**
     * 查询已处理的举报信息
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 已处理举报列表（按处理时间倒序）
     */
    List<Report> findProcessedReports(int pageNum, int pageSize) throws SQLException;

    /**
     * 根据被举报内容的类型和ID查询举报记录
     * @param targetType 内容类型（1=博客，2=评论，3=回复）
     * @param targetId 内容ID
     * @return 相关举报记录列表
     */
    List<Report> findByTarget(Integer targetType, Integer targetId) throws SQLException;

    /**
     * 统计不同状态的举报数量
     * @param status 举报状态（0=待处理，1=已处理-有效，2=已处理-无效）
     * @return 举报数量
     */
    int countByStatus(Integer status) throws SQLException;

    /**
     * 新增举报信息
     * @param report 举报实体（包含举报类型、对象ID、原因等信息）
     * @return 新增举报的ID（自增主键），失败返回0
     */
    int insert(Report report) throws SQLException;

    /**
     * 更新举报处理结果
     * @param reportId 举报ID
     * @param status 处理状态（1=有效，2=无效）
     * @param handleResult 处理结果说明
     * @param handlerId 处理人ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int updateHandleResult(Integer reportId, Integer status, String handleResult, Integer handlerId) throws SQLException;

    /**
     * 删除举报记录（仅用于清理过期数据）
     * @param reportId 举报ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer reportId) throws SQLException;

    /**
     * 检查用户是否已举报过同一内容
     * @param userId 举报用户ID
     * @param targetType 内容类型
     * @param targetId 内容ID
     * @return 已举报返回true，否则返回false
     */
    boolean hasReported(Integer userId, Integer targetType, Integer targetId) throws SQLException;

    /**
     * 查询所有举报信息（不分页）
     * @return 所有举报列表
     */
    List<Report> findAll() throws SQLException;
}