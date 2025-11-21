package com.petblog.dao;

import com.petblog.model.PetChallenge;

import java.sql.SQLException;
import java.util.List;

/**
 * 宠物-挑战关联DAO接口
 * 定义对petchallenge表的所有数据操作方法
 * 该表用于维护宠物与挑战活动的多对多关系（一只宠物可参与多个挑战，一个挑战可包含多只宠物）
 */
public interface PetChallengeDAO {

    /**
     * 根据宠物ID查询其参与的所有挑战ID
     * @param petId 宠物ID
     * @return 参与的挑战ID列表（按参与时间倒序）
     */
    List<Integer> findChallengeIdsByPetId(Integer petId) throws SQLException;

    /**
     * 根据挑战ID查询参与该挑战的所有宠物ID
     * @param challengeId 挑战ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 参与的宠物ID列表
     */
    List<Integer> findPetIdsByChallengeId(Integer challengeId, int pageNum, int pageSize) throws SQLException;

    /**
     * 统计参与某挑战的宠物总数
     * @param challengeId 挑战ID
     * @return 宠物数量
     */
    int countPetsByChallengeId(Integer challengeId) throws SQLException;

    /**
     * 检查宠物是否已参与某挑战
     * @param petId 宠物ID
     * @param challengeId 挑战ID
     * @return 已参与返回true，否则返回false
     */
    boolean hasParticipated(Integer petId, Integer challengeId) throws SQLException;

    /**
     * 新增宠物参与挑战的关联关系
     * @param petChallenge 宠物-挑战关联实体
     * @return 影响行数（1表示成功，0表示失败）
     */
    int insert(PetChallenge petChallenge) throws SQLException;

    /**
     * 批量新增宠物参与挑战的关联关系
     * @param petChallenges 宠物-挑战关联实体列表
     * @return 成功插入的数量
     */
    int batchInsert(List<PetChallenge> petChallenges) throws SQLException;

    /**
     * 取消宠物参与的所有挑战
     * @param petId 宠物ID
     * @return 影响行数
     */
    int deleteByPetId(Integer petId) throws SQLException;

    /**
     * 移除参与某挑战的所有宠物
     * @param challengeId 挑战ID
     * @return 影响行数
     */
    int deleteByChallengeId(Integer challengeId) throws SQLException;

    /**
     * 取消宠物参与的指定挑战
     * @param petId 宠物ID
     * @param challengeId 挑战ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer petId, Integer challengeId) throws SQLException;
}