package com.agriminds.repository;
import com.agriminds.model.MarketPrice;
import com.agriminds.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class MarketPriceRepository {
    private static final Logger logger = LoggerFactory.getLogger(MarketPriceRepository.class);
    public Long save(MarketPrice price) {
        String sql = "INSERT INTO market_prices (crop_id, crop_name, market_name, district, " +
                    "division, wholesale_price, retail_price, unit, price_date, source, recorded_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, price.getCropId());
            stmt.setString(2, price.getCropName());
            stmt.setString(3, price.getMarketName());
            stmt.setString(4, price.getDistrict());
            stmt.setString(5, price.getDivision());
            stmt.setDouble(6, price.getWholesalePrice() != null ? price.getWholesalePrice() : 0.0);
            stmt.setDouble(7, price.getRetailPrice());
            stmt.setString(8, price.getUnit());
            stmt.setDate(9, Date.valueOf(price.getPriceDate()));
            stmt.setString(10, price.getSource());
            stmt.setTimestamp(11, Timestamp.valueOf(price.getRecordedAt()));
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long id = rs.getLong(1);
                        price.setId(id);
                        logger.info("Market price saved successfully with ID: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving market price", e);
        }
        return null;
    }
    public List<MarketPrice> findByCropName(String cropName) {
        List<MarketPrice> prices = new ArrayList<>();
        String sql = "SELECT * FROM market_prices WHERE crop_name = ? ORDER BY price_date DESC LIMIT 30";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cropName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                prices.add(mapResultSetToMarketPrice(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding market prices by crop", e);
        }
        return prices;
    }
    public List<MarketPrice> findLatestPrices() {
        List<MarketPrice> prices = new ArrayList<>();
        String sql = "SELECT * FROM market_prices WHERE price_date = (SELECT MAX(price_date) FROM market_prices) " +
                    "ORDER BY crop_name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                prices.add(mapResultSetToMarketPrice(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding latest market prices", e);
        }
        return prices;
    }
    public List<MarketPrice> findByDistrict(String district) {
        List<MarketPrice> prices = new ArrayList<>();
        String sql = "SELECT * FROM market_prices WHERE district = ? AND price_date >= ? " +
                    "ORDER BY price_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, district);
            stmt.setDate(2, Date.valueOf(LocalDate.now().minusDays(7)));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                prices.add(mapResultSetToMarketPrice(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding market prices by district", e);
        }
        return prices;
    }
    public Optional<MarketPrice> findById(Long id) {
        String sql = "SELECT * FROM market_prices WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToMarketPrice(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding market price by ID", e);
        }
        return Optional.empty();
    }
    private MarketPrice mapResultSetToMarketPrice(ResultSet rs) throws SQLException {
        MarketPrice price = new MarketPrice();
        price.setId(rs.getLong("id"));
        price.setCropId(rs.getObject("crop_id", Long.class));
        price.setCropName(rs.getString("crop_name"));
        price.setMarketName(rs.getString("market_name"));
        price.setDistrict(rs.getString("district"));
        price.setDivision(rs.getString("division"));
        price.setWholesalePrice(rs.getDouble("wholesale_price"));
        price.setRetailPrice(rs.getDouble("retail_price"));
        price.setUnit(rs.getString("unit"));
        price.setPriceDate(rs.getDate("price_date").toLocalDate());
        price.setSource(rs.getString("source"));
        price.setRecordedAt(rs.getTimestamp("recorded_at").toLocalDateTime());
        Double priceChange = rs.getObject("price_change", Double.class);
        price.setPriceChange(priceChange);
        return price;
    }
}
