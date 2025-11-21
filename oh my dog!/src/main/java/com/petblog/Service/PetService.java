package com.petblog.Service;

import com.petblog.dao.PetDAO;
import com.petblog.dao.impl.PetDAOImpl;
import com.petblog.model.Pet;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class PetService extends BaseService {

    private final PetDAO petDAO;

    public PetService() {
        this.petDAO = new PetDAOImpl();
    }

    /**
     * 根据宠物ID查询宠物详情
     */
    public Pet getPetById(Integer petId) {
        try {
            return petDAO.findById(petId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询宠物详情");
            return null;
        }
    }

    /**
     * 根据用户ID查询其所有宠物
     */
    public List<Pet> getPetsByUserId(Integer userId) {
        try {
            return petDAO.findByUserId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据用户ID查询宠物列表");
            return null;
        }
    }

    /**
     * 根据宠物名称模糊搜索（限定用户范围）
     */
    public List<Pet> searchPetsByName(Integer userId, String nameKeyword) {
        try {
            return petDAO.searchByName(userId, nameKeyword);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据名称搜索宠物");
            return null;
        }
    }

    /**
     * 根据宠物类型查询（如猫、狗等）
     */
    public List<Pet> getPetsByType(String type, int pageNum, int pageSize) {
        try {
            return petDAO.findByType(type, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据类型查询宠物");
            return null;
        }
    }

    /**
     * 统计用户的宠物数量
     */
    public int countPetsByUserId(Integer userId) {
        try {
            return petDAO.countByUserId(userId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计用户宠物数量");
            return 0;
        }
    }

    /**
     * 新增宠物档案
     */
    public Integer createPet(Pet pet) {
        try {
            return petDAO.insert(pet);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增宠物档案");
            return 0;
        }
    }

    /**
     * 更新宠物信息
     */
    public boolean updatePet(Pet pet) {
        try {
            int result = petDAO.update(pet);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新宠物信息");
            return false;
        }
    }

    /**
     * 更新宠物的头像
     */
    public boolean updatePetAvatar(Integer petId, String avatarUrl) {
        try {
            int result = petDAO.updateAvatar(petId, avatarUrl);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新宠物头像");
            return false;
        }
    }

    /**
     * 删除宠物档案
     */
    public boolean deletePet(Integer petId) {
        try {
            int result = petDAO.delete(petId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除宠物档案");
            return false;
        }
    }

    /**
     * 批量删除用户的宠物
     */
    public boolean batchDeletePets(List<Integer> petIds) {
        try {
            int result = petDAO.batchDelete(petIds);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "批量删除宠物");
            return false;
        }
    }

    /**
     * 检查宠物名称在用户范围内是否重复
     */
    public boolean isPetNameExistsInUser(Integer userId, String petName) {
        try {
            return petDAO.existsNameInUser(userId, petName);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "检查宠物名称是否重复");
            return false;
        }
    }
}
