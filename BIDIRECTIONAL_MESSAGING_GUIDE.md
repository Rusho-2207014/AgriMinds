# Bidirectional Messaging System - Complete Guide

## Overview

The AgriMinds platform features a **complete bidirectional messaging system** where farmers and experts can have unlimited back-and-forth conversations. Messages show sender names, appear in both parties' dashboards, and support continuous communication.

## How It Works

### 1. Farmer Sends Message to Expert

**Via Reply Button:**

1. Farmer views an expert's answer to their question
2. Clicks **💬 Reply to Expert** button
3. Dialog opens showing conversation history (if any)
4. Types message and clicks "Send Message"
5. Message is saved with **farmer's full name**

**What Happens:**

- Message appears in expert's Messages tab
- Shows as: **"[Farmer Name]: Your message here"**
- Expert gets notification of new message

### 2. Expert Sees and Replies

**In Messages Tab:**

1. Expert opens Messages tab in their dashboard
2. Sees conversation with farmer's name
3. Clicks to open full conversation
4. Types reply and clicks Send
5. Reply is saved with **expert's full name**

**What Happens:**

- Reply appears in farmer's Messages tab
- Shows as: **"Dr. [Expert Name]: Reply message here"**
- Farmer gets notification of reply

### 3. Continuous Conversation

- Both parties can keep replying
- All messages show sender names
- Conversation history preserved
- Both dashboards show same conversation
- Can continue for unlimited messages

## Message Display

### In Reply Dialog:

```
Recent Messages:
┌────────────────────────────────────┐
│ Abdul Rahman (Farmer)              │ ← Sender name shown
│ "What fertilizer should I use?"    │
│ Dec 28, 2:30 PM                    │
└────────────────────────────────────┘

┌────────────────────────────────────┐
│ Dr. Karim (Expert)                 │ ← Sender name shown
│ "Use NPK 20-20-20, 2kg per acre"   │
│ Dec 28, 2:45 PM                    │
└────────────────────────────────────┘

┌────────────────────────────────────┐
│ Abdul Rahman (Farmer)              │ ← Sender name shown
│ "Thank you! How often?"            │
│ Dec 28, 3:00 PM                    │
└────────────────────────────────────┘
```

### In Messages Tab:

Both farmer and expert see the same conversation with all sender names clearly displayed.

## Features

### ✅ Bidirectional Communication

- Farmer → Expert: Ask questions, follow-ups
- Expert → Farmer: Provide answers, advice
- Both can reply unlimited times

### ✅ Sender Names Displayed

- Every message shows sender's full name
- No confusion about who said what
- Professional appearance

### ✅ Synchronized Across Dashboards

- Farmer sees conversation in their Messages tab
- Expert sees same conversation in their Messages tab
- Real-time updates when switching tabs

### ✅ Context from Answers

- Reply button next to expert answers
- Start conversation from question context
- No need to search for expert separately

### ✅ Expert Collaboration

- Experts can message other experts
- Discuss farmer questions
- Share expertise and second opinions

## User Scenarios

### Scenario 1: Follow-up Question

```
Day 1:
- Farmer posts: "My rice plants have brown spots"
- Dr. Karim answers: "This is leaf blight disease"

Day 2:
- Farmer clicks Reply → "What medicine cures this?"
- Dr. Karim sees message in Messages tab
- Dr. Karim replies: "Use fungicide XYZ"

Day 3:
- Farmer replies: "Used it, still spreading. What now?"
- Dr. Karim replies: "Send photo to assess severity"
- Conversation continues...
```

### Scenario 2: Expert Collaboration

```
- Dr. Fatima answers question about soil pH
- Dr. Karim views answer, clicks Reply
- Dr. Karim: "I also recommend nitrogen test"
- Dr. Fatima sees message in Messages tab
- Dr. Fatima: "Good point! Yes, nitrogen is key"
- Both experts collaborate on farmer's problem
```

## Technical Details

### Message Model:

```java
public class Message {
    private Long id;
    private String conversationId;      // "farmer_123_expert_456"
    private Long farmerId;
    private Long expertId;
    private SenderType senderType;      // FARMER or EXPERT
    private String senderName;          // "Abdul Rahman" or "Dr. Karim"
    private String messageText;
    private Boolean isRead;
    private LocalDateTime sentDate;
}
```

