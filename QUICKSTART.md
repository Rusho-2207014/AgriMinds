# AgriMinds Quick Start Guide

## Prerequisites Checklist

- [ ] Java JDK 17 or higher installed
- [ ] Maven 3.6+ installed
- [ ] MySQL 8.0+ installed and running
- [ ] IDE (IntelliJ IDEA, Eclipse, or VS Code)

## Setup in 5 Minutes

### Step 1: Database Setup (2 minutes)

```bash
# Login to MySQL
mysql -u root -p

# Create database and import schema
source src/main/resources/database/schema.sql

# Verify tables created
USE agriminds_db;
SHOW TABLES;
```

### Step 2: Configure Application (1 minute)

Edit `src/main/resources/config.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/agriminds_db
db.username=root
db.password=YOUR_PASSWORD

# Optional: Add API keys for full functionality
weather.api.key=your_key_here
sms.api.key=your_key_here
```

### Step 3: Build & Run (2 minutes)

```bash
# Build project
mvn clean install

# Run application
mvn javafx:run
```

## First Login

**Test Account** (if you inserted sample data):

- Email: `test@agriminds.com`
- Password: `password123`

Or register a new account through the registration form.

## Quick Feature Tour

### 1. Dashboard

- Overview of your farm
- Quick access to all modules
- Recent activities

### 2. Market Prices

- View today's crop prices
- Compare markets
- Track price trends

### 3. Weather Alerts

- 7-day forecast
- Severe weather warnings
- Agricultural advisories

### 4. Crop Diseases

- Search by crop name
- Symptom matching
- Treatment recommendations in Bengali

### 5. Soil Health

- Record soil test results
- Get health assessment
- Receive recommendations

## Troubleshooting

### "Cannot connect to database"

```bash
# Check MySQL is running
systemctl status mysql    # Linux
services.msc              # Windows - find MySQL service
```

### "JavaFX not found"

Ensure you're using JDK 17+ and Maven is downloading JavaFX dependencies correctly:

```bash
mvn dependency:tree | grep javafx
```

### "Module error"

Make sure `module-info.java` is present and properly configured.

## Default Data

The schema includes sample data:

- 10 crops (Rice, Wheat, Jute, etc.)
- 5 market price entries
- 3 crop diseases
- Bangladesh divisions and districts

## Next Steps

1. **Customize**: Update `Constants.java` with your specific requirements
2. **Add Data**: Insert more crops, diseases, and market data
3. **Configure APIs**: Get real API keys for weather and SMS services
4. **Test**: Try all features with sample data
5. **Deploy**: Build production JAR and distribute

## Support

- Check `docs/DOCUMENTATION.md` for detailed information
- Review `README.md` for comprehensive guide
- Check logs in `logs/agriminds.log` for errors

## Quick Commands Reference

```bash
# Build
mvn clean install

# Run
mvn javafx:run

# Test
mvn test

# Package
mvn package

# Create distributable
mvn javafx:jlink
```

---

**You're ready to go!** 🚀

The application is production-ready with:
✅ Complete database schema
✅ All model classes
✅ Repository layer with CRUD operations
✅ Service layer with business logic
✅ Controller layer
✅ JavaFX GUI with login and dashboard
✅ Weather API integration
✅ SMS service integration
✅ Configuration management
✅ Comprehensive documentation
