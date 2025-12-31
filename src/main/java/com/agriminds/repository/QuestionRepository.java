package com.agriminds.repository;

import com.agriminds.model.Question;
import com.agriminds.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuestionRepository {

    private static final Logger logger = LoggerFactory.getLogger(QuestionRepository.class);

    public Long save(Question question) {
        String sql = "INSERT INTO questions (farmer_id, farmer_name, category, question_text, " +
                "image_url, status, asked_date, answer_text, answered_by_expert_id, expert_name, " +
                "answered_date, ai_generated, ai_answer_text, ai_answered_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, question.getFarmerId());
            stmt.setString(2, question.getFarmerName());
            stmt.setString(3, question.getCategory());
            stmt.setString(4, question.getQuestionText());
            stmt.setString(5, question.getImageUrl());
            stmt.setString(6, question.getStatus() != null ? question.getStatus() : "Open");
            stmt.setTimestamp(7,
                    Timestamp.valueOf(question.getAskedDate() != null ? question.getAskedDate() : LocalDateTime.now()));
            stmt.setString(8, question.getAnswerText());

            if (question.getAnsweredByExpertId() != null) {
                stmt.setLong(9, question.getAnsweredByExpertId());
            } else {
                stmt.setNull(9, Types.BIGINT);
            }

            stmt.setString(10, question.getExpertName());

            if (question.getAnsweredDate() != null) {
                stmt.setTimestamp(11, Timestamp.valueOf(question.getAnsweredDate()));
            } else {
                stmt.setNull(11, Types.TIMESTAMP);
            }

            stmt.setBoolean(12, question.getAiGenerated() != null ? question.getAiGenerated() : false);
            stmt.setString(13, question.getAiAnswerText());

            if (question.getAiAnsweredDate() != null) {
                stmt.setTimestamp(14, Timestamp.valueOf(question.getAiAnsweredDate()));
            } else {
                stmt.setNull(14, Types.TIMESTAMP);
            }

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Long id = generatedKeys.getLong(1);
                        question.setId(id);
                        logger.info("Question saved with ID: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving question", e);
        }
        return null;
    }

    public List<Question> findByFarmerId(Long farmerId) {
        String sql = "SELECT * FROM questions WHERE farmer_id = ? ORDER BY asked_date DESC";
        List<Question> questions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, farmerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(mapResultSetToQuestion(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching questions for farmer: {}", farmerId, e);
        }
        return questions;
    }

    public Optional<Question> findById(Long id) {
        String sql = "SELECT * FROM questions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToQuestion(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching question by ID: {}", id, e);
        }
        return Optional.empty();
    }

    /**
     * Get question by ID (returns null if not found)
     */
    public Question getQuestionById(Long id) {
        return findById(id).orElse(null);
    }

    public List<Question> findAll() {
        String sql = "SELECT * FROM questions ORDER BY asked_date DESC";
        List<Question> questions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                questions.add(mapResultSetToQuestion(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all questions", e);
        }
        return questions;
    }

    public boolean updateAnswer(Long questionId, Long expertId, String expertName, String answerText) {
        String sql = "UPDATE questions SET answered_by_expert_id = ?, expert_name = ?, " +
                "answer_text = ?, answered_date = ?, status = 'Answered' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (expertId != null) {
                stmt.setLong(1, expertId);
            } else {
                stmt.setNull(1, Types.BIGINT);
            }
            stmt.setString(2, expertName);
            stmt.setString(3, answerText);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(5, questionId);

            int affected = stmt.executeUpdate();
            logger.info("Question {} answered by expert {}", questionId, expertName);
            return affected > 0;

        } catch (SQLException e) {
            logger.error("Error updating question answer", e);
            return false;
        }
    }

    public boolean updateAIAnswer(Long questionId, String aiAnswerText) {
        String sql = "UPDATE questions SET ai_answer_text = ?, ai_answered_date = ?, " +
                "ai_generated = TRUE, status = 'Answered' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, aiAnswerText);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, questionId);

            int affected = stmt.executeUpdate();
            logger.info("Question {} answered by AI", questionId);
            return affected > 0;

        } catch (SQLException e) {
            logger.error("Error updating AI answer", e);
            return false;
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM questions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            logger.error("Error deleting question: {}", id, e);
            return false;
        }
    }

    private Question mapResultSetToQuestion(ResultSet rs) throws SQLException {
        Question question = new Question();
        question.setId(rs.getLong("id"));
        question.setFarmerId(rs.getLong("farmer_id"));
        question.setFarmerName(rs.getString("farmer_name"));
        question.setCategory(rs.getString("category"));
        question.setQuestionText(rs.getString("question_text"));
        question.setImageUrl(rs.getString("image_url"));
        question.setStatus(rs.getString("status"));

        Timestamp askedTimestamp = rs.getTimestamp("asked_date");
        if (askedTimestamp != null) {
            question.setAskedDate(askedTimestamp.toLocalDateTime());
        }

        Long expertId = rs.getLong("answered_by_expert_id");
        if (!rs.wasNull()) {
            question.setAnsweredByExpertId(expertId);
        }

        question.setExpertName(rs.getString("expert_name"));
        question.setAnswerText(rs.getString("answer_text"));

        Timestamp answeredTimestamp = rs.getTimestamp("answered_date");
        if (answeredTimestamp != null) {
            question.setAnsweredDate(answeredTimestamp.toLocalDateTime());
        }

        // Get AI generated flag
        boolean aiGenerated = rs.getBoolean("ai_generated");
        question.setAiGenerated(aiGenerated);

        // Get AI answer fields
        question.setAiAnswerText(rs.getString("ai_answer_text"));
        Timestamp aiAnsweredTimestamp = rs.getTimestamp("ai_answered_date");
        if (aiAnsweredTimestamp != null) {
            question.setAiAnsweredDate(aiAnsweredTimestamp.toLocalDateTime());
        }

        return question;
    }

    public List<Question> getAllQuestions() {
        String sql = "SELECT * FROM questions ORDER BY asked_date DESC";
        List<Question> questions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                questions.add(mapResultSetToQuestion(rs));
            }
            logger.info("Retrieved {} questions", questions.size());

        } catch (SQLException e) {
            logger.error("Error retrieving all questions", e);
        }

        return questions;
    }

    public int deleteAllByFarmerId(Long farmerId) {
        String sql = "DELETE FROM questions WHERE farmer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, farmerId);
            int deletedCount = stmt.executeUpdate();
            logger.info("Deleted {} questions for farmer ID: {}", deletedCount, farmerId);
            return deletedCount;

        } catch (SQLException e) {
            logger.error("Error deleting all questions for farmer ID: {}", farmerId, e);
            throw new RuntimeException("Failed to delete questions", e);
        }
    }
}
