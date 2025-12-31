package com.agriminds.repository;

import com.agriminds.model.ExpertAnswer;
import com.agriminds.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing expert answers to questions.
 * Supports multiple experts answering the same question.
 */
public class ExpertAnswerRepository {
    private static final Logger logger = LoggerFactory.getLogger(ExpertAnswerRepository.class);

    /**
     * Save a new expert answer to the database
     */
    public ExpertAnswer save(ExpertAnswer expertAnswer) {
        String sql = "INSERT INTO expert_answers (question_id, expert_id, expert_name, parent_answer_id, reply_type, answer_text, answered_date) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, expertAnswer.getQuestionId());
            stmt.setLong(2, expertAnswer.getExpertId());
            stmt.setString(3, expertAnswer.getExpertName());

            if (expertAnswer.getParentAnswerId() != null) {
                stmt.setLong(4, expertAnswer.getParentAnswerId());
            } else {
                stmt.setNull(4, java.sql.Types.BIGINT);
            }

            stmt.setString(5, expertAnswer.getReplyType());
            stmt.setString(6, expertAnswer.getAnswerText());
            stmt.setTimestamp(7, Timestamp.valueOf(expertAnswer.getAnsweredDate()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        expertAnswer.setId(generatedKeys.getLong(1));
                        logger.info("Expert answer saved with ID: {}", expertAnswer.getId());
                        return expertAnswer;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving expert answer", e);
        }
        return null;
    }

    /**
     * Get all expert answers for a specific question (only original answers, not
     * replies)
     */
    public List<ExpertAnswer> getAnswersByQuestionId(Long questionId) {
        List<ExpertAnswer> answers = new ArrayList<>();
        String sql = "SELECT * FROM expert_answers WHERE question_id = ? AND parent_answer_id IS NULL ORDER BY answered_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, questionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                answers.add(mapResultSetToExpertAnswer(rs));
            }

            logger.info("Retrieved {} expert answers for question ID: {}", answers.size(), questionId);
        } catch (SQLException e) {
            logger.error("Error retrieving expert answers for question: {}", questionId, e);
        }

