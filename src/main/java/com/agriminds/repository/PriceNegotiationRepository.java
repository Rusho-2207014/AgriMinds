package com.agriminds.repository;

import com.agriminds.model.PriceNegotiation;
import com.agriminds.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PriceNegotiationRepository {

    private static final Logger logger = LoggerFactory.getLogger(PriceNegotiationRepository.class);

    public Long save(PriceNegotiation negotiation) {
        // Get farmer_id from farmer_crops table
        String getFarmerIdSql = "SELECT farmer_id FROM farmer_crops WHERE id = ?";
        Long farmerId = null;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(getFarmerIdSql)) {
            stmt.setLong(1, negotiation.getFarmerCropId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    farmerId = rs.getLong("farmer_id");
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting farmer_id for crop", e);
            return null;
        }

        if (farmerId == null) {
            logger.error("Could not find farmer_id for crop_id: {}", negotiation.getFarmerCropId());
            return null;
        }

        String sql = "INSERT INTO price_negotiations (crop_id, farmer_id, buyer_id, buyer_name, " +
                "buyer_offer, farmer_price, quantity, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, negotiation.getFarmerCropId());
            stmt.setLong(2, farmerId);
            stmt.setLong(3, negotiation.getBuyerId());
            stmt.setString(4, negotiation.getBuyerName());
            stmt.setBigDecimal(5, negotiation.getOfferedPrice());
            stmt.setBigDecimal(6, negotiation.getFarmerAskingPrice());
            stmt.setBigDecimal(7, negotiation.getQuantityKg() != null ? negotiation.getQuantityKg() : BigDecimal.ZERO);
            stmt.setString(8, negotiation.getStatus() != null ? negotiation.getStatus() : "Pending");

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Long id = generatedKeys.getLong(1);
                        negotiation.setId(id);
                        logger.info("Price negotiation saved with ID: {}, Status: {}", id, negotiation.getStatus());
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving price negotiation", e);
        }
        return null;
    }

    public List<PriceNegotiation> findByBuyerId(Long buyerId) {
        String sql = "SELECT * FROM price_negotiations WHERE buyer_id = ? ORDER BY created_date DESC";
        List<PriceNegotiation> negotiations = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, buyerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    negotiations.add(mapResultSetToNegotiation(rs));
                }
            }

            logger.info("Retrieved {} negotiations for buyer ID: {}", negotiations.size(), buyerId);

        } catch (SQLException e) {
            logger.error("Error retrieving negotiations for buyer", e);
        }

        return negotiations;
    }

    public List<PriceNegotiation> findByFarmerId(Long farmerId) {
        String sql = "SELECT * FROM price_negotiations WHERE farmer_id = ? ORDER BY created_date DESC";
        List<PriceNegotiation> negotiations = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, farmerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    negotiations.add(mapResultSetToNegotiation(rs));
                }
            }

            logger.info("Retrieved {} negotiations for farmer ID: {}", negotiations.size(), farmerId);

        } catch (SQLException e) {
            logger.error("Error retrieving negotiations for farmer", e);
        }

        return negotiations;
    }

    public boolean updateStatus(Long id, String status) {
        String sql = "UPDATE price_negotiations SET status = ?, accepted_date = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);

            // Set accepted_date only when status is "Accepted"
            if ("Accepted".equalsIgnoreCase(status)) {
                stmt.setObject(2, java.time.LocalDateTime.now());
            } else {
                stmt.setObject(2, null);
            }

            stmt.setLong(3, id);

            int affected = stmt.executeUpdate();
            logger.info("Updated negotiation {} status to {}", id, status);
            return affected > 0;

        } catch (SQLException e) {
            logger.error("Error updating negotiation status", e);
        }

        return false;
    }

    public List<PriceNegotiation> getAll() {
        String sql = "SELECT * FROM price_negotiations ORDER BY negotiation_date DESC";
        List<PriceNegotiation> negotiations = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                negotiations.add(mapResultSetToNegotiation(rs));
            }

            logger.info("Retrieved {} total negotiations", negotiations.size());

        } catch (SQLException e) {
            logger.error("Error retrieving all negotiations", e);
        }

        return negotiations;
    }

    private PriceNegotiation mapResultSetToNegotiation(ResultSet rs) throws SQLException {
        PriceNegotiation negotiation = new PriceNegotiation();

        negotiation.setId(rs.getLong("id"));
        negotiation.setFarmerCropId(rs.getLong("crop_id"));
        negotiation.setBuyerId(rs.getLong("buyer_id"));
        negotiation.setBuyerName(rs.getString("buyer_name"));
        negotiation.setOfferedPrice(rs.getBigDecimal("buyer_offer"));
        negotiation.setFarmerAskingPrice(rs.getBigDecimal("farmer_price"));
        negotiation.setQuantityKg(rs.getBigDecimal("quantity"));
        negotiation.setStatus(rs.getString("status"));

        Timestamp createdTimestamp = rs.getTimestamp("created_date");
        if (createdTimestamp != null) {
            negotiation.setNegotiationDate(createdTimestamp.toLocalDateTime());
        }

        Timestamp acceptedTimestamp = rs.getTimestamp("accepted_date");
        if (acceptedTimestamp != null) {
            negotiation.setAcceptedDate(acceptedTimestamp.toLocalDateTime());
        }

        return negotiation;
    }
}
