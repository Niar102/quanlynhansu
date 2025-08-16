// Department.java
package com.model;

public class Department {
    private int id;
    private String departmentCode;
    private String departmentName;
    private Integer parentId;
    private Integer managerId;
    private String description;
    private String address;
    private String phone;
    private String email;
    
    // Fields bổ sung để hiển thị
    private String managerName;
    private int employeeCount;

    @Override
    public String toString() {
        return departmentName;
    }

    public Department() {
    }

    public Department(int id, String departmentCode, String departmentName, Integer parentId, Integer managerId,
                      String description, String address, String phone, String email) {
        this.id = id;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.parentId = parentId;
        this.managerId = managerId;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    // Getter và setter cho fields bổ sung
    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }
}