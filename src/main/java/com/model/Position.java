package com.model;

public class Position {
    private int id;
    private String positionCode;
    private String positionName;
    private int departmentId;
    private int level;
    private String description;
    private String requirements;

    @Override
    public String toString() {
        return positionName;
    }

    public Position() {
    }

    public Position(int id, String positionCode, String positionName, int departmentId, int level,
                    String description, String requirements) {
        this.id = id;
        this.positionCode = positionCode;
        this.positionName = positionName;
        this.departmentId = departmentId;
        this.level = level;
        this.description = description;
        this.requirements = requirements;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPositionCode() {
        return positionCode;
    }

    public void setPositionCode(String positionCode) {
        this.positionCode = positionCode;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }
}