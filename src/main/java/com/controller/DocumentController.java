package com.controller;

import com.model.Document;
import com.service.DocumentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;


import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DocumentController implements Initializable {

    @FXML private TableView<Document> documentTable;
    @FXML private TableColumn<Document, Integer> colId;
    @FXML private TableColumn<Document, String> colTitle;
    @FXML private TableColumn<Document, String> colFileName;
    @FXML private TableColumn<Document, String> colFileSize;
    @FXML private TableColumn<Document, String> colCategory;
    @FXML private TableColumn<Document, String> colLastUpdated;
    @FXML private TableColumn<Document, Void> actionColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Button clearDateFilterBtn;
    @FXML private Button btnAddDocument;

    @FXML private Label lblTotal;

    private final DocumentService documentService = new DocumentService();
    private ObservableList<Document> documentList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadDocumentData();
        loadCategoryFilter();
        setupSearchAndFilter();
        setupDateFilters();
        setupActionColumn();
        updateStatistics();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colFileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        
        // Format file size
        colFileSize.setCellValueFactory(cellData -> {
            byte[] fileData = cellData.getValue().getFileData();
            if (fileData != null) {
                return new SimpleStringProperty(formatFileSize(fileData.length));
            }
            return new SimpleStringProperty("N/A");
        });
        
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        // Format last updated date
        colLastUpdated.setCellValueFactory(cellData -> {
            LocalDateTime lastUpdated = cellData.getValue().getLastUpdated();
            if (lastUpdated != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(lastUpdated.format(formatter));
            }
            return new SimpleStringProperty("");
        });

        // Set column widths and visibility
        colId.setVisible(false);  // Ẩn cột ID
        colFileSize.setVisible(false);  // Ẩn cột kích thước
        
        colTitle.setPrefWidth(250);  // Tăng width cho tiêu đề
        colFileName.setPrefWidth(200);  // Tăng width cho tên file
        colCategory.setPrefWidth(150);  // Tăng width cho danh mục
        colLastUpdated.setPrefWidth(180);  // Tăng width cho ngày cập nhật
        actionColumn.setPrefWidth(150);
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(new Callback<TableColumn<Document, Void>, TableCell<Document, Void>>() {
            @Override
            public TableCell<Document, Void> call(final TableColumn<Document, Void> param) {
                return new TableCell<>() {
                    private final Button btnDownload = new Button("Tải về");
                    private final Button btnEdit = new Button("Sửa");
                    private final Button btnDelete = new Button("Xóa");
                    private final HBox hBox = new HBox(btnDownload, btnEdit, btnDelete);

                    {
                        btnDownload.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                        btnEdit.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                        btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");

                        hBox.setSpacing(5);
                        hBox.setAlignment(Pos.CENTER);

                        btnDownload.setOnAction(event -> {
                            Document document = getTableView().getItems().get(getIndex());
                            handleDownload(document);
                        });

                        btnEdit.setOnAction(event -> {
                            Document document = getTableView().getItems().get(getIndex());
                            handleEdit(document);
                        });

                        btnDelete.setOnAction(event -> {
                            Document document = getTableView().getItems().get(getIndex());
                            handleDelete(document);
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

    private void setupSearchAndFilter() {
        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterDocuments();
        });

        // Filter functionality
        categoryFilter.setOnAction(e -> filterDocuments());
    }
    
    private void setupDateFilters() {
        // Date filter functionality
        fromDatePicker.setOnAction(e -> filterDocuments());
        toDatePicker.setOnAction(e -> filterDocuments());
        
        // Clear filter button
        clearDateFilterBtn.setOnAction(e -> clearDateFilter());
    }

    private void loadDocumentData() {
        List<Document> documents = documentService.getAllDocuments();
        documentList.clear();
        documentList.addAll(documents);
        documentTable.setItems(documentList);
    }

    private void loadCategoryFilter() {
        List<String> categories = documentService.getAllCategories();
        categories.add(0, "Tất cả");
        categoryFilter.setItems(FXCollections.observableArrayList(categories));
        categoryFilter.setValue("Tất cả");
    }

    private void filterDocuments() {
        String keyword = searchField.getText();
        String selectedCategory = categoryFilter.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        
        List<Document> filteredDocuments = documentService.searchDocuments(keyword, selectedCategory, fromDate, toDate);
        documentList.clear();
        documentList.addAll(filteredDocuments);
        documentTable.setItems(documentList);
        updateStatistics();
    }

    private void updateStatistics() {
        lblTotal.setText(String.valueOf(documentList.size()));
    }

    @FXML
    private void handleAddDocument() {
        showAddDocumentDialog();
    }

    private void handleEdit(Document document) {
        showEditDocumentDialog(document);
    }

    private void handleDownload(Document document) {
        // Download document file
        byte[] fileData = documentService.getDocumentFile(document.getId());
        if (fileData != null && fileData.length > 0) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Lưu tài liệu");
            fileChooser.setInitialFileName(document.getFileName());
            
            File file = fileChooser.showSaveDialog(documentTable.getScene().getWindow());
            if (file != null) {
                try {
                    Files.write(file.toPath(), fileData);
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Tải xuống tài liệu thành công!");
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lưu tài liệu: " + e.getMessage());
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không tìm thấy dữ liệu tài liệu!");
        }
    }

    private void handleDelete(Document document) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn xóa tài liệu \"" + document.getTitle() + "\" không?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = documentService.deleteDocument(document.getId());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa tài liệu thành công!");
                loadDocumentData();
                updateStatistics();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa tài liệu!");
            }
        }
    }

    private void showAddDocumentDialog() {
        Dialog<Document> dialog = new Dialog<>();
        dialog.setTitle("Thêm tài liệu mới");
        dialog.setHeaderText("Vui lòng nhập thông tin tài liệu");

        // Set button types
        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 100, 10, 10)); // Giảm padding bên phải để có thêm không gian

        TextField titleField = new TextField();
        titleField.setPromptText("Tiêu đề tài liệu");
        Label fileLabel = new Label("Chưa chọn file");
        fileLabel.setWrapText(true); // Cho phép xuống dòng
        fileLabel.setMaxWidth(300); // Tăng width tối đa
        fileLabel.setPrefWidth(300);
        Button chooseFileBtn = new Button("Chọn file");
        Button removeFileBtn = new Button("Xóa");
        removeFileBtn.setVisible(false); // Ẩn ban đầu
        removeFileBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
        ComboBox<String> categoryCombo = new ComboBox<>();
        
        // Load categories
        List<String> categories = getAvailableCategories();
        categoryCombo.setItems(FXCollections.observableArrayList(categories));
        categoryCombo.setPromptText("Chọn danh mục");

        final File[] selectedFile = new File[1];

        // Tạo HBox để chứa button chọn file và xóa file
        HBox fileButtonBox = new HBox(10);
        fileButtonBox.getChildren().addAll(chooseFileBtn, removeFileBtn);
        
        grid.add(new Label("Tiêu đề:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("File:"), 0, 1);
        grid.add(fileButtonBox, 1, 1);
        grid.add(fileLabel, 1, 2);
        grid.add(new Label("Danh mục:"), 0, 3);
        grid.add(categoryCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Enable/Disable save button depending on whether fields are filled
        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Validation logic to enable/disable save button
        Runnable validateForm = () -> {
            boolean isValid = !titleField.getText().trim().isEmpty() 
                && selectedFile[0] != null 
                && categoryCombo.getValue() != null;
            saveButton.setDisable(!isValid);
        };

        // Chạy validation ban đầu
        validateForm.run();

        // Setup file chooser action AFTER validateForm is defined
        chooseFileBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Chọn tài liệu");
            
            File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                // Kiểm tra loại file
                if (!isValidFileType(file.getName())) {
                    showAlert(Alert.AlertType.ERROR, "Loại file không hỗ trợ", 
                        "Chỉ cho phép các loại file: PDF (.pdf), Text (.txt), Word (.doc, .docx), Excel (.xls, .xlsx)");
                    return;
                }
                
                // Kiểm tra kích thước file (700KB limit)
                long fileSizeInKB = file.length() / 1024;
                if (file.length() > 700 * 1024) {
                    showAlert(Alert.AlertType.WARNING, "File quá lớn", 
                        "Kích thước file: " + fileSizeInKB +
                                "KB\nKích thước tối đa cho phép: 700KB" +
                                "\nVui lòng chọn file khác .");
                    return;
                }
                
                // File hợp lệ
                selectedFile[0] = file;
                fileLabel.setText(file.getName() + " (" + formatFileSize(file.length()) + ")");
                removeFileBtn.setVisible(true); // Hiển thị button xóa
                validateForm.run(); // Kích hoạt validation sau khi chọn file
            }
        });

        // Setup remove file button action
        removeFileBtn.setOnAction(e -> {
            selectedFile[0] = null; // Reset file selection
            fileLabel.setText("Chưa chọn file"); // Reset label
            removeFileBtn.setVisible(false); // Ẩn button xóa
            validateForm.run(); // Chạy validation để disable nút Lưu
        });

        titleField.textProperty().addListener((observable, oldValue, newValue) -> validateForm.run());
        categoryCombo.valueProperty().addListener((observable, oldValue, newValue) -> validateForm.run());

        // Convert the result to a document when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType && selectedFile[0] != null) {
                try {
                    byte[] fileData = Files.readAllBytes(selectedFile[0].toPath());
                Document document = new Document();
                document.setTitle(titleField.getText().trim());
                    document.setFileName(selectedFile[0].getName());
                    document.setFileData(fileData);
                    document.setCategory(categoryCombo.getValue());
                    document.setLastUpdated(LocalDateTime.now());
                return document;
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể đọc file: " + e.getMessage());
                }
            }
            return null;
        });

        Optional<Document> result = dialog.showAndWait();
        result.ifPresent(document -> {
            String saveResult = documentService.addDocument(document);
            if ("SUCCESS".equals(saveResult)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm tài liệu thành công!");
                loadDocumentData();
                updateStatistics();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", saveResult);
            }
        });
    }

    private void showEditDocumentDialog(Document document) {
        Dialog<Document> dialog = new Dialog<>();
        dialog.setTitle("Sửa tài liệu");
        dialog.setHeaderText("Chỉnh sửa thông tin tài liệu");

        // Set button types
        ButtonType saveButtonType = new ButtonType("Cập nhật", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 100, 10, 10)); // Giảm padding bên phải để có thêm không gian

        TextField titleField = new TextField(document.getTitle());
        Label fileLabel = new Label(formatFileName(document.getFileName()));
        fileLabel.setWrapText(true); // Cho phép xuống dòng
        fileLabel.setMaxWidth(300); // Tăng width tối đa  
        fileLabel.setPrefWidth(300);
        
        // Khai báo tooltip ở scope method để có thể reuse
        Tooltip fileTooltip = new Tooltip(document.getFileName());
        fileLabel.setTooltip(fileTooltip);
        
        Button chooseFileBtn = new Button("Thay đổi file");
        ComboBox<String> categoryCombo = new ComboBox<>();
        
        // Load categories
        List<String> categories = getAvailableCategories();
        categoryCombo.setItems(FXCollections.observableArrayList(categories));
        categoryCombo.setValue(document.getCategory());

        final File[] selectedFile = new File[1];

        grid.add(new Label("Tiêu đề:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("File hiện tại:"), 0, 1);
        grid.add(fileLabel, 1, 1);
        grid.add(new Label("Thay đổi file:"), 0, 2);
        grid.add(chooseFileBtn, 1, 2);
        grid.add(new Label("Danh mục:"), 0, 3);
        grid.add(categoryCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Enable/Disable save button depending on whether fields are filled
        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        
        // Validation logic
        Runnable validateForm = () -> {
            boolean isValid = !titleField.getText().trim().isEmpty() && categoryCombo.getValue() != null;
            saveButton.setDisable(!isValid);
        };

        // Chạy validation ban đầu (cho edit dialog, chỉ cần title và category)
        validateForm.run();

        // Setup file chooser action AFTER validateForm is defined
        chooseFileBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Chọn file mới");
            
            File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                // Kiểm tra loại file
                if (!isValidFileType(file.getName())) {
                    showAlert(Alert.AlertType.ERROR, "Loại file không hỗ trợ", 
                        "Chỉ cho phép các loại file: PDF (.pdf), Text (.txt), Word (.doc, .docx), Excel (.xls, .xlsx)");
                    return;
                }
                
                // Kiểm tra kích thước file (700KB limit)
                long fileSizeInKB = file.length() / 1024;
                if (file.length() > 700 * 1024) {
                    showAlert(Alert.AlertType.WARNING, "File quá lớn", 
                        "Kích thước file: " + fileSizeInKB + "KB\nKích thước tối đa cho phép: 700KB\nVui lòng chọn file khác hoặc nén file.");
                    return;
                }
                
                // File hợp lệ
                selectedFile[0] = file;
                String displayText = formatFileName(file.getName()) + " (" + formatFileSize(file.length()) + ")";
                fileLabel.setText(displayText);
                
                // Cập nhật tooltip
                fileTooltip.setText(file.getName() + "\nKích thước: " + formatFileSize(file.length()));
                fileLabel.setTooltip(fileTooltip);
                
                validateForm.run(); // Kích hoạt validation sau khi chọn file (không bắt buộc cho edit)
            }
        });

        titleField.textProperty().addListener((observable, oldValue, newValue) -> validateForm.run());
        categoryCombo.valueProperty().addListener((observable, oldValue, newValue) -> validateForm.run());

        // Convert the result to a document when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    document.setTitle(titleField.getText().trim());
                    document.setCategory(categoryCombo.getValue());
                    
                    // If new file selected, update file data and name
                    if (selectedFile[0] != null) {
                        byte[] fileData = Files.readAllBytes(selectedFile[0].toPath());
                        document.setFileData(fileData);
                        document.setFileName(selectedFile[0].getName());
                    } else {
                        // Không có file mới, set fileData = null để báo hiệu không cập nhật file
                        document.setFileData(null);
                    }
                    
                    return document;
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể đọc file: " + e.getMessage());
                }
            }
            return null;
        });

        Optional<Document> result = dialog.showAndWait();
        result.ifPresent(updatedDocument -> {
            String updateResult = documentService.updateDocument(updatedDocument);
            if ("SUCCESS".equals(updateResult)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật tài liệu thành công!");
                loadDocumentData();
                updateStatistics();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", updateResult);
            }
        });
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Hover effects for add button
    @FXML
    private void onHoverAdd() {
        btnAddDocument.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
    }

    @FXML
    private void onExitAdd() {
        btnAddDocument.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
    }
    
    // Method để kiểm tra loại file hợp lệ
    private boolean isValidFileType(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        String lowerFileName = fileName.toLowerCase();
        String[] allowedExtensions = {".pdf", ".txt", ".doc", ".docx", ".xls", ".xlsx"};
        
        for (String ext : allowedExtensions) {
            if (lowerFileName.endsWith(ext)) {
                return true;
            }
        }
        
        return false;
    }
    
    // Method để lấy danh sách categories không bị trùng lặp
    private List<String> getAvailableCategories() {
        // Tạo LinkedHashSet để tránh duplicate và giữ thứ tự
        java.util.LinkedHashSet<String> categorySet = new java.util.LinkedHashSet<>();
        
        // Thêm categories từ database trước
        List<String> dbCategories = documentService.getAllCategories();
        categorySet.addAll(dbCategories);
        
        // Thêm categories mặc định (sẽ bỏ qua nếu đã tồn tại)
        categorySet.addAll(List.of("Policy", "HR", "Finance", "Training", "General"));
        
        // Chuyển về ArrayList để dùng trong ComboBox
        return new java.util.ArrayList<>(categorySet);
    }
    
    // Method để format tên file hiển thị (cắt ngắn nếu quá dài)
    private String formatFileName(String fileName) {
        if (fileName == null) return "";
        
        // Nếu tên file <= 40 ký tự, hiển thị nguyên văn
        if (fileName.length() <= 40) {
            return fileName;
        }
        
        // Nếu quá dài, cắt và hiển thị dạng: "beginning...end.ext"
        String name = fileName;
        String extension = "";
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            name = fileName.substring(0, lastDot);
            extension = fileName.substring(lastDot);
        }
        
        if (name.length() > 30) {
            return name.substring(0, 25) + "..." + name.substring(name.length() - 5) + extension;
        }
        
        return fileName;
    }
    
    @FXML
    private void clearDateFilter() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        filterDocuments();
    }
}