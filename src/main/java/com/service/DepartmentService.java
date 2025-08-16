package com.service;

import com.model.Department;
import com.utils.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepartmentService {

    public List<Department> getAllDepartments() {
        List<Department> departments = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM departments ORDER BY department_code";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Department dept = new Department();
                dept.setId(rs.getInt("id"));
                dept.setDepartmentCode(rs.getString("department_code"));
                dept.setDepartmentName(rs.getString("department_name"));
                dept.setParentId(rs.getObject("parent_id", Integer.class));
                dept.setManagerId(rs.getObject("manager_id", Integer.class));
                dept.setDescription(rs.getString("description"));
                dept.setAddress(rs.getString("address"));
                dept.setPhone(rs.getString("phone"));
                dept.setEmail(rs.getString("email"));
                departments.add(dept);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departments;
    }

    public boolean updateManagerId(int departmentId, int employeeId) {
        String updateSql = "UPDATE departments SET manager_id = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setInt(1, employeeId);
            updateStmt.setInt(2, departmentId);
            return updateStmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Department> getAllDepartmentsWithDetails() {
        List<Department> departments = new ArrayList<>();
        String sql = """
        SELECT d.*,
               CONCAT(e.first_name, ' ', e.last_name) as manager_name,
               (
                   SELECT COUNT(*) 
                   FROM employees emp 
                   WHERE emp.department_id = d.id 
                         AND emp.is_deleted = 0 
                         AND emp.employment_status IN ('Đang làm việc', 'Đã nghỉ việc')
               ) as employee_count
        FROM departments d 
        LEFT JOIN employees e ON d.manager_id = e.id 
        ORDER BY d.department_code
    """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Department dept = new Department();
                dept.setId(rs.getInt("id"));
                dept.setDepartmentCode(rs.getString("department_code"));
                dept.setDepartmentName(rs.getString("department_name"));
                dept.setParentId(rs.getObject("parent_id", Integer.class));
                dept.setManagerId(rs.getObject("manager_id", Integer.class));
                dept.setDescription(rs.getString("description"));
                dept.setAddress(rs.getString("address"));
                dept.setPhone(rs.getString("phone"));
                dept.setEmail(rs.getString("email"));

                // Thêm thông tin bổ sung
                dept.setManagerName(rs.getString("manager_name"));
                dept.setEmployeeCount(rs.getInt("employee_count"));

                departments.add(dept);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departments;
    }

    /**
     * Get total number of departments.
     */
    public int getTotalDepartmentsCount() {
        String sql = "SELECT COUNT(*) FROM departments";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<String> getAllDepartmentNames() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT department_name FROM departments ORDER BY department_name";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("department_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Department getDepartmentById(int id) {
        String sql = "SELECT * FROM departments WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Department dept = new Department();
                dept.setId(rs.getInt("id"));
                dept.setDepartmentCode(rs.getString("department_code"));
                dept.setDepartmentName(rs.getString("department_name"));
                dept.setParentId(rs.getObject("parent_id", Integer.class));
                dept.setManagerId(rs.getObject("manager_id", Integer.class));
                dept.setDescription(rs.getString("description"));
                dept.setAddress(rs.getString("address"));
                dept.setPhone(rs.getString("phone"));
                dept.setEmail(rs.getString("email"));
                return dept;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addDepartment(Department department) {
        String sql = """
            INSERT INTO departments (department_code, department_name, parent_id, manager_id, 
                                   description, address, phone, email) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, department.getDepartmentCode());
            stmt.setString(2, department.getDepartmentName());
            stmt.setObject(3, department.getParentId());
            stmt.setObject(4, department.getManagerId());
            stmt.setString(5, department.getDescription());
            stmt.setString(6, department.getAddress());
            stmt.setString(7, department.getPhone());
            stmt.setString(8, department.getEmail());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateDepartment(Department department) {
        String sql = """
            UPDATE departments SET department_code=?, department_name=?, parent_id=?, manager_id=?, 
                                 description=?, address=?, phone=?, email=? 
            WHERE id=?
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, department.getDepartmentCode());
            stmt.setString(2, department.getDepartmentName());
            stmt.setObject(3, department.getParentId());
            stmt.setObject(4, department.getManagerId());
            stmt.setString(5, department.getDescription());
            stmt.setString(6, department.getAddress());
            stmt.setString(7, department.getPhone());
            stmt.setString(8, department.getEmail());
            stmt.setInt(9, department.getId());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteDepartment(int id) {
        // Kiểm tra xem có nhân viên nào trong phòng ban này không
        String checkSql = "SELECT COUNT(*) FROM employees WHERE department_id = ?";
        String deleteSql = "DELETE FROM departments WHERE id = ?";

        try (Connection conn = DBUtil.getConnection()) {
            // Kiểm tra trước khi xóa
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Có nhân viên trong phòng ban, không thể xóa
                    return false;
                }
            }

            // Thực hiện xóa
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, id);
                int rows = deleteStmt.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Integer> getDepartmentStats() {
        Map<String, Integer> stats = new HashMap<>();

        String sql = """
            SELECT d.department_name, COUNT(e.id) as employee_count 
            FROM departments d 
            LEFT JOIN employees e ON d.id = e.department_id AND e.employment_status IN ('Đang làm việc', 'Đã nghỉ việc')
            GROUP BY d.id, d.department_name 
            ORDER BY d.department_name
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                stats.put(rs.getString("department_name"), rs.getInt("employee_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Lấy thống kê chi tiết nhân viên theo trạng thái cho mỗi phòng ban
     * Dùng để debug và kiểm tra dữ liệu
     */
    public Map<String, Map<String, Integer>> getDepartmentEmployeeStatusStats() {
        Map<String, Map<String, Integer>> stats = new HashMap<>();

        String sql = """
            SELECT d.department_name, 
                   e.employment_status,
                   COUNT(e.id) as count
            FROM departments d 
            LEFT JOIN employees e ON d.id = e.department_id
            GROUP BY d.department_name, e.employment_status
            ORDER BY d.department_name
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String deptName = rs.getString("department_name");
                String status = rs.getString("employment_status");
                int count = rs.getInt("count");

                stats.computeIfAbsent(deptName, k -> new HashMap<>())
                        .put(status != null ? status : "NULL", count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    public boolean isDepartmentCodeExists(String departmentCode, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM departments WHERE department_code = ?";
        if (excludeId != null) {
            sql += " AND id != ?";
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, departmentCode);
            if (excludeId != null) {
                stmt.setInt(2, excludeId);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Debug method để kiểm tra dữ liệu nhân viên và phòng ban
     */
    public void debugEmployeeData() {
        System.out.println("=== DEBUG: Kiểm tra dữ liệu nhân viên và phòng ban ===");

        // Kiểm tra tổng số nhân viên
        try (Connection conn = DBUtil.getConnection()) {
            String totalEmployeesSql = "SELECT COUNT(*) FROM employees";
            try (PreparedStatement stmt = conn.prepareStatement(totalEmployeesSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Tổng số nhân viên trong database: " + rs.getInt(1));
                }
            }

            // Kiểm tra nhân viên theo employment_status
            String statusSql = "SELECT employment_status, COUNT(*) FROM employees GROUP BY employment_status";
            try (PreparedStatement stmt = conn.prepareStatement(statusSql);
                 ResultSet rs = stmt.executeQuery()) {
                System.out.println("Nhân viên theo trạng thái:");
                while (rs.next()) {
                    System.out.println("  - " + rs.getString(1) + ": " + rs.getInt(2) + " nhân viên");
                }
            }

            // Kiểm tra nhân viên theo phòng ban
            String deptSql = """
                SELECT d.department_name, d.id as dept_id, 
                       COUNT(e.id) as total_employees,
                       COUNT(CASE WHEN e.employment_status = 'Đang làm việc' THEN 1 END) as active_employees
                FROM departments d 
                LEFT JOIN employees e ON d.id = e.department_id
                GROUP BY d.id, d.department_name 
                ORDER BY d.department_name
            """;
            try (PreparedStatement stmt = conn.prepareStatement(deptSql);
                 ResultSet rs = stmt.executeQuery()) {
                System.out.println("Nhân viên theo phòng ban:");
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("department_name") + " (ID: " + rs.getInt("dept_id") + "): "
                            + rs.getInt("total_employees") + " tổng, " + rs.getInt("active_employees") + " đang làm việc");
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi debug dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}