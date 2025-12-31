# Question Deletion and Rating Preservation

## Question: What happens when a farmer deletes a question?

### Answer:

**YES, ratings are preserved even after question deletion!**

## Database Behavior

### When a farmer deletes their question:

1. **Question is deleted** - The question entry is removed from the `questions` table
2. **Expert answers are deleted** - All answers to that question are removed (CASCADE)
3. **Ratings are PRESERVED** ✅ - The ratings remain in the database with `expert_answer_id` set to `NULL`

### Why ratings are preserved:

- The `expert_ratings` table has a foreign key constraint with `ON DELETE SET NULL`
- When an expert answer is deleted, the `expert_answer_id` column in ratings is set to NULL instead of deleting the rating
- This ensures the expert's achievement (5-star ratings) and statistics are not lost

## Technical Details

### Database Schema:

```sql
CREATE TABLE expert_ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expert_id BIGINT NOT NULL,
    farmer_id BIGINT NOT NULL,
    expert_answer_id BIGINT NULL,  -- Nullable!
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    rated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (expert_answer_id) REFERENCES expert_answers(id) ON DELETE SET NULL
);
```

### Foreign Key Constraint:

```sql
CONSTRAINT fk_expert_answer
FOREIGN KEY (expert_answer_id)
REFERENCES expert_answers(id)
ON DELETE SET NULL
```

## What is Preserved:

✅ **Rating value (1-5 stars)**
✅ **Expert ID**
✅ **Farmer ID**
✅ **Comment/feedback**
✅ **Rating date**
✅ **Expert's rating statistics**
✅ **Certificate eligibility** (5-star ratings still count)

## What is Lost:

❌ **Link to the original answer** (expert_answer_id becomes NULL)
❌ **Question text** (deleted from questions table)
❌ **Answer text** (deleted from expert_answers table)

## Impact on Features:

### ✅ Works Normally:

- Expert's average rating calculation
- Expert's total rating count
- 5-star certificate download
- Expert ranking/leaderboard
- Expert profile statistics

### ⚠️ Limited Functionality:

- Rating details may show "Question deleted" if expert_answer_id is NULL
- Cannot view the original question/answer that was rated
- Rating history shows only preserved data (rating, farmer name, date)

## Expert Dashboard Display:

When viewing ratings with deleted questions/answers:

- **Rating card will still show:**
  - Star rating
  - Farmer name
  - Rating date
  - Farmer's comment/feedback
- **Rating card will indicate:**
  - "Question: [Deleted]" or hide question section
  - "Answer: [Deleted]" or hide answer section

## Migration Applied:

```sql
-- Fix rating preservation when questions/answers are deleted
ALTER TABLE expert_ratings
DROP FOREIGN KEY expert_ratings_ibfk_3;

ALTER TABLE expert_ratings
MODIFY COLUMN expert_answer_id BIGINT NULL;

ALTER TABLE expert_ratings
ADD CONSTRAINT fk_expert_answer
FOREIGN KEY (expert_answer_id)
REFERENCES expert_answers(id)
ON DELETE SET NULL;
```

## Benefits:

1. **Fair to experts** - Their achievements aren't lost due to farmer actions
2. **Accurate statistics** - Expert ratings remain accurate over time
3. **Certificate preservation** - 5-star achievements persist for certificates
4. **Accountability** - Rating history is maintained
5. **Farmer privacy** - Farmers can delete sensitive questions without guilt

## Summary:

When a farmer deletes a question from their dashboard:

- ❌ Question disappears from expert's dashboard (no longer visible)
- ✅ Ratings are SAVED and contribute to expert's statistics
- ✅ Expert can still download certificates for 5-star ratings
- ✅ Expert's average rating and rating count remain accurate
