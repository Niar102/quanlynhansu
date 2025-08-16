package com.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class PermissionTooltipManager {
    private static PermissionTooltipManager instance;
    private Popup currentPopup;

    private PermissionTooltipManager() {
        // Private constructor for singleton
    }

    public static PermissionTooltipManager getInstance() {
        if (instance == null) {
            instance = new PermissionTooltipManager();
        }
        return instance;
    }

    public void setupPermissionTooltip(Button button, String permissionCode, String pageName) {
        if (PermissionManager.getInstance().hasPermission(permissionCode)) {
            // User has permission - show normal tooltip
            Tooltip tooltip = new Tooltip("Truy cập " + pageName);
            button.setTooltip(tooltip);
        } else {
            // User doesn't have permission - add hover effects and disabled styling
            button.getStyleClass().add("disabled-button");

            button.setOnMouseEntered(event -> showPermissionDeniedPopup(event, pageName));
            button.setOnMouseExited(event -> hidePermissionDeniedPopup());
        }
    }

    private void showPermissionDeniedPopup(MouseEvent event, String pageName) {
        // Hide any existing popup
        hidePermissionDeniedPopup();

        // Create popup content
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: #ffebee; -fx-border-color: #f44336; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Warning icon
        Label warningIcon = new Label("⚠");
        warningIcon.setFont(Font.font("System", FontWeight.BOLD, 24));
        warningIcon.setTextFill(Color.web("#f44336"));

        // Title
        Label titleLabel = new Label("Không đủ quyền truy cập");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web("#d32f2f"));

        // Content
        Label contentLabel = new Label("Bạn không có quyền truy cập vào trang " + pageName + ".\nVui lòng liên hệ quản trị viên để được cấp quyền.");
        contentLabel.setFont(Font.font("System", 12));
        contentLabel.setTextFill(Color.web("#c62828"));
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(200);

        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(10);
        shadow.setSpread(0.1);
        content.setEffect(shadow);

        content.getChildren().addAll(warningIcon, titleLabel, contentLabel);

        // Create and show popup
        currentPopup = new Popup();
        currentPopup.getContent().add(content);
        currentPopup.setAutoHide(true);
        currentPopup.setAutoFix(true);

        // Position popup near mouse
        currentPopup.show(((Button) event.getSource()).getScene().getWindow(), event.getScreenX() + 10, event.getScreenY() - 10);

        // Auto-hide after 10 seconds
        new Thread(() -> {
            try {
                Thread.sleep(1000000000);
                if (currentPopup != null && currentPopup.isShowing()) {
                    javafx.application.Platform.runLater(() -> currentPopup.hide());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void hidePermissionDeniedPopup() {
        if (currentPopup != null && currentPopup.isShowing()) {
            currentPopup.hide();
            currentPopup = null;
        }
    }

    public void setupAllPermissionTooltips(Button dashboardBtn, Button employeeBtn,
                                           Button organizationBtn, Button contractBtn,
                                           Button reportBtn, Button settingsBtn) {
        // Dashboard - everyone can access
        Tooltip dashboardTooltip = new Tooltip("Truy cập Dashboard");
        dashboardBtn.setTooltip(dashboardTooltip);

        // Employee management
        setupPermissionTooltip(employeeBtn, "VIEW_EMPLOYEE", "Quản lý nhân viên");

        // Organization management
        setupPermissionTooltip(organizationBtn, "VIEW_DEPARTMENT", "Quản lý tổ chức");

        // Contract management
        setupPermissionTooltip(contractBtn, "VIEW_CONTRACT", "Quản lý hợp đồng");

        // Documents - deptmanager and admin can access
        setupPermissionTooltip(reportBtn, "VIEW_DOCUMENT", "Tài liệu");

        // Settings - only admin
        setupPermissionTooltip(settingsBtn, "MANAGE_ROLE", "Cài đặt hệ thống");
    }
}