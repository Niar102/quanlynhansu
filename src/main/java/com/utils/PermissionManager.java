package com.utils;

import com.model.User;
import com.service.UserService;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.Node;

public class PermissionManager {
    private static PermissionManager instance;
    private UserService userService = new UserService();

    private PermissionManager() {}

    public static PermissionManager getInstance() {
        if (instance == null) {
            instance = new PermissionManager();
        }
        return instance;
    }

    /**
     * Ẩn/hiện menu item dựa trên quyền
     */
    public void setMenuItemVisibility(MenuItem menuItem, String permissionCode) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            boolean hasPermission = userService.hasPermission(currentUser.getId(), permissionCode);
            menuItem.setVisible(hasPermission);
        } else {
            menuItem.setVisible(false);
        }
    }

    /**
     * Ẩn/hiện button dựa trên quyền
     */
    public void setButtonVisibility(Button button, String permissionCode) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            boolean hasPermission = userService.hasPermission(currentUser.getId(), permissionCode);
            button.setVisible(hasPermission);
        } else {
            button.setVisible(false);
        }
    }

    /**
     * Ẩn/hiện node dựa trên quyền
     */
    public void setNodeVisibility(Node node, String permissionCode) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            boolean hasPermission = userService.hasPermission(currentUser.getId(), permissionCode);
            node.setVisible(hasPermission);
        } else {
            node.setVisible(false);
        }
    }

    /**
     * Kiểm tra quyền cho Admin
     */
    public boolean isAdminOnly() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return userService.isAdmin(currentUser);
    }

    /**
     * Kiểm tra quyền cho HR
     */
    public boolean isHROnly() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return userService.isHR(currentUser);
    }

    /**
     * Kiểm tra quyền cho Department Manager
     */
    public boolean isDeptManagerOnly() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return userService.isDeptManager(currentUser);
    }

    /**
     * Kiểm tra quyền cho Admin hoặc HR
     */
    public boolean isAdminOrHR() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return userService.isAdmin(currentUser) || userService.isHR(currentUser);
    }

    /**
     * Kiểm tra quyền cho Admin hoặc Department Manager
     */
    public boolean isAdminOrDeptManager() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return userService.isAdmin(currentUser) || userService.isDeptManager(currentUser);
    }

    /**
     * Kiểm tra quyền cụ thể
     */
    public boolean hasPermission(String permissionCode) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            return userService.hasPermission(currentUser.getId(), permissionCode);
        }
        return false;
    }
}