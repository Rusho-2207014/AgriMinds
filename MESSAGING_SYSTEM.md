# Farmer-Expert Messaging System

## Overview

A complete real-time messaging system that allows **unlimited back-and-forth communication** between farmers and experts. Both parties can send and reply to messages infinitely, creating natural conversation threads.

## Features

### 🌾 For Farmers:

- **Start New Chats**: Browse all available experts and initiate conversations
- **Unlimited Messaging**: Send unlimited messages to any expert
- **Conversation History**: View all past conversations organized by expert
- **Real-time Chat**: Message bubbles show who sent what and when
- **Expert Selection**: See expert names and specializations before starting chat

### 👨‍🌾 For Experts:

- **Receive Messages**: Get messages from any farmer
- **Unlimited Replies**: Reply to farmers without any limits
- **Conversation Management**: All farmer conversations in one place
- **Professional Communication**: Help multiple farmers simultaneously

### 💬 Chat Features:

- **Message Bubbles**: Different colors for sender/receiver (blue for sent, white for received)
- **Timestamps**: Every message shows time sent
- **Sender Names**: Clear identification of who sent each message
- **Auto-scroll**: Chat scrolls to latest message automatically
- **Read Receipts**: Messages marked as read when conversation opened
- **Conversation Preview**: See last message in conversation list

## Database Schema

### Table: `messages`

```sql
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id VARCHAR(100) NOT NULL,
    farmer_id BIGINT NOT NULL,
    expert_id BIGINT NOT NULL,
    sender_type ENUM('FARMER', 'EXPERT') NOT NULL,
    sender_name VARCHAR(100) NOT NULL,
    message_text TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_farmer_id (farmer_id),
    INDEX idx_expert_id (expert_id),
    INDEX idx_sent_date (sent_date)
);
```

**Fields:**

- `id`: Unique message identifier
- `conversation_id`: Format: "farmer*{farmerId}\_expert*{expertId}"
- `farmer_id`: ID of farmer in conversation
- `expert_id`: ID of expert in conversation
- `sender_type`: Either 'FARMER' or 'EXPERT'
- `sender_name`: Display name of sender
- `message_text`: The actual message content
- `is_read`: Whether recipient has viewed message
- `sent_date`: When message was sent

## Code Architecture

### 1. Model: `Message.java`

```java
Location: src/main/java/com/agriminds/model/Message.java

Key Components:
- SenderType enum (FARMER, EXPERT)
- All message properties with getters/setters
- generateConversationId() helper method
- Full constructor for easy message creation
```

### 2. Repository: `MessageRepository.java`

```java
Location: src/main/java/com/agriminds/repository/MessageRepository.java

Key Methods:
- save(Message): Save new message
- getConversationMessages(conversationId): Get all messages in conversation
- getFarmerConversations(farmerId): Get all farmer's conversations
- getExpertConversations(expertId): Get all expert's conversations
- getUnreadCount(userId, receiverType): Count unread messages
- markConversationAsRead(conversationId, receiverType): Mark messages as read
- deleteConversation(conversationId): Delete entire conversation
```

### 3. Controller: `MessagesController.java`

```java
Location: src/main/java/com/agriminds/controller/MessagesController.java

Dual-mode controller supporting both farmers and experts:

Farmer Mode:
- setCurrentUser(Farmer): Initialize for farmer
- handleStartNewChat(): Select expert and start conversation

Expert Mode:
- setCurrentUser(Expert): Initialize for expert
- Automatically load farmer conversations

Shared Methods:
- loadConversations(): Display conversation list
- openChat(): Load specific conversation
- loadChatMessages(): Display all messages in chat
- handleSendMessage(): Send new message
- createMessageBubble(): Create styled message UI
```

### 4. UI: `messages-tab.fxml`

```xml
Location: src/main/resources/fxml/tabs/messages-tab.fxml

Layout Structure:
┌─────────────────────────────────────────────┐
│ Left Panel (300px)                          │
│ ┌─────────────────────────────────────┐     │
│ │ 💬 Messages          [+]            │     │
│ ├─────────────────────────────────────┤     │
│ │ Conversation 1                       │     │
│ │ Last message preview...              │     │
│ │ Dec 28, 10:30 AM                     │     │
│ ├─────────────────────────────────────┤     │
│ │ Conversation 2                       │     │
│ │ Another message...                   │     │
│ └─────────────────────────────────────┘     │
└─────────────────────────────────────────────┘
│ Right Panel (Chat Area)                     │
│ ┌─────────────────────────────────────┐     │
│ │ Chat with Expert Name               │     │
│ ├─────────────────────────────────────┤     │
│ │ [Message bubbles area]               │     │
│ │                                       │     │
│ │ ┌─────────────┐                       │     │
│ │ │Expert: Hello│  (Their message)      │     │
│ │ └─────────────┘                       │     │
│ │                  ┌──────────────┐     │     │
│ │         (My msg) │Farmer: Thanks│     │     │
│ │                  └──────────────┘     │     │
│ ├─────────────────────────────────────┤     │
│ │ Type your message...        [Send]   │     │
│ └─────────────────────────────────────┘     │
└─────────────────────────────────────────────┘
```

