package com.petblog.dao;

import com.petblog.model.Pet;

import java.sql.SQLException;
import java.util.List;

/**
 * 宠物DAO接口
 * 定义对pets表的所有数据操作方法，用于管理用户的宠物档案信息
 */
public interface PetDAO {

    /**
     * 根据宠物ID查询宠物详情
     * @param petId 宠物ID
     * @return 宠物实体对象，包含完整信息
     */
    Pet findById(Integer petId) throws SQLException;

    /**
     * 根据用户ID查询其所有宠物
     * @param userId 用户ID
     * @return 宠物列表（按添加时间倒序）
     */
    List<Pet> findByUserId(Integer userId) throws SQLException;

    /**
     * 根据宠物名称模糊搜索（限定用户范围）
     * @param userId 用户ID
     * @param nameKeyword 名称关键词
     * @return 符合条件的宠物列表
     */
    List<Pet> searchByName(Integer userId, String nameKeyword) throws SQLException;

    /**
     * 根据宠物类型查询（如猫、狗等）
     * @param type 宠物类型
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 该类型的宠物列表（公开可见的）
     */
    List<Pet> findByType(String type, int pageNum, int pageSize) throws SQLException;

    /**
     * 统计用户的宠物数量
     * @param userId 用户ID
     * @return 宠物数量
     */
    int countByUserId(Integer userId) throws SQLException;

    /**
     * 新增宠物档案
     * @param pet 宠物实体（包含名称、类型、年龄等核心信息）
     * @return 新增宠物的ID（自增主键），失败返回0
     */
    int insert(Pet pet) throws SQLException;

    /**
     * 更新宠物信息
     * @param pet 宠物实体（需包含宠物ID）
     * @return 影响行数（1表示成功，0表示失败）
     */
    int update(Pet pet) throws SQLException;

    /**
     * 更新宠物的头像
     * @param petId 宠物ID
     * @param avatarUrl 头像图片URL
     * @return 影响行数
     */
    int updateAvatar(Integer petId, String avatarUrl) throws SQLException;

    /**
     * 删除宠物档案
     * @param petId 宠物ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer petId) throws SQLException;

    /**
     * 批量删除用户的宠物
     * @param petIds 宠物ID列表
     * @return 成功删除的数量
     */
    int batchDelete(List<Integer> petIds) throws SQLException;

    /**
     * 检查宠物名称在用户范围内是否重复
     * @param userId 用户ID
     * @param petName 宠物名称
     * @return 重复返回true，否则返回false
     */
    boolean existsNameInUser(Integer userId, String petName) throws SQLException;
}