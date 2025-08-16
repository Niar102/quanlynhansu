package com.main;

// TestConnection.java
import java.sql.Connection;
import com.utils.DBUtil;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Kết nối database thành công!");
            } else {
                System.out.println("❌ Kết nối thất bại.");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi kết nối:");
            e.printStackTrace();
        }
    }
}