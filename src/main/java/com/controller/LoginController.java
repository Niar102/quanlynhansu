package com.controller;

import com.model.User;
import com.service.UserService;
import com.utils.SessionManager;
import com.utils.PasswordUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private UserService userService = new UserService();

    @FXML
    private void initialize() {
        // Bấm Enter trong usernameField → gọi đăng nhập
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin(null);
            }
        });

        // Bấm Enter trong passwordField → gọi đăng nhập
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin(null);
            }
        });
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Kiểm tra input
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try {
            // Mã hóa mật khẩu bằng SHA1 trước khi xác thực
            String hashedPassword = PasswordUtil.hashPassword(password);
            
            // Xác thực người dùng từ database
            User user = userService.authenticateUser(username, hashedPassword);

            if (user != null) {
                // Lưu thông tin người dùng vào session
                SessionManager.getInstance().setCurrentUser(user);

                // Hiển thị thông tin đăng nhập thành công
                showLoginSuccess(user);

                // Chuyển đến màn hình chính
                navigateToMainScreen();
            } else {
                errorLabel.setText("Sai tên đăng nhập hoặc mật khẩu!");
                passwordField.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Lỗi kết nối database!");
        }
    }

    private void showLoginSuccess(User user) {
        String roleName = user.getRoleName() != null ? user.getRoleName() : "Không xác định";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Đăng nhập thành công");
        alert.setHeaderText("Chào mừng " + user.getUsername());
        alert.setContentText("Vai trò: " + roleName);
        alert.showAndWait();
    }

    private void navigateToMainScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/main/main-layout.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Quản lý nhân sự - " + SessionManager.getInstance().getCurrentUser().getUsername());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Lỗi khi chuyển màn hình!");
        }
    }
}
