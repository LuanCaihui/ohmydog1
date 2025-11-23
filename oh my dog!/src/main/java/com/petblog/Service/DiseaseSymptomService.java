package com.petblog.Service;

import com.petblog.dao.DiseaseSymptomDAO;
import com.petblog.dao.impl.DiseaseSymptomDAOImpl;
import com.petblog.model.DiseaseSymptom;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

/**
 * 疾病-症状关系服务类
 */
public class DiseaseSymptomService extends BaseService {

    private final DiseaseSymptomDAO diseaseSymptomDAO;

    public DiseaseSymptomService() {
        this.diseaseSymptomDAO = new DiseaseSymptomDAOImpl();
    }

    /**
     * 根据ID获取关系
     */
    public DiseaseSymptom getDiseaseSymptomById(Integer id) {
        try {
            return diseaseSymptomDAO.findById(id);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据ID查询疾病-症状关系");
            return null;
        }
    }

    /**
     * 根据疾病ID获取所有相关症状关系
     */
    public List<DiseaseSymptom> getRelationsByDiseaseId(Integer diseaseId) {
        try {
            return diseaseSymptomDAO.findByDiseaseId(diseaseId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据疾病ID查询关系");
            return null;
        }
    }

    /**
     * 根据症状ID获取所有相关疾病关系
     */
    public List<DiseaseSymptom> getRelationsBySymptomId(Integer symptomId) {
        try {
            return diseaseSymptomDAO.findBySymptomId(symptomId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据症状ID查询关系");
            return null;
        }
    }

    /**
     * 根据疾病ID和症状ID获取关系
     */
    public DiseaseSymptom getRelationByDiseaseAndSymptom(Integer diseaseId, Integer symptomId) {
        try {
            return diseaseSymptomDAO.findByDiseaseAndSymptom(diseaseId, symptomId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据疾病和症状ID查询关系");
            return null;
        }
    }

    /**
     * 获取所有关系
     */
    public List<DiseaseSymptom> getAllRelations() {
        try {
            return diseaseSymptomDAO.findAll();
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "查询所有疾病-症状关系");
            return null;
        }
    }

    /**
     * 创建关系
     */
    public Integer createRelation(DiseaseSymptom diseaseSymptom) {
        try {
            return diseaseSymptomDAO.insert(diseaseSymptom);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "创建疾病-症状关系");
            return 0;
        }
    }

    /**
     * 更新关系
     */
    public boolean updateRelation(DiseaseSymptom diseaseSymptom) {
        try {
            int result = diseaseSymptomDAO.update(diseaseSymptom);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "更新疾病-症状关系");
            return false;
        }
    }

    /**
     * 删除关系
     */
    public boolean deleteRelation(Integer id) {
        try {
            int result = diseaseSymptomDAO.delete(id);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "删除疾病-症状关系");
            return false;
        }
    }

    /**
     * 根据疾病ID和症状ID删除关系
     */
    public boolean deleteRelationByDiseaseAndSymptom(Integer diseaseId, Integer symptomId) {
        try {
            int result = diseaseSymptomDAO.deleteByDiseaseAndSymptom(diseaseId, symptomId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据疾病和症状ID删除关系");
            return false;
        }
    }
}

