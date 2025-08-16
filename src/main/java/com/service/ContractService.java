package com.service;


import com.model.Contract;
import com.utils.DBUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContractService {

    // Get all contracts with employee and contract type information
    public List<Contract> getAllContracts() {
        List<Contract> contracts = new ArrayList<>();
        String sql = """
            SELECT ec.*, CONCAT(e.first_name, ' ', e.last_name) as employee_name,
                      ct.type_name as contract_type_name, u.username as created_by_username
               FROM employment_contracts ec
               LEFT JOIN employees e ON ec.employee_id = e.id
               LEFT JOIN contract_types ct ON ec.contract_type_id = ct.id
               LEFT JOIN users u ON ec.created_by = u.id
               WHERE ec.is_deleted = 0
               ORDER BY ec.start_date DESC
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Contract contract = mapResultSetToContract(rs);
                contracts.add(contract);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return contracts;
    }

    // Get contract by ID
    public Contract getContractById(int id) {
        String sql = """
            SELECT ec.*, CONCAT(e.first_name, ' ', e.last_name) as employee_name,
                      ct.type_name as contract_type_name, u.username as created_by_username
               FROM employment_contracts ec
               LEFT JOIN employees e ON ec.employee_id = e.id
               LEFT JOIN contract_types ct ON ec.contract_type_id = ct.id
               LEFT JOIN users u ON ec.created_by = u.id
               WHERE ec.id = ? AND ec.is_deleted = 0
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToContract(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Search contracts with filters
    public List<Contract> searchContracts(String searchTerm, String contractType, String status, LocalDate fromDate) {
        StringBuilder sql = new StringBuilder("""
            SELECT ec.*, CONCAT(e.first_name, ' ', e.last_name) as employee_name,\s
                       ct.type_name as contract_type_name, u.username as created_by_username
                FROM employment_contracts ec
                LEFT JOIN employees e ON ec.employee_id = e.id
                LEFT JOIN contract_types ct ON ec.contract_type_id = ct.id
                LEFT JOIN users u ON ec.created_by = u.id
                WHERE ec.is_deleted = 0
            """);

        List<Object> parameters = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" AND (ec.contract_number LIKE ? OR CONCAT(e.first_name, ' ', e.last_name) LIKE ?)");
            parameters.add("%" + searchTerm + "%");
            parameters.add("%" + searchTerm + "%");
        }

        if (contractType != null && !contractType.isEmpty()) {
            sql.append(" AND ct.type_name = ?");
            parameters.add(contractType);
        }

        if (status != null && !status.isEmpty()) {
            if (status.equals("Đang hoạt động")) {
                sql.append(" AND (ec.end_date IS NULL OR ec.end_date > CURDATE())");
            } else if (status.equals("Đã hết hạn")) {
                sql.append(" AND ec.end_date IS NOT NULL AND ec.end_date < CURDATE()");
            }
            // status khác thì không lọc
        }


        if (fromDate != null) {
            sql.append(" AND ec.start_date >= ?");
            parameters.add(Date.valueOf(fromDate));
        }

        sql.append(" ORDER BY ec.start_date DESC");

        List<Contract> contracts = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                contracts.add(mapResultSetToContract(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return contracts;
    }

    // Add new contract
    public boolean addContract(Contract contract) {
        String sql = """
            INSERT INTO employment_contracts 
            (contract_number, employee_id, contract_type_id, start_date, end_date, 
             salary, allowances, benefits, terms_conditions, signed_date, notes, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, contract.getContractNumber());
            stmt.setInt(2, contract.getEmployeeId());
            stmt.setInt(3, contract.getContractTypeId());
            stmt.setDate(4, Date.valueOf(contract.getStartDate()));
            stmt.setDate(5, contract.getEndDate() != null ? Date.valueOf(contract.getEndDate()) : null);
            stmt.setBigDecimal(6, contract.getSalary());
            stmt.setBigDecimal(7, contract.getAllowances());
            stmt.setString(8, contract.getBenefits());
            stmt.setString(9, contract.getTermsConditions());
            stmt.setDate(10, contract.getSignedDate() != null ? Date.valueOf(contract.getSignedDate()) : null);
            stmt.setString(11, contract.getNotes());
            stmt.setInt(12, contract.getCreatedBy());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update contract
    public boolean updateContract(Contract contract) {
        String sql = """
            UPDATE employment_contracts 
            SET contract_number = ?, employee_id = ?, contract_type_id = ?, start_date = ?, 
                end_date = ?, salary = ?, allowances = ?, benefits = ?, terms_conditions = ?, 
                signed_date = ?, notes = ?
            WHERE id = ?
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, contract.getContractNumber());
            stmt.setInt(2, contract.getEmployeeId());
            stmt.setInt(3, contract.getContractTypeId());
            stmt.setDate(4, Date.valueOf(contract.getStartDate()));
            stmt.setDate(5, contract.getEndDate() != null ? Date.valueOf(contract.getEndDate()) : null);
            stmt.setBigDecimal(6, contract.getSalary());
            stmt.setBigDecimal(7, contract.getAllowances());
            stmt.setString(8, contract.getBenefits());
            stmt.setString(9, contract.getTermsConditions());
            stmt.setDate(10, contract.getSignedDate() != null ? Date.valueOf(contract.getSignedDate()) : null);
            stmt.setString(11, contract.getNotes());
            stmt.setInt(12, contract.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete contract
    public boolean deleteContract(int id) {
        String sql = "UPDATE employment_contracts SET is_deleted = 1 WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get contract statistics
    public Map<String, Integer> getContractStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = """
                SELECT
                     SUM(CASE WHEN (end_date IS NULL OR end_date > CURDATE()) AND is_deleted = 0 THEN 1 ELSE 0 END) as active_contracts,
                     SUM(CASE WHEN end_date IS NOT NULL AND end_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY) AND is_deleted = 0 THEN 1 ELSE 0 END) as expiring_soon,
                     SUM(CASE WHEN end_date IS NOT NULL AND end_date < CURDATE() AND is_deleted = 0 THEN 1 ELSE 0 END) as expired_contracts
                 FROM employment_contracts
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                stats.put("active", rs.getInt("active_contracts"));
                stats.put("expiring", rs.getInt("expiring_soon"));
                stats.put("expired", rs.getInt("expired_contracts"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    // Get contract types statistics
    public Map<String, Integer> getContractTypeStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = """
                SELECT ct.type_name, COUNT(ec.id) as count
                FROM contract_types ct
                LEFT JOIN employment_contracts ec ON ct.id = ec.contract_type_id
                     AND (ec.end_date IS NULL OR ec.end_date > CURDATE())
                     AND ec.is_deleted = 0
                GROUP BY ct.id, ct.type_name
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                stats.put(rs.getString("type_name"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Get total number of contracts.
     */
    public int getTotalContractsCount() {
        String sql = "SELECT COUNT(*) FROM employment_contracts WHERE is_deleted = 0";
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

    // Get contracts by employee ID
    public List<Contract> getContractsByEmployeeId(int employeeId) {
        List<Contract> contracts = new ArrayList<>();
        String sql = """
                SELECT ec.*, CONCAT(e.first_name, ' ', e.last_name) as employee_name,
                      ct.type_name as contract_type_name, u.username as created_by_username
               FROM employment_contracts ec
               LEFT JOIN employees e ON ec.employee_id = e.id
               LEFT JOIN contract_types ct ON ec.contract_type_id = ct.id
               LEFT JOIN users u ON ec.created_by = u.id
               WHERE ec.employee_id = ? AND ec.is_deleted = 0
               ORDER BY ec.start_date DESC
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                contracts.add(mapResultSetToContract(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return contracts;
    }

    // Generate next contract number
    public String generateContractNumber(String contractTypeName) {
        String prefix = getContractPrefix(contractTypeName);

        String sql = "SELECT contract_number FROM employment_contracts WHERE contract_number LIKE ? ORDER BY id DESC LIMIT 1";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String lastNumber = rs.getString("contract_number");
                // Extract number from format like "HD1Y-015"
                String[] parts = lastNumber.split("-");
                if (parts.length == 2) {
                    int nextNum = Integer.parseInt(parts[1]) + 1;
                    return String.format("%s-%03d", prefix, nextNum);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Default format if no previous contracts exist for this type
        return String.format("%s-001", prefix);
    }

    // THÊM MỚI: Get contract prefix based on contract type
    private String getContractPrefix(String contractTypeName) {
        if (contractTypeName == null) {
            return "HD"; // Default prefix
        }

        switch (contractTypeName) {
            case "Hợp đồng không xác định thời hạn":
                return "HDVT"; // Hợp đồng vô thời hạn
            case "Hợp đồng xác định thời hạn 1 năm":
                return "HD1Y"; // Hợp đồng 1 năm
            case "Hợp đồng xác định thời hạn 2 năm":
                return "HD2Y"; // Hợp đồng 2 năm
            case "Hợp đồng thời vụ":
                return "HDTV"; // Hợp đồng thời vụ
            case "Hợp đồng thử việc":
                return "HDTH"; // Hợp đồng thử việc
            default:
                return "HD"; // Default prefix
        }
    }

    // Generate next contract number (method cũ để tương thích)
    public String generateContractNumber() {
        return generateContractNumber(null);
    }

    // Helper method to map ResultSet to Contract object
    private Contract mapResultSetToContract(ResultSet rs) throws SQLException {
        Contract contract = new Contract();
        contract.setId(rs.getInt("id"));
        contract.setContractNumber(rs.getString("contract_number"));
        contract.setEmployeeId(rs.getInt("employee_id"));
        contract.setEmployeeName(rs.getString("employee_name"));
        contract.setContractTypeId(rs.getInt("contract_type_id"));
        contract.setContractTypeName(rs.getString("contract_type_name"));

        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            contract.setStartDate(startDate.toLocalDate());
        }

        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            contract.setEndDate(endDate.toLocalDate());
        }

        contract.setSalary(rs.getBigDecimal("salary"));
        contract.setAllowances(rs.getBigDecimal("allowances"));
        contract.setBenefits(rs.getString("benefits"));
        contract.setTermsConditions(rs.getString("terms_conditions"));

        Date signedDate = rs.getDate("signed_date");
        if (signedDate != null) {
            contract.setSignedDate(signedDate.toLocalDate());
        }

        contract.setNotes(rs.getString("notes"));
        contract.setCreatedBy(rs.getInt("created_by"));
        contract.setCreatedByUsername(rs.getString("created_by_username"));

        return contract;
    }
}