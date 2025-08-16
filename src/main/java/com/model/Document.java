package com.model;

import java.time.LocalDateTime;

public class Document {
    private int id;
    private String title;
    private String fileName;
    private byte[] fileData;
    private String category;
    private LocalDateTime lastUpdated;

    // Constructor không tham số
    public Document() {
    }

    // Constructor đầy đủ
    public Document(int id, String title, String fileName, byte[] fileData, String category, LocalDateTime lastUpdated) {
        this.id = id;
        this.title = title;
        this.fileName = fileName;
        this.fileData = fileData;
        this.category = category;
        this.lastUpdated = lastUpdated;
    }

    // Getters và Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileData=" + (fileData != null ? fileData.length + " bytes" : "null") +
                ", category='" + category + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}