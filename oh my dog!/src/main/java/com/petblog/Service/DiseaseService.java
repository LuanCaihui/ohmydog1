package com.petblog.Service;

import com.petblog.dao.DiseaseDAO;
import com.petblog.dao.impl.DiseaseDAOImpl;
import com.petblog.model.Disease;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

/**
 * 疾病服务类
 */
public class DiseaseService extends BaseService {

    private final DiseaseDAO diseaseDAO;

    public DiseaseService() {
        this.diseaseDAO = new DiseaseDAOImpl();
    }

    /**
     * 根据ID获取疾病
     */
    public Disease getDiseaseById(Integer id) {
        try {
            return diseaseDAO.findById(id);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询疾病");
            return null;
        }
    }

    /**
     * 获取所有疾病
     */
    public List<Disease> getAllDiseases() {
        try {
            return diseaseDAO.findAll();
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询所有疾病");
            return null;
        }
    }

    /**
     * 根据名称搜索疾病
     */
    public List<Disease> searchDiseasesByName(String name) {
        try {
            return diseaseDAO.findByName(name);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据名称搜索疾病");
            return null;
        }
    }

    /**
     * 根据症状ID获取相关疾病
     */
    public List<Disease> getDiseasesBySymptomId(Integer symptomId) {
        try {
            return diseaseDAO.findBySymptomId(symptomId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据症状ID查询疾病");
            return null;
        }
    }

    /**
     * 根据多个症状ID获取相关疾病（用于诊断）
     */
    public List<Disease> getDiseasesBySymptomIds(List<Integer> symptomIds) {
        try {
            return diseaseDAO.findBySymptomIds(symptomIds);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据症状ID列表查询疾病");
            return null;
        }
    }

    /**
     * 创建疾病
     */
    public Integer createDisease(Disease disease) {
        try {
            return diseaseDAO.insert(disease);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "创建疾病");
            return 0;
        }
    }

    /**
     * 更新疾病
     */
    public boolean updateDisease(Disease disease) {
        try {
            int result = diseaseDAO.update(disease);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新疾病");
            return false;
        }
    }

    /**
     * 删除疾病
     */
    public boolean deleteDisease(Integer id) {
        try {
            int result = diseaseDAO.delete(id);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除疾病");
            return false;
        }
    }
}

