package com.controller;

import com.model.Contract;
import com.model.Employee;
import com.service.ContractService;
import com.service.EmployeeService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // <<< THÊM
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ContractController implements Initializable {

    // Formatter ngày dd/MM/yyyy
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // <<< THÊM

    // Statistics Labels
    @FXML private Label activeContractsLabel;
    @FXML private Label expiringContractsLabel;
    @FXML private Label expiredContractsLabel;

    // Contract Type Statistics
    @FXML private Label longTermContractsLabel;
    @FXML private Label shortTermContractsLabel;
    @FXML private Label probationContractsLabel;

    // Search and Filter Controls
    @FXML private TextField searchField;
    @FXML private ComboBox<String> contractTypeComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private DatePicker fromDatePicker;
    @FXML private Button createContractButton;

    // Table and Columns
    @FXML private TableView<Contract> contractTable;
    @FXML private TableColumn<Contract, Integer> indexColumn;
    @FXML private TableColumn<Contract, String> contractCodeColumn;
    @FXML private TableColumn<Contract, String> employeeNameColumn;
    @FXML private TableColumn<Contract, String> contractTypeColumn;
    @FXML private TableColumn<Contract, LocalDate> startDateColumn;
    @FXML private TableColumn<Contract, LocalDate> endDateColumn;
    @FXML private TableColumn<Contract, BigDecimal> salaryColumn;
    @FXML private TableColumn<Contract, String> statusColumn;
    @FXML private TableColumn<Contract, String> createdByColumn; // THÊM MỚI: Cột người tạo
    @FXML private TableColumn<Contract, Void> actionColumn;

    private ContractService contractService;
    private EmployeeService employeeService;
    private ObservableList<Contract> contractList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contractService = new ContractService();
        employeeService = new EmployeeService();
        contractList = FXCollections.observableArrayList();

        createContractButton.setOnMouseEntered(e -> {
            createContractButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
        });

        createContractButton.setOnMouseExited(e -> {
            createContractButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-scale-x: 1.0; -fx-scale-y: 1.0;");
        });

        initializeTable();
        initializeComboBoxes();
        loadContractData();
        loadStatistics();
        setupSearchAndFilter();
    }

    private void initializeTable() {
        indexColumn.setCellFactory(col -> new TableCell<Contract, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        indexColumn.setSortable(false);

        // Set up table columns
        contractCodeColumn.setCellValueFactory(new PropertyValueFactory<>("contractNumber"));
        employeeNameColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        contractTypeColumn.setCellValueFactory(new PropertyValueFactory<>("contractTypeName"));

        // --- CỘT NGÀY: giữ nguyên value = LocalDate, chỉ custom CellFactory để format dd/MM/yyyy
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startDateColumn.setCellFactory(col -> new TableCell<Contract, LocalDate>() { // <<< THÊM
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : DATE_FMT.format(item));
            }
        });

        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        endDateColumn.setCellFactory(col -> new TableCell<Contract, LocalDate>() { // <<< THÊM
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : DATE_FMT.format(item));
            }
        });

        // THÊM MỚI: Setup cột người tạo hợp đồng
        createdByColumn.setCellValueFactory(new PropertyValueFactory<>("createdByUsername"));

        // Custom cell factory for salary column to format currency
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));
        salaryColumn.setCellFactory(column -> new TableCell<Contract, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f ₫", item));
                }
            }
        });

        // (Giữ nguyên đoạn setCellFactory thứ 2 của salary nếu bạn đã dùng – nhưng 1 cái là đủ)
        salaryColumn.setCellFactory(column -> new TableCell<Contract, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f ₫", item)); // Hiển thị số có dấu chấm và VND
                }
            }
        });

        // Custom cell factory for status column with colored indicators
        statusColumn.setCellValueFactory(cellData -> {
            Contract contract = cellData.getValue();
            return new SimpleStringProperty(contract.getStatus());  // status được tính từ getStatus()
        });
        statusColumn.setCellFactory(column -> new TableCell<Contract, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status.toLowerCase()) {
                        case "đang hoạt động":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        case "đã hết hạn":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "đã gia hạn":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                            break;
                        case "chấm dứt":
                            setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #2c3e50;");
                    }
                }
            }
        });

        // Set up action column with buttons
        actionColumn.setCellFactory(column -> new TableCell<Contract, Void>() {
            private final Button viewButton = new Button("Xem");
            private final Button editButton = new Button("Sửa");
            private final Button deleteButton = new Button("Xóa");
            private final HBox buttonBox = new HBox(5);

            {
                viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 8;");
                editButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 8;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 8;");

                // Hover effects
                viewButton.setOnMouseEntered(e -> viewButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 8;"));
                viewButton.setOnMouseExited(e -> viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 8;"));

                editButton.setOnMouseEntered(e -> editButton.setStyle("-fx-background-color: #d68910; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 8;"));
                editButton.setOnMouseExited(e -> editButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 8;"));

                deleteButton.setOnMouseEntered(e -> deleteButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 8;"));
                deleteButton.setOnMouseExited(e -> deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 8;"));

                buttonBox.setAlignment(Pos.CENTER);
                buttonBox.getChildren().addAll(viewButton, editButton, deleteButton);

                viewButton.setOnAction(event -> {
                    Contract contract = getTableView().getItems().get(getIndex());
                    viewContract(contract);
                });

                editButton.setOnAction(event -> {
                    Contract contract = getTableView().getItems().get(getIndex());
                    editContract(contract);
                });

                deleteButton.setOnAction(event -> {
                    Contract contract = getTableView().getItems().get(getIndex());
                    deleteContract(contract);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });

        contractTable.setItems(contractList);
    }

    private void initializeComboBoxes() {
        // Initialize contract type combo box
        contractTypeComboBox.getItems().addAll(
                "Hợp đồng không xác định thời hạn",
                "Hợp đồng xác định thời hạn 1 năm",
                "Hợp đồng xác định thời hạn 2 năm",
                "Hợp đồng thời vụ",
                "Hợp đồng thử việc"
        );

        // Initialize status combo box
        statusComboBox.getItems().addAll("Đang hoạt động", "Đã hết hạn");
    }

    private void loadContractData() {
        List<Contract> contracts = contractService.getAllContracts();
        contractList.clear();
        contractList.addAll(contracts);
    }

    private void loadStatistics() {
        List<Contract> contracts = contractService.getAllContracts();

        int activeCount = 0;
        int expiringCount = 0;
        int expiredCount = 0;
        int longTerm = 0;
        int shortTerm = 0;
        int probation = 0;

        for (Contract contract : contracts) {
            // Đếm theo trạng thái
            if ("Đang hoạt động".equalsIgnoreCase(contract.getStatus())) {
                activeCount++;
            }

            if (contract.isExpired()) {
                expiredCount++;
            } else if (contract.isExpiringSoon()) {
                expiringCount++;
            }

            // Đếm theo loại hợp đồng
            String type = contract.getContractTypeName();
            if (type != null) {
                switch (type) {
                    case "Hợp đồng không xác định thời hạn":
                    case "Hợp đồng xác định thời hạn 2 năm":
                        longTerm++;
                        break;
                    case "Hợp đồng xác định thời hạn 1 năm":
                    case "Hợp đồng thời vụ":
                        shortTerm++;
                        break;
                    case "Hợp đồng thử việc":
                        probation++;
                        break;
                }
            }
        }

        // Gán lên UI
        activeContractsLabel.setText(String.valueOf(activeCount));
        expiringContractsLabel.setText(String.valueOf(expiringCount));
        expiredContractsLabel.setText(String.valueOf(expiredCount));
        longTermContractsLabel.setText(String.valueOf(longTerm));
        shortTermContractsLabel.setText(String.valueOf(shortTerm));
        probationContractsLabel.setText(String.valueOf(probation));
    }

    private void setupSearchAndFilter() {
        // Add listeners for real-time search and filter
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterContracts();
        });

        contractTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterContracts();
        });

        statusComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterContracts();
        });

        fromDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterContracts();
        });
    }

    private void filterContracts() {
        String searchTerm = searchField.getText();
        String contractType = contractTypeComboBox.getValue();
        String status = statusComboBox.getValue();
        LocalDate fromDate = fromDatePicker.getValue();

        List<Contract> filteredContracts = contractService.searchContracts(searchTerm, contractType, status, fromDate);
        contractList.clear();
        contractList.addAll(filteredContracts);
    }

    @FXML
    private void handleCreateContract() {
        // Open create contract dialog
        openAddContractWindow(null);
    }

    private String fmt(LocalDate d) { // <<< THÊM: dùng cho dialog xem
        return d == null ? "" : DATE_FMT.format(d);
    }

    private void viewContract(Contract contract) {
        // Create a dialog to view contract details
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết Hợp đồng");
        dialog.setHeaderText("Thông tin chi tiết hợp đồng: " + contract.getContractNumber());

        // Create content
        StringBuilder content = new StringBuilder();
        content.append("Mã hợp đồng: ").append(contract.getContractNumber()).append("\n");
        content.append("Nhân viên: ").append(contract.getEmployeeName()).append("\n");
        content.append("Loại hợp đồng: ").append(contract.getContractTypeName()).append("\n");
        content.append("Ngày bắt đầu: ").append(fmt(contract.getStartDate())).append("\n"); // <<< ĐÃ FORMAT
        content.append("Ngày kết thúc: ").append(contract.getEndDate() != null ? fmt(contract.getEndDate()) : "Không xác định").append("\n"); // <<< ĐÃ FORMAT
        content.append("Mức lương: ").append(contract.getFormattedSalary()).append("\n");
        content.append("Phụ cấp: ").append(contract.getAllowances() != null ? String.format("%,.0f VND", contract.getAllowances()) : "0 VND").append("\n");
        content.append("Trạng thái: ").append(contract.getStatus()).append("\n");
        // THÊM MỚI: Hiển thị người tạo hợp đồng
        content.append("Người tạo: ").append(contract.getCreatedByUsername() != null ? contract.getCreatedByUsername() : "Không xác định").append("\n");
        if (contract.getNotes() != null && !contract.getNotes().isEmpty()) {
            content.append("Ghi chú: ").append(contract.getNotes()).append("\n");
        }

        TextArea textArea = new TextArea(content.toString());
        textArea.setEditable(false);
        textArea.setPrefRowCount(15);
        textArea.setPrefColumnCount(50);

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        dialog.showAndWait();
    }

    private void editContract(Contract contract) {
        openAddContractWindow(contract);
    }

    private void deleteContract(Contract contract) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa hợp đồng");
        alert.setContentText("Bạn có chắc chắn muốn xóa hợp đồng " + contract.getContractNumber() + "? Hợp đồng sẽ không hiển thị trong danh sách nhưng vẫn được lưu trữ.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (contractService.deleteContract(contract.getId())) {
                    showSuccessMessage("Xóa hợp đồng thành công!");
                    loadContractData();
                    loadStatistics();
                } else {
                    showErrorMessage("Không thể xóa hợp đồng!");
                }
            }
        });
    }

    private void openAddContractWindow(Contract contract) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/view/add_contract.fxml"));
            Parent root = loader.load();

            AddContractController addController = loader.getController();
            addController.setContract(contract); // null for new, existing for edit
            addController.setOnSaveCallback(() -> {
                loadContractData();
                loadStatistics();
            });

            Stage stage = new Stage();
            stage.setTitle(contract == null ? "Thêm Hợp Đồng Mới" : "Chỉnh Sửa Hợp Đồng");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Không thể mở cửa sổ thêm hợp đồng!");
        }
    }

    // Fixed method: Compatible with Employee model structure
    private void loadEmployeesForComboBox(ComboBox<String> comboBox) {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            comboBox.getItems().clear();

            for (Employee employee : employees) {
                // Create display name as "FirstName LastName"
                String fullName = employee.getFullName();
                comboBox.getItems().add(fullName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback if there's an error
            comboBox.getItems().addAll("Không thể tải danh sách nhân viên");
        }
    }

    // Fixed method: Compatible with Employee model structure
    private int getEmployeeIdByName(String employeeName) {
        try {
            List<Employee> employees = employeeService.getAllEmployees();

            for (Employee employee : employees) {
                String fullName = employee.getFullName();
                if (fullName.equals(employeeName)) {
                    return employee.getId();
                }
            }

            // If not found, return 0 or handle error
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Fallback
        }
    }

    private int getContractTypeIdByName(String contractTypeName) {
        // Map contract type names to IDs based on your database
        switch (contractTypeName) {
            case "Hợp đồng không xác định thời hạn":
                return 1;
            case "Hợp đồng xác định thời hạn 1 năm":
                return 2;
            case "Hợp đồng xác định thời hạn 2 năm":
                return 3;
            case "Hợp đồng thời vụ":
                return 4;
            case "Hợp đồng thử việc":
                return 5;
            default:
                return 1;
        }
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleRefresh() {
        loadContractData();
        loadStatistics();

        // Clear filters
        searchField.clear();
        contractTypeComboBox.setValue(null);
        statusComboBox.setValue(null);
        fromDatePicker.setValue(null);
    }

    @FXML
    private void handleExportContracts() {
        // Implement export functionality
        showSuccessMessage("Chức năng xuất file đang được phát triển!");
    }
}
