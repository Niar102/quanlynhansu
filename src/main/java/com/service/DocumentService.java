package com.service;

import com.model.Document;
import com.utils.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DocumentService {

    // Constants for validation
    private static final long MAX_FILE_SIZE = 700 * 1024; // 700KB
    private static final String[] ALLOWED_EXTENSIONS = {".pdf", ".txt", ".doc", ".docx", ".xls", ".xlsx"};
    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_CATEGORY_LENGTH = 100;

    // Validation methods
    private String validateDocument(Document document, boolean isUpdate) {
        if (document == null) {
            return "Document không được null";
        }

        // Validate title
        if (document.getTitle() == null || document.getTitle().trim().isEmpty()) {
            return "Tiêu đề không được trống";
        }
        if (document.getTitle().length() > MAX_TITLE_LENGTH) {
            return "Tiêu đề không được quá " + MAX_TITLE_LENGTH + " ký tự";
        }

        // Validate category
        if (document.getCategory() == null || document.getCategory().trim().isEmpty()) {
            return "Danh mục không được trống";
        }
        if (document.getCategory().length() > MAX_CATEGORY_LENGTH) {
            return "Danh mục không được quá " + MAX_CATEGORY_LENGTH + " ký tự";
        }

        // For new documents, file is required
        // For updates, file is optional (null means keep existing file)
        if (!isUpdate) {
            if (document.getFileData() == null || document.getFileData().length == 0) {
                return "File dữ liệu không được trống";
            }
            if (document.getFileName() == null || document.getFileName().trim().isEmpty()) {
                return "Tên file không được trống";
            }
        }

        // Validate file if provided
        if (document.getFileData() != null && document.getFileData().length > 0) {
            if (document.getFileData().length > MAX_FILE_SIZE) {
                return "File quá lớn. Kích thước tối đa: " + (MAX_FILE_SIZE / 1024) + "KB";
            }
            
            if (document.getFileName() != null && !isValidFileType(document.getFileName())) {
                return "Loại file không được hỗ trợ. Chỉ cho phép: PDF, TXT, DOC, DOCX, XLS, XLSX";
            }
        }

        return null; // No validation errors
    }

    private boolean isValidFileType(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        String lowerFileName = fileName.toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lowerFileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private String validateId(int id) {
        if (id <= 0) {
            return "ID phải là số dương";
        }
        return null;
    }

    public List<Document> getAllDocuments() {
        List<Document> documents = new ArrayList<>();

        String sql = "SELECT id, title, file_name, category, last_updated FROM documents ORDER BY last_updated DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Document document = new Document();
                document.setId(rs.getInt("id"));
                document.setTitle(rs.getString("title"));
                document.setFileName(rs.getString("file_name"));
                document.setCategory(rs.getString("category"));
                
                Timestamp timestamp = rs.getTimestamp("last_updated");
                if (timestamp != null) {
                    document.setLastUpdated(timestamp.toLocalDateTime());
                }

                documents.add(document);
            }

        } catch (SQLException e) {
            System.err.println("Error getting all documents: " + e.getMessage());
            e.printStackTrace();
        }

        return documents;
    }

    /**
     * Get total number of documents.
     */
    public int getTotalDocumentsCount() {
        String sql = "SELECT COUNT(*) FROM documents";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String addDocument(Document document) {
        // Validate document before adding
        String validationError = validateDocument(document, false);
        if (validationError != null) {
            return validationError;
        }

        String sql = "INSERT INTO documents (title, file_name, file_data, category) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, document.getTitle().trim());
            stmt.setString(2, document.getFileName());
            stmt.setBytes(3, document.getFileData());
            stmt.setString(4, document.getCategory().trim());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return "SUCCESS";
            } else {
                return "Không có dòng nào được thêm vào database.";
            }

        } catch (SQLException e) {
            System.err.println("Error adding document: " + e.getMessage());
            e.printStackTrace();
            
            if (e.getMessage().contains("Packet for query is too large")) {
                return "File quá lớn cho cấu hình database hiện tại.";
            } else if (e.getMessage().contains("doesn't exist")) {
                return "Bảng 'documents' không tồn tại trong database.";
            } else {
                return "Lỗi database: " + e.getMessage();
            }
        }
    }

    public String updateDocument(Document document) {
        // Validate document before updating
        String validationError = validateDocument(document, true);
        if (validationError != null) {
            return validationError;
        }

        // Validate ID
        String idValidationError = validateId(document.getId());
        if (idValidationError != null) {
            return idValidationError;
        }

        String sql;
        boolean hasNewFile = (document.getFileData() != null && document.getFileData().length > 0);
        
        if (hasNewFile) {
            // Có file mới - cập nhật tất cả
            sql = "UPDATE documents SET title = ?, file_name = ?, file_data = ?, category = ? WHERE id = ?";
        } else {
            // Không có file mới - chỉ cập nhật title và category
            sql = "UPDATE documents SET title = ?, category = ? WHERE id = ?";
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, document.getTitle().trim());
            
            if (hasNewFile) {
                stmt.setString(2, document.getFileName());
                stmt.setBytes(3, document.getFileData());
                stmt.setString(4, document.getCategory().trim());
                stmt.setInt(5, document.getId());
            } else {
                stmt.setString(2, document.getCategory().trim());
                stmt.setInt(3, document.getId());
            }

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return "SUCCESS";
            } else {
                return "Không tìm thấy tài liệu để cập nhật.";
            }

        } catch (SQLException e) {
            System.err.println("Error updating document: " + e.getMessage());
            e.printStackTrace();
            
            if (e.getMessage().contains("Packet for query is too large")) {
                return "File quá lớn cho cấu hình database hiện tại.";
            } else {
                return "Lỗi database: " + e.getMessage();
            }
        }
    }

    public boolean deleteDocument(int documentId) {
        // Validate ID
        String idValidationError = validateId(documentId);
        if (idValidationError != null) {
            System.err.println("Validation error: " + idValidationError);
            return false;
        }

        String sql = "DELETE FROM documents WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, documentId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting document: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Document getDocumentById(int id) {
        // Validate ID
        String idValidationError = validateId(id);
        if (idValidationError != null) {
            System.err.println("Validation error: " + idValidationError);
            return null;
        }

        String sql = "SELECT * FROM documents WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Document document = new Document();
                document.setId(rs.getInt("id"));
                document.setTitle(rs.getString("title"));
                document.setFileName(rs.getString("file_name"));
                document.setFileData(rs.getBytes("file_data"));
                document.setCategory(rs.getString("category"));
                
                Timestamp timestamp = rs.getTimestamp("last_updated");
                if (timestamp != null) {
                    document.setLastUpdated(timestamp.toLocalDateTime());
                }

                return document;
            }

        } catch (SQLException e) {
            System.err.println("Error getting document by id: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM documents WHERE category IS NOT NULL ORDER BY category";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting document categories: " + e.getMessage());
            e.printStackTrace();
        }

        return categories;
    }

    public List<Document> searchDocuments(String keyword, String category) {
        return searchDocuments(keyword, category, null, null);
    }
    
    public List<Document> searchDocuments(String keyword, String category, LocalDate fromDate, LocalDate toDate) {
        List<Document> documents = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, title, file_name, category, last_updated FROM documents WHERE 1=1");
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (title LIKE ? OR file_name LIKE ?)");
        }
        
        if (category != null && !category.trim().isEmpty() && !category.equals("Tất cả")) {
            sql.append(" AND category = ?");
        }
        
        if (fromDate != null) {
            sql.append(" AND DATE(last_updated) >= ?");
        }
        
        if (toDate != null) {
            sql.append(" AND DATE(last_updated) <= ?");
        }
        
        sql.append(" ORDER BY last_updated DESC");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchKeyword = "%" + keyword.trim() + "%";
                stmt.setString(paramIndex++, searchKeyword);
                stmt.setString(paramIndex++, searchKeyword);
            }
            
            if (category != null && !category.trim().isEmpty() && !category.equals("Tất cả")) {
                stmt.setString(paramIndex++, category);
            }
            
            if (fromDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(fromDate));
            }
            
            if (toDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(toDate));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Document document = new Document();
                document.setId(rs.getInt("id"));
                document.setTitle(rs.getString("title"));
                document.setFileName(rs.getString("file_name"));
                document.setCategory(rs.getString("category"));
                
                Timestamp timestamp = rs.getTimestamp("last_updated");
                if (timestamp != null) {
                    document.setLastUpdated(timestamp.toLocalDateTime());
                }

                documents.add(document);
            }

        } catch (SQLException e) {
            System.err.println("Error searching documents: " + e.getMessage());
            e.printStackTrace();
        }

        return documents;
    }
    
    public byte[] getDocumentFile(int id) {
        // Validate ID
        String idValidationError = validateId(id);
        if (idValidationError != null) {
            System.err.println("Validation error: " + idValidationError);
            return null;
        }

        String sql = "SELECT file_data FROM documents WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBytes("file_data");
            }

        } catch (SQLException e) {
            System.err.println("Error getting document file: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
    

}