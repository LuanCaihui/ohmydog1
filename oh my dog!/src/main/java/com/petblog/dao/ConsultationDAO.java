package com.petblog.dao;

import com.petblog.model.Consultation;
import java.sql.SQLException;
import java.util.List;

/**
 * 问诊记录数据访问接口
 * 定义对consultations表的所有数据操作方法
 */
public interface ConsultationDAO {

    /**
     * 根据问诊记录ID查询详情
     * @param id 问诊记录ID
     * @return 问诊记录实体对象，不存在则返回null
     */
    Consultation findById(Integer id) throws SQLException;

    /**
     * 根据用户ID查询该用户的所有问诊记录
     * @param userId 用户ID
     * @return 问诊记录列表（按创建时间倒序）
     */
    List<Consultation> findByUserId(Integer userId) throws SQLException;

    /**
     * 根据用户ID分页查询问诊记录
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页条数
     * @return 问诊记录列表（按创建时间倒序）
     */
    List<Consultation> findByUserId(Integer userId, int pageNum, int pageSize) throws SQLException;

    /**
     * 查询所有问诊记录（管理员功能）
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页条数
     * @return 问诊记录列表（按创建时间倒序）
     */
    List<Consultation> findAll(int pageNum, int pageSize) throws SQLException;

    /**
     * 统计用户问诊记录总数
     * @param userId 用户ID
     * @return 记录总数
     */
    int countByUserId(Integer userId) throws SQLException;

    /**
     * 统计所有问诊记录总数
     * @return 记录总数
     */
    int countAll() throws SQLException;

    /**
     * 新增问诊记录
     * @param consultation 问诊记录实体对象
     * @return 新增记录的ID，失败返回0
     */
    int insert(Consultation consultation) throws SQLException;

    /**
     * 更新问诊记录
     * @param consultation 问诊记录实体对象
     * @return 受影响的行数
     */
    int update(Consultation consultation) throws SQLException;

    /**
     * 删除问诊记录
     * @param id 问诊记录ID
     * @return 受影响的行数
     */
    int delete(Integer id) throws SQLException;
}

