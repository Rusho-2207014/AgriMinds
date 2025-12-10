# AgriMinds - Smart Farming Platform for Bangladesh

![AgriMinds Logo](docs/logo.png)

**Version:** 1.0.0  
**Platform:** Desktop (Windows, macOS, Linux)  
**Technology:** Java 17 + JavaFX + MySQL

---

## 📋 Overview

AgriMinds is a comprehensive desktop application designed specifically for Bangladeshi farmers to modernize and optimize their farming operations. The platform provides real-time market information, weather alerts, crop disease identification, soil health management, and a direct marketplace connecting farmers with buyers.

---

## ✨ Key Features

### 1. **User Management**

- Separate authentication for Farmers and Buyers
- Secure password hashing with BCrypt
- Profile management with location tracking (Division/District/Upazila)

### 2. **Real-time Market Price Dashboard**

- Daily crop prices from major markets across Bangladesh
- Price trend analysis and historical data
- Market comparison by district
- Wholesale and retail price tracking

### 3. **Weather Alerts Integration**

- Real-time weather forecasts
- Severe weather alerts (floods, storms, droughts)
- District-wise weather information
- Agricultural recommendations based on weather

### 4. **Crop Disease Identification**

- Comprehensive disease database
- Symptoms and causes in Bengali and English
- Expert treatment recommendations
- Preventive measures

### 5. **Soil Health Management**

- Track soil pH, NPK levels, organic matter
- Soil test recording and history
- Health status assessment
- Personalized recommendations

### 6. **Fertilizer Planning Calculator**

- Crop-specific fertilizer recommendations
- Dosage calculations per acre
- Application timing and methods
- Cost estimation

### 7. **Seasonal Crop Suggestions**

- Season-based crop recommendations (Rabi, Kharif)
- Soil-type matching
- Growing duration and requirements
- Climate suitability analysis

### 8. **SMS Gateway Integration**

- Offline SMS notifications
- Price alerts
- Weather warnings
- Disease outbreak notifications

### 9. **Farmer-Buyer Marketplace**

- Direct connection between farmers and buyers
- Product listing with quality grades
- Organic certification tracking
- Location-based search

### 10. **Reporting & Analytics**

- Sales reports
- Price trends visualization with JFreeChart
- Farm performance metrics
- Export capabilities

---

## 🏗️ Project Structure

```
agriminds-farming-platform/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── agriminds/
│   │   │           ├── AgriMindsApplication.java    # Main entry point
│   │   │           ├── model/                       # Entity classes
│   │   │           │   ├── Farmer.java
│   │   │           │   ├── Buyer.java
│   │   │           │   ├── Crop.java
│   │   │           │   ├── MarketPrice.java
│   │   │           │   ├── SoilHealth.java
│   │   │           │   ├── CropDisease.java
│   │   │           │   └── WeatherAlert.java
│   │   │           ├── view/                        # UI components
│   │   │           │   └── MainFrame.java
│   │   │           ├── controller/                  # Business logic
│   │   │           │   └── AuthController.java
│   │   │           ├── service/                     # External services
│   │   │           │   ├── FarmerService.java
│   │   │           │   ├── WeatherService.java
│   │   │           │   └── SMSService.java
│   │   │           ├── repository/                  # Database operations
│   │   │           │   ├── FarmerRepository.java
│   │   │           │   └── MarketPriceRepository.java
│   │   │           └── util/                        # Utilities
│   │   │               ├── DatabaseConnection.java
│   │   │               ├── Constants.java
│   │   │               └── ValidationUtils.java
│   │   └── resources/
│   │       ├── database/
│   │       │   └── schema.sql                       # MySQL schema
│   │       ├── images/                              # Application images
│   │       ├── icons/                               # UI icons
│   │       ├── config.properties                    # Configuration
│   │       └── simplelogger.properties              # Logging config
│   └── test/
│       └── java/                                    # Unit tests
│
├── docs/                                            # Documentation
├── lib/                                             # External JARs
├── logs/                                            # Application logs
├── pom.xml                                          # Maven dependencies
└── README.md                                        # This file
```

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK) 17** or higher
- **Maven 3.6+** for dependency management
- **MySQL 8.0+** database server
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code (recommended)

### Installation Steps

1. **Clone the Repository**

   ```bash
   git clone https://github.com/your-org/agriminds.git
   cd agriminds
   ```

2. **Set Up MySQL Database**

   ```bash
   # Login to MySQL
   mysql -u root -p

   # Run the schema file
   source src/main/resources/database/schema.sql
   ```

3. **Configure Database Connection**

   Edit `src/main/resources/config.properties`:

   ```properties
   db.url=jdbc:mysql://localhost:3306/agriminds_db
   db.username=your_username
   db.password=your_password
   ```

