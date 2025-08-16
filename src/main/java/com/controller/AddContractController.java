package com.controller;

import com.model.Contract;
import com.model.Employee;
import com.service.ContractService;
import com.service.EmployeeService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AddContractController implements Initializable {

    @FXML private TextField contractNumberField;
    @FXML private ComboBox<String> employeeComboBox;
    @FXML private ComboBox<String> contractTypeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField salaryField;
    @FXML private TextField allowancesField;
    @FXML private DatePicker signedDatePicker;
    @FXML private TextArea benefitsArea;
    @FXML private TextArea notesArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private ContractService contractService;
    private EmployeeService employeeService;
    private Contract contract; // null for new contract, existing contract for edit
    private Runnable onSaveCallback; // Callback to refresh parent controller

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contractService = new ContractService();
        employeeService = new EmployeeService();

        initializeComboBoxes();
        setupValidation();
        loadEmployeesForComboBox();
        setupEventHandlers(); // Setup event handlers

        // Generate contract number for new contract
        contractNumberField.setText(contractService.generateContractNumber());
    }

    private void initializeComboBoxes() {
        contractTypeComboBox.getItems().addAll(
                "Hợp đồng không xác định thời hạn",
                "Hợp đồng xác định thời hạn 1 năm",
                "Hợp đồng xác định thời hạn 2 năm",
                "Hợp đồng thời vụ",
                "Hợp đồng thử việc"
        );
    }

    // Setup event handlers for auto-generation
    private void setupEventHandlers() {
        // Auto-generate contract number when contract type changes
        contractTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && contract == null) { // Only for new contracts
                String newContractNumber = contractService.generateContractNumber(newValue);
                contractNumberField.setText(newContractNumber);
            }
            validateForm();
        });

        // Auto-generate end date when start date or contract type changes
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateEndDateBasedOnContractType();
            }
            validateForm();
        });
    }

    // Update end date based on contract type and start date
    private void updateEndDateBasedOnContractType() {
        String contractType = contractTypeComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();

        if (contractType != null && startDate != null) {
            LocalDate endDate = null;

            switch (contractType) {
                case "Hợp đồng xác định thời hạn 1 năm":
                    endDate = startDate.plusYears(1).minusDays(1); // 1 năm - 1 ngày
                    break;
                case "Hợp đồng xác định thời hạn 2 năm":
                    endDate = startDate.plusYears(2).minusDays(1); // 2 năm - 1 ngày
                    break;
                case "Hợp đồng thử việc":
                    endDate = startDate.plusMonths(2).minusDays(1); // 2 tháng - 1 ngày (thông thường thử việc 2 tháng)
                    break;
                case "Hợp đồng thời vụ":
                    endDate = startDate.plusMonths(6).minusDays(1); // 6 tháng - 1 ngày (có thể điều chỉnh theo nhu cầu)
                    break;
                case "Hợp đồng không xác định thời hạn":
                    // Không có ngày kết thúc
                    endDate = null;
                    break;
            }

            endDatePicker.setValue(endDate);
        }
    }

    private void setupValidation() {
        // Enable/disable save button based on required fields
        saveButton.setDisable(true);

        // Add validation listeners
        contractNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
        });

        employeeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
        });

//        contractTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
//            validateForm();
//        });
//
//        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
//            validateForm();
//        });
    }

    private void validateForm() {
        boolean isValid = !contractNumberField.getText().trim().isEmpty() &&
                employeeComboBox.getValue() != null &&
                contractTypeComboBox.getValue() != null &&
                startDatePicker.getValue() != null;
        saveButton.setDisable(!isValid);
    }

    private void loadEmployeesForComboBox() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            employeeComboBox.getItems().clear();

            for (Employee employee : employees) {
                String fullName = employee.getFullName();
                employeeComboBox.getItems().add(fullName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            employeeComboBox.getItems().addAll("Không thể tải danh sách nhân viên");
        }
    }

    public void setContract(Contract contract) {
        this.contract = contract;
        if (contract != null) {
            populateFields(contract);
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    private void populateFields(Contract contract) {
        contractNumberField.setText(contract.getContractNumber());
        employeeComboBox.setValue(contract.getEmployeeName());
        contractTypeComboBox.setValue(contract.getContractTypeName());
        startDatePicker.setValue(contract.getStartDate());
        endDatePicker.setValue(contract.getEndDate());
        if (contract.getSalary() != null) {
            salaryField.setText(contract.getSalary().toString());
        }
        if (contract.getAllowances() != null) {
            allowancesField.setText(contract.getAllowances().toString());
        }
//        signedDatePicker.setValue(contract.getSignedDate());
//        benefitsArea.setText(contract.getBenefits());
        notesArea.setText(contract.getNotes());
    }

    @FXML
    private void handleSave() {
        try {
            Contract contractToSave = contract != null ? contract : new Contract();

            contractToSave.setContractNumber(contractNumberField.getText().trim());

            // Get employee ID from name
            String selectedEmployeeName = employeeComboBox.getValue();
            int employeeId = getEmployeeIdByName(selectedEmployeeName);
            contractToSave.setEmployeeId(employeeId);
            contractToSave.setEmployeeName(selectedEmployeeName);

            // Get contract type ID from name
            String selectedContractType = contractTypeComboBox.getValue();
            int contractTypeId = getContractTypeIdByName(selectedContractType);
            contractToSave.setContractTypeId(contractTypeId);
            contractToSave.setContractTypeName(selectedContractType);

            contractToSave.setStartDate(startDatePicker.getValue());
            contractToSave.setEndDate(endDatePicker.getValue());

            if (!salaryField.getText().trim().isEmpty()) {
                contractToSave.setSalary(new BigDecimal(salaryField.getText().trim()));
            }
            if (!allowancesField.getText().trim().isEmpty()) {
                contractToSave.setAllowances(new BigDecimal(allowancesField.getText().trim()));
            }

//            contractToSave.setSignedDate(signedDatePicker.getValue());
//            contractToSave.setBenefits(benefitsArea.getText().trim());
            contractToSave.setNotes(notesArea.getText().trim());

            // Set created_by (you should get this from current user session)
            contractToSave.setCreatedBy(1); // Placeholder - replace with actual user ID

            boolean success;
            if (contract == null) {
                success = contractService.addContract(contractToSave);
                if (success) {
                    showSuccessMessage("Tạo hợp đồng thành công!");
                }
            } else {
                success = contractService.updateContract(contractToSave);
                if (success) {
                    showSuccessMessage("Cập nhật hợp đồng thành công!");
                }
            }

            if (success) {
                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }
                closeWindow();
            } else {
                showErrorMessage("Có lỗi xảy ra khi lưu hợp đồng!");
            }

        } catch (NumberFormatException e) {
            showErrorMessage("Vui lòng nhập số hợp lệ cho lương và phụ cấp!");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Có lỗi xảy ra khi lưu hợp đồng!");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private int getEmployeeIdByName(String employeeName) {
        try {
            List<Employee> employees = employeeService.getAllEmployees();

            for (Employee employee : employees) {
                String fullName = employee.getFullName();
                if (fullName.equals(employeeName)) {
                    return employee.getId();
                }
            }

            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int getContractTypeIdByName(String contractTypeName) {
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
}