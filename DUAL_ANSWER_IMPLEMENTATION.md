# Dual Answer System Implementation

## Overview

This document describes the implementation of the dual answer system that allows both AI-generated answers and expert answers to coexist for the same question.

## Problem

Previously, when AI generated an answer, it would overwrite any existing expert answer, and vice versa. Experts couldn't answer questions that already had AI answers.

## Solution

Implemented separate storage and display for both AI and expert answers.

## Database Changes

### New Columns in `questions` Table

- `ai_answer_text` (TEXT): Stores the AI-generated answer
- `ai_answered_date` (DATETIME): Timestamp when AI answered the question

### Existing Columns (for Expert Answers)

- `answer_text` (TEXT): Stores the expert's answer
- `expert_name` (VARCHAR): Name of the expert who answered
- `answered_by_expert_id` (BIGINT): ID of the expert
- `answered_date` (TIMESTAMP): Timestamp when expert answered
- `ai_generated` (BOOLEAN): Flag indicating if AI answer exists

## Code Changes

### 1. Model Layer (`Question.java`)

**Added Fields:**

```java
private String aiAnswerText;
private LocalDateTime aiAnsweredDate;
```

**With Getters and Setters**

### 2. Repository Layer (`QuestionRepository.java`)

**Updated Methods:**

- `save()`: Now handles 14 parameters including AI answer fields
- `mapResultSetToQuestion()`: Reads both AI and expert answer fields from database

**New Method:**

- `updateAIAnswer(Long questionId, String aiAnswer)`: Updates only AI answer fields without touching expert answer

**Existing Method:**

- `updateAnswer()`: Still updates only expert answer fields (unchanged behavior)

### 3. Controller Layer

#### `QuestionsController.java` (Farmer View)

**Updated:**

- `generateAIAnswer()`: Now calls `updateAIAnswer()` instead of `updateAnswer()`
  - Sets `aiAnswerText` and `aiAnsweredDate` fields
  - Does not overwrite expert answer

**Updated:**

- `showAnswerDialog()`: Completely redesigned to show both answers
  - Shows AI answer in blue box (if exists)
  - Shows expert answer in green box (if exists)
  - Shows both with clear separation if both exist
  - Shows "No answer yet" if neither exists

#### `ExpertController.java` (Expert View)

**Updated:**

- `createQuestionCard()`: Shows question status as "AI Answered", "Expert Answered", or "Open"
  - Allows experts to add their answer even if AI already answered
  - Shows "Add Expert Answer" button for AI-answered questions

**Updated:**

- `showAnswerDialog()`: Shows existing AI answer (if any) when expert is writing their answer
  - Helps experts see what AI suggested before providing their expertise
  - Displays AI answer in a read-only text area

**Updated:**

- `showViewAnswerDialog()`: Displays both AI and expert answers with clear labels
  - Shows both answers with timestamps
  - Clearly distinguishes between AI and expert responses

## User Experience

### For Farmers:

1. **View Answers**: Click "View Answer" to see all available answers (AI + Expert)

   - AI answers shown in blue with robot emoji 🤖
   - Expert answers shown in green with farmer emoji 👨‍🌾
   - Both displayed together if both exist

2. **Generate AI Answer**: Click "AI Generated Answer" button
   - AI answer saved separately, doesn't affect expert answers
   - Can still get expert answer later

### For Experts:

1. **Question Status**: Clear badges show question state

   - "Open": No answers yet
   - "AI Answered": Only AI has answered (can still add expert answer)
   - "Expert Answered": Expert has provided answer

2. **Answer Questions**:

   - Can answer any question, even if AI already answered
   - See AI's answer before writing (if it exists)
   - Expert answer saved separately from AI answer

3. **View Answers**: See both AI and expert answers for any question
   - Clear separation and labeling
   - Timestamps for both

## Technical Details

### Database Schema

```sql
ALTER TABLE questions
ADD COLUMN ai_answer_text TEXT AFTER answer_text,
ADD COLUMN ai_answered_date DATETIME AFTER ai_answer_text;
```

### Key Design Decisions

1. **Separate Storage**: AI and expert answers in separate columns

   - `ai_answer_text` vs `answer_text`
   - `ai_answered_date` vs `answered_date`

2. **Independent Updates**:

   - `updateAIAnswer()` only touches AI columns
   - `updateAnswer()` only touches expert columns
   - No mutual interference

3. **Status Logic**:

   - Question considered "unanswered by expert" if `answer_text` is NULL
   - AI answer doesn't prevent expert from answering
   - Both answers can coexist

4. **UI Separation**:
   - Different colors: Blue for AI, Green for Expert
   - Different icons: 🤖 for AI, 👨‍🌾 for Expert
   - Clear labels and timestamps

## Testing Checklist

- [x] Database migration applied successfully
- [x] Code compiles without errors
- [ ] Test Scenario 1: AI answers first, then expert adds answer
- [ ] Test Scenario 2: Expert answers first, then AI generates answer
- [ ] Test Scenario 3: Farmer views both answers
- [ ] Test Scenario 4: Expert sees AI answer before responding
- [ ] Test Scenario 5: Questions with only AI answer show as "unanswered" to experts

## Benefits

1. **No Data Loss**: Neither AI nor expert answers overwrite each other
2. **Faster Response**: Farmers get instant AI answers while waiting for expert
3. **Expert Value**: Experts can still provide their expertise even after AI answers
4. **Better Information**: Farmers get both AI and expert perspectives
5. **Flexibility**: System supports questions with AI only, expert only, or both

## Migration Instructions

If upgrading from previous version:

1. Backup database
2. Run migration SQL to add new columns
3. Deploy updated code
4. Test on development environment first
5. Existing questions will have NULL for new columns (normal)
