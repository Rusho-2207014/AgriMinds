# Answer-Specific Messaging System

## Overview

The messaging system has been updated to support **context-specific threaded conversations**. Now when a farmer clicks "Reply" on a specific expert's answer, those messages are tied to that particular answer, not just a general conversation with the expert.

## How It Works

### For Farmers:

1. **Ask a Question** - Post your agricultural question
2. **Multiple Experts Answer** - Different experts can provide their own answers
3. **Reply to Specific Answers** - Click "💬 Reply" on any expert's answer
4. **Threaded Conversations** - Each reply thread is specific to that expert's answer
5. **Back-and-Forth** - Continue the conversation with that expert about their specific answer

### For Experts:

1. **See Answer-Specific Messages** - When farmers reply to your answers, you see the context
2. **Reply in Context** - Your responses are tied to the specific answer/question
3. **Multiple Threads** - Can have separate conversations about different answers to the same question

## Key Features

### Conversation Context

- **Question ID** - Which question the conversation is about
- **Answer ID** - Which specific expert answer is being discussed
- **Expert ID** - Which expert is involved
- **Farmer ID** - Which farmer initiated the question

### Conversation ID Format

- General: `farmer_{farmerId}_expert_{expertId}`
- Answer-specific: `farmer_{farmerId}_answer_{expertAnswerId}`

### Benefits

1. **Organized Discussions** - Each expert answer has its own reply thread
2. **Clear Context** - Experts know exactly which answer farmers are referring to
3. **Multiple Conversations** - Farmer can discuss different aspects with different experts
4. **No Confusion** - Messages about Expert A's answer don't mix with Expert B's answer

## Database Changes

### New Columns in `messages` Table:

- `question_id` - Links message to the original question
- `expert_answer_id` - Links message to the specific expert answer

### Migration Script:

Run `add_message_context_columns.sql` to update your database:

```bash
mysql -u root -p agriminds_db < add_message_context_columns.sql
```

## Code Changes

### 1. Message Model

- Added `questionId` field
- Added `expertAnswerId` field
- New method: `generateAnswerConversationId(farmerId, expertAnswerId)`

### 2. QuestionsController

- Updated `openMessageDialog()` to accept question and answer IDs
- Messages now include context when sent
- Conversation ID uses answer ID instead of expert ID

### 3. MessagesController

- Detects answer-specific conversations
- Shows "💬 Reply Thread:" header for answer-specific chats
- Preserves question/answer context when replying

### 4. MessageRepository

- Updated `save()` to store question_id and expert_answer_id
- Updated `mapResultSetToMessage()` to retrieve new fields

## Example Flow

1. **Farmer asks**: "How do I treat rice blast disease?"
2. **Expert A answers**: "Use Tricyclazole fungicide..."
3. **Expert B answers**: "Try biological control with Trichoderma..."
4. **Farmer clicks Reply on Expert A's answer**:
   - Conversation ID: `farmer_5_answer_12`
   - Farmer: "What dosage should I use?"
   - Expert A: "Use 0.6 g/L of water..."
5. **Farmer clicks Reply on Expert B's answer**:
   - Different Conversation ID: `farmer_5_answer_13`
   - Farmer: "Where can I buy Trichoderma?"
   - Expert B: "Available at most agricultural shops..."

## Viewing Conversations

### Farmer's Messages Tab:

- Shows all reply threads grouped by expert answer
- Header indicates which expert and answer the thread is about

### Expert's Messages Tab:

- Shows all farmer replies to their answers
- Can see which question and answer each thread relates to

## Benefits for Agricultural Platform

1. **Better Expert Support** - Farmers can get detailed help on specific recommendations
2. **Knowledge Building** - Threaded discussions create valuable Q&A archives
3. **Expert Collaboration** - Different experts can provide complementary advice
4. **Follow-up Questions** - Easy to ask clarifying questions about specific answers
5. **Rating Context** - Farmers can rate AND discuss each expert's contribution separately

## Future Enhancements

- Show question text in message header for context
- Notification when expert replies to your follow-up
- Mark conversations as resolved
- Search within answer threads
- Export conversation history