        return answers;
    }

    /**
     * Get all answers by a specific expert
     */
    public List<ExpertAnswer> getAnswersByExpertId(Long expertId) {
        List<ExpertAnswer> answers = new ArrayList<>();
        String sql = "SELECT * FROM expert_answers WHERE expert_id = ? ORDER BY answered_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, expertId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                answers.add(mapResultSetToExpertAnswer(rs));
            }

            logger.info("Retrieved {} answers by expert ID: {}", answers.size(), expertId);
        } catch (SQLException e) {
            logger.error("Error retrieving answers by expert: {}", expertId, e);
        }

        return answers;
    }

    /**
     * Check if a specific expert has already answered a question
     */
    public boolean hasExpertAnswered(Long questionId, Long expertId) {
        // Only count actual answers, not corrections/replies (parent_answer_id must be
        // NULL)
        String sql = "SELECT COUNT(*) FROM expert_answers WHERE question_id = ? AND expert_id = ? AND parent_answer_id IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, questionId);
            stmt.setLong(2, expertId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking if expert answered question", e);
        }

        return false;
    }

    /**
     * Get the count of expert answers for a question
     */
    public int getAnswerCount(Long questionId) {
        String sql = "SELECT COUNT(*) FROM expert_answers WHERE question_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, questionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error getting answer count for question: {}", questionId, e);
        }

        return 0;
    }

    /**
     * Delete all answers for a question
     */
    public boolean deleteByQuestionId(Long questionId) {
        String sql = "DELETE FROM expert_answers WHERE question_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, questionId);
            int affectedRows = stmt.executeUpdate();
            logger.info("Deleted {} expert answers for question ID: {}", affectedRows, questionId);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting expert answers for question: {}", questionId, e);
        }

        return false;
    }

    /**
     * Get count of questions answered by an expert
     * Counts all answers (including those where the question was deleted by farmer)
     */
    public int getAnsweredQuestionsCount(Long expertId) {
        // Count all distinct answer IDs (each answer = one question answered)
        // This includes answers where question_id is NULL (deleted questions)
        String sql = "SELECT COUNT(*) as count FROM expert_answers WHERE expert_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, expertId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            logger.error("Error getting answered questions count for expert: {}", expertId, e);
        }
        return 0;
    }

    /**
     * Get count of corrections/additions/replies made by an expert
     * Only counts ACCEPTED entries where parent_answer_id IS NOT NULL and accepted
     * = TRUE
     */
    public int getRepliesCount(Long expertId) {
        String sql = "SELECT COUNT(*) as count FROM expert_answers WHERE expert_id = ? AND parent_answer_id IS NOT NULL AND accepted = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, expertId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            logger.error("Error getting replies count for expert: {}", expertId, e);
        }
        return 0;
    }

    /**
     * Get a specific expert answer by ID
     */
    public ExpertAnswer getAnswerById(Long answerId) {
        String sql = "SELECT * FROM expert_answers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, answerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToExpertAnswer(rs);
            }
        } catch (SQLException e) {
            logger.error("Error getting answer by ID: {}", answerId, e);
        }
        return null;
    }

    /**
     * Get all replies to a specific answer
     */
    public List<ExpertAnswer> getRepliesByParentAnswerId(Long parentAnswerId) {
        List<ExpertAnswer> replies = new ArrayList<>();
        String sql = "SELECT * FROM expert_answers WHERE parent_answer_id = ? ORDER BY answered_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, parentAnswerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                replies.add(mapResultSetToExpertAnswer(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting replies for answer: {}", parentAnswerId, e);
        }
        return replies;
    }

    /**
     * Check if an expert has already replied to a specific answer
     */
    public ExpertAnswer getExistingReply(Long parentAnswerId, Long expertId) {
        String sql = "SELECT * FROM expert_answers WHERE parent_answer_id = ? AND expert_id = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, parentAnswerId);
            stmt.setLong(2, expertId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToExpertAnswer(rs);
            }
        } catch (SQLException e) {
            logger.error("Error checking existing reply: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Update an existing reply/correction
     */
    public boolean updateReply(Long replyId, String newText, String replyType) {
        String sql = "UPDATE expert_answers SET answer_text = ?, reply_type = ?, answered_date = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newText);
            stmt.setString(2, replyType);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(4, replyId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating reply: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * Accept a correction/addition/reply (farmer approves it)
     */
    public boolean acceptCorrection(Long replyId) {
        String sql = "UPDATE expert_answers SET accepted = TRUE WHERE id = ? AND parent_answer_id IS NOT NULL";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, replyId);
            int affectedRows = stmt.executeUpdate();
            logger.info("Correction {} accepted", replyId);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error accepting correction: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * Deny a correction/addition/reply (farmer rejects it)
     */
    public boolean denyCorrection(Long replyId) {
        String sql = "UPDATE expert_answers SET accepted = FALSE WHERE id = ? AND parent_answer_id IS NOT NULL";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, replyId);
            int affectedRows = stmt.executeUpdate();
            logger.info("Correction {} denied", replyId);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error denying correction: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * Map ResultSet to ExpertAnswer object
     */
    private ExpertAnswer mapResultSetToExpertAnswer(ResultSet rs) throws SQLException {
        ExpertAnswer answer = new ExpertAnswer();
        answer.setId(rs.getLong("id"));
        answer.setQuestionId(rs.getLong("question_id"));
        answer.setExpertId(rs.getLong("expert_id"));
        answer.setExpertName(rs.getString("expert_name"));

        Long parentAnswerId = rs.getLong("parent_answer_id");
        if (!rs.wasNull()) {
            answer.setParentAnswerId(parentAnswerId);
        }

        answer.setReplyType(rs.getString("reply_type"));

        // Handle accepted field (can be NULL)
        Boolean accepted = rs.getBoolean("accepted");
        if (!rs.wasNull()) {
            answer.setAccepted(accepted);
        }

        answer.setAnswerText(rs.getString("answer_text"));

        Timestamp answeredTimestamp = rs.getTimestamp("answered_date");
        if (answeredTimestamp != null) {
            answer.setAnsweredDate(answeredTimestamp.toLocalDateTime());
        }

        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            answer.setCreatedAt(createdTimestamp.toLocalDateTime());
        }

        return answer;
    }
}
