package com.agriminds.repository;

import com.agriminds.model.Rating;
import com.agriminds.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing expert ratings
 */
public class RatingRepository {
    private static final Logger logger = LoggerFactory.getLogger(RatingRepository.class);

    public RatingRepository() {
        // Auto-create table if it doesn't exist
        createTableIfNotExists();
    }

    /**
     * Create expert_ratings table if it doesn't exist
     */
    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS expert_ratings (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "expert_id BIGINT NOT NULL, " +
                "farmer_id BIGINT NOT NULL, " +
                "expert_answer_id BIGINT NOT NULL, " +
                "rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5), " +
                "comment TEXT, " +
                "rated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (expert_id) REFERENCES experts(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (farmer_id) REFERENCES farmers(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (expert_answer_id) REFERENCES expert_answers(id) ON DELETE CASCADE, " +
                "UNIQUE KEY unique_farmer_answer (farmer_id, expert_answer_id), " +
                "INDEX idx_expert (expert_id), " +
                "INDEX idx_farmer (farmer_id), " +
                "INDEX idx_rating (rating)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("expert_ratings table verified/created successfully");
        } catch (SQLException e) {
            logger.error("Error creating expert_ratings table", e);
        }
    }

    /**
     * Save a new rating or update existing one
     */
    public Rating save(Rating rating) {
        // Check if farmer already rated this answer
        Rating existing = getRatingByFarmerAndAnswer(rating.getFarmerId(), rating.getExpertAnswerId());

        if (existing != null) {
            // Update existing rating
            return update(existing.getId(), rating.getRating(), rating.getComment());
        } else {
            // Insert new rating
            return insert(rating);
        }
    }

    private Rating insert(Rating rating) {
        String sql = "INSERT INTO expert_ratings (expert_id, farmer_id, expert_answer_id, rating, comment, rated_date) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, rating.getExpertId());
            stmt.setLong(2, rating.getFarmerId());
            stmt.setLong(3, rating.getExpertAnswerId());
            stmt.setInt(4, rating.getRating());
            stmt.setString(5, rating.getComment());
            // Set current time if ratedDate is null
            java.time.LocalDateTime ratedDate = rating.getRatedDate() != null ? rating.getRatedDate()
                    : java.time.LocalDateTime.now();
            stmt.setTimestamp(6, Timestamp.valueOf(ratedDate));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        rating.setId(generatedKeys.getLong(1));
                        logger.info("Rating saved: Expert {} rated {} stars by farmer {}",
                                rating.getExpertId(), rating.getRating(), rating.getFarmerId());
                        return rating;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving rating", e);
        }
        return null;
    }

    private Rating update(Long ratingId, Integer newRating, String newComment) {
        String sql = "UPDATE expert_ratings SET rating = ?, comment = ?, rated_date = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newRating);
            stmt.setString(2, newComment);
            stmt.setTimestamp(3, Timestamp.valueOf(java.time.LocalDateTime.now()));
            stmt.setLong(4, ratingId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Rating updated: ID {}", ratingId);
                return getRatingById(ratingId);
            }
        } catch (SQLException e) {
            logger.error("Error updating rating", e);
        }
        return null;
    }

    /**
     * Get rating by farmer and answer (to check if already rated)
     */
    public Rating getRatingByFarmerAndAnswer(Long farmerId, Long expertAnswerId) {
        String sql = "SELECT * FROM expert_ratings WHERE farmer_id = ? AND expert_answer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, farmerId);
            stmt.setLong(2, expertAnswerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToRating(rs);
            }
        } catch (SQLException e) {
            logger.error("Error getting rating for farmer {} and answer {}", farmerId, expertAnswerId, e);
        }
        return null;
    }

    /**
     * Get rating by ID
     */
    public Rating getRatingById(Long id) {
        String sql = "SELECT * FROM expert_ratings WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToRating(rs);
            }
        } catch (SQLException e) {
            logger.error("Error getting rating by ID: {}", id, e);
        }
        return null;
    }

    /**
     * Get average rating for an expert
     */
    public Double getAverageRating(Long expertId) {
        String sql = "SELECT AVG(rating) as avg_rating FROM expert_ratings WHERE expert_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, expertId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double avg = rs.getDouble("avg_rating");
                if (!rs.wasNull()) {
                    return avg;
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting average rating for expert: {}", expertId, e);
        }
        return null;
    }

    /**
     * Get rating count for an expert
     */
    public int getRatingCount(Long expertId) {
        String sql = "SELECT COUNT(*) as count FROM expert_ratings WHERE expert_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, expertId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            logger.error("Error getting rating count for expert: {}", expertId, e);
        }
        return 0;
    }

    /**
     * Get count of rated answers for an expert (distinct answers that received
     * ratings)
     */
    public int getRatedAnswersCount(Long expertId) {
        String sql = "SELECT COUNT(DISTINCT expert_answer_id) as count FROM expert_ratings WHERE expert_id = ? AND expert_answer_id IS NOT NULL";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, expertId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            logger.error("Error getting rating count for expert: {}", expertId, e);
        }
        return 0;
    }

    /**
     * Get all ratings for an expert
     */
    public List<Rating> getRatingsByExpert(Long expertId) {
        List<Rating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM expert_ratings WHERE expert_id = ? ORDER BY rated_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, expertId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ratings.add(mapResultSetToRating(rs));
            }

            logger.info("Retrieved {} ratings for expert: {}", ratings.size(), expertId);
        } catch (SQLException e) {
            logger.error("Error getting ratings for expert: {}", expertId, e);
        }
        return ratings;
    }

    /**
     * Get ratings with full details (question, answer, farmer name) for an expert
     */
    public List<RatingDetails> getRatingsWithDetails(Long expertId) {
        List<RatingDetails> ratingDetails = new ArrayList<>();
        System.out.println(">>> getRatingsWithDetails called for expertId: " + expertId);
        logger.info("getRatingsWithDetails called for expertId: {}", expertId);

        String sql = "SELECT r.*, ea.answer_text, q.question_text, " +
                "f.full_name as farmer_name " +
                "FROM expert_ratings r " +
                "JOIN expert_answers ea ON r.expert_answer_id = ea.id " +
                "JOIN questions q ON ea.question_id = q.id " +
                "JOIN farmers f ON r.farmer_id = f.id " +
                "WHERE r.expert_id = ? " +
                "ORDER BY r.rated_date DESC";

        System.out.println(">>> SQL: " + sql);

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, expertId);
            System.out.println(">>> Executing query for expertId: " + expertId);

            ResultSet rs = stmt.executeQuery();
            System.out.println(">>> Query executed, reading results...");

            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                System.out.println(">>> Processing row " + rowCount);
                try {
                    RatingDetails details = new RatingDetails();
                    details.setRatingId(rs.getLong("id"));
                    details.setRating(rs.getInt("rating"));
                    details.setComment(rs.getString("comment"));

                    Timestamp ts = rs.getTimestamp("rated_date");
                    if (ts != null) {
                        details.setRatedDate(ts.toLocalDateTime());
                    }

                    details.setQuestionTitle(rs.getString("question_text"));
                    details.setAnswer(rs.getString("answer_text"));
                    details.setFarmerName(rs.getString("farmer_name"));
                    ratingDetails.add(details);
                    System.out.println(">>> Successfully added rating detail for row " + rowCount);
                } catch (Exception e) {
                    System.out.println(">>> ERROR processing row " + rowCount + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println(">>> Total rows processed: " + rowCount);
            System.out.println(">>> Total rating details in list: " + ratingDetails.size());

            logger.info("Retrieved {} rating details for expert: {}", ratingDetails.size(), expertId);
        } catch (SQLException e) {
            System.out.println(">>> SQL ERROR: " + e.getMessage());
            e.printStackTrace();
            logger.error("Error getting rating details for expert: {}", expertId, e);
        }
        return ratingDetails;
    }

    /**
     * Inner class to hold rating details with question/answer info
     */
    public static class RatingDetails {
        private Long ratingId;
        private int rating;
        private String comment;
        private java.time.LocalDateTime ratedDate;
        private String questionTitle;
        private String questionDescription;
        private String answer;
        private String farmerName;

        public Long getRatingId() {
            return ratingId;
        }

        public void setRatingId(Long ratingId) {
            this.ratingId = ratingId;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public java.time.LocalDateTime getRatedDate() {
            return ratedDate;
        }

        public void setRatedDate(java.time.LocalDateTime ratedDate) {
            this.ratedDate = ratedDate;
        }

        public String getQuestionTitle() {
            return questionTitle;
        }

        public void setQuestionTitle(String questionTitle) {
            this.questionTitle = questionTitle;
        }

        public String getQuestionDescription() {
            return questionDescription;
        }

        public void setQuestionDescription(String questionDescription) {
            this.questionDescription = questionDescription;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public String getFarmerName() {
            return farmerName;
        }

        public void setFarmerName(String farmerName) {
            this.farmerName = farmerName;
        }
    }

    /**
     * Format rating display with stars
     */
    public static String formatRating(Double avgRating, int count) {
        if (avgRating == null || count == 0) {
            return "⭐ No ratings yet";
        }

        // Round to 2 decimal places
        double rounded = Math.round(avgRating * 100.0) / 100.0;

        // Create star display
        int fullStars = (int) Math.floor(rounded);
        boolean halfStar = (rounded - fullStars) >= 0.5;

        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            stars.append("⭐");
        }
        if (halfStar && fullStars < 5) {
            stars.append("⭐");
        }

        return String.format("%s %.2f (%d)", stars.toString(), rounded, count);
    }

    private Rating mapResultSetToRating(ResultSet rs) throws SQLException {
        Rating rating = new Rating();
        rating.setId(rs.getLong("id"));
        rating.setExpertId(rs.getLong("expert_id"));
        rating.setFarmerId(rs.getLong("farmer_id"));
        rating.setExpertAnswerId(rs.getLong("expert_answer_id"));
        rating.setRating(rs.getInt("rating"));
        rating.setComment(rs.getString("comment"));
        rating.setRatedDate(rs.getTimestamp("rated_date").toLocalDateTime());
        return rating;
    }
}
