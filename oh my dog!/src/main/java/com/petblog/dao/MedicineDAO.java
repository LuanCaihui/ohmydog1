package com.petblog.dao;

import com.petblog.model.Medicine;

import java.sql.SQLException;
import java.util.List;

/**
 * 药品DAO接口
 * 定义对medicine表的所有数据操作方法
 * 该表用于管理宠物药品的基本信息（如名称、用途、使用说明等）
 */
public interface MedicineDAO {

    /**
     * 根据药品ID查询药品详情
     * @param medicineId 药品ID
     * @return 药品实体对象，包含完整信息
     */
    Medicine findById(Integer medicineId) throws SQLException;

    /**
     * 根据药品名称模糊搜索
     * @param nameKeyword 名称关键词
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 符合条件的药品列表
     */
    List<Medicine> searchByName(String nameKeyword, int pageNum, int pageSize) throws SQLException;

    /**
     * 根据药品类别查询
     * @param category 药品类别（如"驱虫药"、"感冒药"等）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 该类别下的药品列表
     */
    List<Medicine> findByCategory(String category, int pageNum, int pageSize) throws SQLException;

    /**
     * 查询所有药品（分页）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 药品列表（按更新时间倒序）
     */
    List<Medicine> findAll(int pageNum, int pageSize) throws SQLException;

    /**
     * 统计药品总数
     * @return 所有药品的数量
     */
    int countAll() throws SQLException;

    /**
     * 统计指定类别的药品数量
     * @param category 药品类别
     * @return 该类别下的药品数量
     */
    int countByCategory(String category) throws SQLException;

    /**
     * 新增药品信息
     * @param medicine 药品实体（包含名称、类别、用途等核心信息）
     * @return 新增药品的ID（自增主键），失败返回0
     */
    int insert(Medicine medicine) throws SQLException;

    /**
     * 更新药品信息
     * @param medicine 药品实体（需包含药品ID）
     * @return 影响行数（1表示成功，0表示失败）
     */
    int update(Medicine medicine) throws SQLException;

    /**
     * 更新药品的状态（上架/下架）
     * @param medicineId 药品ID
     * @param status 状态（0=下架，1=上架）
     * @return 影响行数
     */
    int updateStatus(Integer medicineId, Integer status) throws SQLException;

    /**
     * 删除药品信息（谨慎使用，通常建议逻辑删除）
     * @param medicineId 药品ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer medicineId) throws SQLException;

    /**
     * 检查药品名称是否已存在
     * @param medicineName 药品名称
     * @return 存在返回true，否则返回false
     */
    boolean existsByName(String medicineName) throws SQLException;
}