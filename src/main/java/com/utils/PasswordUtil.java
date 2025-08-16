package com.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {
    
    /**
     * Mã hóa mật khẩu bằng SHA1
     * @param password mật khẩu gốc
     * @return mật khẩu đã được mã hóa SHA1
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(password.getBytes());
            
            // Chuyển đổi byte array thành hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể mã hóa mật khẩu", e);
        }
    }
    
    /**
     * Kiểm tra mật khẩu có khớp với hash đã lưu không
     * @param rawPassword mật khẩu gốc
     * @param hashedPassword mật khẩu đã mã hóa từ database
     * @return true nếu khớp, false nếu không
     */
    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        
        String hashedInput = hashPassword(rawPassword);
        return hashedInput.equals(hashedPassword);
    }
}
