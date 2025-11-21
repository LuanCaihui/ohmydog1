package com.petblog.dao;
import com.petblog.model.Column;

import java.sql.SQLException;
import java.util.List;

/**
 * 专栏DAO接口
 * 定义对columns表的所有数据操作方法，支持专栏的创建、查询、更新等功能
 */
public interface ColumnDAO {

    /**
     * 根据专栏ID查询专栏详情
     * @param columnId 专栏ID
     * @return 专栏实体对象，包含完整信息
     */
    Column findById(Integer columnId) throws SQLException;

    /**
     * 根据创建者ID查询其创建的所有专栏
     * @param userId 创建者用户ID
     * @return 专栏列表（按创建时间倒序）
     */
    List<Column> findByCreatorId(Integer userId) throws SQLException;

    /**
     * 查询热门专栏（按订阅量或文章数量排序）
     * @param limit 最多返回数量
     * @return 热门专栏列表
     */
    List<Column> findPopularColumns(int limit) throws SQLException;

    /**
     * 根据专栏名称模糊搜索
     * @param keyword 搜索关键词
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 符合条件的专栏列表
     */
    List<Column> searchByName(String keyword, int pageNum, int pageSize) throws SQLException;

    /**
     * 统计专栏总数
     * @return 所有专栏的数量
     */
    int countAll() throws SQLException;

    /**
     * 统计指定用户创建的专栏数量
     * @param userId 用户ID
     * @return 该用户创建的专栏数量
     */
    int countByCreatorId(Integer userId) throws SQLException;

    /**
     * 新增专栏
     * @param column 专栏实体（需包含名称、描述、创建者ID等信息）
     * @return 新增专栏的ID（自增主键），失败返回0
     */
    int insert(Column column) throws SQLException;

    /**
     * 更新专栏信息（名称、描述、封面图等）
     * @param column 专栏实体（需包含专栏ID）
     * @return 影响行数（1表示成功，0表示失败）
     */
    int update(Column column) throws SQLException;

    /**
     * 增加专栏的订阅量（+1）
     * @param columnId 专栏ID
     * @return 影响行数
     */
    int incrementSubscribeCount(Integer columnId) throws SQLException;

    /**
     * 减少专栏的订阅量（-1）
     * @param columnId 专栏ID
     * @return 影响行数
     */
    int decrementSubscribeCount(Integer columnId) throws SQLException;

    /**
     * 删除专栏（仅允许创建者或管理员操作）
     * @param columnId 专栏ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer columnId) throws SQLException;

    /**
     * 检查专栏名称是否已存在
     * @param columnName 专栏名称
     * @return 存在返回true，否则返回false
     */
    boolean existsByName(String columnName) throws SQLException;
}