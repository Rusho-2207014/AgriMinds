# Multiple Expert Answers Feature

## Overview

This feature allows **multiple experts** to answer the same farmer question. Previously, only one expert could answer a question. Now, farmers can receive answers from multiple experts, giving them diverse perspectives and expertise.

## Problem Solved

**Before**: When an expert answered a question, the answer was stored in the `questions` table's `answer_text` column. If another expert tried to answer, their response would overwrite the first expert's answer.

**Now**: Expert answers are stored in a separate `expert_answers` table with a one-to-many relationship to questions. Each expert can provide their own answer, and all answers are preserved and displayed.

## Database Changes

### New Table: `expert_answers`

```sql
CREATE TABLE expert_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    expert_id BIGINT NOT NULL,
    expert_name VARCHAR(100) NOT NULL,
    answer_text TEXT NOT NULL,
    answered_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    INDEX idx_question_id (question_id),
    INDEX idx_expert_id (expert_id)
);
```

### Existing Table

The `questions` table still has `answer_text`, `expert_name`, `answered_by_expert_id` columns for backward compatibility, but new expert answers use the `expert_answers` table.

## Code Changes

### 1. New Model: `ExpertAnswer.java`

```java
Location: src/main/java/com/agriminds/model/ExpertAnswer.java

Fields:
- id: Unique identifier
- questionId: Reference to the question
- expertId: ID of the expert who answered
- expertName: Name of the expert
- answerText: The expert's answer
- answeredDate: Timestamp when answered
- createdAt: Record creation time
```

### 2. New Repository: `ExpertAnswerRepository.java`

```java
Location: src/main/java/com/agriminds/repository/ExpertAnswerRepository.java

Key Methods:
- save(ExpertAnswer): Save a new expert answer
- getAnswersByQuestionId(Long): Get all answers for a question
- getAnswersByExpertId(Long): Get all answers by an expert
- hasExpertAnswered(Long questionId, Long expertId): Check if expert already answered
- getAnswerCount(Long questionId): Count answers for a question
- deleteByQuestionId(Long): Delete all answers for a question
```

### 3. Updated: `ExpertController.java`

**Changes:**

- Added `ExpertAnswerRepository` field
- `showAnswerDialog()`: Now saves to `expert_answers` table and prevents duplicate answers from same expert
- `createQuestionCard()`: Shows answer count badge (e.g., "2 Expert Answers")
- Answer button logic: Only shows if current expert hasn't answered yet
- View button: Shows "View All Answers" when multiple answers exist
- `showViewAnswerDialog()`: Displays all expert answers with numbering
- `loadMyAnswers()`: Loads expert's own answers from new table

### 4. Updated: `QuestionsController.java`

**Changes:**

- Added `ExpertAnswerRepository` field
- `showAnswerDialog()`: Displays all expert answers from `expert_answers` table
- Shows each expert answer in a separate green box
- Numbered as "Expert Answer #1", "Expert Answer #2", etc. when multiple exist
- Still displays AI answer in blue box (if exists)

### 5. Updated: `QuestionRepository.java`

**Added:**

- `getQuestionById(Long id)`: Helper method that returns Question directly (not Optional)

## User Experience

### For Farmers (Question Askers):

1. **Ask a Question**: Same as before
2. **View Answers**: Click "View Answer" button
   - See AI answer (if generated) in blue box
   - See ALL expert answers in green boxes
   - Each expert answer shows:
     - Expert's name
     - Answer text
     - Date and time answered
3. **Multiple Perspectives**: Benefit from diverse expert opinions on the same question

### For Experts:

1. **Browse Questions**: See all open questions
2. **Question Status Badges**:
   - "Open": No answers yet
   - "AI Answered": Only AI has answered
   - "1 Expert Answer": One expert has answered
   - "2 Expert Answers": Multiple experts answered
3. **Answer Questions**:
   - Can answer any question (even if other experts or AI already answered)
   - See existing AI answer before writing (if it exists)
   - Cannot answer same question twice (prevented by system)
   - Get warning if trying to answer question they already answered
4. **View Answers**: See all AI and expert answers for any question
5. **My Answers Tab**: View only your own submitted answers

## Benefits

### 1. **Diverse Expertise**

- Farmers get multiple professional opinions
- Different experts may focus on different aspects
- Increases knowledge quality and depth

