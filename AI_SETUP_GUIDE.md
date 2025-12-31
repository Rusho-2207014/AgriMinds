# Google Gemini AI Integration - Setup Guide

## 🚀 Quick Setup (5 minutes)

### Step 1: Get FREE Gemini API Key

1. Visit: https://makersuite.google.com/app/apikey
2. Sign in with your Google account
3. Click **"Create API Key"** button
4. Copy the generated key (looks like: `AIzaSyABC123...`)

**Free Tier Benefits:**

- ✅ 60 requests per minute (perfect for this app)
- ✅ No credit card required
- ✅ No time limit
- ✅ Free forever for moderate use

### Step 2: Add API Key to Config

Open `src/main/resources/config.properties` and add your key:

```properties
gemini.api.key=YOUR_API_KEY_HERE
```

Replace `YOUR_API_KEY_HERE` with the key you copied.

### Step 3: Update Database

Run the SQL migration script to add AI tracking:

```bash
mysql -u root -p agriminds_db < add_ai_column.sql
```

Or manually run this SQL:

```sql
ALTER TABLE questions
ADD COLUMN ai_generated BOOLEAN DEFAULT FALSE AFTER answer_text;
```

### Step 4: Rebuild and Run

```bash
mvn clean compile
mvn javafx:run
```

## ✨ Features Added

### For Farmers:

- 🤖 **Instant AI Answers** - Get immediate responses to agricultural questions
- 👨‍🌾 **Expert Review** - Human experts can still add insights to AI answers
- 🔍 **Smart Categories** - AI understands context from question categories
- 🌾 **Bangladesh-Specific** - Answers tailored for local crops and climate

### AI Capabilities:

- Crop disease diagnosis
- Pest control recommendations
- Soil health advice
- Weather-related guidance
- Fertilizer planning
- Seasonal crop suggestions
- General farming questions

## 🎯 How It Works

1. **Farmer asks question** → UI shows "Getting AI Answer..."
2. **AI processes in background** → No UI freeze
3. **AI generates answer** → Instant response (2-5 seconds)
4. **Question saved** → Marked with AI badge 🤖
5. **Expert can add** → Human insights if needed

## 📊 Answer Display

**AI Answers:**

- Shown with 🤖 icon and blue badge
- Labeled: "AI Agricultural Advisor (Instant Response)"

**Human Expert Answers:**

- Shown with 👨‍🌾 icon
- Labeled with expert's name

## 🔧 Troubleshooting

### "AI service unavailable"

- Check API key is set in config.properties
- Verify internet connection
- Check Gemini API status: https://status.google.com

### "Unable to generate response"

- Question might trigger safety filters
- Try rephrasing the question
- Expert will still receive the question

### Database errors

- Ensure `ai_generated` column exists in `questions` table
- Run the migration script

## 🎨 Customization

### Change AI Model Temperature

Edit `AIService.java`:

```java
generationConfig.addProperty("temperature", 0.7); // 0.0 = precise, 1.0 = creative
```

### Adjust Response Length

Edit `AIService.java`:

```java
generationConfig.addProperty("maxOutputTokens", 1024); // Increase for longer answers
```

### Modify AI Prompt

Edit the `buildAgriculturePrompt()` method in `AIService.java` to customize:

- Language preferences
- Response format
- Specific guidelines
- Regional focus

## 📈 Usage Statistics

To track AI vs Human answers:

```sql
SELECT
    category,
    COUNT(*) as total_questions,
    SUM(CASE WHEN ai_generated = TRUE THEN 1 ELSE 0 END) as ai_answers,
    SUM(CASE WHEN ai_generated = FALSE THEN 1 ELSE 0 END) as expert_answers
FROM questions
WHERE status = 'Answered'
GROUP BY category;
```

## 🔐 Security Notes

- ⚠️ Never commit API keys to version control
- ✅ Add config.properties to .gitignore
- ✅ Use environment variables for production
- ✅ Gemini has built-in content safety filters

## 🌟 Future Enhancements

Possible additions:

- Image-based disease diagnosis (Gemini Vision)
- Voice input for questions (Speech-to-Text)
- Multi-language support (Bengali translation)
- Chat history with follow-up questions
- Crop recommendation based on farm data

## 📞 Support

If AI isn't working:

1. Check logs: `logs/agriminds.log`
2. Test API: Use the `testConnection()` method in AIService
3. Verify database schema matches code

---

**Note:** AI provides helpful guidance but farmers should consult local agricultural experts for critical decisions.
