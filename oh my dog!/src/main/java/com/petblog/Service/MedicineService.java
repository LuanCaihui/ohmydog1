package com.petblog.Service;

import com.petblog.dao.MedicineDAO;
import com.petblog.dao.impl.MedicineDAOImpl;
import com.petblog.model.Medicine;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class MedicineService extends BaseService {

    private final MedicineDAO medicineDAO;

    public MedicineService() {
        this.medicineDAO = new MedicineDAOImpl();
    }

    /**
     * 根据药品ID查询药品详情
     */
    public Medicine getMedicineById(Integer medicineId) {
        try {
            return medicineDAO.findById(medicineId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询药品详情");
            return null;
        }
    }

    /**
     * 根据药品名称模糊搜索
     */
    public List<Medicine> searchMedicinesByName(String nameKeyword, int pageNum, int pageSize) {
        try {
            return medicineDAO.searchByName(nameKeyword, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据名称搜索药品");
            return null;
        }
    }

    /**
     * 根据药品类别查询
     */
    public List<Medicine> getMedicinesByCategory(String category, int pageNum, int pageSize) {
        try {
            return medicineDAO.findByCategory(category, pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据类别查询药品");
            return null;
        }
    }

    /**
     * 查询所有药品（分页）
     */
    public List<Medicine> getAllMedicines(int pageNum, int pageSize) {
        try {
            return medicineDAO.findAll(pageNum, pageSize);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询所有药品");
            return null;
        }
    }

    /**
     * 统计药品总数
     */
    public int countAllMedicines() {
        try {
            return medicineDAO.countAll();
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计药品总数");
            return 0;
        }
    }

    /**
     * 统计指定类别的药品数量
     */
    public int countMedicinesByCategory(String category) {
        try {
            return medicineDAO.countByCategory(category);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "统计类别药品数量");
            return 0;
        }
    }

    /**
     * 新增药品信息
     */
    public Integer createMedicine(Medicine medicine) {
        try {
            return medicineDAO.insert(medicine);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "新增药品信息");
            return 0;
        }
    }

    /**
     * 更新药品信息
     */
    public boolean updateMedicine(Medicine medicine) {
        try {
            int result = medicineDAO.update(medicine);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新药品信息");
            return false;
        }
    }

    /**
     * 更新药品的状态（上架/下架）
     */
    public boolean updateMedicineStatus(Integer medicineId, Integer status) {
        try {
            int result = medicineDAO.updateStatus(medicineId, status);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新药品状态");
            return false;
        }
    }

    /**
     * 删除药品信息
     */
    public boolean deleteMedicine(Integer medicineId) {
        try {
            int result = medicineDAO.delete(medicineId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除药品信息");
            return false;
        }
    }

    /**
     * 检查药品名称是否已存在
     */
    public boolean isMedicineNameExists(String medicineName) {
        try {
            return medicineDAO.existsByName(medicineName);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "检查药品名称是否存在");
            return false;
        }
    }
}
