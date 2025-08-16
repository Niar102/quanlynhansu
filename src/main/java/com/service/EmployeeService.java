package com.service;

import com.model.Employee;
import com.utils.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeService {

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();

        String sql = "SELECT e.*, d.department_name, p.position_name " +
                "FROM employees e " +
                "LEFT JOIN departments d ON e.department_id = d.id " +
                "LEFT JOIN positions p ON e.position_id = p.id " +
                "WHERE e.is_deleted = 0";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Employee employee = new Employee();

                employee.setId(rs.getInt("id"));
                employee.setFirstName(rs.getString("first_name"));
                employee.setLastName(rs.getString("last_name"));
                employee.setEmail(rs.getString("email"));
                employee.setPhone(rs.getString("phone"));
                employee.setCitizenId(rs.getString("citizen_id"));

                Date dobSql = rs.getDate("date_of_birth");
                if (dobSql != null) {
                    employee.setDateOfBirth(dobSql.toLocalDate());
                }

                employee.setGender(rs.getString("gender"));
                employee.setDepartmentId(rs.getInt("department_id"));
                employee.setPositionId(rs.getInt("position_id"));
                employee.setRoleId(rs.getInt("role_id"));
                employee.setManagerId(rs.getInt("manager_id"));

                Date hireDateSql = rs.getDate("hire_date");
                if (hireDateSql != null) {
                    employee.setHireDate(hireDateSql.toLocalDate());
                }

                employee.setEmploymentStatus(rs.getString("employment_status"));
                employee.setEmergencyContactName(rs.getString("emergency_contact_name"));
                employee.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
                employee.setEmergencyContactRelationship(rs.getString("emergency_contact_relationship"));
                employee.setNotes(rs.getString("notes"));

                employee.setDepartmentName(rs.getString("department_name"));
                employee.setPositionName(rs.getString("position_name"));

                employees.add(employee);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employees;
    }

    public List<Employee> getFilteredEmployees(String departmentName, String positionName) {
        List<Employee> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT e.*, d.department_name, p.position_name
            FROM employees e
            LEFT JOIN departments d ON e.department_id = d.id
            LEFT JOIN positions p ON e.position_id = p.id
            WHERE e.is_deleted = 0
        """);

        if (departmentName != null && !departmentName.equals("Tất cả")) {
            sql.append(" AND d.department_name = ?");
        }
        if (positionName != null && !positionName.equals("Tất cả")) {
            sql.append(" AND p.position_name = ?");
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (departmentName != null && !departmentName.equals("Tất cả")) {
                ps.setString(index++, departmentName);
            }
            if (positionName != null && !positionName.equals("Tất cả")) {
                ps.setString(index++, positionName);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Employee employee = new Employee();
                employee.setId(rs.getInt("id"));
                employee.setFirstName(rs.getString("first_name"));
                employee.setLastName(rs.getString("last_name"));
                employee.setEmail(rs.getString("email"));
                employee.setPhone(rs.getString("phone"));
                employee.setGender(rs.getString("gender"));
                employee.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                employee.setHireDate(rs.getDate("hire_date").toLocalDate());
                employee.setEmploymentStatus(rs.getString("employment_status"));
                employee.setDepartmentName(rs.getString("department_name"));
                employee.setPositionName(rs.getString("position_name"));
                list.add(employee);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Employee> searchEmployee(String keyword) {
        List<Employee> list = new ArrayList<>();

        String sql = """
            SELECT e.*, d.department_name, p.position_name
            FROM employees e
            LEFT JOIN departments d ON e.department_id = d.id
            LEFT JOIN positions p ON e.position_id = p.id
            WHERE e.is_deleted = 0
              AND (
                   e.id LIKE ?
                OR e.first_name LIKE ?
                OR e.last_name LIKE ?
                OR CONCAT(e.first_name, ' ', e.last_name) LIKE ?
                OR e.phone LIKE ?
                OR e.email LIKE ?
              )
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setString(6, like);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Employee employee = new Employee();
                employee.setId(rs.getInt("id"));
                employee.setFirstName(rs.getString("first_name"));
                employee.setLastName(rs.getString("last_name"));
                employee.setEmail(rs.getString("email"));
                employee.setPhone(rs.getString("phone"));
                employee.setGender(rs.getString("gender"));
                employee.setEmploymentStatus(rs.getString("employment_status"));
                employee.setHireDate(rs.getDate("hire_date") != null ? rs.getDate("hire_date").toLocalDate() : null);
                employee.setDepartmentName(rs.getString("department_name"));
                employee.setPositionName(rs.getString("position_name"));
                list.add(employee);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Map<String, Integer> getEmployeeStats() {
        Map<String, Integer> stats = new HashMap<>();

        String totalSql = "SELECT COUNT(*) FROM employees WHERE is_deleted = 0";
        String workingSql = "SELECT COUNT(*) FROM employees WHERE employment_status = 'Đang làm việc' AND is_deleted = 0";
        String inactiveSql = "SELECT COUNT(*) FROM employees WHERE employment_status IN ('Đã nghỉ việc') AND is_deleted = 0";

        try (Connection conn = DBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(totalSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stats.put("total", rs.getInt(1));
            }
            try (PreparedStatement ps = conn.prepareStatement(workingSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stats.put("working", rs.getInt(1));
            }
            try (PreparedStatement ps = conn.prepareStatement(inactiveSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stats.put("inactive", rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stats;
    }

    public int getTotalEmployeesCount() {
        String sql = "SELECT COUNT(*) FROM employees";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean addEmployee(Employee emp) {
        String insertSql = """
        INSERT INTO employees (
            first_name, last_name, email, phone, citizen_id,
            date_of_birth, gender, hire_date, employment_status,
            emergency_contact_name, emergency_contact_phone,
            emergency_contact_relationship, department_id, position_id
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            ps.setString(1, emp.getFirstName());
            ps.setString(2, emp.getLastName());
            ps.setString(3, emp.getEmail());
            ps.setString(4, emp.getPhone());
            ps.setString(5, emp.getCitizenId());
            ps.setDate(6, java.sql.Date.valueOf(emp.getDateOfBirth()));
            ps.setString(7, emp.getGender());
            ps.setDate(8, java.sql.Date.valueOf(emp.getHireDate()));
            ps.setString(9, emp.getEmploymentStatus());
            ps.setString(10, emp.getEmergencyContactName());
            ps.setString(11, emp.getEmergencyContactPhone());
            ps.setString(12, emp.getEmergencyContactRelationship());
            ps.setInt(13, emp.getDepartmentId());
            ps.setInt(14, emp.getPositionId());

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLIntegrityConstraintViolationException ex) {
            System.err.println("❌ Lỗi trùng email hoặc CCCD: " + ex.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi thêm nhân viên: " + e.getMessage());
        }

        return false;
    }

    public void deleteEmployee(int id) {
        String sql = "UPDATE employees SET is_deleted = 1 WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateEmployee(Employee employee) {
        String sql = "UPDATE employees SET first_name=?, last_name=?, email=?, phone=?, citizen_id=?, date_of_birth=?, gender=?, department_id=?, position_id=?, hire_date=?, employment_status=?, emergency_contact_name=?, emergency_contact_phone=?, emergency_contact_relationship=?, notes=? WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setString(3, employee.getEmail());
            stmt.setString(4, employee.getPhone());
            stmt.setString(5, employee.getCitizenId());
            stmt.setDate(6, Date.valueOf(employee.getDateOfBirth()));
            stmt.setString(7, employee.getGender());
            stmt.setInt(8, employee.getDepartmentId());
            stmt.setInt(9, employee.getPositionId());
            stmt.setDate(10, Date.valueOf(employee.getHireDate()));
            stmt.setString(11, employee.getEmploymentStatus());
            stmt.setString(12, employee.getEmergencyContactName());
            stmt.setString(13, employee.getEmergencyContactPhone());
            stmt.setString(14, employee.getEmergencyContactRelationship());
            stmt.setString(15, employee.getNotes());
            stmt.setInt(16, employee.getId());

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isEmailExists(String email, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM employees WHERE email = ?";
        if (excludeId != null) sql += " AND id <> ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            if (excludeId != null) ps.setInt(2, excludeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isPhoneExists(String phone, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM employees WHERE phone = ?";
        if (excludeId != null) sql += " AND id <> ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            if (excludeId != null) ps.setInt(2, excludeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Employee> getEmployeesByDepartment(int departmentId) {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE department_id = ? AND is_deleted = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Employee emp = new Employee();
                emp.setId(rs.getInt("id"));
                emp.setFirstName(rs.getString("first_name"));
                emp.setLastName(rs.getString("last_name"));
                emp.setEmail(rs.getString("email"));
                emp.setPhone(rs.getString("phone"));
                emp.setGender(rs.getString("gender"));
                emp.setEmploymentStatus(rs.getString("employment_status"));
                employees.add(emp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public List<Employee> getEmployeesByDepartmentForManager(int departmentId) {
        List<Employee> employees = new ArrayList<>();
        String sql = """
            SELECT e.*, d.department_name, p.position_name
            FROM employees e
            LEFT JOIN departments d ON e.department_id = d.id
            LEFT JOIN positions p ON e.position_id = p.id
            WHERE e.department_id = ? 
              AND e.is_deleted = 0 
              AND e.employment_status = 'Đang làm việc'
            ORDER BY e.first_name, e.last_name
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee emp = new Employee();
                emp.setId(rs.getInt("id"));
                emp.setFirstName(rs.getString("first_name"));
                emp.setLastName(rs.getString("last_name"));
                emp.setEmail(rs.getString("email"));
                emp.setPhone(rs.getString("phone"));
                emp.setGender(rs.getString("gender"));
                emp.setEmploymentStatus(rs.getString("employment_status"));
                emp.setDepartmentId(rs.getInt("department_id"));
                emp.setPositionId(rs.getInt("position_id"));
                emp.setRoleId(rs.getInt("role_id"));
                emp.setDepartmentName(rs.getString("department_name"));
                emp.setPositionName(rs.getString("position_name"));

                Date dobSql = rs.getDate("date_of_birth");
                if (dobSql != null) emp.setDateOfBirth(dobSql.toLocalDate());

                Date hireDateSql = rs.getDate("hire_date");
                if (hireDateSql != null) emp.setHireDate(hireDateSql.toLocalDate());

                employees.add(emp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public List<Employee> getEmployeesNotInDepartment(int departmentId) {
        List<Employee> employees = new ArrayList<>();
        String sql = """
            SELECT e.*, d.department_name, p.position_name
            FROM employees e
            LEFT JOIN departments d ON e.department_id = d.id
            LEFT JOIN positions p ON e.position_id = p.id
            WHERE e.is_deleted = 0 
              AND e.employment_status = 'Đang làm việc'
              AND (e.department_id IS NULL OR e.department_id != ?)
            ORDER BY e.first_name, e.last_name
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee emp = new Employee();
                emp.setId(rs.getInt("id"));
                emp.setFirstName(rs.getString("first_name"));
                emp.setLastName(rs.getString("last_name"));
                emp.setEmail(rs.getString("email"));
                emp.setPhone(rs.getString("phone"));
                emp.setGender(rs.getString("gender"));
                emp.setEmploymentStatus(rs.getString("employment_status"));
                emp.setDepartmentId(rs.getInt("department_id"));
                emp.setPositionId(rs.getInt("position_id"));
                emp.setRoleId(rs.getInt("role_id"));
                emp.setDepartmentName(rs.getString("department_name"));
                emp.setPositionName(rs.getString("position_name"));

                Date dobSql = rs.getDate("date_of_birth");
                if (dobSql != null) emp.setDateOfBirth(dobSql.toLocalDate());

                Date hireDateSql = rs.getDate("hire_date");
                if (hireDateSql != null) emp.setHireDate(hireDateSql.toLocalDate());

                employees.add(emp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public boolean updateEmployeeDepartment(int employeeId, int newDepartmentId) {
        String sql = "UPDATE employees SET department_id = ? WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newDepartmentId);
            stmt.setInt(2, employeeId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ======================= MANAGER / POSITION SYNC =======================

    /**
     * Promote employeeId thành Trưởng phòng của departmentId:
     * - Demote tất cả nhân viên đang giữ bất kỳ position level=4 trong phòng (trừ employeeId).
     * - Set position_id của employeeId về 1 position level=4 (tạo nếu chưa có).
     * - Đồng bộ departments.manager_id.
     */
    public boolean promoteToManager(int employeeId, int departmentId) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            Integer managerPosId = getOrCreateManagerPositionId(conn, departmentId);
            Integer defaultPosId = getOrCreateDefaultPositionId(conn, departmentId);

            // 1) Demote tất cả người đang giữ level=4 (dù position_id khác nhau)
            String demoteSql = """
                UPDATE employees e
                JOIN positions p ON e.position_id = p.id
                   AND p.department_id = ?
                   AND p.level = 4
                   AND p.is_deleted = 0
                SET e.position_id = ?
                WHERE e.department_id = ?
                  AND e.is_deleted = 0
                  AND e.id <> ?
            """;
            try (PreparedStatement st = conn.prepareStatement(demoteSql)) {
                st.setInt(1, departmentId);
                st.setInt(2, defaultPosId);
                st.setInt(3, departmentId);
                st.setInt(4, employeeId);
                st.executeUpdate();
            }

            // 2) Set employeeId -> managerPosId (đảm bảo đúng phòng)
            try (PreparedStatement st = conn.prepareStatement(
                    "UPDATE employees SET department_id=?, position_id=? WHERE id=? AND is_deleted=0")) {
                st.setInt(1, departmentId);
                st.setInt(2, managerPosId);
                st.setInt(3, employeeId);
                int a = st.executeUpdate();
                if (a == 0) { conn.rollback(); conn.setAutoCommit(true); return false; }
            }

            // 3) Update departments.manager_id
            try (PreparedStatement st = conn.prepareStatement(
                    "UPDATE departments SET manager_id=? WHERE id=?")) {
                st.setInt(1, employeeId);
                st.setInt(2, departmentId);
                st.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Demote tất cả nhân viên đang giữ bất kỳ position level=4 của phòng ban.
     * exceptEmployeeId != null thì bỏ qua người đó.
     */
    public boolean demoteManagersOfDepartment(int departmentId, Integer exceptEmployeeId) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            Integer defaultPosId = getOrCreateDefaultPositionId(conn, departmentId);

            StringBuilder sb = new StringBuilder("""
                UPDATE employees e
                JOIN positions p ON e.position_id = p.id
                   AND p.department_id = ?
                   AND p.level = 4
                   AND p.is_deleted = 0
                SET e.position_id = ?
                WHERE e.department_id = ?
                  AND e.is_deleted = 0
            """);
            if (exceptEmployeeId != null) {
                sb.append("  AND e.id <> ?");
            }

            try (PreparedStatement st = conn.prepareStatement(sb.toString())) {
                st.setInt(1, departmentId);
                st.setInt(2, defaultPosId);
                st.setInt(3, departmentId);
                if (exceptEmployeeId != null) st.setInt(4, exceptEmployeeId);
                st.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ======================= Helpers ======================= */

    /** Lấy id position level=4 của phòng ban, nếu chưa có thì tạo mới. */
    private Integer getOrCreateManagerPositionId(Connection conn, int departmentId) throws SQLException {
        Integer id = getManagerPositionId(conn, departmentId);
        if (id != null) return id;

        String code = "MANAGER_" + departmentId;
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO positions (position_code, position_name, department_id, level, is_deleted) " +
                        "VALUES (?, 'Trưởng phòng', ?, 4, 0)", Statement.RETURN_GENERATED_KEYS)) {
            ins.setString(1, code);
            ins.setInt(2, departmentId);
            ins.executeUpdate();
            try (ResultSet keys = ins.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return null;
    }

    /** Lấy id của một position level=4 (nếu có) trong phòng ban, không tạo mới. */
    private Integer getManagerPositionId(Connection conn, int departmentId) throws SQLException {
        String sql = """
            SELECT id
            FROM positions
            WHERE department_id = ? AND level = 4 AND is_deleted = 0
            ORDER BY id ASC
            LIMIT 1
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    /** Lấy id position mặc định (level < 4) trong phòng ban; nếu chưa có thì tạo "Nhân viên" level=1. */
    private Integer getOrCreateDefaultPositionId(Connection conn, int departmentId) throws SQLException {
        Integer id = null;
        String sql = """
        SELECT id
        FROM positions
        WHERE department_id=? AND is_deleted=0 AND level < 4
        ORDER BY level ASC,
                 CASE WHEN position_name LIKE '%Nhân viên%' OR position_name LIKE '%Engineer%' OR position_name LIKE '%Chuyên viên%' THEN 0 ELSE 1 END,
                 id ASC
        LIMIT 1
    """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) id = rs.getInt(1);
            }
        }
        if (id != null) return id;

        String code = "NV_" + departmentId;
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO positions (position_code, position_name, department_id, level, is_deleted) " +
                        "VALUES (?, 'Nhân viên', ?, 1, 0)", Statement.RETURN_GENERATED_KEYS)) {
            ins.setString(1, code);
            ins.setInt(2, departmentId);
            ins.executeUpdate();
            try (ResultSet keys = ins.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return null;
    }

    public Employee getEmployeeById(int id) {
        String sql = "SELECT e.*, d.department_name, p.position_name " +
                "FROM employees e " +
                "LEFT JOIN departments d ON e.department_id = d.id " +
                "LEFT JOIN positions p ON e.position_id = p.id " +
                "WHERE e.is_deleted = 0 AND e.id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Employee emp = new Employee();
                    emp.setId(rs.getInt("id"));
                    emp.setFirstName(rs.getString("first_name"));
                    emp.setLastName(rs.getString("last_name"));
                    emp.setEmail(rs.getString("email"));
                    emp.setPhone(rs.getString("phone"));
                    emp.setCitizenId(rs.getString("citizen_id"));

                    Date dobSql = rs.getDate("date_of_birth");
                    if (dobSql != null) emp.setDateOfBirth(dobSql.toLocalDate());

                    emp.setGender(rs.getString("gender"));
                    emp.setDepartmentId(rs.getInt("department_id"));
                    emp.setPositionId(rs.getInt("position_id"));

                    Date hireSql = rs.getDate("hire_date");
                    if (hireSql != null) emp.setHireDate(hireSql.toLocalDate());

                    emp.setEmploymentStatus(rs.getString("employment_status"));
                    emp.setEmergencyContactName(rs.getString("emergency_contact_name"));
                    emp.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
                    emp.setEmergencyContactRelationship(rs.getString("emergency_contact_relationship"));

                    emp.setDepartmentName(rs.getString("department_name"));
                    emp.setPositionName(rs.getString("position_name"));

                    return emp;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}