package com.controller;

import com.model.Employee;
import com.service.DepartmentService;
import com.service.EmployeeService;
import com.service.PositionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class EmployeeController implements Initializable {

    // ===== Table & Columns =====
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> colId;
    @FXML private TableColumn<Employee, String> colFullName;
    @FXML private TableColumn<Employee, String> colEmail;
    @FXML private TableColumn<Employee, String> colPhone;
    @FXML private TableColumn<Employee, String> colGender;
    @FXML private TableColumn<Employee, LocalDate> colDateOfBirth;
    @FXML private TableColumn<Employee, String> colDepartment;
    @FXML private TableColumn<Employee, String> colPosition;
    @FXML private TableColumn<Employee, LocalDate> colHireDate;
    @FXML private TableColumn<Employee, String> colStatus;
    @FXML private TableColumn<Employee, Void> actionColumn;

    // ===== Filters / Search / Stats =====
    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private ComboBox<String> positionFilter;
    @FXML private Button btnAddEmployee;

    @FXML private Label lblTotal;
    @FXML private Label lblWorking;
    @FXML private Label lblInactive;

    // ===== Services =====
    private final EmployeeService employeeService = new EmployeeService();
    private final DepartmentService departmentService = new DepartmentService();
    private final PositionService positionService = new PositionService();

    // ===== Button Styles (giữ dáng khi hover) =====
    private static final String ADD_BTN_BASE_STYLE =
            "-fx-background-color:#3498db; -fx-text-fill:white; -fx-padding:10 20; -fx-background-radius:5; -fx-cursor:hand;";
    private static final String ADD_BTN_HOVER_STYLE =
            "-fx-background-color:#2980b9; -fx-text-fill:white; -fx-padding:10 20; -fx-background-radius:5; -fx-cursor:hand;";

    private static final String EDIT_BASE_STYLE =
            "-fx-background-color:#3498db; -fx-text-fill:white; -fx-padding:4 10; -fx-background-radius:5; -fx-cursor:hand;";
    private static final String EDIT_HOVER_STYLE =
            "-fx-background-color:#2f89d6; -fx-text-fill:white; -fx-padding:4 10; -fx-background-radius:5; -fx-cursor:hand;";
    private static final String EDIT_PRESSED_STYLE =
            "-fx-background-color:#2874bf; -fx-text-fill:white; -fx-padding:4 10; -fx-background-radius:5; -fx-cursor:hand;";

    private static final String DELETE_BASE_STYLE =
            "-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-padding:4 10; -fx-background-radius:5; -fx-cursor:hand;";
    private static final String DELETE_HOVER_STYLE =
            "-fx-background-color:#cf4436; -fx-text-fill:white; -fx-padding:4 10; -fx-background-radius:5; -fx-cursor:hand;";
    private static final String DELETE_PRESSED_STYLE =
            "-fx-background-color:#b93a2e; -fx-text-fill:white; -fx-padding:4 10; -fx-background-radius:5; -fx-cursor:hand;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupActionColumn();

        loadEmployeeData();
        loadEmployeeStats();

        // Load dropdowns
        loadDepartmentFilter();          // gồm "Tất cả"
        loadPositionFilterAll();         // gồm "Tất cả"
        updatePositionFilterByDepartment(); // đồng bộ lần đầu

        // Sự kiện filter:
        departmentFilter.setOnAction(e -> {
            updatePositionFilterByDepartment();
            filterEmployees();
        });
        positionFilter.setOnAction(e -> filterEmployees());

        // Search
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null || newV.isBlank()) {
                loadEmployeeData();
            } else {
                List<Employee> results = employeeService.searchEmployee(newV.trim());
                employeeTable.setItems(FXCollections.observableArrayList(results));
            }
        });

        if (btnAddEmployee != null) btnAddEmployee.setStyle(ADD_BTN_BASE_STYLE);
    }

    // ====== Table setup ======
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFullName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFullName()));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("positionName"));
        colHireDate.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("employmentStatus"));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colDateOfBirth.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        colDateOfBirth.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : fmt.format(date));
            }
        });
        colHireDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : fmt.format(date));
            }
        });
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Employee, Void> call(final TableColumn<Employee, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Sửa");
                    private final Button btnDelete = new Button("Xoá");
                    private final HBox hBox = new HBox(8, btnEdit, btnDelete);

                    {
                        hBox.setAlignment(Pos.CENTER);

                        // Style mặc định
                        btnEdit.setStyle(EDIT_BASE_STYLE);
                        btnDelete.setStyle(DELETE_BASE_STYLE);

                        // Hover/Pessed cho Sửa
                        btnEdit.setOnMouseEntered(e -> btnEdit.setStyle(EDIT_HOVER_STYLE));
                        btnEdit.setOnMouseExited(e -> btnEdit.setStyle(EDIT_BASE_STYLE));
                        btnEdit.setOnMousePressed(e -> btnEdit.setStyle(EDIT_PRESSED_STYLE));
                        btnEdit.setOnMouseReleased(e -> btnEdit.setStyle(EDIT_HOVER_STYLE));

                        // Hover/Pessed cho Xoá
                        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle(DELETE_HOVER_STYLE));
                        btnDelete.setOnMouseExited(e -> btnDelete.setStyle(DELETE_BASE_STYLE));
                        btnDelete.setOnMousePressed(e -> btnDelete.setStyle(DELETE_PRESSED_STYLE));
                        btnDelete.setOnMouseReleased(e -> btnDelete.setStyle(DELETE_HOVER_STYLE));

                        // Hành động
                        btnEdit.setOnAction(e -> {
                            Employee employee = getTableView().getItems().get(getIndex());
                            handleEdit(employee);
                        });

                        btnDelete.setOnAction(e -> {
                            Employee employee = getTableView().getItems().get(getIndex());
                            handleDelete(employee);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : hBox);
                    }
                };
            }
        });
    }

    // ====== Data load ======
    private void loadEmployeeData() {
        List<Employee> employeeList = employeeService.getAllEmployees();
        employeeTable.setItems(FXCollections.observableArrayList(employeeList));
    }

    private void loadEmployeeStats() {
        Map<String, Integer> stats = employeeService.getEmployeeStats();
        lblTotal.setText(String.valueOf(stats.getOrDefault("total", 0)));
        lblWorking.setText(String.valueOf(stats.getOrDefault("working", 0)));
        lblInactive.setText(String.valueOf(stats.getOrDefault("inactive", 0)));
    }

    private void loadDepartmentFilter() {
        List<String> departments = departmentService.getAllDepartmentNames();
        departmentFilter.getItems().clear();
        departmentFilter.getItems().add("Tất cả");
        departmentFilter.getItems().addAll(departments);
        departmentFilter.getSelectionModel().selectFirst();
    }

    private void loadPositionFilterAll() {
        List<String> positions = positionService.getAllPositionNames();
        positionFilter.getItems().clear();
        positionFilter.getItems().add("Tất cả");
        positionFilter.getItems().addAll(positions);
        positionFilter.getSelectionModel().selectFirst();
    }

    /**
     * Khi chọn phòng ban, combobox Chức vụ chỉ hiển thị position của phòng ban đó.
     */
    private void updatePositionFilterByDepartment() {
        String deptName = departmentFilter.getValue();

        positionFilter.getItems().clear();
        positionFilter.getItems().add("Tất cả");

        if (deptName != null && !"Tất cả".equals(deptName)) {
            List<String> positionsOfDept = positionService.getPositionNamesByDepartment(deptName);
            positionFilter.getItems().addAll(positionsOfDept);
        } else {
            positionFilter.getItems().addAll(positionService.getAllPositionNames());
        }

        positionFilter.getSelectionModel().selectFirst();
    }

    // ====== Filtering ======
    private void filterEmployees() {
        String selectedDepartment = departmentFilter.getValue();
        String selectedPosition = positionFilter.getValue();
        List<Employee> filtered = employeeService.getFilteredEmployees(selectedDepartment, selectedPosition);
        employeeTable.setItems(FXCollections.observableArrayList(filtered));
    }

    // ====== Actions ======
    @FXML
    private void handleAddEmployee(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/view/add_employee.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Thêm nhân viên mới");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // reload sau khi thêm
            loadEmployeeData();
            loadEmployeeStats();
            filterEmployees(); // áp lại filter đang chọn
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleEdit(com.model.Employee employee) {
        try {
            com.model.Employee fresh = employeeService.getEmployeeById(employee.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/view/add_employee.fxml"));
            Parent root = loader.load();
            com.controller.AddEmployeeController controller = loader.getController();
            controller.setEmployeeToEdit(fresh != null ? fresh : employee);

            Stage stage = new Stage();
            stage.setTitle("Chỉnh sửa nhân viên");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadEmployeeData();
            loadEmployeeStats();
            filterEmployees();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Employee employee) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xoá");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xoá nhân viên: " + employee.getFullName() + " ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Nếu có xoá mềm thì gọi service; tạm thời refresh.
            loadEmployeeData();
            loadEmployeeStats();
            filterEmployees();
        }
    }

    // ====== Hover style cho nút Thêm ======
    @FXML
    private void onHoverAdd(MouseEvent e) {
        btnAddEmployee.setStyle(ADD_BTN_HOVER_STYLE);
    }

    @FXML
    private void onExitAdd(MouseEvent e) {
        btnAddEmployee.setStyle(ADD_BTN_BASE_STYLE);
    }

    // Dùng cho nơi khác gọi (nếu có)
    public void loadEmployeesByDepartment(int departmentId) {
        List<Employee> employees = employeeService.getEmployeesByDepartment(departmentId);
        ObservableList<Employee> employeeList = FXCollections.observableArrayList(employees);
        employeeTable.setItems(employeeList);
    }
}
