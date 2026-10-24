package com.library.dao.impl;

import com.library.config.DBConnection;
import com.library.dao.StaffDao;
import com.library.exception.DataAccessException;
import com.library.exception.LibraryException;
import com.library.model.Role;
import com.library.model.Staff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StaffDaoImpl implements StaffDao {
    private static final String SELECT =
            "SELECT staff_id, username, password_hash, full_name, email, phone, role, active FROM staff";

    @Override
    public Staff save(Staff staff) {
        String sql = "INSERT INTO staff (username, password_hash, full_name, email, phone, role, active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, staff.getUsername());
            ps.setString(2, staff.getPasswordHash());
            ps.setString(3, staff.getFullName());
            ps.setString(4, staff.getEmail());
            ps.setString(5, staff.getPhone());
            ps.setString(6, staff.getRole().name());
            ps.setBoolean(7, staff.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    staff.setId(keys.getInt(1));
                }
            }
            return staff;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save staff member", e);
        }
    }

    @Override
    public Optional<Staff> findById(Integer id) {
        return findOne(SELECT + " WHERE staff_id = ?", id);
    }

    @Override
    public Optional<Staff> findByUsername(String username) {
        return findOne(SELECT + " WHERE username = ?", username);
    }

    @Override
    public List<Staff> findAll() {
        return findMany(SELECT + " ORDER BY username");
    }

    @Override
    public long count() {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM staff")) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count staff", e);
        }
    }

    @Override
    public boolean update(Staff staff) {
        String sql = "UPDATE staff SET password_hash = ?, full_name = ?, email = ?, phone = ?, "
                + "role = ?, active = ? WHERE staff_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, staff.getPasswordHash());
            ps.setString(2, staff.getFullName());
            ps.setString(3, staff.getEmail());
            ps.setString(4, staff.getPhone());
            ps.setString(5, staff.getRole().name());
            ps.setBoolean(6, staff.isActive());
            ps.setInt(7, staff.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update staff member", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM staff WHERE staff_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new LibraryException("This staff member has issued loans and cannot be deleted; deactivate instead");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete staff member", e);
        }
    }

    private Optional<Staff> findOne(String sql, Object... params) {
        List<Staff> result = findMany(sql, params);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    private List<Staff> findMany(String sql, Object... params) {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Staff> staff = new ArrayList<>();
                while (rs.next()) {
                    staff.add(map(rs));
                }
                return staff;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query staff", e);
        }
    }

    private Staff map(ResultSet rs) throws SQLException {
        return Staff.create(
                Role.valueOf(rs.getString("role")),
                rs.getInt("staff_id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getBoolean("active"));
    }
}
