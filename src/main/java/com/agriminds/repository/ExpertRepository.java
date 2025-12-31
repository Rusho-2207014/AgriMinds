package com.agriminds.repository;

import com.agriminds.model.Expert;
import com.agriminds.util.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExpertRepository {

    private static final Logger logger = LoggerFactory.getLogger(ExpertRepository.class);

    public Long save(Expert expert) {
        String sql = "INSERT INTO experts (full_name, email, phone_number, password_hash, " +
                "specialization, qualifications, years_of_experience, location, is_verified, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, expert.getFullName());
            stmt.setString(2, expert.getEmail());
            stmt.setString(3, expert.getPhoneNumber());
            stmt.setString(4, expert.getPasswordHash());
            stmt.setString(5, expert.getSpecialization());
            stmt.setString(6, expert.getQualifications());
            stmt.setInt(7, expert.getYearsOfExperience() != null ? expert.getYearsOfExperience() : 0);
            stmt.setString(8, expert.getLocation());
            stmt.setBoolean(9, expert.isVerified());
            stmt.setBoolean(10, expert.isActive());

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long id = rs.getLong(1);
                        expert.setId(id);
                        logger.info("Expert saved successfully with ID: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving expert", e);
        }
        return null;
    }

    public Optional<Expert> findByEmail(String email) {
        String sql = "SELECT * FROM experts WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToExpert(rs));
            }

        } catch (SQLException e) {
            logger.error("Error finding expert by email", e);
        }

        return Optional.empty();
    }

    public Optional<Expert> findById(Long id) {
        String sql = "SELECT * FROM experts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToExpert(rs));
            }

        } catch (SQLException e) {
            logger.error("Error finding expert by ID", e);
        }

        return Optional.empty();
    }

    public List<Expert> findAll() {
        List<Expert> experts = new ArrayList<>();
        String sql = "SELECT * FROM experts WHERE is_active = TRUE ORDER BY full_name";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                experts.add(mapResultSetToExpert(rs));
            }

            logger.info("Retrieved {} experts", experts.size());
        } catch (SQLException e) {
            logger.error("Error finding all experts", e);
        }

        return experts;
    }

    private Expert mapResultSetToExpert(ResultSet rs) throws SQLException {
        Expert expert = new Expert();
        expert.setId(rs.getLong("id"));
        expert.setFullName(rs.getString("full_name"));
        expert.setEmail(rs.getString("email"));
        expert.setPhoneNumber(rs.getString("phone_number"));
        expert.setPasswordHash(rs.getString("password_hash"));
        expert.setSpecialization(rs.getString("specialization"));
        expert.setQualifications(rs.getString("qualifications"));
        expert.setYearsOfExperience(rs.getInt("years_of_experience"));
        expert.setLocation(rs.getString("location"));
        expert.setVerified(rs.getBoolean("is_verified"));
        expert.setActive(rs.getBoolean("is_active"));

        Timestamp regDate = rs.getTimestamp("registration_date");
        if (regDate != null) {
            expert.setRegistrationDate(regDate.toLocalDateTime());
        }

        return expert;
    }
}
