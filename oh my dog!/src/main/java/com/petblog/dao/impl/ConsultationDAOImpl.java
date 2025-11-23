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
        // 查询所有字段，包括result_disease_id和probability（如果存在）
        String sql = "SELECT id, user_id, selected_symptoms, " +
                     "COALESCE(result_disease_id, NULL) as result_disease_id, " +
                     "COALESCE(probability, NULL) as probability, " +
                     "created_at " +
                     "FROM consultations WHERE user_id = ? ORDER BY created_at DESC";
        try {
            return queryForList(sql, this::mapRowToConsultation, userId);
        } catch (SQLException e) {
            // 如果字段不存在，回退到基本查询
            try {
                String fallbackSql = "SELECT id, user_id, selected_symptoms, diagnosis, created_at " +
                                   "FROM consultations WHERE user_id = ? ORDER BY created_at DESC";
                return queryForList(fallbackSql, this::mapRowToConsultationWithDiagnosis, userId);
            } catch (SQLException e2) {
                return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "根据用户ID查询问诊记录", null);
            }
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
        // 尝试使用result_disease_id和probability字段
        String sql = "INSERT INTO consultations (user_id, selected_symptoms, result_disease_id, probability, created_at) VALUES (?, ?, ?, ?, ?)";
        try {
            return insert(sql, consultation.getUserId(), consultation.getSelectedSymptoms(),
                         consultation.getResultDiseaseId(),
                         consultation.getProbability(),
                         consultation.getCreatedAt() != null ? consultation.getCreatedAt() : LocalDateTime.now());
        } catch (SQLException e) {
            // 如果字段不存在，回退到使用diagnosis文本字段
            try {
                String fallbackSql = "INSERT INTO consultations (user_id, selected_symptoms, diagnosis, created_at) VALUES (?, ?, ?, ?)";
                String diagnosisText = null;
                if (consultation.getResultDiseaseId() != null) {
                    diagnosisText = "疾病ID: " + consultation.getResultDiseaseId();
                    if (consultation.getProbability() != null) {
                        diagnosisText += ", 置信度: " + String.format("%.2f%%", consultation.getProbability() * 100);
                    }
                }
                return insert(fallbackSql, consultation.getUserId(), consultation.getSelectedSymptoms(),
                             diagnosisText,
                             consultation.getCreatedAt() != null ? consultation.getCreatedAt() : LocalDateTime.now());
            } catch (SQLException e2) {
                return SQLExceptionHandler.handleSQLExceptionWithDefault(e, "新增问诊记录", 0);
            }
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
        try {
            Object diseaseIdObj = rs.getObject("result_disease_id");
            if (diseaseIdObj != null) {
                consultation.setResultDiseaseId(rs.getInt("result_disease_id"));
            } else {
                consultation.setResultDiseaseId(null);
            }
        } catch (SQLException e) {
            // 字段不存在，设置为null
            consultation.setResultDiseaseId(null);
        }
        
        // 处理probability可能为null的情况
        try {
            Object probObj = rs.getObject("probability");
            if (probObj != null) {
                consultation.setProbability(rs.getFloat("probability"));
            } else {
                consultation.setProbability(null);
            }
        } catch (SQLException e) {
            // 字段不存在，设置为null
            consultation.setProbability(null);
        }
        
        consultation.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return consultation;
    }
    
    /**
     * 从diagnosis文本字段解析数据（用于兼容旧表结构）
     */
    private Consultation mapRowToConsultationWithDiagnosis(ResultSet rs) throws SQLException {
        Consultation consultation = new Consultation();
        consultation.setId(rs.getInt("id"));
        consultation.setUserId(rs.getInt("user_id"));
        consultation.setSelectedSymptoms(rs.getString("selected_symptoms"));
        
        // 尝试从diagnosis字段解析疾病ID和概率
        try {
            String diagnosis = rs.getString("diagnosis");
            if (diagnosis != null && !diagnosis.isEmpty()) {
                // 解析格式："疾病ID: X, 置信度: Y%"
                if (diagnosis.contains("疾病ID:")) {
                    try {
                        String diseaseIdStr = diagnosis.substring(diagnosis.indexOf("疾病ID:") + 5).trim();
                        if (diseaseIdStr.contains(",")) {
                            diseaseIdStr = diseaseIdStr.substring(0, diseaseIdStr.indexOf(",")).trim();
                        }
                        consultation.setResultDiseaseId(Integer.parseInt(diseaseIdStr));
                    } catch (Exception e) {
                        consultation.setResultDiseaseId(null);
                    }
                }
                
                if (diagnosis.contains("置信度:")) {
                    try {
                        String probStr = diagnosis.substring(diagnosis.indexOf("置信度:") + 4).trim();
                        probStr = probStr.replace("%", "").trim();
                        float probPercent = Float.parseFloat(probStr);
                        consultation.setProbability(probPercent / 100.0f); // 转换为0-1之间的小数
                    } catch (Exception e) {
                        consultation.setProbability(null);
                    }
                }
            } else {
                consultation.setResultDiseaseId(null);
                consultation.setProbability(null);
            }
        } catch (SQLException e) {
            consultation.setResultDiseaseId(null);
            consultation.setProbability(null);
        }
        
        consultation.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return consultation;
    }
}

