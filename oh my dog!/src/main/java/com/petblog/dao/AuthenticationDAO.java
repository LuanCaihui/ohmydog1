package com.petblog.dao;

import com.petblog.model.Authentication;

import java.sql.SQLException;
import java.util.List;

public interface AuthenticationDAO {
    
    void insert(Authentication auth) throws SQLException;
    void update(Authentication auth) throws SQLException;
    void delete(int id) throws SQLException;
    Authentication findById(int id) throws SQLException;
    Authentication findByUserId(int userId) throws SQLException;
    List<Authentication> findAll() throws SQLException;
    Authentication selectById(Integer authId) throws SQLException;
}