package com.petblog.model;

public class Auditor {
    private int auditorId;
    private String auditorName;
    private String auditorPassword;
    
    public Auditor() {
    }
    
    public Auditor(int auditorId, String auditorName, String auditorPassword) {
        this.auditorId = auditorId;
        this.auditorName = auditorName;
        this.auditorPassword = auditorPassword;
    }
    
    // Getters and setters
    public int getAuditorId() {
        return auditorId;
    }
    
    public void setAuditorId(int auditorId) {
        this.auditorId = auditorId;
    }
    
    // 为了兼容性，保留getId方法
    public int getId() {
        return auditorId;
    }
    
    public void setId(int id) {
        this.auditorId = id;
    }
    
    public String getAuditorName() {
        return auditorName;
    }
    
    public void setAuditorName(String auditorName) {
        this.auditorName = auditorName;
    }
    
    // 为了兼容性，保留getName方法
    public String getName() {
        return auditorName;
    }
    
    public void setName(String name) {
        this.auditorName = name;
    }
    
    public String getAuditorPassword() {
        return auditorPassword;
    }
    
    public void setAuditorPassword(String auditorPassword) {
        this.auditorPassword = auditorPassword;
    }
}