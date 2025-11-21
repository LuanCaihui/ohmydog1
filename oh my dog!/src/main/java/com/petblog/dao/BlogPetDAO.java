package com.petblog.dao;
import com.petblog.model.BlogPet;

import java.sql.SQLException;
import java.util.List;

/**
 * 博客-宠物关联DAO接口
 * 定义对blogpet表的所有数据操作方法
 * 该表用于维护博客与宠物的多对多关系（一篇博客可关联多个宠物，一个宠物可出现在多篇博客中）
 */
public interface BlogPetDAO {

    /**
     * 根据博客ID查询关联的所有宠物ID
     * @param blogId 博客ID
     * @return 关联的宠物ID列表
     */
    List<Integer> findPetIdsByBlogId(Integer blogId) throws SQLException;

    /**
     * 根据宠物ID查询关联的所有博客ID
     * @param petId 宠物ID
     * @return 关联的博客ID列表（按发布时间倒序）
     */
    List<Integer> findBlogIdsByPetId(Integer petId) throws SQLException;

    /**
     * 检查博客与宠物的关联关系是否存在
     * @param blogId 博客ID
     * @param petId 宠物ID
     * @return 存在返回true，否则返回false
     */
    boolean exists(Integer blogId, Integer petId) throws SQLException;

    /**
     * 新增博客与宠物的关联关系
     * @param blogPet 博客-宠物关联实体
     * @return 影响行数（1表示成功，0表示失败）
     */
    int insert(BlogPet blogPet) throws SQLException;

    /**
     * 批量新增博客与宠物的关联关系
     * @param blogPets 博客-宠物关联实体列表
     * @return 成功插入的数量
     */
    int batchInsert(List<BlogPet> blogPets) throws SQLException;

    /**
     * 解除某篇博客与所有宠物的关联
     * @param blogId 博客ID
     * @return 影响行数
     */
    int deleteByBlogId(Integer blogId) throws SQLException;

    /**
     * 解除某个宠物与所有博客的关联
     * @param petId 宠物ID
     * @return 影响行数
     */
    int deleteByPetId(Integer petId) throws SQLException;

    /**
     * 解除指定博客与指定宠物的关联
     * @param blogId 博客ID
     * @param petId 宠物ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer blogId, Integer petId) throws SQLException;
}