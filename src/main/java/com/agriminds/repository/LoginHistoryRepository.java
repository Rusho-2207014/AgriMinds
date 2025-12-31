package com.agriminds.repository;

import com.agriminds.model.LoginHistory;
import com.agriminds.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoginHistoryRepository {

    private static final Logger logger = LoggerFactory.getLogger(LoginHistoryRepository.class);

    public Long save(LoginHistory loginHistory) {
        String sql = "INSERT INTO login_history (user_id, user_type, email, login_time, " +
                "ip_address, device_info, login_status, failure_reason) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, loginHistory.getUserId());
            stmt.setString(2, loginHistory.getUserType());
            stmt.setString(3, loginHistory.getEmail());
            stmt.setTimestamp(4, Timestamp.valueOf(loginHistory.getLoginTime()));
            stmt.setString(5, loginHistory.getIpAddress());
            stmt.setString(6, loginHistory.getDeviceInfo());
            stmt.setString(7, loginHistory.getLoginStatus());
            stmt.setString(8, loginHistory.getFailureReason());

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Long id = generatedKeys.getLong(1);
                        loginHistory.setId(id);
                        logger.info("Login history saved with ID: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving login history", e);
        }
        return null;
    }

    public Optional<LoginHistory> getLastSuccessfulLogin(Long userId, String userType) {
        String sql = "SELECT * FROM login_history " +
                "WHERE user_id = ? AND user_type = ? AND login_status = 'success' " +
                "ORDER BY login_time DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, userType);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LoginHistory lastLogin = mapResultSetToLoginHistory(rs);
                    logger.info("Found last login for user {}: {}", userId, lastLogin.getLoginTime());
                    return Optional.of(lastLogin);
                } else {
                    logger.info("No previous login found for user {}", userId);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching last login for user: {}", userId, e);
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<LoginHistory> getLoginHistoryByUser(Long userId, String userType, int limit) {
        String sql = "SELECT * FROM login_history " +
                "WHERE user_id = ? AND user_type = ? " +
                "ORDER BY login_time DESC LIMIT ?";
        List<LoginHistory> history = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, userType);
            stmt.setInt(3, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    history.add(mapResultSetToLoginHistory(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching login history for user: {}", userId, e);
        }
        return history;
    }

    public int getFailedLoginCount(String email, int withinMinutes) {
        String sql = "SELECT COUNT(*) FROM login_history " +
                "WHERE email = ? AND login_status = 'failed' " +
                "AND login_time > DATE_SUB(NOW(), INTERVAL ? MINUTE)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setInt(2, withinMinutes);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching failed login count for email: {}", email, e);
        }
        return 0;
    }

    private LoginHistory mapResultSetToLoginHistory(ResultSet rs) throws SQLException {
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setId(rs.getLong("id"));
        loginHistory.setUserId(rs.getLong("user_id"));
        loginHistory.setUserType(rs.getString("user_type"));
        loginHistory.setEmail(rs.getString("email"));

        Timestamp loginTimestamp = rs.getTimestamp("login_time");
        if (loginTimestamp != null) {
            loginHistory.setLoginTime(loginTimestamp.toLocalDateTime());
        }

        loginHistory.setIpAddress(rs.getString("ip_address"));
        loginHistory.setDeviceInfo(rs.getString("device_info"));
        loginHistory.setLoginStatus(rs.getString("login_status"));
        loginHistory.setFailureReason(rs.getString("failure_reason"));

        return loginHistory;
    }
}
