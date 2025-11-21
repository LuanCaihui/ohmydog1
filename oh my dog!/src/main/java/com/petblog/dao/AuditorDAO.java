package com.petblog.dao;

import java.sql.SQLException;
import java.util.List;
import com.petblog.model.Auditor;

public interface AuditorDAO {
    
    void insert(Auditor auditor) throws SQLException;
    void update(Auditor auditor) throws SQLException;
    void delete(int id) throws SQLException;
    Auditor findById(int id) throws SQLException;
    Auditor findByName(String name) throws SQLException;
    List<Auditor> findAll() throws SQLException;
    List<Auditor> findByStatus(String status) throws SQLException;
}