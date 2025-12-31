package com.agriminds.repository;

import com.agriminds.model.Buyer;
import com.agriminds.util.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;

public class BuyerRepository {

    private static final Logger logger = LoggerFactory.getLogger(BuyerRepository.class);

    public Long save(Buyer buyer) {
        String sql = "INSERT INTO buyers (full_name, email, phone_number, password_hash, " +
                "business_name, business_type, division, district, address, is_verified, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, buyer.getFullName());
            stmt.setString(2, buyer.getEmail());
            stmt.setString(3, buyer.getPhoneNumber());
            stmt.setString(4, buyer.getPasswordHash());
            stmt.setString(5, buyer.getBusinessName());
            stmt.setString(6, buyer.getBusinessType());
            stmt.setString(7, buyer.getDivision());
            stmt.setString(8, buyer.getDistrict());
            stmt.setString(9, buyer.getAddress());
            stmt.setBoolean(10, buyer.isVerified());
            stmt.setBoolean(11, buyer.isActive());

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long id = rs.getLong(1);
                        buyer.setId(id);
                        logger.info("Buyer saved successfully with ID: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving buyer", e);
        }
        return null;
    }

    public Optional<Buyer> findByEmail(String email) {
        String sql = "SELECT * FROM buyers WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToBuyer(rs));
            }

        } catch (SQLException e) {
            logger.error("Error finding buyer by email", e);
        }

        return Optional.empty();
    }

    public Optional<Buyer> findById(Long id) {
        String sql = "SELECT * FROM buyers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToBuyer(rs));
            }

        } catch (SQLException e) {
            logger.error("Error finding buyer by ID", e);
        }

        return Optional.empty();
    }

    private Buyer mapResultSetToBuyer(ResultSet rs) throws SQLException {
        Buyer buyer = new Buyer();
        buyer.setId(rs.getLong("id"));
        buyer.setFullName(rs.getString("full_name"));
        buyer.setEmail(rs.getString("email"));
        buyer.setPhoneNumber(rs.getString("phone_number"));
        buyer.setPasswordHash(rs.getString("password_hash"));
        buyer.setBusinessName(rs.getString("business_name"));
        buyer.setBusinessType(rs.getString("business_type"));
        buyer.setDivision(rs.getString("division"));
        buyer.setDistrict(rs.getString("district"));
        buyer.setAddress(rs.getString("address"));
        buyer.setVerified(rs.getBoolean("is_verified"));
        buyer.setActive(rs.getBoolean("is_active"));

        Timestamp regDate = rs.getTimestamp("registration_date");
        if (regDate != null) {
            buyer.setRegistrationDate(regDate.toLocalDateTime());
        }

        return buyer;
    }
}
