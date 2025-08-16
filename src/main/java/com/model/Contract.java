package com.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Contract {
    private int id;
    private String contractNumber;
    private int employeeId;
    private String employeeName; // For display purposes
    private int contractTypeId;
    private String contractTypeName; // For display purposes
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal salary;
    private BigDecimal allowances;
    private String benefits;
    private String termsConditions;
    //private String status;
    private LocalDate signedDate;
    private String notes;
    private int createdBy;
    private String createdByUsername; // THÊM MỚI: Tên người tạo hợp đồng
    private boolean isDeleted; // THÊM MỚI: Trạng thái xóa mềm


    // Constructors
    public Contract() {}

    public Contract(int id, String contractNumber, int employeeId, String employeeName,
                    int contractTypeId, String contractTypeName, LocalDate startDate,
                    LocalDate endDate, BigDecimal salary, BigDecimal allowances,
                    String benefits, String termsConditions,
                    //String status,
                    LocalDate signedDate, String notes, int createdBy) {
        this.id = id;
        this.contractNumber = contractNumber;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.contractTypeId = contractTypeId;
        this.contractTypeName = contractTypeName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.salary = salary;
        this.allowances = allowances;
        this.benefits = benefits;
        this.termsConditions = termsConditions;
        //this.status = status;
        this.signedDate = signedDate;
        this.notes = notes;
        this.createdBy = createdBy;
        this.isDeleted = false; // Mặc định không bị xóa
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public int getContractTypeId() {
        return contractTypeId;
    }

    public void setContractTypeId(int contractTypeId) {
        this.contractTypeId = contractTypeId;
    }

    public String getContractTypeName() {
        return contractTypeName;
    }

    public void setContractTypeName(String contractTypeName) {
        this.contractTypeName = contractTypeName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public BigDecimal getAllowances() {
        return allowances;
    }

    public void setAllowances(BigDecimal allowances) {
        this.allowances = allowances;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public String getTermsConditions() {
        return termsConditions;
    }

    public void setTermsConditions(String termsConditions) {
        this.termsConditions = termsConditions;
    }

//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }

    public String getStatus() {
        if (endDate == null || endDate.isAfter(LocalDate.now())) {
            return "Đang hoạt động";
        } else {
            return "Đã hết hạn";
        }
    }

    public LocalDate getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(LocalDate signedDate) {
        this.signedDate = signedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    // THÊM MỚI: Getter và Setter cho createdByUsername
    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    // THÊM MỚI: Getter và Setter cho isDeleted
    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    // Helper methods
    public boolean isExpiringSoon() {
        if (endDate == null) return false;
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return endDate.isBefore(thirtyDaysFromNow) && endDate.isAfter(LocalDate.now());
    }

    public boolean isExpired() {
        if (endDate == null) return false;
        return endDate.isBefore(LocalDate.now());
    }

    public String getFormattedSalary() {
        if (salary == null) return "0";
        return String.format("%,.0f VND", salary);
    }

    @Override
    public String toString() {
        return "Contract{" +
                "id=" + id +
                ", contractNumber='" + contractNumber + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", contractTypeName='" + contractTypeName + '\'' +
                ", status='" + getStatus() + '\'' +
                '}';
    }
}