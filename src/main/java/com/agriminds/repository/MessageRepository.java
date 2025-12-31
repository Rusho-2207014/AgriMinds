package com.agriminds.repository;

import com.agriminds.model.Message;
import com.agriminds.model.Message.SenderType;
import com.agriminds.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for managing messages between farmers and experts.
 */
public class MessageRepository {
    private static final Logger logger = LoggerFactory.getLogger(MessageRepository.class);

    /**
     * Save a new message
     */
    public Message save(Message message) {
        String sql = "INSERT INTO messages (conversation_id, farmer_id, expert_id, question_id, " +
                "expert_answer_id, sender_type, sender_name, message_text, is_read, sent_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, message.getConversationId());

            // Handle nullable farmer_id (for expert-to-expert conversations)
            if (message.getFarmerId() != null) {
                stmt.setLong(2, message.getFarmerId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }

            stmt.setLong(3, message.getExpertId());

            if (message.getQuestionId() != null) {
                stmt.setLong(4, message.getQuestionId());
            } else {
                stmt.setNull(4, Types.BIGINT);
            }

            if (message.getExpertAnswerId() != null) {
                stmt.setLong(5, message.getExpertAnswerId());
            } else {
                stmt.setNull(5, Types.BIGINT);
            }

            stmt.setString(6, message.getSenderType().name());
            stmt.setString(7, message.getSenderName());
            stmt.setString(8, message.getMessageText());
            stmt.setBoolean(9, message.getIsRead());
            stmt.setTimestamp(10, Timestamp.valueOf(message.getSentDate()));

            int affectedRows = stmt.executeUpdate();
            logger.info("Executed insert, affected rows: {}", affectedRows);

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        message.setId(generatedKeys.getLong(1));
                        logger.info("Message saved with ID: {}", message.getId());
                        return message;
                    } else {
                        logger.error("No generated keys returned after insert");
                    }
                }
            } else {
                logger.error("Insert failed, no rows affected");
            }
        } catch (SQLException e) {
            logger.error("SQL Error saving message: {}", e.getMessage(), e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Unexpected error saving message: {}", e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all messages for a conversation, ordered by date
     */
    public List<Message> getConversationMessages(String conversationId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE conversation_id = ? ORDER BY sent_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, conversationId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }

            logger.info("Retrieved {} messages for conversation: {}", messages.size(), conversationId);
        } catch (SQLException e) {
            logger.error("Error retrieving messages for conversation: {}", conversationId, e);
        }

        return messages;
    }

    /**
     * Get all conversations for a farmer with latest message info
     */
    public Map<String, Message> getFarmerConversations(Long farmerId) {
        Map<String, Message> conversations = new HashMap<>();
        String sql = "SELECT m.* FROM messages m " +
                "INNER JOIN (SELECT conversation_id, MAX(sent_date) as max_date " +
                "FROM messages WHERE farmer_id = ? GROUP BY conversation_id) latest " +
                "ON m.conversation_id = latest.conversation_id AND m.sent_date = latest.max_date " +
                "WHERE m.farmer_id = ? ORDER BY m.sent_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, farmerId);
            stmt.setLong(2, farmerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Message message = mapResultSetToMessage(rs);
                conversations.put(message.getConversationId(), message);
            }

            logger.info("Retrieved {} conversations for farmer: {}", conversations.size(), farmerId);
        } catch (SQLException e) {
            logger.error("Error retrieving farmer conversations: {}", farmerId, e);
        }

        return conversations;
    }

    /**
     * Get all conversations for an expert with latest message info
     * Includes both farmer-expert and expert-expert conversations
     */
    public Map<String, Message> getExpertConversations(Long expertId) {
        Map<String, Message> conversations = new HashMap<>();
        String sql = "SELECT m.* FROM messages m " +
                "INNER JOIN (SELECT conversation_id, MAX(sent_date) as max_date " +
                "FROM messages WHERE expert_id = ? OR " +
                "(farmer_id IS NULL AND (conversation_id = CONCAT('expert_', ?, '_expert_', expert_id) OR " +
                "conversation_id = CONCAT('expert_', expert_id, '_expert_', ?))) " +
                "GROUP BY conversation_id) latest " +
                "ON m.conversation_id = latest.conversation_id AND m.sent_date = latest.max_date " +
                "WHERE (m.expert_id = ? OR " +
                "(m.farmer_id IS NULL AND (m.conversation_id = CONCAT('expert_', ?, '_expert_', m.expert_id) OR " +
                "m.conversation_id = CONCAT('expert_', m.expert_id, '_expert_', ?)))) " +
                "ORDER BY m.sent_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, expertId);
            stmt.setLong(2, expertId);
            stmt.setLong(3, expertId);
            stmt.setLong(4, expertId);
            stmt.setLong(5, expertId);
            stmt.setLong(6, expertId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Message message = mapResultSetToMessage(rs);
                conversations.put(message.getConversationId(), message);
            }

            logger.info("Retrieved {} conversations for expert: {}", conversations.size(), expertId);
        } catch (SQLException e) {
            logger.error("Error retrieving expert conversations: {}", expertId, e);
        }

        return conversations;
    }

    /**
     * Get the latest message for a specific expert answer
     */
    public Message getLatestMessageForAnswer(Long expertAnswerId) {
        String sql = "SELECT * FROM messages WHERE expert_answer_id = ? ORDER BY sent_date DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, expertAnswerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToMessage(rs);
            }
        } catch (SQLException e) {
            logger.error("Error getting latest message for answer: {}", expertAnswerId, e);
        }

        return null;
    }

    /**
     * Check if there are any farmer messages for a specific expert answer
     */
    public boolean hasFarmerRepliesForAnswer(Long expertAnswerId) {
        String sql = "SELECT COUNT(*) FROM messages WHERE expert_answer_id = ? AND sender_type = 'FARMER'";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, expertAnswerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                logger.info("Farmer replies for answer {}: {} messages", expertAnswerId, count);
                return count > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking farmer replies for answer: {}", expertAnswerId, e);
        }

        return false;
    }

    /**
     * Check if expert has replied to farmer messages for a specific answer
     */
    public boolean hasExpertRepliedToAnswer(Long expertAnswerId, Long expertId) {
        String sql = "SELECT COUNT(*) FROM messages WHERE expert_answer_id = ? " +
                "AND expert_id = ? AND sender_type = 'EXPERT'";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, expertAnswerId);
            stmt.setLong(2, expertId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking expert replies for answer: {}", expertAnswerId, e);
        }

        return false;
    }

    /**
     * Get count of unread messages for a user
     */
    public int getUnreadCount(Long userId, SenderType receiverType) {
        String sql;
        if (receiverType == SenderType.FARMER) {
            sql = "SELECT COUNT(*) FROM messages WHERE farmer_id = ? AND sender_type = 'EXPERT' AND is_read = FALSE";
        } else {
            sql = "SELECT COUNT(*) FROM messages WHERE expert_id = ? AND sender_type = 'FARMER' AND is_read = FALSE";
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error getting unread count for user: {}", userId, e);
        }

        return 0;
    }

    /**
     * Mark messages as read in a conversation for a specific receiver
     */
    public void markConversationAsRead(String conversationId, SenderType receiverType) {
        String sql;
        if (receiverType == SenderType.FARMER) {
            sql = "UPDATE messages SET is_read = TRUE WHERE conversation_id = ? AND sender_type = 'EXPERT'";
        } else {
            sql = "UPDATE messages SET is_read = TRUE WHERE conversation_id = ? AND sender_type = 'FARMER'";
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, conversationId);
            int updated = stmt.executeUpdate();
            logger.info("Marked {} messages as read in conversation: {}", updated, conversationId);
        } catch (SQLException e) {
            logger.error("Error marking messages as read: {}", conversationId, e);
        }
    }

    /**
     * Delete all messages in a conversation
     */
    public boolean deleteConversation(String conversationId) {
        String sql = "DELETE FROM messages WHERE conversation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, conversationId);
            int deleted = stmt.executeUpdate();
            logger.info("Deleted {} messages from conversation: {}", deleted, conversationId);
            return deleted > 0;
        } catch (SQLException e) {
            logger.error("Error deleting conversation: {}", conversationId, e);
        }

        return false;
    }

    /**
     * Map ResultSet to Message object
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getLong("id"));
        message.setConversationId(rs.getString("conversation_id"));

        // Handle nullable farmer_id (for expert-to-expert conversations)
        long farmerId = rs.getLong("farmer_id");
        if (!rs.wasNull()) {
            message.setFarmerId(farmerId);
        }

        message.setExpertId(rs.getLong("expert_id"));

        // Handle nullable question_id and expert_answer_id
        long questionId = rs.getLong("question_id");
        if (!rs.wasNull()) {
            message.setQuestionId(questionId);
        }

        long expertAnswerId = rs.getLong("expert_answer_id");
        if (!rs.wasNull()) {
            message.setExpertAnswerId(expertAnswerId);
        }

        message.setSenderType(SenderType.valueOf(rs.getString("sender_type")));
        message.setSenderName(rs.getString("sender_name"));
        message.setMessageText(rs.getString("message_text"));
        message.setIsRead(rs.getBoolean("is_read"));

        Timestamp sentTimestamp = rs.getTimestamp("sent_date");
        if (sentTimestamp != null) {
            message.setSentDate(sentTimestamp.toLocalDateTime());
        }

        return message;
    }
}
