# Expert Rating System - Complete Guide

## Overview

The AgriMinds platform now includes a **comprehensive rating system** where farmers can rate expert answers from 1 to 5 stars. The average rating and total rating count are displayed beside expert names throughout the application.

## Features

### ⭐ 1-5 Star Rating System

- Farmers can rate any expert answer they receive
- Rating scale: 1 star (poor) to 5 stars (excellent)
- Optional feedback/comment with each rating
- Can update ratings after initial submission

### 📊 Average Rating Display

- Shows average rating with star symbols
- Displays total number of ratings
- Format: `⭐⭐⭐⭐ 4.5 (23)` = 4.5 stars from 23 ratings
- Visible beside expert names everywhere in the app

### 🔄 Real-time Updates

- Ratings update immediately after submission
- Average recalculated automatically
- Refreshes in all views

## Where Ratings Are Displayed

### 1. **Farmer Dashboard - Question Answers**

When viewing expert answers to questions:

```
👨‍🌾 Answered by: Dr. Abdul Karim ⭐⭐⭐⭐⭐ 4.8 (45)
[Answer text here]
[💬 Reply] [⭐ Rate Answer]
```

### 2. **Expert Dashboard - Welcome Message**

```
Welcome, Dr. Abdul Karim ⭐⭐⭐⭐⭐ 4.8 (45)
```

### 3. **Expert Dashboard - View Answers**

When experts view other experts' answers:

```
By: Dr. Fatima Rahman ⭐⭐⭐⭐ 4.2 (18) • Dec 28, 2:30 PM
```

### 4. **Messages Tab - Conversation List**

For farmers viewing expert conversations:

```
Dr. Abdul Karim ⭐⭐⭐⭐⭐ 4.8 (45)
"Thanks for the helpful advice..."
```

### 5. **No Ratings Yet**

```
Dr. New Expert ⭐ No ratings yet
```

## How to Rate an Expert

### Step-by-Step (Farmer):

1. **View an answered question**

   - Go to Questions tab
   - Click "View Answer" on any question with expert answers

2. **Find the expert answer you want to rate**

   - Scroll to the expert answer section
   - See the expert's current rating

3. **Click "⭐ Rate Answer" button**

   - Orange button beside "Reply" button
   - Opens rating dialog

4. **Select star rating**

   - Choose 1-5 stars
   - Click on the star visualization
   - Or select radio button below stars

5. **Add optional feedback (recommended)**

   - Type comments in the text area
   - Share what was helpful or what could be improved

6. **Submit rating**
   - Click "Submit Rating" button
   - See confirmation message
   - Rating appears immediately

### Updating a Rating:

1. Click "⭐ Rate Answer" again on same answer
2. Dialog shows your previous rating
3. Select new star rating
4. Update feedback if desired
5. Click "Update Rating"
6. New rating replaces old one

## Rating Dialog Interface

```
┌─────────────────────────────────────┐
│ Rate Expert Answer                  │
│ Rate the answer from Dr. Karim      │
├─────────────────────────────────────┤
│ How helpful was this answer?        │
│                                     │
│   ⭐    ⭐⭐   ⭐⭐⭐  ⭐⭐⭐⭐ ⭐⭐⭐⭐⭐  │
│   ○      ○      ○       ○      ●    │
│                                     │
│ Optional feedback:                  │
│ ┌─────────────────────────────────┐ │
│ │ Very helpful! Clear explanation │ │
│ │ and solved my problem.          │ │
│ └─────────────────────────────────┘ │
│                                     │
│   [Submit Rating]  [Cancel]         │
└─────────────────────────────────────┘
```

## Technical Implementation

### Database Schema

**expert_ratings table:**

```sql
CREATE TABLE expert_ratings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    expert_id BIGINT NOT NULL,              -- Which expert was rated
    farmer_id BIGINT NOT NULL,              -- Who gave the rating
    expert_answer_id BIGINT NOT NULL,       -- Which answer was rated
    rating INT NOT NULL CHECK (1-5),        -- Star rating
    comment TEXT,                           -- Optional feedback
    rated_date TIMESTAMP,                   -- When rated
    UNIQUE (farmer_id, expert_answer_id)    -- One rating per answer per farmer
);
```

### Model Classes

**Rating.java:**

```java
public class Rating {
    private Long id;
    private Long expertId;
    private Long farmerId;
    private Long expertAnswerId;
    private Integer rating;          // 1-5
    private String comment;
    private LocalDateTime ratedDate;
}
```

### Repository Methods

**RatingRepository.java:**

```java
// Save or update rating
Rating save(Rating rating)

// Get rating by farmer and answer
Rating getRatingByFarmerAndAnswer(Long farmerId, Long expertAnswerId)

// Get average rating for expert
Double getAverageRating(Long expertId)

// Get total rating count for expert
int getRatingCount(Long expertId)

// Get all ratings for expert
List<Rating> getRatingsByExpert(Long expertId)

// Format rating for display
String formatRating(Double avgRating, int count)
```

### Controllers Updated

1. **QuestionsController.java** - Farmer dashboard

   - Added `ratingRepository` field
   - Shows expert ratings beside names
   - "Rate Answer" button for each expert answer
   - `openRatingDialog()` method for rating submission

2. **ExpertController.java** - Expert dashboard

   - Added `ratingRepository` field
   - Shows expert's own rating in welcome message
   - Shows other experts' ratings in answer views

3. **MessagesController.java** - Messages tab
   - Added `ratingRepository` field
   - Shows expert ratings in conversation list

## Rating Calculation

### Average Rating Formula:

```
Average = SUM(all ratings) / COUNT(ratings)
```

Example:

