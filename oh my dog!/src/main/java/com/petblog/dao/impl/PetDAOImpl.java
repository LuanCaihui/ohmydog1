// src/main/java/com/petblog/dao/impl/PetDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.PetDAO;
import com.petblog.model.Pet;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class PetDAOImpl extends BaseJdbcDAO<Pet> implements PetDAO {

    @Override
    public Pet findById(Integer petId) {
        String sql = "SELECT pet_id, pet_name, pet_gender, pet_breed, pet_birthdate, user_id, pet_avatar_path FROM pets WHERE pet_id = ?";
        try {
            return queryForObject(sql, this::mapRowToPet, petId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询宠物信息", null);
        }
    }

    @Override
    public List<Pet> findByUserId(Integer userId) {
        String sql = "SELECT pet_id, pet_name, pet_gender, pet_breed, pet_birthdate, user_id, pet_avatar_path FROM pets WHERE user_id = ? ORDER BY pet_id DESC";
        try {
            return queryForList(sql, this::mapRowToPet, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "查询用户的宠物列表", null);
        }
    }

    @Override
    public List<Pet> searchByName(Integer userId, String nameKeyword) {
        String sql = "SELECT pet_id, pet_name, pet_gender, pet_breed, pet_birthdate, user_id, pet_avatar_path FROM pets WHERE user_id = ? AND pet_name LIKE ?";
        try {
            return queryForList(sql, this::mapRowToPet, userId, "%" + nameKeyword + "%");
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据名称关键词搜索宠物", null);
        }
    }

    @Override
    public List<Pet> findByType(String type, int pageNum, int pageSize) {
        String sql = "SELECT pet_id, pet_name, pet_gender, pet_breed, pet_birthdate, user_id, pet_avatar_path FROM pets WHERE pet_breed = ? LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToPet, type, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据品种分页查询宠物", null);
        }
    }

    @Override
    public int countByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM pets WHERE user_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, userId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计用户的宠物数量", 0);
        }
    }

    @Override
    public int insert(Pet pet) {
        String sql = "INSERT INTO pets (pet_name, pet_gender, pet_breed, pet_birthdate, user_id, pet_avatar_path) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            return insert(sql, pet.getPetName(), pet.getPetGender(), pet.getPetBreed(),
                         pet.getPetBirthdate(), pet.getUserId(), pet.getPetAvatarPath());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "添加宠物信息", 0);
        }
    }

    @Override
    public int update(Pet pet) {
        String sql = "UPDATE pets SET pet_name = ?, pet_gender = ?, pet_breed = ?, pet_birthdate = ?, user_id = ?, pet_avatar_path = ? WHERE pet_id = ?";
        try {
            return update(sql, pet.getPetName(), pet.getPetGender(), pet.getPetBreed(),
                         pet.getPetBirthdate(), pet.getUserId(), pet.getPetAvatarPath(), pet.getPetId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新宠物信息", 0);
        }
    }

    @Override
    public int updateAvatar(Integer petId, String avatarUrl) {
        String sql = "UPDATE pets SET pet_avatar_path = ? WHERE pet_id = ?";
        try {
            return update(sql, avatarUrl, petId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新宠物头像", 0);
        }
    }

    @Override
    public int delete(Integer petId) {
        String sql = "DELETE FROM pets WHERE pet_id = ?";
        try {
            return delete(sql, petId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除宠物信息", 0);
        }
    }

    @Override
    public int batchDelete(List<Integer> petIds) {
        if (petIds == null || petIds.isEmpty()) {
            return 0;
        }

        StringBuilder sql = new StringBuilder("DELETE FROM pets WHERE pet_id IN (");
        for (int i = 0; i < petIds.size(); i++) {
            sql.append("?");
            if (i < petIds.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");

        try {
            Object[] params = petIds.toArray();
            return update(sql.toString(), params);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "批量删除宠物信息", 0);
        }
    }

    @Override
    public boolean existsNameInUser(Integer userId, String petName) {
        String sql = "SELECT COUNT(*) FROM pets WHERE user_id = ? AND pet_name = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, userId, petName);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查用户下宠物名称是否存在", false);
        }
    }

    /**
     * 将ResultSet映射为Pet对象
     */
    private Pet mapRowToPet(ResultSet rs) throws SQLException {
        Pet pet = new Pet();
        pet.setPetId(rs.getInt("pet_id"));
        pet.setPetName(rs.getString("pet_name"));
        pet.setPetGender(rs.getString("pet_gender"));
        pet.setPetBreed(rs.getString("pet_breed"));
        pet.setPetBirthdate(rs.getDate("pet_birthdate"));
        pet.setUserId(rs.getInt("user_id"));
        pet.setPetAvatarPath(rs.getString("pet_avatar_path"));
        return pet;
    }
}