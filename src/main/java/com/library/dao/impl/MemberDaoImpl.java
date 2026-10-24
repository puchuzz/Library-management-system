package com.library.dao.impl;

import com.library.config.DBConnection;
import com.library.dao.MemberDao;
import com.library.exception.DataAccessException;
import com.library.exception.LibraryException;
import com.library.model.Member;
import com.library.model.MembershipType;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberDaoImpl implements MemberDao {
    private static final String SELECT =
            "SELECT member_id, full_name, email, phone, member_type, joined_on, active FROM members";

    @Override
    public Member save(Member member) {
        String sql = "INSERT INTO members (full_name, email, phone, member_type, joined_on, active) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, member.getFullName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setString(4, member.getMembershipType().name());
            ps.setDate(5, Date.valueOf(member.getJoinedOn()));
            ps.setBoolean(6, member.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    member.setId(keys.getInt(1));
                }
            }
            return member;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save member", e);
        }
    }

    @Override
    public Optional<Member> findById(Integer id) {
        return findOne(SELECT + " WHERE member_id = ?", id);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return findOne(SELECT + " WHERE email = ?", email);
    }

    @Override
    public List<Member> findAll() {
        return findMany(SELECT + " ORDER BY full_name");
    }

    @Override
    public boolean update(Member member) {
        String sql = "UPDATE members SET full_name = ?, email = ?, phone = ?, member_type = ?, active = ? "
                + "WHERE member_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, member.getFullName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setString(4, member.getMembershipType().name());
            ps.setBoolean(5, member.isActive());
            ps.setInt(6, member.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update member", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM members WHERE member_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new LibraryException("This member has borrow history and cannot be deleted; deactivate instead");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete member", e);
        }
    }

    private Optional<Member> findOne(String sql, Object... params) {
        List<Member> result = findMany(sql, params);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    private List<Member> findMany(String sql, Object... params) {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Member> members = new ArrayList<>();
                while (rs.next()) {
                    members.add(map(rs));
                }
                return members;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query members", e);
        }
    }

    private Member map(ResultSet rs) throws SQLException {
        return Member.create(
                MembershipType.valueOf(rs.getString("member_type")),
                rs.getInt("member_id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getDate("joined_on").toLocalDate(),
                rs.getBoolean("active"));
    }
}
