// src/main/java/com/petblog/dao/impl/MedicineDAOImpl.java
package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.MedicineDAO;
import com.petblog.model.Medicine;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class MedicineDAOImpl extends BaseJdbcDAO<Medicine> implements MedicineDAO {

    @Override
    public Medicine findById(Integer medicineId) {
        String sql = "SELECT medicine_id, medicine_name, content, release_time, user_id FROM petmedicine WHERE medicine_id = ?";
        try {
            return queryForObject(sql, this::mapRowToMedicine, medicineId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询药品信息", null);
        }
    }

    @Override
    public List<Medicine> searchByName(String nameKeyword, int pageNum, int pageSize) {
        String sql = "SELECT medicine_id, medicine_name, content, release_time, user_id FROM petmedicine WHERE medicine_name LIKE ? ORDER BY release_time DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToMedicine, "%" + nameKeyword + "%", pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据名称关键词搜索药品", null);
        }
    }

    @Override
    public List<Medicine> findByCategory(String category, int pageNum, int pageSize) {
        // 假设category字段在表中存在
        String sql = "SELECT medicine_id, medicine_name, content, release_time, user_id FROM petmedicine WHERE category = ? ORDER BY release_time DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToMedicine, category, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据分类查询药品", null);
        }
    }

    @Override
    public List<Medicine> findAll(int pageNum, int pageSize) {
        String sql = "SELECT medicine_id, medicine_name, content, release_time, user_id FROM petmedicine ORDER BY release_time DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToMedicine, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "分页查询所有药品", null);
        }
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM petmedicine";
        try {
            Number count = (Number) queryForSingleValue(sql);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计所有药品数量", 0);
        }
    }

    @Override
    public int countByCategory(String category) {
        String sql = "SELECT COUNT(*) FROM petmedicine WHERE category = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, category);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计分类药品数量", 0);
        }
    }

    @Override
    public int insert(Medicine medicine) {
        String sql = "INSERT INTO petmedicine (medicine_name, content, release_time, user_id) VALUES (?, ?, ?, ?)";
        try {
            return insert(sql, medicine.getMedicineName(), medicine.getContent(),
                         medicine.getReleaseTime(), medicine.getUserId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "插入药品信息", 0);
        }
    }

    @Override
    public int update(Medicine medicine) {
        String sql = "UPDATE petmedicine SET medicine_name = ?, content = ?, release_time = ? WHERE medicine_id = ?";
        try {
            return update(sql, medicine.getMedicineName(), medicine.getContent(),
                         medicine.getReleaseTime(), medicine.getMedicineId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新药品信息", 0);
        }
    }

    @Override
    public int updateStatus(Integer medicineId, Integer status) {
        // 假设有一个status字段
        String sql = "UPDATE petmedicine SET status = ? WHERE medicine_id = ?";
        try {
            return update(sql, status, medicineId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新药品状态", 0);
        }
    }

    @Override
    public int delete(Integer medicineId) {
        String sql = "DELETE FROM petmedicine WHERE medicine_id = ?";
        try {
            return delete(sql, medicineId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除药品信息", 0);
        }
    }

    @Override
    public boolean existsByName(String medicineName) {
        String sql = "SELECT COUNT(*) FROM petmedicine WHERE medicine_name = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, medicineName);
            return count != null && count.intValue() > 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "检查药品名称是否存在", false);
        }
    }

    private Medicine mapRowToMedicine(ResultSet rs) throws SQLException {
        Medicine medicine = new Medicine();
        medicine.setMedicineId(rs.getInt("medicine_id"));
        medicine.setMedicineName(rs.getString("medicine_name"));
        medicine.setContent(rs.getString("content"));
        medicine.setReleaseTime(rs.getDate("release_time"));
        medicine.setUserId(rs.getInt("user_id"));
        return medicine;
    }
}