## Integration

### Farmer Dashboard

**File**: `dashboard.fxml` and `DashboardController.java`

Added tab:

```xml
<Tab text="💬 Messages" fx:id="messagesTab"/>
```

Controller loads messages tab:

```java
loadTabContent(messagesTab, "/fxml/tabs/messages-tab.fxml");
```

### Expert Dashboard

**File**: `expert-dashboard.fxml` and `ExpertController.java`

Added tab:

```xml
<Tab text="💬 Messages" fx:id="messagesTab"/>
```

Controller loads messages tab:

```java
private void loadMessagesTab() {
    // Loads messages-tab.fxml
    // Sets current expert in MessagesController
}
```

## User Workflows

### Workflow 1: Farmer Starts Conversation

1. Farmer clicks **Messages** tab
2. Clicks **[+]** button to start new chat
3. Dialog shows list of all experts with specializations
4. Farmer selects an expert
5. Chat window opens with expert's name in header
6. Farmer types message and clicks **Send**
7. Message appears in blue bubble on right side
8. Expert receives message

### Workflow 2: Expert Replies

1. Expert clicks **Messages** tab
2. Sees list of conversations from farmers
3. Clicks on a conversation to open
4. Views farmer's messages (white bubbles on left)
5. Types reply in text area
6. Clicks **Send**
7. Reply appears in blue bubble on right
8. Farmer receives notification

### Workflow 3: Ongoing Conversation

1. Either party opens conversation
2. Messages marked as read automatically
3. Scroll shows full conversation history
4. Type and send unlimited messages
5. Each message appears immediately
6. Conversation updates in real-time

## Message Flow

```
Farmer sends message:
1. Click Send button
2. Message created with:
   - conversation_id = "farmer_5_expert_12"
   - farmer_id = 5
   - expert_id = 12
   - sender_type = FARMER
   - sender_name = "John Doe"
   - message_text = "Hello, I need help"
3. MessageRepository.save() inserts to database
4. UI refreshes, shows message in chat
5. Conversation list updates with latest message

Expert replies:
1. Opens conversation (marks farmer messages as read)
2. Types reply, clicks Send
3. Message created with same conversation_id
4. sender_type = EXPERT
5. Save to database
6. UI updates with expert's message
7. Farmer sees message next time they check
```

## Conversation ID System

**Format**: `farmer_{farmerId}_expert_{expertId}`

**Examples**:

- `farmer_5_expert_12`
- `farmer_8_expert_3`
- `farmer_15_expert_7`

**Benefits**:

- Unique identifier for each farmer-expert pair
- Easy to query all messages in conversation
- Bidirectional (works for both farmer and expert)
- Scalable (supports unlimited conversations)

## Message Display Logic

### Sender's Message (Blue, Right-aligned):

```
Current user type matches sender type
Example: Farmer viewing their own message
Style: Blue background (#2196F3)
Position: Right side
Text color: White
```

### Receiver's Message (White, Left-aligned):

```
Current user type differs from sender type
Example: Farmer viewing expert's message
Style: White background with gray border
Position: Left side
Text color: Black
```

### Message Bubble Components:

1. **Sender name** (bold, 11px)
2. **Message text** (13px, wrapped)
3. **Time** (10px, format: "hh:mm a")

## Styling & UX

### Colors:

- **Primary Blue**: #2196F3 (sent messages, headers)
- **White**: Messages received
- **Light Gray**: #f0f0f0 (chat background)
- **Border Gray**: #ddd (borders, separators)

### Fonts:

- **Headers**: Arial Bold, 16-18px
- **Messages**: 13px
- **Meta info**: 10-12px

### Animations:

- Hover effect on conversation cards
- Smooth scrolling to latest message
- Instant message appearance (no animation needed)

## Database Queries

### Get Conversation Messages:

```sql
SELECT * FROM messages
WHERE conversation_id = 'farmer_5_expert_12'
ORDER BY sent_date ASC;
```

### Get Farmer's Conversations:

```sql
SELECT m.* FROM messages m
INNER JOIN (
    SELECT conversation_id, MAX(sent_date) as max_date
    FROM messages WHERE farmer_id = 5
    GROUP BY conversation_id
) latest
ON m.conversation_id = latest.conversation_id
   AND m.sent_date = latest.max_date
WHERE m.farmer_id = 5
ORDER BY m.sent_date DESC;
```

