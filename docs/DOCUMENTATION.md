# AgriMinds Project Documentation

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Database Design](#database-design)
3. [API Integration](#api-integration)
4. [User Workflows](#user-workflows)
5. [Development Guidelines](#development-guidelines)

---

## Architecture Overview

AgriMinds follows a layered MVC (Model-View-Controller) architecture:

### Layers

1. **Presentation Layer (View)**

   - JavaFX-based GUI
   - User interaction handling
   - Data display and visualization

2. **Business Logic Layer (Controller)**

   - Request processing
   - Data validation
   - Business rule enforcement

3. **Service Layer**

   - External API integration
   - Business operations
   - Transaction management

4. **Data Access Layer (Repository)**

   - Database CRUD operations
   - Query execution
   - Data mapping

5. **Data Layer (Model)**
   - Entity definitions
   - Domain objects
   - Data structures

---

## Database Design

### Entity-Relationship Diagram

```
Farmers 1----* SoilHealth
Farmers 1----* MarketplaceListings
Crops 1----* MarketPrices
Crops 1----* CropDiseases
Crops 1----* FertilizerRecommendations
```

### Key Relationships

- One farmer can have multiple soil health records
- One farmer can create multiple marketplace listings
- One crop can have multiple market price entries
- One crop can have multiple disease associations

---

## API Integration

### Weather API

- **Provider**: WeatherAPI.com
- **Endpoints**:
  - `/current.json` - Current weather
  - `/forecast.json` - Weather forecast
- **Rate Limit**: 1 million calls/month (free tier)

### SMS Gateway

- **Provider**: Bangladesh SMS Gateway
- **Format**: JSON POST request
- **Character Limit**: 160 characters per SMS
- **Delivery**: Near real-time

---

## User Workflows

### Farmer Registration

1. User opens application
2. Clicks "Register"
3. Fills registration form
4. System validates input
5. Password hashed with BCrypt
6. Record saved to database
7. Success confirmation shown

### Market Price Check

1. Farmer logs in
2. Navigates to "Market Prices" tab
3. System fetches latest prices from database
4. Displays prices in table/chart format
5. Farmer can filter by crop/district

### Disease Identification

1. Farmer observes symptoms on crops
2. Opens "Crop Diseases" module
3. Searches by crop name or symptoms
4. System displays matching diseases
5. Shows treatment recommendations in Bengali/English
6. Option to get SMS alert

---

## Development Guidelines

### Code Style

- Follow Java naming conventions
- Use meaningful variable names
- Add JavaDoc comments for public methods
- Keep methods under 50 lines when possible

### Database Operations

- Always use prepared statements
- Close connections in try-with-resources
- Use transactions for multi-step operations
- Index frequently queried columns

### Error Handling

- Log all exceptions with SLF4J
- Show user-friendly error messages
- Never expose stack traces to users
- Implement graceful degradation

### Testing

- Write unit tests for services
- Test validation logic thoroughly
- Mock external API calls
- Use H2 in-memory database for tests

---

## Deployment

### Build for Production

```bash
mvn clean package
```

### Create Executable JAR

```bash
mvn javafx:jlink
```

### Database Migration

1. Backup existing database
2. Run schema updates
3. Test with sample data
4. Monitor logs for errors

---

## Troubleshooting

### Common Issues

**Database Connection Failed**

- Check MySQL server is running
- Verify credentials in config.properties
- Ensure database exists
- Check firewall settings

**JavaFX Not Loading**

- Verify Java 17+ with JavaFX support
- Check module-info.java configuration
- Ensure JAVA_HOME is set correctly

**API Calls Failing**

- Verify API keys in config.properties
- Check internet connectivity
- Review API rate limits
- Check API endpoint URLs

---

## Performance Optimization

### Database

- Use connection pooling (HikariCP)
- Add indexes on foreign keys
- Optimize slow queries
- Regular database maintenance

### Application

- Cache frequently accessed data
- Lazy load large datasets
- Optimize image loading
- Use background threads for long operations

---

## Security Best Practices

1. Never commit API keys to version control
2. Use environment variables for sensitive data
3. Validate all user input
4. Sanitize data before database operations
5. Use HTTPS for API calls
6. Regularly update dependencies
7. Implement rate limiting
8. Log security events

---

## Future Enhancements

### Short-term

- Add export to Excel functionality
- Implement advanced search filters
- Add crop calendar feature
- Integrate payment gateway

### Long-term

- Machine learning for disease prediction
- Mobile app development
- Multi-tenant support
- Cloud deployment

---

## Support and Maintenance

### Regular Tasks

- Monitor application logs
- Review error reports
- Update crop/disease database
- Refresh market price data
- Check API usage limits

### Backup Strategy

- Daily database backups
- Weekly full system backup
- Off-site backup storage
- Test restore procedures monthly

---

**Last Updated**: December 2025  
**Document Version**: 1.0
