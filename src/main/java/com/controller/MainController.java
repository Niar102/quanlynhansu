package com.controller;

import com.utils.PermissionTooltipManager;
import com.utils.PermissionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private VBox sidebar;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button employeeBtn;

    @FXML
    private Button organizationBtn;

    @FXML
    private Button contractBtn;

    @FXML
    private Button reportBtn;

    @FXML
    private Button settingsBtn;



    @FXML
    public void initialize() {
        // Load CSS
        contentArea.getStylesheets().add(getClass().getResource("/com/main/sidebar.css").toExternalForm());
        sidebar.getStylesheets().add(getClass().getResource("/com/main/sidebar.css").toExternalForm());
        sidebar.getStylesheets().add(getClass().getResource("/com/main/button.css").toExternalForm());

        // Thiết lập tooltip phân quyền cho các button
        setupPermissionTooltips();

        // Load dashboard by default
        loadPage("dashboard");
    }

    @FXML
    public void handleMenu(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        // Kiểm tra quyền truy cập theo button thay vì text (tránh lệ thuộc ngôn ngữ/label)
        if (!checkPermissionForButton(clickedButton)) {
            showPermissionDeniedAlert(getDisplayNameForButton(clickedButton));
            return;
        }

        // Remove active class from all buttons
        removeActiveClass();

        // Add active class to clicked button
        clickedButton.getStyleClass().add("active");

        // Điều hướng theo fx:id (so sánh tham chiếu button)
        if (clickedButton == dashboardBtn) {
            loadPage("dashboard");
        } else if (clickedButton == employeeBtn) {
            loadPage("employees");
        } else if (clickedButton == organizationBtn) {
            loadPage("organization");
        } else if (clickedButton == contractBtn) {
            loadPage("contracts");
        } else if (clickedButton == reportBtn) {
            loadPage("documents");
        } else if (clickedButton == settingsBtn) {
            loadPage("settings");
        } else {
            loadPage("dashboard");
        }
    }

    private void removeActiveClass() {
        dashboardBtn.getStyleClass().remove("active");
        employeeBtn.getStyleClass().remove("active");
        organizationBtn.getStyleClass().remove("active");
        contractBtn.getStyleClass().remove("active");
        reportBtn.getStyleClass().remove("active");
        settingsBtn.getStyleClass().remove("active");

    }

    private void loadPage(String page) {
        try {
            String fxmlPath = "/com/main/" + page + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node pageContent = loader.load();

            // If the loaded page is the dashboard, inject this controller
            Object childController = loader.getController();
            if (childController instanceof DashboardController) {
                ((DashboardController) childController).setMainController(this);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(pageContent);

        } catch (IOException e) {
            System.err.println("Could not load page: " + page);
            e.printStackTrace();

            // Fallback: show simple label
            contentArea.getChildren().clear();
            javafx.scene.control.Label errorLabel = new javafx.scene.control.Label("Trang " + page + " đang được phát triển");
            errorLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #7f8c8d; -fx-padding: 50;");
            contentArea.getChildren().add(errorLabel);
        }
    }

    /**
     * Allow other controllers (e.g., Dashboard) to navigate like sidebar buttons do.
     */
    public void navigateToPage(String page) {
        Button targetButton;
        switch (page) {
            case "employees":
                targetButton = employeeBtn;
                break;
            case "organization":
                targetButton = organizationBtn;
                break;
            case "contracts":
                targetButton = contractBtn;
                break;
            case "documents":
                targetButton = reportBtn;
                break;
            case "settings":
                targetButton = settingsBtn;
                break;
            case "dashboard":
            default:
                targetButton = dashboardBtn;
                break;
        }

        if (!checkPermissionForButton(targetButton)) {
            showPermissionDeniedAlert(getDisplayNameForButton(targetButton));
            return;
        }

        removeActiveClass();
        targetButton.getStyleClass().add("active");
        loadPage(page);
    }

    /**
     * Thiết lập tooltip phân quyền cho tất cả các button
     */
    private void setupPermissionTooltips() {
        PermissionTooltipManager tooltipManager = PermissionTooltipManager.getInstance();
        tooltipManager.setupAllPermissionTooltips(dashboardBtn, employeeBtn,
                organizationBtn, contractBtn,
                reportBtn, settingsBtn);
    }

    /**
     * Kiểm tra quyền truy cập cho từng trang
     */
    private boolean checkPermissionForButton(Button button) {
        PermissionManager permissionManager = PermissionManager.getInstance();

        if (button == dashboardBtn) {
            return true;
        }
        if (button == employeeBtn) {
            return permissionManager.hasPermission("VIEW_EMPLOYEE");
        }
        if (button == organizationBtn) {
            return permissionManager.hasPermission("VIEW_DEPARTMENT");
        }
        if (button == contractBtn) {
            return permissionManager.hasPermission("VIEW_CONTRACT");
        }
        if (button == reportBtn) {
            return permissionManager.hasPermission("VIEW_DOCUMENT");
        }
        if (button == settingsBtn) {
            return permissionManager.hasPermission("MANAGE_ROLE");
        }
        return true;
    }

    private String getDisplayNameForButton(Button button) {
        if (button == dashboardBtn) return "Dashboard";
        if (button == employeeBtn) return "Nhân viên";
        if (button == organizationBtn) return "Tổ chức";
        if (button == contractBtn) return "Hợp đồng";
        if (button == reportBtn) return "Tài Liệu";
        if (button == settingsBtn) return "Quản Lý";
        return "Trang";
    }

    /**
     * Hiển thị alert thông báo không đủ quyền
     */
    private void showPermissionDeniedAlert(String pageName) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Không đủ quyền truy cập");
        alert.setHeaderText("Truy cập bị từ chối");
        alert.setContentText("Bạn không có quyền truy cập vào trang " + pageName + ".\n" +
                "Vui lòng liên hệ quản trị viên để được cấp quyền.");

        alert.showAndWait();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Xoá thông tin đăng nhập (nếu có dùng session lưu user

        // Đóng cửa sổ hiện tại
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();

        // Mở lại giao diện đăng nhập
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/main/login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Đăng nhập");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
