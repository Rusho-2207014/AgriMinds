-- AgriMinds Database Schema
-- MySQL Database for Smart Farming Platform
-- Created: 2025
-- Version: 1.0.0

-- Create database
CREATE DATABASE IF NOT EXISTS agriminds_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE agriminds_db;


CREATE TABLE IF NOT EXISTS farmers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    national_id VARCHAR(50),
    division VARCHAR(50),
    district VARCHAR(50),
    upazila VARCHAR(50),
    village VARCHAR(100),
    farm_size DECIMAL(10,2) DEFAULT 0.00,
    farming_type VARCHAR(50),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    language VARCHAR(20) DEFAULT 'Bengali',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_phone (phone_number),
    INDEX idx_district (district),
    INDEX idx_division (division)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS buyers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    business_name VARCHAR(150),
    business_type VARCHAR(50),
    trade_license VARCHAR(100),
    division VARCHAR(50),
    district VARCHAR(50),
    address TEXT,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_phone (phone_number),
    INDEX idx_district (district)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS crops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crop_name VARCHAR(100) NOT NULL,
    crop_name_bengali VARCHAR(100),
    scientific_name VARCHAR(150),
    category VARCHAR(50),
    season VARCHAR(50),
    growing_days INT,
    soil_type VARCHAR(100),
    water_requirement DECIMAL(10,2),
    climate_requirement TEXT,
    description TEXT,
    description_bengali TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_season (season)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS market_prices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crop_id BIGINT,
    crop_name VARCHAR(100) NOT NULL,
    market_name VARCHAR(100),
    district VARCHAR(50),
    division VARCHAR(50),
    wholesale_price DECIMAL(10,2),
    retail_price DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20) DEFAULT 'kg',
    price_date DATE NOT NULL,
    source VARCHAR(50),
    price_change DECIMAL(5,2),
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_crop_id (crop_id),
    INDEX idx_crop_name (crop_name),
    INDEX idx_district (district),
    INDEX idx_price_date (price_date),
    FOREIGN KEY (crop_id) REFERENCES crops(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS soil_health (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farmer_id BIGINT NOT NULL,
    test_date DATE NOT NULL,
    ph DECIMAL(4,2),
    nitrogen DECIMAL(5,2),
    phosphorus DECIMAL(5,2),
    potassium DECIMAL(5,2),
    organic_matter DECIMAL(5,2),
    soil_type VARCHAR(50),
    soil_texture VARCHAR(50),
    moisture DECIMAL(5,2),
    health_status VARCHAR(20),
    recommendations TEXT,
    recommendations_bengali TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_farmer_id (farmer_id),
    INDEX idx_test_date (test_date),
    FOREIGN KEY (farmer_id) REFERENCES farmers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS crop_diseases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crop_id BIGINT,
    disease_name VARCHAR(150) NOT NULL,
    disease_name_bengali VARCHAR(150),
    symptoms TEXT,
    symptoms_bengali TEXT,
    causes TEXT,
    severity VARCHAR(20),
    treatment TEXT,
    treatment_bengali TEXT,
    preventive_measures TEXT,
    preventive_measures_bengali TEXT,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_crop_id (crop_id),
    INDEX idx_severity (severity),
    FOREIGN KEY (crop_id) REFERENCES crops(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS weather_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    district VARCHAR(50) NOT NULL,
    division VARCHAR(50),
    alert_date DATE NOT NULL,
    alert_type VARCHAR(50),
    severity VARCHAR(20),
    description TEXT,
    description_bengali TEXT,
    recommendations TEXT,
    recommendations_bengali TEXT,
    temperature DECIMAL(5,2),
    rainfall DECIMAL(10,2),
    humidity DECIMAL(5,2),
    wind_speed VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_district (district),
    INDEX idx_alert_date (alert_date),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS marketplace_listings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farmer_id BIGINT NOT NULL,
    crop_id BIGINT,
    crop_name VARCHAR(100) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20) DEFAULT 'kg',
    price_per_unit DECIMAL(10,2) NOT NULL,
    harvest_date DATE,
    available_date DATE NOT NULL,
    location VARCHAR(100),
    district VARCHAR(50),
    description TEXT,
    quality_grade VARCHAR(20),
    is_organic BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'available',
    views_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_farmer_id (farmer_id),
    INDEX idx_crop_id (crop_id),
    INDEX idx_status (status),
    INDEX idx_district (district),
    FOREIGN KEY (farmer_id) REFERENCES farmers(id) ON DELETE CASCADE,
    FOREIGN KEY (crop_id) REFERENCES crops(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS fertilizer_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crop_id BIGINT NOT NULL,
    soil_type VARCHAR(50),
    growth_stage VARCHAR(50),
    fertilizer_type VARCHAR(100),
    quantity_per_acre DECIMAL(10,2),
    unit VARCHAR(20),
    application_method TEXT,
    timing VARCHAR(100),
    notes TEXT,
    notes_bengali TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_crop_id (crop_id),
    FOREIGN KEY (crop_id) REFERENCES crops(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



-- Insert sample crops
INSERT INTO crops (crop_name, crop_name_bengali, category, season, growing_days, soil_type) VALUES
('Rice', 'ধান', 'Cereal', 'Rabi', 120, 'Loamy'),
('Wheat', 'গম', 'Cereal', 'Rabi', 110, 'Loamy'),
('Jute', 'পাট', 'Cash Crop', 'Kharif', 120, 'Alluvial'),
('Potato', 'আলু', 'Vegetable', 'Rabi', 90, 'Sandy'),
('Tomato', 'টমেটো', 'Vegetable', 'Year-round', 75, 'Loamy'),
('Mango', 'আম', 'Fruit', 'Year-round', 365, 'Loamy'),
('Banana', 'কলা', 'Fruit', 'Year-round', 300, 'Loamy'),
('Onion', 'পেঁয়াজ', 'Vegetable', 'Rabi', 90, 'Sandy'),
('Garlic', 'রসুন', 'Spice', 'Rabi', 150, 'Loamy'),
('Chili', 'মরিচ', 'Spice', 'Year-round', 90, 'Loamy');

-- Insert sample market prices (current date)
INSERT INTO market_prices (crop_name, market_name, district, division, retail_price, unit, price_date, source) VALUES
('Rice', 'Karwan Bazar', 'Dhaka', 'Dhaka', 55.00, 'kg', CURDATE(), 'Local Survey'),
('Wheat', 'Badamtoli Bazar', 'Chittagong', 'Chittagong', 45.00, 'kg', CURDATE(), 'Local Survey'),
('Potato', 'Munshiganj Market', 'Munshiganj', 'Dhaka', 25.00, 'kg', CURDATE(), 'DAM'),
('Tomato', 'Kawran Bazar', 'Dhaka', 'Dhaka', 60.00, 'kg', CURDATE(), 'Local Survey'),
('Onion', 'Sher-e-Bangla Nagar', 'Dhaka', 'Dhaka', 80.00, 'kg', CURDATE(), 'DAM');

-- Insert sample crop diseases
INSERT INTO crop_diseases (crop_id, disease_name, disease_name_bengali, severity, symptoms, treatment) VALUES
(1, 'Rice Blast', 'ধান ব্লাস্ট', 'High', 'Diamond-shaped lesions on leaves', 'Apply Tricyclazole fungicide'),
(4, 'Late Blight', 'দেরি ব্লাইট', 'Critical', 'Dark spots on leaves and tubers', 'Use Mancozeb or copper-based fungicides'),
(5, 'Tomato Leaf Curl', 'টমেটো পাতা কার্ল', 'Medium', 'Curling and yellowing of leaves', 'Remove infected plants, control whiteflies');

COMMIT;


CREATE TABLE IF NOT EXISTS experts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    specialization VARCHAR(100),
    qualifications TEXT,
    years_of_experience INT DEFAULT 0,
    location VARCHAR(100),
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_specialization (specialization),
    INDEX idx_verified (is_verified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farmer_id BIGINT NOT NULL,
    farmer_name VARCHAR(100),
    category VARCHAR(50),
    question_text TEXT NOT NULL,
    image_url VARCHAR(255),
    status VARCHAR(20) DEFAULT 'Open',
    asked_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    answered_by_expert_id BIGINT,
    expert_name VARCHAR(100),
    answer_text TEXT,
    answered_date TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (farmer_id) REFERENCES farmers(id) ON DELETE CASCADE,
    FOREIGN KEY (answered_by_expert_id) REFERENCES experts(id) ON DELETE SET NULL,
    INDEX idx_farmer (farmer_id),
    INDEX idx_expert (answered_by_expert_id),
    INDEX idx_status (status),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS price_negotiations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crop_id BIGINT,
    crop_name VARCHAR(100),
    farmer_id BIGINT NOT NULL,
    farmer_name VARCHAR(100),
    buyer_id BIGINT NOT NULL,
    buyer_name VARCHAR(100),
    farmer_price DECIMAL(10,2),
    buyer_offer DECIMAL(10,2),
    agreed_price DECIMAL(10,2),
    quantity DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'Pending',
    accepted_date TIMESTAMP NULL,
    buyer_message TEXT,
    farmer_response TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (farmer_id) REFERENCES farmers(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES buyers(id) ON DELETE CASCADE,
    FOREIGN KEY (crop_id) REFERENCES crops(id) ON DELETE SET NULL,
    INDEX idx_farmer (farmer_id),
    INDEX idx_buyer (buyer_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS farmer_crops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farmer_id BIGINT NOT NULL,
    crop_id BIGINT,
    crop_name VARCHAR(100),
    quantity DECIMAL(10,2),
    unit VARCHAR(20),
    selling_price DECIMAL(10,2),
    harvest_date DATE,
    is_available BOOLEAN DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (farmer_id) REFERENCES farmers(id) ON DELETE CASCADE,
    FOREIGN KEY (crop_id) REFERENCES crops(id) ON DELETE SET NULL,
    INDEX idx_farmer (farmer_id),
    INDEX idx_crop (crop_id),
    INDEX idx_available (is_available)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample expert data
INSERT INTO experts (full_name, email, phone_number, password_hash, specialization, qualifications, years_of_experience, location, is_verified) VALUES
('Dr. Abdul Karim', 'dr.karim@agriminds.com', '01712345678', '$2a$10$dummyhash1', 'Crop Diseases', 'PhD in Plant Pathology', 15, 'Dhaka', TRUE),
('Dr. Fatima Rahman', 'dr.fatima@agriminds.com', '01812345678', '$2a$10$dummyhash2', 'Soil Management', 'MSc in Soil Science', 10, 'Rajshahi', TRUE),
('Engr. Kamal Hossain', 'kamal.expert@agriminds.com', '01912345678', '$2a$10$dummyhash3', 'Pest Control', 'BSc in Agriculture', 8, 'Chittagong', TRUE);

CREATE TABLE IF NOT EXISTS login_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_type ENUM('farmer', 'expert', 'buyer') NOT NULL,
    email VARCHAR(100) NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    device_info VARCHAR(255),
    login_status ENUM('success', 'failed') DEFAULT 'success',
    failure_reason VARCHAR(255),
    INDEX idx_user (user_id, user_type),
    INDEX idx_login_time (login_time),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

COMMIT;

