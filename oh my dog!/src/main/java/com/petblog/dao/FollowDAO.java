package com.petblog.dao;

import com.petblog.model.Follow;

import java.sql.SQLException;
import java.util.List;

/**
 * 用户关注DAO接口
 * 定义对follows表的所有数据操作方法
 * 该表用于记录用户之间的关注关系（支持用户A关注用户B的社交场景）
 */
public interface FollowDAO {

    /**
     * 查询用户的所有关注列表（我关注了谁）
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 被关注用户的ID列表（按关注时间倒序）
     */
    List<Integer> findFollowingIds(Integer userId, int pageNum, int pageSize) throws SQLException;

    /**
     * 查询用户的所有粉丝列表（谁关注了我）
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 粉丝用户的ID列表（按关注时间倒序）
     */
    List<Integer> findFollowerIds(Integer userId, int pageNum, int pageSize) throws SQLException;

    /**
     * 统计用户的关注数量（我关注了多少人）
     * @param userId 用户ID
     * @return 关注数量
     */
    int countFollowing(Integer userId) throws SQLException;

    /**
     * 统计用户的粉丝数量（多少人关注了我）
     * @param userId 用户ID
     * @return 粉丝数量
     */
    int countFollowers(Integer userId) throws SQLException;

    /**
     * 检查用户A是否关注了用户B
     * @param fromUserId 关注者ID（用户A）
     * @param toUserId 被关注者ID（用户B）
     * @return 已关注返回true，否则返回false
     */
    boolean isFollowing(Integer fromUserId, Integer toUserId) throws SQLException;

    /**
     * 新增关注关系（用户A关注用户B）
     * @param follow 关注关系实体（包含关注者ID、被关注者ID和关注时间）
     * @return 影响行数（1表示成功，0表示失败）
     */
    int insert(Follow follow) throws SQLException;

    /**
     * 取消关注（删除用户A对用户B的关注关系）
     * @param fromUserId 关注者ID（用户A）
     * @param toUserId 被关注者ID（用户B）
     * @return 影响行数（1表示成功，0表示失败）
     */
    int delete(Integer fromUserId, Integer toUserId) throws SQLException;

    /**
     * 移除用户的所有关注关系（用户A关注的所有人）
     * @param fromUserId 关注者ID（用户A）
     * @return 影响行数
     */
    int deleteAllFollowing(Integer fromUserId) throws SQLException;

    /**
     * 移除用户的所有粉丝关系（所有关注用户A的人）
     * @param toUserId 被关注者ID（用户A）
     * @return 影响行数
     */
    int deleteAllFollowers(Integer toUserId) throws SQLException;
}