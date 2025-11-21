package com.petblog.Service;

import com.petblog.dao.PetChallengeDAO;
import com.petblog.dao.impl.PetChallengeDAOImpl;
import com.petblog.model.PetChallenge;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class PetChallengeService extends BaseService {

    private final PetChallengeDAO petChallengeDAO;

    public PetChallengeService() {
        this.petChallengeDAO = new PetChallengeDAOImpl();
    }

    /**
     * 根据宠物ID查询其参与的所有挑战ID
     */
    public List<Integer> getChallengeIdsByPetId(Integer petId) {
        try {
            return petChallengeDAO.findChallengeIdsByPetId(petId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据宠物ID查询挑战列表");
            return null;
        }
    }

    /**
     * 根据挑战ID查询参与该挑战的所有宠物ID
     */
    public List<Integer> getPetIdsByChallengeId(Integer challengeId, int pageNum, int pageSize) {
        try {
            return petChallengeDAO.findPetIdsByChallengeId(challengeId, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据挑战ID查询宠物列表");
            return null;
        }
    }

    /**
     * 统计参与某挑战的宠物总数
     */
    public int countPetsByChallengeId(Integer challengeId) {
        try {
            return petChallengeDAO.countPetsByChallengeId(challengeId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计挑战参与宠物数量");
            return 0;
        }
    }

    /**
     * 检查宠物是否已参与某挑战
     */
    public boolean hasPetParticipated(Integer petId, Integer challengeId) {
        try {
            return petChallengeDAO.hasParticipated(petId, challengeId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "检查宠物是否参与挑战");
            return false;
        }
    }

    /**
     * 新增宠物参与挑战的关联关系
     */
    public boolean addPetToChallenge(PetChallenge petChallenge) {
        try {
            int result = petChallengeDAO.insert(petChallenge);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增宠物挑战关联");
            return false;
        }
    }

    /**
     * 批量新增宠物参与挑战的关联关系
     */
    public boolean batchAddPetsToChallenge(List<PetChallenge> petChallenges) {
        try {
            int result = petChallengeDAO.batchInsert(petChallenges);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "批量新增宠物挑战关联");
            return false;
        }
    }

    /**
     * 取消宠物参与的所有挑战
     */
    public boolean removePetFromAllChallenges(Integer petId) {
        try {
            int result = petChallengeDAO.deleteByPetId(petId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "取消宠物所有挑战参与");
            return false;
        }
    }

    /**
     * 移除参与某挑战的所有宠物
     */
    public boolean removeAllPetsFromChallenge(Integer challengeId) {
        try {
            int result = petChallengeDAO.deleteByChallengeId(challengeId);
            return result >= 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "移除挑战所有参与宠物");
            return false;
        }
    }

    /**
     * 取消宠物参与的指定挑战
     */
    public boolean removePetFromChallenge(Integer petId, Integer challengeId) {
        try {
            int result = petChallengeDAO.delete(petId, challengeId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "取消宠物挑战参与");
            return false;
        }
    }
}