- 20 farmers gave 5 stars
- 10 farmers gave 4 stars
- 5 farmers gave 3 stars

Average = (20×5 + 10×4 + 5×3) / 35 = 4.4 stars

### Star Display Logic:

- **0-0.4**: ⭐ (1 star or less)
- **0.5-1.4**: ⭐⭐ (1-2 stars)
- **1.5-2.4**: ⭐⭐⭐ (2-3 stars)
- **2.5-3.4**: ⭐⭐⭐⭐ (3-4 stars)
- **3.5-4.4**: ⭐⭐⭐⭐⭐ (4-5 stars)
- **4.5-5.0**: ⭐⭐⭐⭐⭐ (full 5 stars)

Displayed as: `⭐⭐⭐⭐ 4.2 (18)` (stars, number, count)

## Business Rules

### Rating Constraints:

- ✅ **One rating per answer per farmer** - Can update but not duplicate
- ✅ **Rating required** - Must select 1-5 stars
- ✅ **Comment optional** - Feedback is encouraged but not required
- ✅ **Farmers only** - Only farmers can rate experts (not other experts)
- ✅ **Must have answer** - Can only rate actual expert answers

### Rating Updates:

- Farmers can change their rating anytime
- Updated rating replaces previous one (not added)
- Average recalculated automatically
- No notification to expert about updates

### Privacy:

- Expert sees average rating and count
- Expert cannot see individual farmer ratings
- Expert cannot see who rated what
- Comments are stored but not displayed (future feature)

## Setup Instructions

### 1. Run Database Migration:

```bash
mysql -u root -p agriminds_db < create_ratings_table.sql
```

This creates the `expert_ratings` table with proper constraints and indexes.

### 2. Compile Application:

```bash
mvn clean compile
```

### 3. Test the Rating System:

**As Farmer:**

1. Login to farmer account
2. Ask a question (or use existing)
3. Wait for expert to answer (or use existing answer)
4. Click "View Answer"
5. Click "⭐ Rate Answer" button
6. Select 5 stars
7. Add comment: "Very helpful!"
8. Click "Submit Rating"
9. See rating appear beside expert name

**As Expert:**

1. Login to expert account
2. Check welcome message - see your rating
3. Answer some questions
4. Encourage farmers to rate answers
5. Watch rating improve over time!

## Benefits

### For Farmers:

✅ **Identify best experts** - See which experts are highly rated
✅ **Provide feedback** - Share appreciation for good answers
✅ **Improve platform** - Help other farmers choose experts
✅ **Recognition** - Thank experts for their help

### For Experts:

✅ **Build reputation** - High ratings attract more farmers
✅ **Performance feedback** - Know if answers are helpful
✅ **Professional growth** - Understand what farmers value
✅ **Motivation** - Positive ratings encourage quality answers
✅ **Credibility** - Ratings displayed prominently

### For Platform:

✅ **Quality control** - Identify top-performing experts
✅ **User engagement** - Farmers interact more with ratings
✅ **Expert motivation** - Gamification through ratings
✅ **Data insights** - Track expert performance over time
✅ **Trust building** - Transparent quality indicators

## Analytics Possibilities (Future)

With the rating system in place, you can add:

- **Expert leaderboard** - Top rated experts
- **Rating trends** - Expert rating over time
- **Category ratings** - Ratings by crop type or issue
- **Badge system** - Awards for high ratings
  - 🥇 Gold Expert: 4.8+ average, 50+ ratings
  - 🥈 Silver Expert: 4.5+ average, 25+ ratings
  - 🥉 Bronze Expert: 4.0+ average, 10+ ratings
- **Rating requirements** - Minimum rating to answer questions
- **Expert rewards** - Bonus for highly-rated answers

## Future Enhancements

- [ ] Show rating breakdown (how many 5⭐, 4⭐, etc.)
- [ ] Display recent ratings/comments on expert profile
- [ ] Allow experts to respond to feedback
- [ ] Rating reminders for farmers
- [ ] Verify ratings (prevent spam/abuse)
- [ ] Rating analytics dashboard for admins
- [ ] Export rating reports
- [ ] Compare expert ratings in same specialty
- [ ] Farmer credibility score (helpful raters)
- [ ] Featured expert badges based on ratings
- [ ] Rating-based expert recommendations
- [ ] Email notifications for new ratings

## Best Practices

### For Farmers:

- Rate answers honestly and fairly
- Provide constructive feedback in comments
- Update rating if expert provides additional help
- Rate based on answer quality, not personal preference

### For Experts:

- Focus on providing quality, helpful answers
- Don't obsess over ratings - focus on helping farmers
- Learn from lower ratings to improve
- Maintain professionalism regardless of ratings

## Troubleshooting

**Issue: Rating button not appearing**

- Check if answer is from an expert (not AI)
- Verify you're logged in as farmer
- Refresh the questions list

**Issue: "Already rated" but want to update**

- Click same "Rate Answer" button again
- Dialog will show "Update Rating"
- Change stars and click "Update Rating"

**Issue: Rating not updating immediately**

- Close and reopen answer dialog
- Click refresh in dashboard
- Rating is saved in database immediately

**Issue: Expert doesn't see rating**

- Expert sees average only, not individual ratings
- Must close and reopen dashboard to refresh
- Rating appears in welcome message

## Summary

The rating system provides:

- ⭐ **1-5 star ratings** for expert answers
- 📊 **Average ratings** displayed beside expert names
- 💬 **Optional feedback** with each rating
- 🔄 **Update capability** to change ratings
- 🎯 **Quality indicator** throughout the app

This helps farmers identify trustworthy experts and motivates experts to provide high-quality answers!
