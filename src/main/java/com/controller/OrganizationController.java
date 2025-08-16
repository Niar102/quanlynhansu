package com.controller;

import com.model.Department;
import com.model.Employee;
import com.service.DepartmentService;
import com.service.EmployeeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import javafx.beans.property.SimpleStringProperty;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class OrganizationController implements Initializable {

    // Debug flag - set to false for production
    private static final boolean DEBUG = true;

    @FXML private TableView<Department> departmentTable;

    @FXML private TableColumn<Department, String> colDepartmentCode;
    @FXML private TableColumn<Department, String> colDepartmentName;
    @FXML private TableColumn<Department, String> colManagerName;
    @FXML private TableColumn<Department, Integer> colEmployeeCount;
    @FXML private TableColumn<Department, Void> actionColumn;

    @FXML private Button btnAddDepartment;

    @FXML private Label lblTotalDepartments;
    @FXML private Label lblDebugInfo;

    // Organizational Chart Elements
    @FXML private VBox organizationChart;
    @FXML private HBox departmentBoxes;

    private final DepartmentService departmentService = new DepartmentService();
    private final EmployeeService employeeService = new EmployeeService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupTableStyling();
        setupActionButtons();

        // Load dữ liệu ban đầu (tương tự DocumentController)
        loadDepartmentData();
        loadOrganizationChart();
        updateStatistics();

        if (DEBUG) {
            System.out.println("=== ORGANIZATION PAGE INITIALIZED ===");
        }
    }

    private void setupTableColumns() {
        // Cột mã phòng ban với styling
        colDepartmentCode.setCellValueFactory(new PropertyValueFactory<>("departmentCode"));
        colDepartmentCode.setCellFactory(column -> new TableCell<Department, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db; -fx-alignment: CENTER; -fx-font-size: 13px;");
                }
            }
        });

        // Cột tên phòng ban
        colDepartmentName.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        colDepartmentName.setCellFactory(column -> new TableCell<Department, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
                }
            }
        });

        // Cột trưởng phòng với xử lý null
        colManagerName.setCellValueFactory(cellData -> {
            String managerName = cellData.getValue().getManagerName();
            return new SimpleStringProperty(managerName != null ? managerName : "Chưa có");
        });
        colManagerName.setCellFactory(column -> new TableCell<Department, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Chưa có".equals(item)) {
                        setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
                    } else {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Cột số nhân viên với badge styling
        colEmployeeCount.setCellValueFactory(new PropertyValueFactory<>("employeeCount"));
        colEmployeeCount.setCellFactory(column -> new TableCell<Department, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label badge = new Label(item + " nhân viên");

                    String badgeStyle;
                    if (item == 0) {
                        badgeStyle = "-fx-background-color: #e74c3c; -fx-text-fill: white;";
                    } else if (item <= 5) {
                        badgeStyle = "-fx-background-color: #f39c12; -fx-text-fill: white;";
                    } else if (item <= 15) {
                        badgeStyle = "-fx-background-color: #27ae60; -fx-text-fill: white;";
                    } else {
                        badgeStyle = "-fx-background-color: #3498db; -fx-text-fill: white;";
                    }

                    badge.setStyle(badgeStyle + " -fx-padding: 4 12; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;");
                    setText(null);
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void setupTableStyling() {
        departmentTable.setRowFactory(tv -> {
            TableRow<Department> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setStyle("");
                } else {
                    if (row.getIndex() % 2 == 0) {
                        row.setStyle("-fx-background-color: #ffffff;");
                    } else {
                        row.setStyle("-fx-background-color: #f8f9fa;");
                    }
                }
            });

            row.setOnMouseEntered(e -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: #e3f2fd; -fx-cursor: hand;");
                }
            });

            row.setOnMouseExited(e -> {
                if (!row.isEmpty()) {
                    if (row.getIndex() % 2 == 0) {
                        row.setStyle("-fx-background-color: #ffffff;");
                    } else {
                        row.setStyle("-fx-background-color: #f8f9fa;");
                    }
                }
            });

            return row;
        });
    }

    private void setupActionButtons() {
        actionColumn.setCellFactory(new Callback<TableColumn<Department, Void>, TableCell<Department, Void>>() {
            @Override
            public TableCell<Department, Void> call(final TableColumn<Department, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("✏️ Sửa");
                    private final Button btnViewEmployees = new Button("👥 Xem NV");
                    private final Button btnDelete = new Button("🗑️ Xoá");
                    private final HBox hBox = new HBox(btnEdit, btnViewEmployees, btnDelete);

                    {
                        btnEdit.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 8; -fx-font-size: 10px; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-width: 45px;");
                        btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 8; -fx-font-size: 10px; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-width: 45px;"));
                        btnEdit.setOnMouseExited(e -> btnEdit.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 8; -fx-font-size: 10px; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-width: 45px;"));

                        btnViewEmployees.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 8; -fx-font-size: 10px; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-width: 65px;");
                        btnViewEmployees.setOnMouseEntered(e -> btnViewEmployees.setStyle("-fx-background-color: #229954; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 8; -fx-font-size: 10px; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-width: 65px;"));
                        btnViewEmployees.setOnMouseExited(e -> btnViewEmployees.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 8; -fx-font-size: 10px; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-width: 65px;"));

                        btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 8; -fx-font-size: 10px; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-width: 45px;");
                        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 8; -fx-font-size: 10px; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-width: 45px;"));
                        btnDelete.setOnMouseExited(e -> btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 8; -fx-font-size: 10px; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-width: 45px;"));

                        hBox.setSpacing(5);
                        hBox.setAlignment(Pos.CENTER);

                        btnEdit.setOnAction(event -> {
                            Department department = getTableView().getItems().get(getIndex());
                            handleEditDepartment(department);
                        });

                        btnViewEmployees.setOnAction(event -> {
                            Department department = getTableView().getItems().get(getIndex());
                            handleViewEmployees(department);
                        });

                        btnDelete.setOnAction(event -> {
                            Department department = getTableView().getItems().get(getIndex());
                            handleDeleteDepartment(department);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(hBox);
                        }
                    }
                };
            }
        });
    }

    private void loadDepartmentData() {
        try {
            var departmentList = departmentService.getAllDepartmentsWithDetails();
            ObservableList<Department> observableList = FXCollections.observableArrayList(departmentList);
            departmentTable.setItems(observableList);

            updateTotalLabel(departmentList.size());

            int totalEmployees = departmentList.stream().mapToInt(Department::getEmployeeCount).sum();
            updateDebugInfo(departmentList.size(), totalEmployees);

            if (DEBUG) {
                System.out.println("=== DEBUG: Department Table Data ===");
                for (Department dept : departmentList) {
                    System.out.println(dept.getDepartmentName() + ": " + dept.getEmployeeCount() + " nhân viên");
                }
                System.out.println("Tổng cộng: " + totalEmployees + " nhân viên trong " + departmentList.size() + " phòng ban");
            }
        } catch (Exception e) {
            updateDebugInfo(0, 0);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu phòng ban: " + e.getMessage());
        }
    }

    private void updateTotalLabel(int count) {
        if (lblTotalDepartments != null) {
            lblTotalDepartments.setText("📊 Tổng: " + count + " phòng ban");
        }
    }

    private void updateDebugInfo(int departmentCount, int totalEmployees) {
        if (lblDebugInfo != null) {
            lblDebugInfo.setText("👥 " + totalEmployees + " nhân viên");
        }
    }

    private void loadOrganizationChart() {
        try {
            List<Department> departments = departmentService.getAllDepartmentsWithDetails();

            if (DEBUG) {
                System.out.println("=== DEBUG: Organization Chart Data ===");
                for (Department dept : departments) {
                    System.out.println(dept.getDepartmentName() + ": " + dept.getEmployeeCount() + " nhân viên");
                }
            }

            departmentBoxes.getChildren().clear();
            departmentBoxes.setAlignment(Pos.CENTER);

            for (Department department : departments) {
                VBox deptBox = createDepartmentBox(department);
                departmentBoxes.getChildren().add(deptBox);
            }

            if (departments.size() > 6) {
                departmentBoxes.setSpacing(10);
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi tải sơ đồ tổ chức: " + e.getMessage());
        }
    }

    /**
     * Refresh toàn bộ dữ liệu - method chính để load lại trang
     */
    public void refreshAllData() {
        if (DEBUG) {
            departmentService.debugEmployeeData();
        }

        loadDepartmentData();
        loadOrganizationChart();
        updateStatistics();

        if (DEBUG) {
            try {
                var detailedStats = departmentService.getDepartmentEmployeeStatusStats();
                System.out.println("=== DEBUG: Detailed Employee Status ===");
                for (Map.Entry<String, Map<String, Integer>> entry : detailedStats.entrySet()) {
                    System.out.println("Phòng " + entry.getKey() + ":");
                    for (Map.Entry<String, Integer> statusEntry : entry.getValue().entrySet()) {
                        System.out.println("  - " + statusEntry.getKey() + ": " + statusEntry.getValue() + " nhân viên");
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi lấy thống kê chi tiết: " + e.getMessage());
            }
        }
    }

    public void loadDepartmentTable() {
        loadDepartmentData();
    }

    public void loadOrgChart() {
        loadOrganizationChart();
    }

    public void updateStatistics() {
        try {
            var departmentList = departmentService.getAllDepartmentsWithDetails();
            int totalDepartments = departmentList.size();
            int totalEmployees = departmentList.stream().mapToInt(Department::getEmployeeCount).sum();

            updateTotalLabel(totalDepartments);
            updateDebugInfo(totalDepartments, totalEmployees);

            if (DEBUG) {
                System.out.println("=== REFRESH STATISTICS ===");
                System.out.println("Tổng phòng ban: " + totalDepartments);
                System.out.println("Tổng nhân viên: " + totalEmployees);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật thống kê: " + e.getMessage());
        }
    }

    private VBox createDepartmentBox(Department department) {
        VBox deptBox = new VBox();
        deptBox.setAlignment(Pos.CENTER);
        deptBox.setSpacing(8);

        String departmentName = department.getDepartmentName();
        int employeeCount = department.getEmployeeCount();

        deptBox.setPrefWidth(180);
        deptBox.setMinWidth(180);
        deptBox.setMaxWidth(180);
        deptBox.setPrefHeight(120);
        deptBox.setMinHeight(120);
        deptBox.setMaxHeight(120);

        String backgroundColor = getDepartmentColor(departmentName);
        String baseStyle = "-fx-background-color: linear-gradient(to bottom, " + backgroundColor + ", " +
                getDarkerColor(backgroundColor) + "); -fx-padding: 15; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3);";
        deptBox.setStyle(baseStyle);

        String icon = getDepartmentIcon(departmentName);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label nameLabel = new Label(departmentName);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white; -fx-text-alignment: center;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(150);
        nameLabel.setAlignment(Pos.CENTER);

        Label countLabel = new Label(employeeCount + " nhân viên");
        countLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.9); -fx-text-alignment: center;");
        countLabel.setAlignment(Pos.CENTER);

        deptBox.getChildren().addAll(iconLabel, nameLabel, countLabel);

        deptBox.setOnMouseEntered(e -> {
            String hoverStyle = "-fx-background-color: linear-gradient(to bottom, " + backgroundColor + ", " +
                    getDarkerColor(backgroundColor) + "); -fx-padding: 15; -fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 5); -fx-cursor: hand; -fx-scale-x: 1.03; -fx-scale-y: 1.03;";
            deptBox.setStyle(hoverStyle);
        });

        deptBox.setOnMouseExited(e -> {
            deptBox.setStyle(baseStyle);
        });

        deptBox.setOnMouseClicked(e -> {
            handleViewEmployees(department);
        });

        return deptBox;
    }

    private String getDepartmentIcon(String deptName) {
        if (deptName.contains("Nhân sự") || deptName.contains("HR")) {
            return "👥";
        } else if (deptName.contains("Tài chính") || deptName.contains("Kế toán")) {
            return "💰";
        } else if (deptName.contains("IT") || deptName.contains("Hạ tầng") || deptName.contains("Phát triển")) {
            return "💻";
        } else if (deptName.contains("Marketing")) {
            return "📈";
        } else if (deptName.contains("Quản lý") || deptName.contains("Sản phẩm")) {
            return "📦";
        } else if (deptName.contains("QA") || deptName.contains("Kiểm thử")) {
            return "🔍";
        } else {
            return "🏢";
        }
    }

    private String getDepartmentColor(String deptName) {
        if (deptName.contains("Nhân sự") || deptName.contains("HR")) {
            return "#27ae60";
        } else if (deptName.contains("Tài chính") || deptName.contains("Kế toán")) {
            return "#e74c3c";
        } else if (deptName.contains("IT") || deptName.contains("Hạ tầng") || deptName.contains("Phát triển")) {
            return "#f39c12";
        } else if (deptName.contains("Marketing")) {
            return "#9b59b6";
        } else if (deptName.contains("Quản lý") || deptName.contains("Sản phẩm")) {
            return "#3498db";
        } else if (deptName.contains("QA") || deptName.contains("Kiểm thử")) {
            return "#1abc9c";
        } else {
            return "#95a5a6";
        }
    }

    private String getDarkerColor(String color) {
        switch (color) {
            case "#27ae60": return "#229954";
            case "#e74c3c": return "#c0392b";
            case "#f39c12": return "#d68910";
            case "#9b59b6": return "#8e44ad";
            case "#3498db": return "#2980b9";
            case "#1abc9c": return "#17a2b8";
            default: return "#7f8c8d";
        }
    }

    @FXML
    private void handleAddDepartment(ActionEvent event) {
        try {
            Dialog<Department> dialog = createDepartmentDialog(null);
            Optional<Department> result = dialog.showAndWait();

            if (result.isPresent()) {
                Department newDept = result.get();

                if (departmentService.isDepartmentCodeExists(newDept.getDepartmentCode(), null)) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mã phòng ban đã tồn tại!");
                    return;
                }

                boolean success = departmentService.addDepartment(newDept);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm phòng ban thành công!");
                    // Trường hợp thêm mới: nếu bạn muốn set luôn trưởng phòng thì
                    // cần lấy được id phòng ban vừa tạo (tuỳ addDepartment có trả id lại không).
                    refreshAllData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm phòng ban!");
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    private void handleEditDepartment(Department department) {
        try {
            Dialog<Department> dialog = createDepartmentDialog(department);
            Optional<Department> result = dialog.showAndWait();

            if (result.isPresent()) {
                Department updatedDept = result.get();

                if (departmentService.isDepartmentCodeExists(updatedDept.getDepartmentCode(), department.getId())) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mã phòng ban đã tồn tại!");
                    return;
                }

                boolean success = departmentService.updateDepartment(updatedDept);
                if (success) {
                    Integer managerId = updatedDept.getManagerId();
                    int departmentId = updatedDept.getId();

                    if (managerId != null && managerId > 0) {
                        employeeService.promoteToManager(managerId, departmentId);
                    } else {
                        employeeService.demoteManagersOfDepartment(departmentId, null);
                    }

                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật phòng ban thành công!");
                    refreshAllData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật phòng ban!");
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    private void handleViewEmployees(Department department) {
        try {
            List<Employee> employees = employeeService.getEmployeesByDepartment(department.getId());

            if (DEBUG) {
                System.out.println("=== VIEW EMPLOYEES DEBUG ===");
                System.out.println("Phòng ban: " + department.getDepartmentName());
                System.out.println("Số nhân viên: " + employees.size());
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("👥 Danh sách nhân viên thuộc phòng ban - " + department.getDepartmentName());
            dialog.setHeaderText(null);
            dialog.setResizable(true);

            TableView<Employee> employeeTable = new TableView<>();
            employeeTable.setStyle("-fx-font-size: 13px;");

            TableColumn<Employee, String> nameCol = new TableColumn<>("👤 Họ tên");
            nameCol.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getFirstName() + " " + data.getValue().getLastName()));
            nameCol.setPrefWidth(180);

            TableColumn<Employee, String> emailCol = new TableColumn<>("📧 Email");
            emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
            emailCol.setPrefWidth(200);

            TableColumn<Employee, String> phoneCol = new TableColumn<>("📞 Điện thoại");
            phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
            phoneCol.setPrefWidth(130);

            TableColumn<Employee, String> statusCol = new TableColumn<>("📊 Trạng thái");
            statusCol.setCellValueFactory(new PropertyValueFactory<>("employmentStatus"));
            statusCol.setPrefWidth(120);
            statusCol.setCellFactory(column -> new TableCell<Employee, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Label statusLabel = new Label(item);
                        if ("Đang làm việc".equals(item)) {
                            statusLabel.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 12; -fx-font-size: 11px;");
                        } else {
                            statusLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 12; -fx-font-size: 11px;");
                        }
                        setText(null);
                        setGraphic(statusLabel);
                        setAlignment(Pos.CENTER);
                    }
                }
            });

            employeeTable.getColumns().add(nameCol);
            employeeTable.getColumns().add(emailCol);
            employeeTable.getColumns().add(phoneCol);
            employeeTable.getColumns().add(statusCol);
            employeeTable.setItems(FXCollections.observableArrayList(employees));

            VBox content = new VBox(15);
            content.setStyle("-fx-padding: 20;");

            HBox headerBox = new HBox(15);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            Label infoLabel = new Label("📋 Tổng số nhân viên: " + employees.size());
            infoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            Button btnAddEmployee = new Button("➕ Thêm nhân viên");
            btnAddEmployee.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
            btnAddEmployee.setOnMouseEntered(e -> btnAddEmployee.setStyle("-fx-background-color: #229954; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;"));
            btnAddEmployee.setOnMouseExited(e -> btnAddEmployee.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;"));

            btnAddEmployee.setOnAction(e -> {
                handleAddEmployeeToDepartment(department, dialog, employeeTable);
            });

            headerBox.getChildren().addAll(infoLabel, spacer, btnAddEmployee);
            content.getChildren().addAll(headerBox, employeeTable);

            employeeTable.setPrefHeight(400);
            employeeTable.setPrefWidth(650);

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                    "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5;"
            );

            dialog.showAndWait();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteDepartment(Department department) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xoá");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Bạn có chắc chắn muốn xoá phòng ban \"" + department.getDepartmentName() + "\" không?\n" +
                "Lưu ý: Không thể xoá phòng ban đang có nhân viên.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = departmentService.deleteDepartment(department.getId());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xoá phòng ban thành công!");
                refreshAllData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi",
                        "Không thể xoá phòng ban!\nĐã xảy ra lỗi.");
            }
        }
    }

    private Dialog<Department> createDepartmentDialog(Department existingDept) {
        Dialog<Department> dialog = new Dialog<>();
        dialog.setTitle(existingDept == null ? "➕ Thêm phòng ban mới - Điền đầy đủ thông tin" : "✏️ Chỉnh sửa phòng ban");
        dialog.setHeaderText(existingDept == null ? "Tất cả các trường thông tin đều bắt buộc" : null);

        TextField codeField = new TextField();
        TextField nameField = new TextField();
        TextField descField = new TextField();
        TextField addressField = new TextField();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        ComboBox<Department> departmentFilterComboBox = new ComboBox<>();
        ComboBox<Employee> managerComboBox = new ComboBox<>();

        String fieldStyle = "-fx-padding: 10; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-background-radius: 5; -fx-min-width: 250px;";
        codeField.setStyle(fieldStyle);
        nameField.setStyle(fieldStyle);
        descField.setStyle(fieldStyle);
        addressField.setStyle(fieldStyle);
        phoneField.setStyle(fieldStyle);
        emailField.setStyle(fieldStyle);
        departmentFilterComboBox.setStyle(fieldStyle);
        managerComboBox.setStyle(fieldStyle);

        try {
            List<Department> allDepartments = departmentService.getAllDepartmentsWithDetails();
            ObservableList<Department> departmentList = FXCollections.observableArrayList();

            Department allDeptOption = new Department();
            allDeptOption.setId(0);
            allDeptOption.setDepartmentName("Chọn phòng ban để lọc nhân viên");
            departmentList.add(allDeptOption);

            departmentList.addAll(allDepartments);
            departmentFilterComboBox.setItems(departmentList);

            departmentFilterComboBox.setCellFactory(new Callback<ListView<Department>, ListCell<Department>>() {
                @Override
                public ListCell<Department> call(ListView<Department> param) {
                    return new ListCell<Department>() {
                        @Override
                        protected void updateItem(Department item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                if (item.getId() == 0) {
                                    setText("🔍 " + item.getDepartmentName());
                                } else {
                                    setText("🏢 " + item.getDepartmentName() + " (" + item.getDepartmentCode() + ")");
                                }
                            }
                        }
                    };
                }
            });

            departmentFilterComboBox.setButtonCell(new ListCell<Department>() {
                @Override
                protected void updateItem(Department item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Chọn phòng ban để lọc...");
                    } else {
                        if (item.getId() == 0) {
                            setText("🔍 " + item.getDepartmentName());
                        } else {
                            setText("🏢 " + item.getDepartmentName());
                        }
                    }
                }
            });

            ObservableList<Employee> initialEmployeeList = FXCollections.observableArrayList();
            Employee noManager = new Employee();
            noManager.setId(0);
            noManager.setFirstName("Không có");
            noManager.setLastName("trưởng phòng");
            initialEmployeeList.add(noManager);
            managerComboBox.setItems(initialEmployeeList);

            managerComboBox.setCellFactory(new Callback<ListView<Employee>, ListCell<Employee>>() {
                @Override
                public ListCell<Employee> call(ListView<Employee> param) {
                    return new ListCell<Employee>() {
                        @Override
                        protected void updateItem(Employee item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                if (item.getId() == 0) {
                                    setText("❌ " + item.getFirstName() + " " + item.getLastName());
                                } else {
                                    setText("👤 " + item.getFullName());
                                }
                            }
                        }
                    };
                }
            });

            managerComboBox.setButtonCell(new ListCell<Employee>() {
                @Override
                protected void updateItem(Employee item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Chọn trưởng phòng...");
                    } else {
                        if (item.getId() == 0) {
                            setText("❌ " + item.getFirstName() + " " + item.getLastName());
                        } else {
                            setText("👤 " + item.getFullName());
                        }
                    }
                }
            });

            managerComboBox.getSelectionModel().selectFirst();

            departmentFilterComboBox.valueProperty().addListener((obs, oldDept, newDept) -> {
                updateManagerComboBox(newDept, managerComboBox);
            });

            departmentFilterComboBox.getSelectionModel().selectFirst();

        } catch (Exception e) {
            System.err.println("Lỗi load danh sách phòng ban và nhân viên: " + e.getMessage());
            e.printStackTrace();
        }

        codeField.setPromptText("VD: HR, IT, ACC... (Bắt buộc)");
        nameField.setPromptText("VD: Phòng Nhân sự (Bắt buộc)");
        descField.setPromptText("Mô tả về phòng ban (Tùy chọn)");
        addressField.setPromptText("Địa chỉ phòng ban (Bắt buộc)");
        phoneField.setPromptText("VD: 0912345678, 024.1234567 (Bắt buộc)");
        emailField.setPromptText("VD: hr@company.com (Bắt buộc)");
        departmentFilterComboBox.setPromptText("Chọn phòng ban để lọc...");
        managerComboBox.setPromptText("Chọn trưởng phòng (Tùy chọn)");

        codeField.setTooltip(new Tooltip("Mã phòng ban (Bắt buộc):\n• Viết tắt tên phòng ban\n• VD: HR, IT, ACC, SALES"));
        nameField.setTooltip(new Tooltip("Tên phòng ban (Bắt buộc):\n• Tên đầy đủ của phòng ban\n• VD: Phòng Nhân sự, Phòng IT"));
        descField.setTooltip(new Tooltip("Mô tả phòng ban (Tùy chọn):\n• Chức năng và nhiệm vụ của phòng ban\n• VD: Quản lý nhân sự và đào tạo"));
        addressField.setTooltip(new Tooltip("Địa chỉ phòng ban (Bắt buộc):\n• Vị trí văn phòng phòng ban\n• VD: Tầng 2, Tòa nhà A"));
        phoneField.setTooltip(new Tooltip("Số điện thoại (Bắt buộc):\n• Di động: 09x, 08x, 07x, 03x, 05x\n• Cố định: 02x + 7-8 số\n• Có thể có +84 hoặc 84"));
        emailField.setTooltip(new Tooltip("Email phòng ban (Bắt buộc):\n• Có chứa @ và domain\n• VD: hr@company.com"));
        departmentFilterComboBox.setTooltip(new Tooltip("Lọc nhân viên theo phòng ban:\n• Chọn phòng ban để hiển thị nhân viên thuộc phòng đó\n• Khi chỉnh sửa, tự động chọn phòng ban hiện tại"));
        managerComboBox.setTooltip(new Tooltip("Trưởng phòng (Tùy chọn):\n• Hiển thị tất cả nhân viên đang làm việc trong phòng ban\n• Chọn bất kỳ nhân viên nào làm trưởng phòng\n• Có thể để trống nếu chưa có"));

        if (existingDept != null) {
            codeField.setText(existingDept.getDepartmentCode());
            nameField.setText(existingDept.getDepartmentName());
            descField.setText(existingDept.getDescription());
            addressField.setText(existingDept.getAddress());
            phoneField.setText(existingDept.getPhone());
            emailField.setText(existingDept.getEmail());

            for (Department dept : departmentFilterComboBox.getItems()) {
                if (dept.getId() == existingDept.getId()) {
                    departmentFilterComboBox.getSelectionModel().select(dept);
                    break;
                }
            }

            if (existingDept.getManagerId() != null && existingDept.getManagerId() > 0) {
                javafx.application.Platform.runLater(() -> {
                    for (Employee emp : managerComboBox.getItems()) {
                        if (emp.getId() == existingDept.getManagerId()) {
                            managerComboBox.getSelectionModel().select(emp);
                            break;
                        }
                    }
                });
            } else {
                javafx.application.Platform.runLater(() -> {
                    managerComboBox.getSelectionModel().selectFirst();
                });
            }
        } else {
            departmentFilterComboBox.getSelectionModel().selectFirst();
        }

        GridPane form = new GridPane();
        form.setStyle("-fx-padding: 20;");
        form.setHgap(15);
        form.setVgap(15);

        String labelStyle = "-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-min-width: 120px;";
        String requiredStyle = "-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-min-width: 120px;";

        Label codeLabel = new Label("🏷️ Mã phòng ban: *");
        codeLabel.setStyle(requiredStyle);
        Label nameLabel = new Label("🏢 Tên phòng ban: *");
        nameLabel.setStyle(requiredStyle);
        Label descLabel = new Label("📝 Mô tả:");
        descLabel.setStyle(labelStyle);
        Label addressLabel = new Label("🏠 Địa chỉ: *");
        addressLabel.setStyle(requiredStyle);
        Label phoneLabel = new Label("📞 Điện thoại: *");
        phoneLabel.setStyle(requiredStyle);
        Label emailLabel = new Label("📧 Email: *");
        emailLabel.setStyle(requiredStyle);
        Label departmentFilterLabel = new Label("🔍 Lọc theo phòng ban:");
        departmentFilterLabel.setStyle(labelStyle);
        Label managerLabel = new Label("👤 Trưởng phòng:");
        managerLabel.setStyle(labelStyle);

        form.add(codeLabel, 0, 0);
        form.add(codeField, 1, 0);
        form.add(nameLabel, 0, 1);
        form.add(nameField, 1, 1);
        form.add(descLabel, 0, 2);
        form.add(descField, 1, 2);
        form.add(addressLabel, 0, 3);
        form.add(addressField, 1, 3);
        form.add(phoneLabel, 0, 4);
        form.add(phoneField, 1, 4);
        form.add(emailLabel, 0, 5);
        form.add(emailField, 1, 5);
        form.add(departmentFilterLabel, 0, 6);
        form.add(departmentFilterComboBox, 1, 6);
        form.add(managerLabel, 0, 7);
        form.add(managerComboBox, 1, 7);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);

        okButton.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5;"
        );
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5;"
        );

        Runnable validateForm = () -> {
            boolean hasRequiredFields = true;

            String defaultStyle = "-fx-padding: 10; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-background-radius: 5; -fx-min-width: 250px;";

            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            codeField.setStyle(defaultStyle);
            nameField.setStyle(defaultStyle);
            descField.setStyle(defaultStyle);
            addressField.setStyle(defaultStyle);
            emailField.setStyle(defaultStyle);
            phoneField.setStyle(defaultStyle);
            managerComboBox.setStyle(defaultStyle);

            if (code.isEmpty() || name.isEmpty() || address.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                hasRequiredFields = false;
            }

            Employee selectedManager = managerComboBox.getSelectionModel().getSelectedItem();
            if (selectedManager == null) {
                managerComboBox.getSelectionModel().selectFirst();
            }

            dialog.setHeaderText(existingDept == null ? "Tất cả các trường thông tin đều bắt buộc" : null);

            okButton.setDisable(!hasRequiredFields);
        };

        codeField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        addressField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        managerComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm.run());

        validateForm.run();

        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            StringBuilder errorMessages = new StringBuilder();
            boolean isFormValid = true;

            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            if (code.isEmpty()) {
                errorMessages.append("• Mã phòng ban không được để trống\n");
                isFormValid = false;
            }
            if (name.isEmpty()) {
                errorMessages.append("• Tên phòng ban không được để trống\n");
                isFormValid = false;
            }
            if (address.isEmpty()) {
                errorMessages.append("• Địa chỉ không được để trống\n");
                isFormValid = false;
            }
            if (email.isEmpty()) {
                errorMessages.append("• Email không được để trống\n");
                isFormValid = false;
            }
            if (phone.isEmpty()) {
                errorMessages.append("• Số điện thoại không được để trống\n");
                isFormValid = false;
            }

            if (!code.isEmpty() && (code.length() < 2 || code.length() > 10)) {
                errorMessages.append("• Mã phòng ban phải từ 2-10 ký tự\n");
                isFormValid = false;
            }
            if (!name.isEmpty() && (name.length() < 3 || name.length() > 100)) {
                errorMessages.append("• Tên phòng ban phải từ 3-100 ký tự\n");
                isFormValid = false;
            }
            if (!address.isEmpty() && address.length() < 5) {
                errorMessages.append("• Địa chỉ phải có ít nhất 5 ký tự\n");
                isFormValid = false;
            }
            if (!email.isEmpty() && !isValidEmail(email)) {
                errorMessages.append("• Email không đúng định dạng (vd: hr@company.com)\n");
                isFormValid = false;
            }
            if (!phone.isEmpty() && !isValidPhone(phone)) {
                errorMessages.append("• Số điện thoại không đúng định dạng VN (vd: 0987654321)\n");
                isFormValid = false;
            }

            if (!isFormValid) {
                showAlert(Alert.AlertType.WARNING, "Thông tin chưa hợp lệ",
                        "Vui lòng kiểm tra lại thông tin:\n\n" + errorMessages.toString());
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                Department dept = existingDept != null ? existingDept : new Department();
                dept.setDepartmentCode(codeField.getText().trim());
                dept.setDepartmentName(nameField.getText().trim());
                dept.setDescription(descField.getText().trim());
                dept.setAddress(addressField.getText().trim());
                dept.setPhone(phoneField.getText().trim());
                dept.setEmail(emailField.getText().trim());

                Employee selectedManager = managerComboBox.getSelectionModel().getSelectedItem();
                if (selectedManager != null && selectedManager.getId() > 0) {
                    dept.setManagerId(selectedManager.getId());
                } else {
                    dept.setManagerId(null);
                }

                return dept;
            }
            return null;
        });

        return dialog;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }

    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[\\s\\-\\.\\(\\)]", "");
        String phoneRegex = "^(\\+84|84|0)(3[2-9]|5[689]|7[06-9]|8[1-689]|9[0-46-9])[0-9]{7}$|^(\\+84|84|0)(2[0-9])[0-9]{7,8}$";
        return cleanPhone.matches(phoneRegex);
    }

    public void refreshPage() {
        if (DEBUG) {
            System.out.println("=== EXTERNAL REFRESH TRIGGERED ===");
        }
        refreshAllData();
    }

    public void refreshTableOnly() {
        loadDepartmentData();
        updateStatistics();
    }

    public void refreshChartOnly() {
        loadOrganizationChart();
    }

    @FXML
    private void onHoverAdd() {
        btnAddDepartment.setStyle("-fx-background-color: #229954; -fx-text-fill: white; -fx-padding: 12 24; -fx-background-radius: 25; -fx-font-size: 14px; -fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 12, 0, 0, 3); -fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
    }

    @FXML
    private void onExitAdd() {
        btnAddDepartment.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 12 24; -fx-background-radius: 25; -fx-font-size: 14px; -fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 2); -fx-cursor: hand;");
    }

    /**
     * Cập nhật danh sách trưởng phòng dựa trên phòng ban được chọn
     */
    private void updateManagerComboBox(Department selectedDepartment, ComboBox<Employee> managerComboBox) {
        try {
            ObservableList<Employee> employeeList = FXCollections.observableArrayList();

            Employee noManager = new Employee();
            noManager.setId(0);
            noManager.setFirstName("Không có");
            noManager.setLastName("trưởng phòng");
            employeeList.add(noManager);

            if (selectedDepartment != null && selectedDepartment.getId() > 0) {
                List<Employee> departmentEmployees = employeeService.getEmployeesByDepartmentForManager(selectedDepartment.getId());
                employeeList.addAll(departmentEmployees);

                if (DEBUG) {
                    System.out.println("=== MANAGER FILTER DEBUG ===");
                    System.out.println("Phòng ban: " + selectedDepartment.getDepartmentName());
                    System.out.println("Số nhân viên đang làm việc trong phòng ban: " + departmentEmployees.size());
                    for (Employee emp : departmentEmployees) {
                        System.out.println("- " + emp.getFullName() + " (ID: " + emp.getId() + ", Trạng thái: " + emp.getEmploymentStatus() + ")");
                    }
                }
            }

            Employee currentSelection = managerComboBox.getSelectionModel().getSelectedItem();

            managerComboBox.setItems(employeeList);

            if (currentSelection != null) {
                boolean found = false;
                for (Employee emp : employeeList) {
                    if (emp.getId() == currentSelection.getId()) {
                        managerComboBox.getSelectionModel().select(emp);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    managerComboBox.getSelectionModel().selectFirst();
                }
            } else {
                managerComboBox.getSelectionModel().selectFirst();
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật danh sách trưởng phòng: " + e.getMessage());
            e.printStackTrace();

            ObservableList<Employee> fallbackList = FXCollections.observableArrayList();
            Employee noManager = new Employee();
            noManager.setId(0);
            noManager.setFirstName("Không có");
            noManager.setLastName("trưởng phòng");
            fallbackList.add(noManager);
            managerComboBox.setItems(fallbackList);
            managerComboBox.getSelectionModel().selectFirst();
        }
    }

    private void handleAddEmployeeToDepartment(Department department, Dialog<Void> parentDialog, TableView<Employee> employeeTable) {
        try {
            List<Employee> availableEmployees = employeeService.getEmployeesNotInDepartment(department.getId());

            if (availableEmployees.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Thông báo",
                        "Không có nhân viên nào khả dụng để thêm vào phòng ban này.\n" +
                                "Tất cả nhân viên đang làm việc đã được phân công phòng ban.");
                return;
            }

            Dialog<List<Employee>> selectDialog = createEmployeeSelectionDialog(availableEmployees, department);
            Optional<List<Employee>> result = selectDialog.showAndWait();

            if (result.isPresent() && !result.get().isEmpty()) {
                List<Employee> selectedEmployees = result.get();
                int successCount = 0;

                for (Employee emp : selectedEmployees) {
                    boolean success = employeeService.updateEmployeeDepartment(emp.getId(), department.getId());
                    if (success) {
                        successCount++;
                    }
                }

                if (successCount > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công",
                            "Đã thêm thành công " + successCount + " nhân viên vào phòng ban " + department.getDepartmentName() + "!");

                    refreshEmployeeTableInDialog(department, employeeTable);
                    refreshAllData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm nhân viên vào phòng ban!");
                }
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshEmployeeTableInDialog(Department department, TableView<Employee> employeeTable) {
        try {
            List<Employee> employees = employeeService.getEmployeesByDepartment(department.getId());
            employeeTable.setItems(FXCollections.observableArrayList(employees));

            javafx.scene.Parent parent = employeeTable.getParent();
            while (parent != null) {
                if (parent instanceof VBox) {
                    VBox vbox = (VBox) parent;
                    for (javafx.scene.Node node : vbox.getChildren()) {
                        if (node instanceof HBox) {
                            HBox hbox = (HBox) node;
                            for (javafx.scene.Node child : hbox.getChildren()) {
                                if (child instanceof Label) {
                                    Label label = (Label) child;
                                    if (label.getText().startsWith("📋 Tổng số nhân viên:")) {
                                        label.setText("📋 Tổng số nhân viên: " + employees.size());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
                parent = parent.getParent();
            }

            if (DEBUG) {
                System.out.println("=== REFRESH EMPLOYEE TABLE ===");
                System.out.println("Phòng ban: " + department.getDepartmentName());
                System.out.println("Số nhân viên sau khi cập nhật: " + employees.size());
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi refresh bảng nhân viên: " + e.getMessage());
        }
    }

    private Dialog<List<Employee>> createEmployeeSelectionDialog(List<Employee> availableEmployees, Department department) {
        Dialog<List<Employee>> dialog = new Dialog<>();
        dialog.setTitle("➕ Thêm nhân viên vào phòng ban - " + department.getDepartmentName());
        dialog.setHeaderText("Chọn nhân viên muốn thêm vào phòng ban (có thể chọn nhiều)");
        dialog.setResizable(true);

        TableView<Employee> availableTable = new TableView<>();
        availableTable.setStyle("-fx-font-size: 13px;");
        availableTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Employee, String> nameCol = new TableColumn<>("👤 Họ tên");
        nameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFirstName() + " " + data.getValue().getLastName()));
        nameCol.setPrefWidth(180);

        TableColumn<Employee, String> currentDeptCol = new TableColumn<>("🏢 Phòng ban hiện tại");
        currentDeptCol.setCellValueFactory(data -> {
            String deptName = data.getValue().getDepartmentName();
            return new SimpleStringProperty(deptName != null ? deptName : "Chưa có");
        });
        currentDeptCol.setPrefWidth(150);

        TableColumn<Employee, String> emailCol = new TableColumn<>("📧 Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<Employee, String> phoneCol = new TableColumn<>("📞 Điện thoại");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(130);

        availableTable.getColumns().add(nameCol);
        availableTable.getColumns().add(currentDeptCol);
        availableTable.getColumns().add(emailCol);
        availableTable.getColumns().add(phoneCol);
        availableTable.setItems(FXCollections.observableArrayList(availableEmployees));
        availableTable.setEditable(true);

        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20;");

        Label infoLabel = new Label("📋 Có " + availableEmployees.size() + " nhân viên khả dụng");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label instructionLabel = new Label("💡 Click để chọn nhân viên, giữ Ctrl+Click để chọn nhiều nhân viên");
        instructionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        content.getChildren().addAll(infoLabel, instructionLabel, availableTable);

        availableTable.setPrefHeight(400);
        availableTable.setPrefWidth(750);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5;"
        );
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5;"
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new ArrayList<>(availableTable.getSelectionModel().getSelectedItems());
            }
            return null;
        });

        return dialog;
    }
}