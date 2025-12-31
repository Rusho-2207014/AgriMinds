# Reply to Answer Feature

## Overview

This feature adds **Reply buttons** next to expert answers in the question/answer view, allowing users to directly message the expert who provided the answer.

## Features

### 1. Farmer Dashboard - Reply to Expert Answers

- When viewing answers to a question (View Answer button), farmers see all AI and expert answers
- Each expert answer now has a **💬 Reply to Expert** button
- Clicking the button opens a messaging dialog with that specific expert
- The dialog shows:
  - Recent message history with that expert
  - Text area to type a new message
  - Send button to instantly message the expert

### 2. Expert Dashboard - Reply to Other Experts

- When viewing answers to a question, experts see all answers (AI + all expert answers)
- For answers from OTHER experts (not their own), a **💬 Reply to Expert** button appears
- Clicking the button opens a messaging dialog to discuss the question with the other expert
- Useful for expert collaboration and second opinions

## User Flow

### For Farmers:

1. Navigate to Questions tab
2. Click "View Answer" on any answered question
3. See the answer dialog with:
   - 🤖 AI Answer (if available)
   - 👨‍🌾 Expert Answer #1 with Reply button
   - 👨‍🌾 Expert Answer #2 with Reply button
   - etc.
4. Click "Reply to Expert" on the desired answer
5. Messaging dialog opens with:
   - Last 10 messages (if any)
   - New message text area
6. Type message and click "Send Message"
7. Message is instantly saved and expert will see it in their Messages tab

### For Experts:

1. Navigate to Questions tab
2. Click "View Answer" on any answered question
3. See answers from AI, yourself, and other experts
4. Other experts' answers show a "Reply to Expert" button
5. Click to open messaging dialog
6. Can discuss the question or collaborate with other experts
7. Messages appear in both experts' Messages tabs

## Technical Implementation

### Files Modified:

1. **QuestionsController.java** (Farmer Dashboard)

   - Added MessageRepository field
   - Modified answer display to use VBox layout with styled boxes
   - Added Reply button to each expert answer
   - Implemented `openMessageDialog()` method

2. **ExpertController.java** (Expert Dashboard)
   - Added MessageRepository field
   - Converted showViewAnswerDialog from Alert to custom Dialog
   - Added Reply button to other experts' answers
   - Implemented `openExpertMessageDialog()` method

### UI Design:

- **AI Answer Box**: Blue background (#E3F2FD) with blue border
- **Expert Answer Box**: Light green background (#F1F8E9) with green border
- **Reply Button**: Blue background with white text, rounded corners
- **Message Bubbles**:
  - Sent messages: Light green (#DCF8C6)
  - Received messages: Light gray (#E8E8E8)

### Message Flow:

1. Conversation ID generated using: `Message.generateConversationId(farmerId, expertId)`
2. Messages saved with proper sender type (FARMER or EXPERT)
3. Message history loaded from database (last 10 messages shown)
4. New messages instantly saved to database
5. Recipients see messages in their Messages tab

## Benefits

### User Experience:

- **Contextual messaging**: Reply directly from the answer view
- **Fewer clicks**: No need to navigate to Messages tab → find expert → start conversation
- **Seamless flow**: View answer → Like it → Reply instantly
- **Better engagement**: Farmers can ask follow-up questions to specific experts

### Expert Collaboration:

- Experts can discuss complex cases with each other
- Share knowledge and second opinions
- Build professional network within the platform

## Database Schema

Uses existing `messages` table:

- conversation_id: Unique identifier for farmer-expert conversations
- farmer_id: ID of the farmer
- expert_id: ID of the expert
- sender_type: FARMER or EXPERT
- message_text: The message content
- sent_date: Timestamp
- is_read: Read receipt flag

## Future Enhancements

- Add unread message badge on Reply buttons
- Show message count next to Reply button
- Add "Reply to AI" feature (feedback/clarification)
- Implement push notifications for new replies
- Add emoji support in messaging
- File attachment support for images/documents
