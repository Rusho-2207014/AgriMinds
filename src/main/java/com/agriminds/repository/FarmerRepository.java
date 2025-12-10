package com.agriminds.repository;
import com.agriminds.model.Farmer;
import com.agriminds.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class FarmerRepository {
    private static final Logger logger = LoggerFactory.getLogger(FarmerRepository.class);
    public Long save(Farmer farmer) {
        String sql = "INSERT INTO farmers (full_name, email, phone_number, password_hash, " +
                    "national_id, division, district, upazila, village, farm_size, " +
                    "farming_type, registration_date, is_active, language) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, farmer.getFullName());
            stmt.setString(2, farmer.getEmail());
            stmt.setString(3, farmer.getPhoneNumber());
            stmt.setString(4, farmer.getPasswordHash());
            stmt.setString(5, farmer.getNationalId());
            stmt.setString(6, farmer.getDivision());
            stmt.setString(7, farmer.getDistrict());
            stmt.setString(8, farmer.getUpazila());
            stmt.setString(9, farmer.getVillage());
            stmt.setDouble(10, farmer.getFarmSize() != null ? farmer.getFarmSize() : 0.0);
            stmt.setString(11, farmer.getFarmingType());
            stmt.setTimestamp(12, Timestamp.valueOf(farmer.getRegistrationDate()));
            stmt.setBoolean(13, farmer.isActive());
            stmt.setString(14, farmer.getLanguage());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long id = rs.getLong(1);
                        farmer.setId(id);
                        logger.info("=== DEBUG: Farmer saved successfully with ID: {} ===", id);
                        logger.info("=== DEBUG: Returning ID from repository: {} ===", id);
                        return id;
                    } else {
                        logger.error("=== DEBUG: No generated keys returned! ===");
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("=== DEBUG: SQLException in save() ===", e);
        }
        logger.error("=== DEBUG: Returning NULL from repository save ===");
        return null;
    }
    public Optional<Farmer> findById(Long id) {
        String sql = "SELECT * FROM farmers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToFarmer(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding farmer by ID", e);
        }
        return Optional.empty();
    }
    public Optional<Farmer> findByEmail(String email) {
        String sql = "SELECT * FROM farmers WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToFarmer(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding farmer by email", e);
        }
        return Optional.empty();
    }
    public Optional<Farmer> findByPhoneNumber(String phoneNumber) {
        String sql = "SELECT * FROM farmers WHERE phone_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToFarmer(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding farmer by phone", e);
        }
        return Optional.empty();
    }
    public List<Farmer> findAll() {
        List<Farmer> farmers = new ArrayList<>();
        String sql = "SELECT * FROM farmers ORDER BY registration_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                farmers.add(mapResultSetToFarmer(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all farmers", e);
        }
        return farmers;
    }
    public boolean update(Farmer farmer) {
        String sql = "UPDATE farmers SET full_name = ?, email = ?, phone_number = ?, " +
                    "national_id = ?, division = ?, district = ?, upazila = ?, village = ?, " +
                    "farm_size = ?, farming_type = ?, is_active = ?, language = ? " +
                    "WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, farmer.getFullName());
            stmt.setString(2, farmer.getEmail());
            stmt.setString(3, farmer.getPhoneNumber());
            stmt.setString(4, farmer.getNationalId());
            stmt.setString(5, farmer.getDivision());
            stmt.setString(6, farmer.getDistrict());
            stmt.setString(7, farmer.getUpazila());
            stmt.setString(8, farmer.getVillage());
            stmt.setDouble(9, farmer.getFarmSize() != null ? farmer.getFarmSize() : 0.0);
            stmt.setString(10, farmer.getFarmingType());
            stmt.setBoolean(11, farmer.isActive());
            stmt.setString(12, farmer.getLanguage());
            stmt.setLong(13, farmer.getId());
            int affected = stmt.executeUpdate();
            logger.info("Farmer updated: {}", affected > 0);
            return affected > 0;
        } catch (SQLException e) {
            logger.error("Error updating farmer", e);
        }
        return false;
    }
    public boolean delete(Long id) {
        String sql = "DELETE FROM farmers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();
            logger.info("Farmer deleted: {}", affected > 0);
            return affected > 0;
        } catch (SQLException e) {
            logger.error("Error deleting farmer", e);
        }
        return false;
    }
    private Farmer mapResultSetToFarmer(ResultSet rs) throws SQLException {
        Farmer farmer = new Farmer();
        farmer.setId(rs.getLong("id"));
        farmer.setFullName(rs.getString("full_name"));
        farmer.setEmail(rs.getString("email"));
        farmer.setPhoneNumber(rs.getString("phone_number"));
        farmer.setPasswordHash(rs.getString("password_hash"));
        farmer.setNationalId(rs.getString("national_id"));
        farmer.setDivision(rs.getString("division"));
        farmer.setDistrict(rs.getString("district"));
        farmer.setUpazila(rs.getString("upazila"));
        farmer.setVillage(rs.getString("village"));
        farmer.setFarmSize(rs.getDouble("farm_size"));
        farmer.setFarmingType(rs.getString("farming_type"));
        farmer.setRegistrationDate(rs.getTimestamp("registration_date").toLocalDateTime());
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            farmer.setLastLogin(lastLogin.toLocalDateTime());
        }
        farmer.setActive(rs.getBoolean("is_active"));
        farmer.setLanguage(rs.getString("language"));
        return farmer;
    }
}