4. **Configure API Keys**

   Update the following in `config.properties`:

   - Weather API key (get from [weatherapi.com](https://www.weatherapi.com/))
   - SMS Gateway credentials (Bangladesh SMS provider)

5. **Build the Project**

   ```bash
   mvn clean install
   ```

6. **Run the Application**
   ```bash
   mvn javafx:run
   ```

---

## 🔧 Configuration

### Database Configuration

The application uses HikariCP for database connection pooling. Configure in `config.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/agriminds_db?useSSL=false&serverTimezone=Asia/Dhaka
db.username=root
db.password=
db.pool.maxPoolSize=10
db.pool.minIdle=2
```

### API Configuration

#### Weather API

```properties
weather.api.key=your_api_key
weather.api.url=https://api.weatherapi.com/v1
```

#### SMS Gateway

```properties
sms.api.key=your_sms_key
sms.api.url=https://api.smsgateway.bd/send
sms.sender.id=AgriMinds
```

---

## 📦 Dependencies

### Core Dependencies

- **JavaFX 21.0.6** - GUI framework
- **MySQL Connector 8.2.0** - Database connectivity
- **HikariCP 5.1.0** - Connection pooling
- **Gson 2.10.1** - JSON processing
- **Apache HttpClient 5.3** - HTTP requests
- **JFreeChart 1.5.4** - Data visualization
- **BCrypt 0.4** - Password hashing
- **SLF4J 2.0.9** - Logging

See `pom.xml` for complete dependency list.

---

## 💾 Database Schema

The application uses a normalized MySQL database with the following main tables:

- `farmers` - Farmer user accounts
- `buyers` - Buyer/trader accounts
- `crops` - Crop information database
- `market_prices` - Daily market price records
- `soil_health` - Soil test results
- `crop_diseases` - Disease identification data
- `weather_alerts` - Weather notifications
- `marketplace_listings` - Farmer product listings
- `fertilizer_recommendations` - Fertilizer guidelines

Full schema: `src/main/resources/database/schema.sql`

---

## 🎨 User Interface

The application features a clean, farmer-friendly interface with:

- **Login/Registration** screens
- **Dashboard** with quick access cards
- **Tabbed Navigation** for different modules
- **Data Tables** for market prices and listings
- **Charts** for price trends and analytics
- **Forms** for data entry
- **Bilingual Support** (Bengali/English)

---

## 🔐 Security Features

- Password hashing with BCrypt (12 rounds)
- SQL injection prevention with prepared statements
- Input validation and sanitization
- Session management
- Secure API key storage

---

## 📱 SMS Integration

The SMS service supports:

- Price alerts
- Weather warnings
- Disease notifications
- Marketplace inquiries
- Bulk SMS for announcements

Bangladesh phone number format: `+880 1XXXXXXXXX`

---

## 🌤️ Weather Integration

Weather service provides:

- Current weather conditions
- 3-10 day forecasts
- Severe weather alerts
- Temperature, rainfall, humidity data
- District-wise information

---

## 🧪 Testing

Run tests with:

```bash
mvn test
```

Test coverage includes:

- Unit tests for services
- Repository tests with H2 database
- Validation utility tests

---

## 📊 Data Sources

- **Market Prices**: Department of Agricultural Marketing (DAM), Bangladesh
- **Weather Data**: WeatherAPI.com
- **Crop Information**: Bangladesh Agricultural Research Council (BARC)
- **Disease Database**: Plant Pathology Division, BARI

---

## 🚧 Roadmap

### Version 1.1 (Planned)

- [ ] Mobile app integration
- [ ] AI-powered crop disease detection
- [ ] Video tutorials in Bengali
- [ ] Government subsidy information
- [ ] Crop insurance integration

### Version 1.2 (Future)

- [ ] IoT sensor integration
- [ ] Drone-based field monitoring
- [ ] Blockchain for supply chain
- [ ] Multi-language support (Chittagonian, Sylheti)

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see LICENSE file for details.

---

## 👥 Team

**AgriMinds Development Team**

- Project Lead: [Name]
- Backend Developer: [Name]
- Frontend Developer: [Name]
- Database Architect: [Name]
- Agricultural Consultant: [Name]

---

## 📞 Support

For support and queries:

- **Email**: support@agriminds.com
- **Phone**: +880 1XXX-XXXXXX
- **Website**: www.agriminds.com
- **GitHub Issues**: [Project Issues](https://github.com/your-org/agriminds/issues)

---

## 🙏 Acknowledgments

- Bangladesh Agricultural Research Council (BARC)
- Department of Agricultural Marketing (DAM)
- Local farmer communities for feedback
- Open source community for tools and libraries

---

## 📝 Version History

### v1.0.0 (December 2025)

- Initial release
- Core features implemented
- MySQL database integration
- JavaFX GUI
- Weather and SMS integration

---

**Built with ❤️ for Bangladeshi Farmers**
