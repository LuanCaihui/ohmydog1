package com.petblog.dao;


import com.petblog.model.Vote;

import java.sql.SQLException;
import java.util.List;

/**
 * 投票DAO接口
 * 定义对votes表的所有数据操作方法
 * 该表用于管理用户对多种内容（如评论、回复、调查选项等）的投票互动
 */
public interface VoteDAO {

    /**
     * 根据投票ID查询投票详情
     * @param voteId 投票ID
     * @return 投票实体对象，包含完整信息
     */
    Vote findById(Integer voteId) throws SQLException;

    /**
     * 根据目标内容查询所有投票记录
     * @param targetType 内容类型（1=评论，2=回复，3=调查选项等）
     * @param targetId 内容ID
     * @return 该内容的所有投票记录列表
     */
    List<Vote> findByTarget(Integer targetType, Integer targetId) throws SQLException;

    /**
     * 查询用户对指定类型内容的投票记录
     * @param userId 用户ID
     * @param targetType 内容类型
     * @return 用户对该类型内容的投票记录列表
     */
    List<Vote> findByUserAndType(Integer userId, Integer targetType) throws SQLException;

    /**
     * 统计指定内容的赞成票数量
     * @param targetType 内容类型
     * @param targetId 内容ID
     * @return 赞成票数量
     */
    int countUpVotes(Integer targetType, Integer targetId) throws SQLException;

    /**
     * 统计指定内容的反对票数量
     * @param targetType 内容类型
     * @param targetId 内容ID
     * @return 反对票数量
     */
    int countDownVotes(Integer targetType, Integer targetId) throws SQLException;

    /**
     * 检查用户对指定内容的投票状态
     * @param userId 用户ID
     * @param targetType 内容类型
     * @param targetId 内容ID
     * @return 1=赞成，-1=反对，0=未投票
     */
    int getUserVoteStatus(Integer userId, Integer targetType, Integer targetId) throws SQLException;

    /**
     * 新增投票记录
     * @param vote 投票实体（包含用户ID、内容类型、内容ID、投票类型等信息）
     * @return 新增投票的ID（自增主键），失败返回0
     */
    int insert(Vote vote) throws SQLException;

    /**
     * 更新用户的投票（从赞成改为反对或反之）
     * @param voteId 投票ID
     * @param voteType 新投票类型（1=赞成，-1=反对）
     * @return 影响行数（1表示成功，0表示失败）
     */
    int updateVoteType(Integer voteId, Integer voteType) throws SQLException;

    /**
     * 取消用户对指定内容的投票
     * @param userId 用户ID
     * @param targetType 内容类型
     * @param targetId 内容ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    int deleteByUserAndTarget(Integer userId, Integer targetType, Integer targetId) throws SQLException;

    /**
     * 删除指定内容的所有投票记录
     * @param targetType 内容类型
     * @param targetId 内容ID
     * @return 影响行数
     */
    int deleteByTarget(Integer targetType, Integer targetId) throws SQLException;

    /**
     * 删除用户的所有投票记录
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Integer userId) throws SQLException;
}
    