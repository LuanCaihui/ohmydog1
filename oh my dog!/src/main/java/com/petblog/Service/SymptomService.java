package com.petblog.Service;

import com.petblog.dao.SymptomDAO;
import com.petblog.dao.impl.SymptomDAOImpl;
import com.petblog.model.Symptom;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

/**
 * 症状服务类
 */
public class SymptomService extends BaseService {

    private final SymptomDAO symptomDAO;

    public SymptomService() {
        this.symptomDAO = new SymptomDAOImpl();
    }

    /**
     * 根据ID获取症状
     */
    public Symptom getSymptomById(Integer id) {
        try {
            return symptomDAO.findById(id);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询症状");
            return null;
        }
    }

    /**
     * 获取所有症状
     */
    public List<Symptom> getAllSymptoms() {
        try {
            return symptomDAO.findAll();
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询所有症状");
            return null;
        }
    }

    /**
     * 根据名称搜索症状
     */
    public List<Symptom> searchSymptomsByName(String name) {
        try {
            return symptomDAO.findByName(name);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据名称搜索症状");
            return null;
        }
    }

    /**
     * 创建症状
     */
    public Integer createSymptom(Symptom symptom) {
        try {
            return symptomDAO.insert(symptom);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "创建症状");
            return 0;
        }
    }

    /**
     * 更新症状
     */
    public boolean updateSymptom(Symptom symptom) {
        try {
            int result = symptomDAO.update(symptom);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新症状");
            return false;
        }
    }

    /**
     * 删除症状
     */
    public boolean deleteSymptom(Integer id) {
        try {
            int result = symptomDAO.delete(id);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除症状");
            return false;
        }
    }
}

