package com.agriminds.repository;
import com.agriminds.model.FarmerCrop;
import com.agriminds.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class FarmerCropRepository {
    private static final Logger logger = LoggerFactory.getLogger(FarmerCropRepository.class);
    public Long save(FarmerCrop crop) {
        String sql = "INSERT INTO farmer_crops (farmer_id, crop_name, quantity, unit, selling_price, " +
                    "harvest_date, is_available, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, crop.getFarmerId());
            stmt.setString(2, crop.getCropName());
            stmt.setDouble(3, crop.getQuantity());
            stmt.setString(4, crop.getUnit());
            stmt.setDouble(5, crop.getSellingPrice());
            stmt.setDate(6, crop.getHarvestDate() != null ? Date.valueOf(crop.getHarvestDate()) : null);
            stmt.setBoolean(7, crop.isAvailable());
            stmt.setString(8, crop.getDescription());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long id = rs.getLong(1);
                        crop.setId(id);
                        logger.info("Farmer crop saved successfully with ID: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving farmer crop", e);
        }
        return null;
    }
    public List<FarmerCrop> findByFarmerId(Long farmerId) {
        String sql = "SELECT * FROM farmer_crops WHERE farmer_id = ? ORDER BY created_at DESC";
        List<FarmerCrop> crops = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, farmerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                crops.add(mapResultSetToFarmerCrop(rs));
            }
            logger.info("Found {} crops for farmer {}", crops.size(), farmerId);
        } catch (SQLException e) {
            logger.error("Error finding crops by farmer ID", e);
        }
        return crops;
    }
    public Optional<FarmerCrop> findById(Long id) {
        String sql = "SELECT * FROM farmer_crops WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToFarmerCrop(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding crop by ID", e);
        }
        return Optional.empty();
    }
    public boolean update(FarmerCrop crop) {
        String sql = "UPDATE farmer_crops SET crop_name = ?, quantity = ?, unit = ?, " +
                    "selling_price = ?, harvest_date = ?, is_available = ?, description = ? " +
                    "WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, crop.getCropName());
            stmt.setDouble(2, crop.getQuantity());
            stmt.setString(3, crop.getUnit());
            stmt.setDouble(4, crop.getSellingPrice());
            stmt.setDate(5, crop.getHarvestDate() != null ? Date.valueOf(crop.getHarvestDate()) : null);
            stmt.setBoolean(6, crop.isAvailable());
            stmt.setString(7, crop.getDescription());
            stmt.setLong(8, crop.getId());
            int affected = stmt.executeUpdate();
            logger.info("Crop updated: {}", affected > 0);
            return affected > 0;
        } catch (SQLException e) {
            logger.error("Error updating crop", e);
        }
        return false;
    }
    public boolean delete(Long id) {
        String sql = "DELETE FROM farmer_crops WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();
            logger.info("Crop deleted: {}", affected > 0);
            return affected > 0;
        } catch (SQLException e) {
            logger.error("Error deleting crop", e);
        }
        return false;
    }
    private FarmerCrop mapResultSetToFarmerCrop(ResultSet rs) throws SQLException {
        FarmerCrop crop = new FarmerCrop();
        crop.setId(rs.getLong("id"));
        crop.setFarmerId(rs.getLong("farmer_id"));
        crop.setCropId(rs.getObject("crop_id") != null ? rs.getLong("crop_id") : null);
        crop.setCropName(rs.getString("crop_name"));
        crop.setQuantity(rs.getDouble("quantity"));
        crop.setUnit(rs.getString("unit"));
        crop.setSellingPrice(rs.getDouble("selling_price"));
        Date harvestDate = rs.getDate("harvest_date");
        if (harvestDate != null) {
            crop.setHarvestDate(harvestDate.toLocalDate());
        }
        crop.setAvailable(rs.getBoolean("is_available"));
        crop.setDescription(rs.getString("description"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            crop.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            crop.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return crop;
    }
}
