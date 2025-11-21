package com.petblog.dao;
import com.petblog.model.Challenge;

import java.sql.SQLException;
import java.util.List;
import java.util.Date;

/**
 * 挑战活动DAO接口
 * 定义对challenges表的所有数据操作方法，支持挑战活动的创建、查询、更新等功能
 */
public interface ChallengeDAO {

    /**
     * 根据挑战ID查询挑战详情
     * @param challengeId 挑战ID
     * @return 挑战活动实体对象，包含完整信息
     */
    Challenge findById(Integer challengeId) throws SQLException;

    /**
     * 查询所有进行中的挑战活动
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 进行中的挑战列表（按开始时间倒序）
     */
    List<Challenge> findActiveChallenges(int pageNum, int pageSize) throws SQLException;

    /**
     * 查询已结束的挑战活动
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 已结束的挑战列表（按结束时间倒序）
     */
    List<Challenge> findCompletedChallenges(int pageNum, int pageSize)throws SQLException;

    /**
     * 根据挑战标题模糊搜索
     * @param keyword 搜索关键词
     * @param status 挑战状态（0=未开始，1=进行中，2=已结束，null=全部）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 符合条件的挑战列表
     */
    List<Challenge> searchByTitle(String keyword, Integer status, int pageNum, int pageSize) throws SQLException;

    /**
     * 统计不同状态的挑战数量
     * @param status 挑战状态（0=未开始，1=进行中，2=已结束，null=全部）
     * @return 挑战数量
     */
    int countByStatus(Integer status) throws SQLException;

    /**
     * 新增挑战活动
     * @param challenge 挑战实体（需包含标题、描述、时间范围等核心信息）
     * @return 新增挑战的ID（自增主键），失败返回0
     */
    int insert(Challenge challenge) throws SQLException;

    /**
     * 更新挑战活动信息（标题、描述、时间等）
     * @param challenge 挑战实体（需包含挑战ID）
     * @return 影响行数（1表示成功，0表示失败）
     */
    int update(Challenge challenge) throws SQLException;

    /**
     * 更新挑战状态（手动调整，如提前结束或延期）
     * @param challengeId 挑战ID
     * @param status 新状态（0=未开始，1=进行中，2=已结束）
     * @param endTime 结束时间（状态设为2时必填）
     * @return 影响行数
     */
    int updateStatus(Integer challengeId, Integer status, Date endTime) throws SQLException;

    /**
     * 删除挑战活动（仅允许删除未开始的挑战）
     * @param challengeId 挑战ID
     * @return 影响行数（1表示成功，0表示失败或不允许删除）
     */
    int delete(Integer challengeId) throws SQLException;

    /**
     * 查询指定时间范围内的挑战活动
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时间范围内的挑战列表
     */
    List<Challenge> findByTimeRange(Date startTime, Date endTime) throws SQLException;
}