package com.agriminds.model;

import java.time.LocalDateTime;

/**
 * Represents a rating given by a farmer to an expert for their answer
 */
public class Rating {
    private Long id;
    private Long expertId;
    private Long farmerId;
    private Long expertAnswerId; // Which answer was rated
    private Integer rating; // 1-5 stars
    private String comment; // Optional feedback
    private LocalDateTime ratedDate;

    public Rating() {
    }

    public Rating(Long expertId, Long farmerId, Long expertAnswerId, Integer rating) {
        this.expertId = expertId;
        this.farmerId = farmerId;
        this.expertAnswerId = expertAnswerId;
        this.rating = rating;
        this.ratedDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExpertId() {
        return expertId;
    }

    public void setExpertId(Long expertId) {
        this.expertId = expertId;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public Long getExpertAnswerId() {
        return expertAnswerId;
    }

    public void setExpertAnswerId(Long expertAnswerId) {
        this.expertAnswerId = expertAnswerId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getRatedDate() {
        return ratedDate;
    }

    public void setRatedDate(LocalDateTime ratedDate) {
        this.ratedDate = ratedDate;
    }

    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", expertId=" + expertId +
                ", farmerId=" + farmerId +
                ", expertAnswerId=" + expertAnswerId +
                ", rating=" + rating +
                ", ratedDate=" + ratedDate +
                '}';
    }
}
