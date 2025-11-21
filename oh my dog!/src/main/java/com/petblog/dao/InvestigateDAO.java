package com.petblog.dao;

import com.petblog.model.Investigate;

import java.sql.SQLException;
import java.util.List;

/**
 * 药品-用户调查关联DAO接口
 * 定义对investigate表的所有数据操作方法
 * 该表用于记录用户参与药品调查的关联关系（支持用户对多种药品进行调查反馈）
 */
public interface InvestigateDAO {

    /**
     * 根据调查记录ID查询详情
     * @param investigateId 调查记录ID
     * @return 调查关联实体对象，包含完整信息
     */
    Investigate findById(Integer investigateId) throws SQLException;

    /**
     * 根据用户ID查询其参与的所有药品调查记录
     * @param userId 用户ID
     * @return 调查记录列表（按参与时间倒序）
     */
    List<Investigate> findByUserId(Integer userId) throws SQLException;

    /**
     * 根据药品ID查询所有参与该药品调查的用户ID
     * @param medicineId 药品ID
     * @return 参与用户ID列表
     */
    List<Integer> findUserIdsByMedicineId(Integer medicineId) throws SQLException;

    /**
     * 统计某药品的调查参与人数
     * @param medicineId 药品ID
     * @return 参与人数
     */
    int countByMedicineId(Integer medicineId) throws SQLException;

    /**
     * 检查用户是否已参与某药品的调查
     * @param userId 用户ID
     * @param medicineId 药品ID
     * @return 已参与返回true，否则返回false
     */
    boolean hasParticipated(Integer userId, Integer medicineId) throws SQLException;

    /**
     * 新增调查参与记录
     * @param investigate 调查关联实体（包含用户ID、药品ID、反馈内容等）
     * @return 新增记录的ID（自增主键），失败返回0
     */
    int insert(Investigate investigate) throws SQLException;

    /**
     * 更新用户的调查反馈内容
     * @param investigate 调查关联实体（需包含调查记录ID和新反馈内容）
     * @return 影响行数（1表示成功，0表示失败）
     */
    int updateFeedback(Investigate investigate) throws SQLException;

    /**
     * 删除用户的调查参与记录
     * @param investigateId 调查记录ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer investigateId) throws SQLException;

    /**
     * 删除用户参与的所有调查记录
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Integer userId)throws SQLException;

    /**
     * 删除某药品的所有调查记录
     * @param medicineId 药品ID
     * @return 影响行数
     */
    int deleteByMedicineId(Integer medicineId) throws SQLException;
}