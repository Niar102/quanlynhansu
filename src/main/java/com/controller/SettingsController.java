package com.controller;

import com.model.User;
import com.model.Role;
import com.service.UserService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.util.Callback;

import java.net.URL;
import java.util.*;

public class SettingsController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRoleName;
    @FXML private TableColumn<User, String> colRoleCode;
    @FXML private TableColumn<User, Void> actionColumn;

    @FXML private Label lblTotal;
    @FXML private Label lblAdmin;
    @FXML private Label lblHR;

    @FXML private Button btnAddUser;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadRoleFilter();
        loadUsers();
        loadUserStats();

        roleFilter.setOnAction(e -> applyFilters());
        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters());

        actionColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Sửa");
                    private final Button btnDelete = new Button("Xoá");
                    private final HBox box = new HBox(btnEdit, btnDelete);

                    {
                        btnEdit.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");
                        btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;");
                        box.setSpacing(10);
                        box.setAlignment(Pos.CENTER);

                        btnEdit.setOnAction(e -> handleEdit(getCurrentRow()));
                        btnDelete.setOnAction(e -> handleDelete(getCurrentRow()));
                    }

                    private User getCurrentRow() {
                        return getTableView().getItems().get(getIndex());
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : box);
                    }
                };
            }
        });
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()).asObject());
        colUsername.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUsername()));
        colRoleName.setCellValueFactory(cell -> new SimpleStringProperty(Optional.ofNullable(cell.getValue().getRoleName()).orElse("")));
        colRoleCode.setCellValueFactory(cell -> new SimpleStringProperty(Optional.ofNullable(cell.getValue().getRoleCode()).orElse("")));
    }

    private void loadRoleFilter() {
        roleFilter.getItems().clear();
        roleFilter.getItems().add("Tất cả");
        for (Role r : userService.getRolesForUser()) {
            if (r.getRoleCode() != null) roleFilter.getItems().add(r.getRoleCode());
        }
        roleFilter.getSelectionModel().selectFirst();
    }

    private void loadUsers() {
        List<User> users = userService.getAllUsersWithRoles();
        userTable.setItems(FXCollections.observableArrayList(users));
    }

    private void loadUserStats() {
        Map<String, Integer> stats = userService.getUserStatsByRole();
        int total = stats.values().stream().mapToInt(Integer::intValue).sum();
        lblTotal.setText(String.valueOf(total));
        lblAdmin.setText(String.valueOf(stats.getOrDefault("ADMIN", 0)));
        lblHR.setText(String.valueOf(stats.getOrDefault("HR", 0)));
    }

    private void applyFilters() {
        String keyword = Optional.ofNullable(searchField.getText()).orElse("").trim().toLowerCase();
        String role = roleFilter.getValue();

        List<User> users = userService.getAllUsersWithRoles();
        List<User> filtered = new ArrayList<>();
        for (User u : users) {
            boolean matchKeyword = keyword.isEmpty()
                    || Optional.ofNullable(u.getUsername()).orElse("").toLowerCase().contains(keyword)
                    || Optional.ofNullable(u.getRoleName()).orElse("").toLowerCase().contains(keyword)
                    || Optional.ofNullable(u.getRoleCode()).orElse("").toLowerCase().contains(keyword);
            boolean matchRole = (role == null || role.equals("Tất cả")) || role.equalsIgnoreCase(Optional.ofNullable(u.getRoleCode()).orElse(""));
            if (matchKeyword && matchRole) {
                filtered.add(u);
            }
        }
        userTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        showUserDialog(null);
    }

    private void handleEdit(User user) {
        if (user == null) return;
        showUserDialog(user);
    }

    private void handleDelete(User user) {
        if (user == null) return;

        // UI pre-check: chặn xoá Admin cuối cùng
        String roleCode = Optional.ofNullable(user.getRoleCode()).orElse("");
        if ("ADMIN".equals(roleCode) && userService.getAdminCount() <= 1) {
            showError("Không thể xoá Admin cuối cùng.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xoá");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xoá tài khoản " + user.getUsername() + "?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            boolean ok = userService.deleteUserById(user.getId());
            if (ok) {
                loadUsers();
                loadUserStats();
                applyFilters();
            } else {
                showError("Không thể xoá tài khoản.");
            }
        }
    }

    // ===== Dialog to Add/Edit User =====
    private void showUserDialog(User editing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editing == null ? "Thêm tài khoản" : "Sửa tài khoản");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Controls
        TextField txtUsername = new TextField();
        PasswordField txtPassword = new PasswordField();            // dùng khi THÊM
        PasswordField txtConfirmPassword = new PasswordField();     // xác nhận mật khẩu khi THÊM
        ComboBox<Role> cbRole = new ComboBox<>();
        cbRole.getItems().setAll(userService.getRolesForUser());

        if (editing != null) {
            txtUsername.setText(editing.getUsername());
            // chọn role hiện tại
            for (Role r : cbRole.getItems()) {
                if (r.getId() == editing.getRoleId()) {
                    cbRole.getSelectionModel().select(r);
                    break;
                }
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;
        grid.addRow(row++, new Label("Tài khoản:"), txtUsername);

        // CHỈ hiển thị mật khẩu & xác nhận khi THÊM user
        if (editing == null) {
            grid.addRow(row++, new Label("Mật khẩu:"), txtPassword);
            grid.addRow(row++, new Label("Xác nhận mật khẩu:"), txtConfirmPassword);
        }

        grid.addRow(row, new Label("Vai trò:"), cbRole);
        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Validation on OK + UI pre-check hạ cấp Admin cuối
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(ActionEvent.ACTION, e -> {
            String username = txtUsername.getText().trim();
            String password = (editing == null) ? txtPassword.getText() : null; // chỉ check khi THÊM
            String confirm  = (editing == null) ? txtConfirmPassword.getText() : null;
            Role role = cbRole.getValue();

            StringBuilder errors = new StringBuilder();
            if (username.isEmpty()) errors.append("- Vui lòng nhập tài khoản\n");

            if (editing == null) {
                if (password == null || password.isEmpty()) {
                    errors.append("- Vui lòng nhập mật khẩu\n");
                }
                if (confirm == null || confirm.isEmpty()) {
                    errors.append("- Vui lòng nhập xác nhận mật khẩu\n");
                }
                if (password != null && confirm != null && !password.equals(confirm)) {
                    errors.append("- Mật khẩu và Xác nhận mật khẩu không trùng khớp\n");
                }
            }

            if (role == null) errors.append("- Vui lòng chọn vai trò\n");
            if (userService.isUsernameExists(username, editing == null ? null : editing.getId()))
                errors.append("- Tài khoản đã tồn tại\n");

            // Chặn hạ cấp Admin cuối cùng ở UI (khi SỬA)
            if (errors.length() == 0 && editing != null) {
                String oldCode = Optional.ofNullable(editing.getRoleCode()).orElse("");
                String newCode = role != null ? Optional.ofNullable(role.getRoleCode()).orElse("") : "";
                if ("ADMIN".equals(oldCode) && !"ADMIN".equals(newCode)) {
                    int adminCount = userService.getAdminCount();
                    if (adminCount <= 1) {
                        e.consume();
                        showError("Không thể hạ cấp Admin cuối cùng.");
                        return;
                    }
                }
            }

            if (errors.length() > 0) {
                e.consume();
                showError(errors.toString());
            }
        });

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            String username = txtUsername.getText().trim();
            String password = (editing == null) ? txtPassword.getText() : null; // khi sửa: truyền null
            Role role = cbRole.getValue();

            boolean ok;
            if (editing == null) {
                ok = userService.addUser(username, password, role.getId());
            } else {
                ok = userService.updateUser(editing.getId(), username, password, role.getId()); // password=null -> giữ nguyên
            }

            if (ok) {
                loadUsers();
                loadUserStats();
                applyFilters();
            } else {
                showError("Không thể lưu tài khoản");
            }
        }
    }

    // Helper hiển thị lỗi
    private void showError(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}