### Count Unread Messages (for Farmer):

```sql
SELECT COUNT(*) FROM messages
WHERE farmer_id = 5
  AND sender_type = 'EXPERT'
  AND is_read = FALSE;
```

### Mark Conversation as Read (for Farmer):

```sql
UPDATE messages
SET is_read = TRUE
WHERE conversation_id = 'farmer_5_expert_12'
  AND sender_type = 'EXPERT';
```

## Key Features Implementation

### ✅ Unlimited Messaging

- No limit on number of messages
- No message length restriction (TEXT field)
- Infinite conversation threads

### ✅ Real-time Updates

- loadChatMessages() called after each send
- Conversation list refreshed
- Auto-scroll to latest

### ✅ Read Receipts

- markConversationAsRead() called on chat open
- Only marks messages FROM the other party
- Preserves sent message read status

### ✅ Conversation Management

- Group messages by conversation_id
- Show latest message in list
- Sort by most recent activity

### ✅ User-Friendly UI

- Clean two-panel layout
- Intuitive message bubbles
- Clear sender identification
- Responsive design

## Testing Scenarios

### Scenario 1: First Message

1. Farmer clicks Messages tab → Empty state shows
2. Clicks [+] → Expert selection dialog
3. Selects "Dr. Smith - Crop Expert" → Chat opens
4. Types "Hello doctor" → Sends
5. Message appears in blue bubble
6. Dr. Smith's Messages tab shows new conversation

### Scenario 2: Back-and-Forth

1. Expert opens conversation
2. Sees farmer's "Hello doctor" (white bubble, left)
3. Replies "How can I help?" (blue bubble, right)
4. Farmer refreshes → Sees expert's reply
5. Farmer: "My crops are wilting"
6. Expert: "What type of crops?"
7. Farmer: "Tomatoes"
8. Expert: "Check soil pH"
9. ...continues infinitely

### Scenario 3: Multiple Conversations

1. Farmer has 3 active expert conversations
2. Conversation list shows all 3
3. Each shows last message preview
4. Click any conversation → Opens that chat
5. Messages isolated per conversation
6. No cross-contamination

### Scenario 4: Read Status

1. Expert sends message to farmer
2. Message is_read = FALSE
3. Farmer opens Messages tab → Conversation list loads
4. Farmer clicks conversation → markConversationAsRead()
5. All expert messages → is_read = TRUE
6. Farmer's own messages → unchanged

## Future Enhancements

### Possible Additions:

1. **Typing Indicators**: "Expert is typing..."
2. **Message Notifications**: Badge count on Messages tab
3. **Image Sharing**: Send photos of crops
4. **Voice Messages**: Record and send audio
5. **Message Search**: Find messages by keyword
6. **Archive Conversations**: Hide old chats
7. **Block Users**: Prevent unwanted messages
8. **Message Reactions**: Emoji reactions to messages
9. **File Attachments**: Send documents, PDFs
10. **Group Chats**: Multiple experts in one conversation

## Files Created/Modified

### New Files:

- ✅ `Message.java` - Message model with SenderType enum
- ✅ `MessageRepository.java` - Database operations for messages
- ✅ `MessagesController.java` - Chat UI controller (dual-mode)
- ✅ `messages-tab.fxml` - Chat interface layout

### Modified Files:

- ✅ `DashboardController.java` - Added messagesTab field and loading
- ✅ `dashboard.fxml` - Added Messages tab
- ✅ `ExpertController.java` - Added messagesTab and loadMessagesTab()
- ✅ `expert-dashboard.fxml` - Added Messages tab
- ✅ `ExpertRepository.java` - Added findAll() method for expert selection

### Database:

- ✅ Created `messages` table with indexes

## Summary

The messaging system provides a **complete communication channel** between farmers and experts:

### Core Capabilities:

- ✅ **Unlimited Messages**: No restrictions on conversation length
- ✅ **Two-Way Communication**: Both parties can send/receive freely
- ✅ **Conversation Threading**: Messages grouped by farmer-expert pair
- ✅ **Clean UI**: Professional chat interface with bubbles
- ✅ **Read Tracking**: Know when messages are viewed
- ✅ **Scalable**: Supports thousands of conversations

### User Benefits:

- **Farmers**: Get personalized help through direct expert communication
- **Experts**: Provide detailed, ongoing support to multiple farmers
- **Both**: Build relationships through continuous dialogue

The system transforms AgriMinds from a Q&A platform into a **full agricultural support network** with direct farmer-expert collaboration! 🌾💬👨‍🌾
