package com.controller;

import com.service.ContractService;
import com.service.DepartmentService;
import com.service.DocumentService;
import com.service.EmployeeService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class DashboardController {

    private MainController mainController;

    private final EmployeeService employeeService = new EmployeeService();
    private final DepartmentService departmentService = new DepartmentService();
    private final ContractService contractService = new ContractService();
    private final DocumentService documentService = new DocumentService();

    @FXML private Label totalEmployeesLabel;
    @FXML private Label totalDepartmentsLabel;
    @FXML private Label totalContractsLabel;
    @FXML private Label totalReportsLabel;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        refreshStats();
    }

    private void refreshStats() {
        int employees = employeeService.getTotalEmployeesCount();
        int departments = departmentService.getTotalDepartmentsCount();
        int contracts = contractService.getTotalContractsCount();
        int documents = documentService.getTotalDocumentsCount();

        if (totalEmployeesLabel != null) totalEmployeesLabel.setText(String.valueOf(employees));
        if (totalDepartmentsLabel != null) totalDepartmentsLabel.setText(String.valueOf(departments));
        if (totalContractsLabel != null) totalContractsLabel.setText(String.valueOf(contracts));
        if (totalReportsLabel != null) totalReportsLabel.setText(String.valueOf(documents));
    }

    @FXML
    public void openEmployees(MouseEvent event) {
        if (mainController != null) {
            mainController.navigateToPage("employees");
        }
    }

    @FXML
    public void openOrganization(MouseEvent event) {
        if (mainController != null) {
            mainController.navigateToPage("organization");
        }
    }

    @FXML
    public void openContracts(MouseEvent event) {
        if (mainController != null) {
            mainController.navigateToPage("contracts");
        }
    }

    @FXML
    public void openDocuments(MouseEvent event) {
        if (mainController != null) {
            mainController.navigateToPage("documents");
        }
    }

    @FXML
    public void openSettings(MouseEvent event) {
        if (mainController != null) {
            mainController.navigateToPage("settings");
        }
    }

    @FXML
    public void openStatistics(MouseEvent event) {
        if (mainController != null) {
            mainController.navigateToPage("dashboard");
        }
    }
}