package com.petblog.dao;
import com.petblog.model.CreateColumn;

import java.sql.SQLException;
import java.util.List;

/**
 * 用户-专栏创建关联DAO接口
 * 定义对createcolumn表的所有数据操作方法
 * 该表用于记录用户创建专栏的关联关系（支持一个专栏可由多个用户共同创建的场景）
 */
public interface CreateColumnDAO {

    /**
     * 根据专栏ID查询所有创建者ID
     * @param columnId 专栏ID
     * @return 创建者用户ID列表
     */
    List<Integer> findCreatorIdsByColumnId(Integer columnId) throws SQLException;

    /**
     * 根据用户ID查询其创建的所有专栏ID
     * @param userId 用户ID
     * @return 该用户创建的专栏ID列表（按创建时间倒序）
     */
    List<Integer> findColumnIdsByCreatorId(Integer userId) throws SQLException;

    /**
     * 检查用户是否为专栏的创建者
     * @param userId 用户ID
     * @param columnId 专栏ID
     * @return 是创建者返回true，否则返回false
     */
    boolean isCreator(Integer userId, Integer columnId) throws SQLException;

    /**
     * 新增用户与专栏的创建关联关系
     * @param createColumn 创建关联实体
     * @return 影响行数（1表示成功，0表示失败）
     */
    int insert(CreateColumn createColumn) throws SQLException;

    /**
     * 批量新增用户与专栏的创建关联关系
     * @param createColumns 创建关联实体列表
     * @return 成功插入的数量
     */
    int batchInsert(List<CreateColumn> createColumns) throws SQLException;

    /**
     * 移除用户与专栏的创建关联关系（仅用于多人共创场景的权限移除）
     * @param userId 用户ID
     * @param columnId 专栏ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer userId, Integer columnId) throws SQLException;

    /**
     * 移除专栏的所有创建者关联（通常在删除专栏时调用）
     * @param columnId 专栏ID
     * @return 影响行数
     */
    int deleteByColumnId(Integer columnId) throws SQLException;

    /**
     * 移除用户创建的所有专栏关联（通常在删除用户时调用）
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Integer userId) throws SQLException;
}