### Conversation ID Format:

- Pattern: `farmer_{farmerId}_expert_{expertId}`
- Example: `farmer_5_expert_12`
- Unique for each farmer-expert pair
- All messages in conversation use same ID

### Database:

**messages table:**

```sql
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id VARCHAR(100) NOT NULL,
    farmer_id BIGINT NOT NULL,
    expert_id BIGINT NOT NULL,
    sender_type ENUM('FARMER', 'EXPERT'),
    sender_name VARCHAR(100),           -- Full name of sender
    message_text TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    sent_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Controllers:

1. **QuestionsController** - Farmer dashboard

   - Shows Reply buttons on expert answers
   - Creates messages with farmer's name
   - Displays sender names in dialog

2. **ExpertController** - Expert dashboard

   - Shows Reply buttons on other experts' answers
   - Creates messages with expert's name
   - Displays sender names in dialog

3. **MessagesController** - Messages tab (both)
   - Lists all conversations
   - Shows full chat history
   - Sends and receives messages
   - Displays sender names

## Setup

### 1. Database Migration:

```bash
mysql -u root -p agriminds_db < add_sender_name_column.sql
```

This adds the `sender_name` column to existing messages table.

### 2. Compile:

```bash
mvn clean compile
```

### 3. Run:

Start the application and test messaging flow.

## Testing the Feature

### Test as Farmer:

1. Login as farmer
2. Go to Questions tab
3. Click "View Answer" on answered question
4. Click "💬 Reply to Expert" button
5. Type message: "Hello, thank you for the answer!"
6. Click "Send Message"
7. Go to Messages tab → see conversation

### Test as Expert:

1. Login as expert
2. Go to Messages tab
3. See new message from farmer (with farmer's name)
4. Click conversation to open
5. Type reply: "You're welcome! Happy to help!"
6. Click Send
7. Message appears in farmer's Messages tab

### Verify Bidirectional Flow:

1. Farmer replies again
2. Expert sees new message
3. Expert replies
4. Farmer sees reply
5. Continue back and forth
6. All messages show correct sender names

## Benefits

**For Farmers:**

- Direct access to experts who answered their questions
- Can ask follow-up questions easily
- See expert names clearly
- Get personalized ongoing support
- Build relationship with trusted advisors

**For Experts:**

- Provide continuous support to farmers
- Build reputation and trust
- Collaborate with other experts
- Track all conversations in one place
- Improve farming outcomes through ongoing advice

**For Platform:**

- Higher user engagement
- Better support quality
- Expert collaboration
- User retention
- Valuable conversation data

## UI Elements

### Reply Button:

- **Style**: Blue background, white text, rounded
- **Text**: "💬 Reply to Expert"
- **Location**: Next to each expert answer
- **Condition**: Only shown for other experts (not your own)

### Message Bubbles:

- **Farmer messages**: Green background (#DCF8C6)
- **Expert messages**: Gray background (#E8E8E8)
- **Sender name**: Bold text, colored
- **Timestamp**: Small gray text

### Dialog Layout:

```
┌─────────────────────────────────────┐
│ Message [Expert/Farmer Name]       │
├─────────────────────────────────────┤
│ Recent Messages:                    │
│ ┌─────────────────────────────────┐ │
│ │ [Message bubbles with names]    │ │
│ │ ...scrollable...                │ │
│ └─────────────────────────────────┘ │
│                                     │
│ New Message:                        │
│ ┌─────────────────────────────────┐ │
│ │ Type your message here...       │ │
│ │                                 │ │
│ └─────────────────────────────────┘ │
│                                     │
│    [Send Message]  [Close]          │
└─────────────────────────────────────┘
```

## Future Enhancements

- [ ] Unread message count badges
- [ ] Push notifications
- [ ] Typing indicators
- [ ] Read receipts (✓✓)
- [ ] File/image attachments
- [ ] Emoji picker
- [ ] Voice messages
- [ ] Search in conversations
- [ ] Archive conversations
- [ ] Export chat history
- [ ] Message reactions (👍 ❤️)
- [ ] Group conversations
- [ ] Auto-translate messages
