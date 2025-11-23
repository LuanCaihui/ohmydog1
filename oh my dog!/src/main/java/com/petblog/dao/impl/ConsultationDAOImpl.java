package com.petblog.dao.impl;

import com.petblog.dao.BaseJdbcDAO;
import com.petblog.dao.ConsultationDAO;
import com.petblog.model.Consultation;
import com.petblog.util.SQLExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问诊记录数据访问实现类
 */
public class ConsultationDAOImpl extends BaseJdbcDAO<Consultation> implements ConsultationDAO {

    @Override
    public Consultation findById(Integer id) throws SQLException {
        // 只查询存在的字段：id, user_id, selected_symptoms, created_at
        String sql = "SELECT id, user_id, selected_symptoms, created_at FROM consultations WHERE id = ?";
        try {
            return queryForObject(sql, this::mapRowToConsultationBasic, id);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据ID查询问诊记录", null);
        }
    }

    @Override
    public List<Consultation> findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT id, user_id, selected_symptoms, created_at " +
                     "FROM consultations WHERE user_id = ? ORDER BY created_at DESC";
        try {
            return queryForList(sql, this::mapRowToConsultationBasic, userId);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据用户ID查询问诊记录", null);
        }
    }

    @Override
    public List<Consultation> findByUserId(Integer userId, int pageNum, int pageSize) throws SQLException {
        String sql = "SELECT id, user_id, selected_symptoms, created_at " +
                     "FROM consultations WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToConsultationBasic, userId, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "分页查询用户问诊记录", null);
        }
    }

    @Override
    public List<Consultation> findAll(int pageNum, int pageSize) throws SQLException {
        // 只查询存在的字段：id, user_id, selected_symptoms, created_at
        String sql = "SELECT id, user_id, selected_symptoms, created_at " +
                     "FROM consultations ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try {
            return queryForList(sql, this::mapRowToConsultationBasic, pageSize, (pageNum - 1) * pageSize);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "分页查询所有问诊记录", null);
        }
    }
    
    /**
     * 映射结果集到Consultation对象（只包含基本字段）
     */
    private Consultation mapRowToConsultationBasic(ResultSet rs) throws SQLException {
        Consultation consultation = new Consultation();
        consultation.setId(rs.getInt("id"));
        consultation.setUserId(rs.getInt("user_id"));
        consultation.setSelectedSymptoms(rs.getString("selected_symptoms"));
        consultation.setResultDiseaseId(null); // 字段不存在，设置为null
        consultation.setProbability(null); // 字段不存在，设置为null
        consultation.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return consultation;
    }

    @Override
    public int countByUserId(Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM consultations WHERE user_id = ?";
        try {
            Number count = (Number) queryForSingleValue(sql, userId);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计用户问诊记录数", 0);
        }
    }

    @Override
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM consultations";
        try {
            Number count = (Number) queryForSingleValue(sql);
            return count != null ? count.intValue() : 0;
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "统计所有问诊记录数", 0);
        }
    }

    @Override
    public int insert(Consultation consultation) throws SQLException {
        // 根据数据库表结构，只插入存在的字段：user_id, selected_symptoms, diagnosis, created_at
        String sql = "INSERT INTO consultations (user_id, selected_symptoms, diagnosis, created_at) VALUES (?, ?, ?, ?)";
        try {
            // 构建诊断结果文本（包含疾病ID和概率信息）
            String diagnosisText = null;
            if (consultation.getResultDiseaseId() != null) {
                diagnosisText = "疾病ID: " + consultation.getResultDiseaseId();
                if (consultation.getProbability() != null) {
                    diagnosisText += ", 置信度: " + String.format("%.2f%%", consultation.getProbability() * 100);
                }
            }
            
            return insert(sql, consultation.getUserId(), consultation.getSelectedSymptoms(),
                         diagnosisText,
                         consultation.getCreatedAt() != null ? consultation.getCreatedAt() : LocalDateTime.now());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "新增问诊记录", 0);
        }
    }

    @Override
    public int update(Consultation consultation) throws SQLException {
        // 根据数据库表结构，只更新存在的字段
        String sql = "UPDATE consultations SET user_id = ?, selected_symptoms = ?, diagnosis = ?, created_at = ? WHERE id = ?";
        try {
            // 构建诊断结果文本
            String diagnosisText = null;
            if (consultation.getResultDiseaseId() != null) {
                diagnosisText = "疾病ID: " + consultation.getResultDiseaseId();
                if (consultation.getProbability() != null) {
                    diagnosisText += ", 置信度: " + String.format("%.2f%%", consultation.getProbability() * 100);
                }
            }
            
            return update(sql, consultation.getUserId(), consultation.getSelectedSymptoms(),
                         diagnosisText,
                         consultation.getCreatedAt(), consultation.getId());
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "更新问诊记录", 0);
        }
    }

    @Override
    public int delete(Integer id) throws SQLException {
        String sql = "DELETE FROM consultations WHERE id = ?";
        try {
            return delete(sql, id);
        } catch (SQLException e) {
            return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "删除问诊记录", 0);
        }
    }

    private Consultation mapRowToConsultation(ResultSet rs) throws SQLException {
        Consultation consultation = new Consultation();
        consultation.setId(rs.getInt("id"));
        consultation.setUserId(rs.getInt("user_id"));
        consultation.setSelectedSymptoms(rs.getString("selected_symptoms"));
        
        // 处理result_disease_id可能为null的情况
        Object diseaseIdObj = rs.getObject("result_disease_id");
        if (diseaseIdObj != null) {
            consultation.setResultDiseaseId(rs.getInt("result_disease_id"));
        }
        
        // 处理probability可能为null的情况
        Object probObj = rs.getObject("probability");
        if (probObj != null) {
            consultation.setProbability(rs.getFloat("probability"));
        }
        
        consultation.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return consultation;
    }
}

