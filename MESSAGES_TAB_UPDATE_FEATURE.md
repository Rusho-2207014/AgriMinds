# Messages Tab Auto-Update Feature

## Overview

When a farmer sends a reply to an expert's answer through the message dialog in the "Ask Expert" tab, the entire conversation is now automatically shown in the "Messages" section of the farmer's dashboard.

## Implementation Details

### Files Modified

#### 1. QuestionsController.java

**Changes:**

- Added `mainTabPane` and `messagesTab` fields to store references
- Added `setMainTabPane()` and `setMessagesTab()` setter methods
- Added `updateMessagesTab()` helper method to switch tabs and open conversation
- Modified send message handler to call `updateMessagesTab()` after successful send

**Key Logic:**

```java
private void updateMessagesTab(Long expertId, String expertName, Long questionId, Long expertAnswerId) {
    // Get MessagesController from tab
    MessagesController messagesController = (MessagesController) messagesTab.getContent().getUserData();

    if (messagesController != null) {
        // Switch to Messages tab
        mainTabPane.getSelectionModel().select(messagesTab);

        // Open the specific conversation
        messagesController.openAnswerChat(currentUser.getId(), currentUser.getFullName(), questionId, expertAnswerId);
    }
}
```

#### 2. DashboardController.java

**Changes:**

- Modified `loadTabContent()` to pass TabPane and MessagesTab references to QuestionsController
- Store MessagesController reference in tab's userData for later access

**Key Logic:**

```java
if (controller instanceof QuestionsController) {
    qController.setCurrentUser(currentUser);
    qController.setMainTabPane(mainTabPane);
    qController.setMessagesTab(messagesTab);
} else if (controller instanceof MessagesController) {
    mController.setCurrentUser(currentUser);
    content.setUserData(mController); // Store for later access
}
```

## User Flow

### Before Enhancement

1. Farmer opens expert answer
2. Clicks "Reply" button
3. Types and sends message in dialog
4. Message appears in "Recent Messages" section of dialog
5. **Messages tab remains unchanged**

### After Enhancement

1. Farmer opens expert answer
2. Clicks "Reply" button
3. Types and sends message in dialog
4. Message appears in "Recent Messages" section of dialog
5. **Dashboard automatically switches to "Messages" tab**
6. **Conversation is opened and displayed with all messages**
7. Farmer can continue chatting directly in Messages tab

## Benefits

✅ **Better User Experience** - Seamless transition from asking questions to managing conversations

✅ **Context Preservation** - Full conversation history immediately visible

✅ **Unified Interface** - All messaging interactions centralized in Messages tab

✅ **No Data Loss** - Messages are synchronized across both dialog and Messages tab

## Technical Notes

- Uses `Platform.runLater()` to ensure UI updates happen on JavaFX Application Thread
- Leverages existing `MessagesController.openAnswerChat()` method for consistency
- Maintains answer-specific conversation threading via `conversation_id`
- Gracefully handles cases where TabPane/MessagesTab references are not set

## Testing Checklist

- [ ] Farmer sends message in Ask Expert dialog
- [ ] Message appears in dialog's Recent Messages
- [ ] Dashboard switches to Messages tab automatically
- [ ] Conversation shows all messages (farmer + expert)
- [ ] Can send additional messages from Messages tab
- [ ] Tab switching works smoothly without errors
- [ ] Works for multiple different expert conversations
