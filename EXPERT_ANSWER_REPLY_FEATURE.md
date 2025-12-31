# Expert Answer Reply Feature

## Overview

This feature allows experts to see when farmers have replied to their specific answers and provides a convenient button to respond directly from the "My Answers" tab.

## How It Works

### 1. Farmer Reply Flow

1. Farmer views an expert's answer to their question
2. Farmer clicks "Reply" button on that specific expert's answer
3. Farmer sends messages in the answer-specific conversation thread
4. The conversation is tied to that expert's specific answer (using `expert_answer_id`)

### 2. Expert Dashboard Notification

In the expert's "My Answers" tab, each answer card now displays:

**When farmer has NOT replied:**

- Only shows the answer with date

**When farmer HAS replied but expert hasn't responded:**

- Shows an **"✉️ Answer Reply"** button in orange
- Indicates the expert needs to respond to the farmer's message

**When expert HAS already responded:**

- Shows a **"💬 View Answer"** button in blue
- Allows expert to view/continue the conversation

### 3. Button Actions

When the expert clicks either button:

1. Automatically switches to the Messages tab
2. Opens the specific conversation thread for that answer
3. Shows all messages between farmer and expert about that specific answer
4. Expert can read and reply to farmer's messages

## Technical Implementation

### Database Changes

Added methods to `MessageRepository.java`:

- `hasFarmerRepliesForAnswer(expertAnswerId)` - Checks if farmer has sent any messages about this answer
- `hasExpertRepliedToAnswer(expertAnswerId, expertId)` - Checks if expert has responded

### Controller Updates

#### ExpertController.java

- **Modified `createMyAnswerCard()`**: Adds conditional button based on message status
- **Added `openAnswerMessageDialog()`**: Opens the message conversation for specific answer
- **Modified `loadMessagesTab()`**: Stores controller reference for programmatic access

#### MessagesController.java

- **Added `openAnswerChat()`**: Public method to open answer-specific conversation from external controllers

### Conversation Context

Messages are tied to specific answers using:

- `question_id` - Which question was asked
- `expert_answer_id` - Which specific expert answer this conversation is about
- Conversation ID format: `farmer_{farmerId}_answer_{expertAnswerId}`

## User Experience

### For Experts

1. Navigate to "✅ My Answers" tab
2. See all questions you've answered
3. If a farmer has replied:
   - Orange "Answer Reply" button appears if you haven't responded
   - Blue "View Answer" button appears if you've already replied
4. Click the button to instantly access the conversation
5. Read farmer's questions/feedback
6. Reply directly in the message thread

### Benefits

- **Context-Specific**: Each answer has its own dedicated conversation
- **Visual Indicators**: Color-coded buttons show reply status at a glance
- **Seamless Navigation**: One click takes expert from answer to messages
- **No Confusion**: When multiple experts answer same question, each has separate thread with the farmer

## Example Scenario

**Situation:**

- Farmer asks: "How to treat rice leaf disease?"
- Expert A suggests chemical treatment and answers
- Expert B suggests organic treatment and answers
- Farmer has follow-up questions about Expert A's chemical dosage

**Flow:**

1. Farmer clicks "Reply" on Expert A's answer
2. Farmer asks: "What's the exact dosage for the pesticide?"
3. In Expert A's dashboard → "My Answers" tab:
   - Expert A sees orange **"✉️ Answer Reply"** button on that rice disease answer
4. Expert A clicks the button
5. Messages tab opens showing farmer's dosage question
6. Expert A replies with dosage details
7. Button changes to blue **"💬 View Answer"** for future reference

Meanwhile, Expert B doesn't see any button on their answer since the farmer only replied to Expert A.

## Code Locations

**Files Modified:**

1. [ExpertController.java](src/main/java/com/agriminds/controller/ExpertController.java#L497-L598)

   - `createMyAnswerCard()` method
   - `openAnswerMessageDialog()` method
   - `loadMessagesTab()` method

2. [MessagesController.java](src/main/java/com/agriminds/controller/MessagesController.java#L242-L253)

   - `openAnswerChat()` method

3. [MessageRepository.java](src/main/java/com/agriminds/repository/MessageRepository.java#L157-L196)
   - `hasFarmerRepliesForAnswer()` method
   - `hasExpertRepliedToAnswer()` method

## Related Documentation

- [ANSWER_SPECIFIC_MESSAGING.md](ANSWER_SPECIFIC_MESSAGING.md) - Complete messaging architecture
- [MULTIPLE_EXPERT_ANSWERS.md](MULTIPLE_EXPERT_ANSWERS.md) - Multiple expert answer system
- [MESSAGING_SYSTEM.md](MESSAGING_SYSTEM.md) - General messaging documentation
