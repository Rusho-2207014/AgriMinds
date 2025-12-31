# Correction/Addition/Reply Approval System

## Overview

Farmers now have control over which expert corrections/additions/replies are accepted. Only **accepted** corrections count towards an expert's statistics and certificate.

## Database Changes

### New Column: `accepted`

- **Type**: BOOLEAN
- **Default**: NULL
- **Values**:
  - `NULL` - Regular answer (no approval needed) OR pending correction
  - `TRUE` - Accepted correction/addition/reply
  - `FALSE` - Denied correction/addition/reply

### Migration File

- `add_correction_acceptance.sql`
- `add-acceptance-column.bat`

## How It Works

### For Experts

1. **Give Correction**: Expert B can correct/add to Expert A's answer
2. **Initial Status**: Correction appears as "Pending Approval" (yellow background)
3. **Wait for Farmer**: Farmer must accept or deny the correction
4. **Statistics Update**: Only ACCEPTED corrections count in:
   - My Ratings dashboard "Corrections/Additions/Replies" count
   - Certificate statistics (if rating ≥ 4.8)

### For Farmers

1. **View Answers**: See all expert answers with nested corrections
2. **See Status**: Visual indicators:
   - 🟨 **Yellow** - Pending Approval (shows Accept/Deny buttons)
   - 🟩 **Green** - Accepted (marked with ✓)
   - 🟥 **Red** - Denied (marked with ✗)
3. **Make Decision**: Click "✓ Accept" or "✗ Deny" buttons
4. **Impact**: Accepted corrections help the expert's statistics

## Visual Indicators

### Pending Correction (Yellow)

```
↳ CORRECTION by Expert John Doe (Pending Approval)
"Your answer needs clarification..."
Posted on Dec 29, 2025 05:45 PM

[✓ Accept]  [✗ Deny]
```

### Accepted Correction (Green)

```
↳ CORRECTION by Expert John Doe ✓ Accepted
"Your answer needs clarification..."
Posted on Dec 29, 2025 05:45 PM
```

### Denied Correction (Red)

```
↳ CORRECTION by Expert John Doe ✗ Denied
"Your answer needs clarification..."
Posted on Dec 29, 2025 05:45 PM
```

## Code Changes

### Model: ExpertAnswer.java

- Added `Boolean accepted` field
- Added getter/setter methods

### Repository: ExpertAnswerRepository.java

- Updated `getRepliesCount()`: Only counts WHERE `accepted = TRUE`
- Added `acceptCorrection(Long replyId)`: Sets accepted = TRUE
- Added `denyCorrection(Long replyId)`: Sets accepted = FALSE
- Updated `mapResultSetToExpertAnswer()`: Reads accepted field

### Controller: QuestionsController.java

- Updated reply display with color coding
- Added Accept/Deny buttons for pending corrections
- Shows status labels (Pending/Accepted/Denied)
- Refreshes view after accept/deny action

### Controller: ExpertController.java

- My Ratings dashboard shows only accepted corrections count
- Certificate includes only accepted corrections in statistics

### Service: CertificateGenerator.java

- Certificate displays accepted corrections count
- AI message includes correction statistics

## Statistics Counting Rules

### Questions Answered

- Counts: All answers where `parent_answer_id IS NULL`
- Does NOT require approval

### Rated Answers

- Counts: All answers that have ratings
- Does NOT require approval

### Corrections/Additions/Replies

- Counts: Only where `parent_answer_id IS NOT NULL AND accepted = TRUE`
- **REQUIRES farmer approval**

## Example Workflow

1. **Farmer asks**: "How do I prevent rice blast disease?"
2. **Expert A answers**: "Use resistant varieties..."
3. **Expert B corrects**: "Also important: spray fungicide before flowering"
4. **Farmer sees**: Yellow box with correction, Accept/Deny buttons
5. **Farmer accepts**: Correction turns green, counts for Expert B
6. **Expert B dashboard**: Corrections count increases by 1
7. **Expert B certificate**: Shows updated correction count (if rating ≥ 4.8)

## Benefits

### For Farmers

- Quality control over expert corrections
- Prevents spam or unhelpful corrections
- Empowers farmers to validate expert contributions

### For Experts

- Fair recognition for helpful corrections
- Motivation to provide quality feedback
- Statistics reflect farmer-approved contributions only

### For Platform

- Higher quality expert interactions
- Farmer-driven quality assurance
- Transparent expert evaluation system