### 2. **No Data Loss**

- All expert answers are preserved
- No overwriting of previous answers
- Complete answer history maintained

### 3. **Expert Collaboration**

- Experts can see others' answers
- Builds collective knowledge base
- Encourages comprehensive responses

### 4. **Better Decision Making**

- Farmers can compare different expert approaches
- Multiple solutions to same problem
- Increased confidence in decisions

### 5. **Fair Participation**

- All experts can contribute equally
- No "first come, first served" limitation
- Encourages active expert participation

## Technical Details

### Database Relationships

```
questions (1) ----< expert_answers (many)
    ^                     |
    |                     |
    id  <----  question_id
```

### Answer Flow

1. Expert clicks "Answer This Question"
2. System checks if expert already answered this question
3. If yes: Show warning "You have already answered this question!"
4. If no: Show answer dialog (with AI answer if exists)
5. Expert writes answer and submits
6. System saves to `expert_answers` table
7. Question card updates to show new answer count

### Duplicate Prevention

- Before saving, system checks `hasExpertAnswered(questionId, expertId)`
- If true, shows warning and prevents duplicate
- Each expert can only answer once per question
- No limit on number of different experts answering

### Display Logic

**Farmer View:**

- Loop through all expert answers for question
- Display each in separate box
- Number answers if multiple exist
- Show most helpful/complete answer first (ordered by date)

**Expert View:**

- Show answer count in status badge
- "Add Your Answer" if current expert hasn't answered
- "Add Another Answer" if others have answered
- "View All Answers" to see everyone's responses

## Testing Scenarios

### Scenario 1: Multiple Experts Answer Same Question

1. Farmer asks: "What fertilizer for tomatoes?"
2. Expert A answers: "Use nitrogen-rich fertilizer"
3. Expert B answers: "I recommend organic compost"
4. Expert C answers: "Consider soil pH first"
5. Farmer sees all 3 answers when viewing

### Scenario 2: Expert Tries to Answer Twice

1. Expert A answers a question
2. Expert A tries to answer same question again
3. System shows: "You have already answered this question!"
4. Answer submission prevented

### Scenario 3: AI + Multiple Experts

1. Farmer generates AI answer first
2. Expert A adds professional answer
3. Expert B adds another perspective
4. Farmer sees:
   - 🤖 AI Answer (blue box)
   - 👨‍🌾 Expert Answer #1 by Expert A (green box)
   - 👨‍🌾 Expert Answer #2 by Expert B (green box)

### Scenario 4: Expert Views Others' Answers

1. Expert A answers question
2. Expert B views question
3. Expert B sees:
   - Existing AI answer (if any)
   - Existing expert answers (including Expert A's)
   - Can still add their own answer
4. Expert B adds different perspective

## Migration from Old System

### Existing Data

- Old answers in `questions.answer_text` remain intact
- New expert answers go to `expert_answers` table
- System supports both (backward compatible)

### Recommendation

For clean data, consider:

1. Backup database
2. Migrate old expert answers to `expert_answers` table
3. Clear old `answer_text` columns (optional)
4. Or keep both for historical reference

## Future Enhancements

### Possible Improvements

1. **Answer Voting**: Let farmers rate expert answers
2. **Best Answer**: Mark one answer as "best" or "most helpful"
3. **Expert Ranking**: Show expert's success rate
4. **Answer Editing**: Allow experts to edit their own answers
5. **Comments**: Let experts comment on each other's answers
6. **Notifications**: Alert experts when others answer same question

## Code Files Changed

### New Files

- `ExpertAnswer.java` - Model class
- `ExpertAnswerRepository.java` - Database operations

### Modified Files

- `ExpertController.java` - Expert dashboard logic
- `QuestionsController.java` - Farmer dashboard display
- `QuestionRepository.java` - Added getQuestionById method

### Database

- Added `expert_answers` table with foreign key to `questions`

## Summary

This feature transforms the Q&A system from single-expert to **multi-expert**, enabling:

- ✅ Multiple experts answering same question
- ✅ All answers preserved and displayed
- ✅ Duplicate prevention (one answer per expert)
- ✅ Better farmer experience with diverse opinions
- ✅ Backward compatible with existing system
- ✅ Works alongside AI answers

Farmers now get comprehensive answers from multiple experts plus AI, making better-informed agricultural decisions! 🌾👨‍🌾
