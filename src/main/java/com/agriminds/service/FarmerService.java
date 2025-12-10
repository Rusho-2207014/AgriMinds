package com.agriminds.service;
import com.agriminds.model.Farmer;
import com.agriminds.repository.FarmerRepository;
import com.agriminds.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public class FarmerService {
    private static final Logger logger = LoggerFactory.getLogger(FarmerService.class);
    private final FarmerRepository farmerRepository;
    public FarmerService() {
        this.farmerRepository = new FarmerRepository();
    }
    public Long registerFarmer(Farmer farmer, String password) {
        if (!ValidationUtils.isNotEmpty(farmer.getFullName())) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (!ValidationUtils.isValidEmail(farmer.getEmail())) {
            throw new IllegalArgumentException("Invalid email address");
        }
        if (!ValidationUtils.isValidPhoneNumber(farmer.getPhoneNumber())) {
            throw new IllegalArgumentException("Invalid phone number");
        }
        if (!ValidationUtils.isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        try {
            if (farmerRepository.findByEmail(farmer.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email already registered");
            }
            if (farmerRepository.findByPhoneNumber(farmer.getPhoneNumber()).isPresent()) {
                throw new IllegalArgumentException("Phone number already registered");
            }
            String hashedPassword = ValidationUtils.hashPassword(password);
            farmer.setPasswordHash(hashedPassword);
            farmer.setRegistrationDate(LocalDateTime.now());
            farmer.setActive(true);
            Long farmerId = farmerRepository.save(farmer);
            logger.info("=== DEBUG: Repository returned farmerId: {} ===", farmerId);
            logger.info("Farmer registered successfully: {}", farmer.getFullName());
            return farmerId;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Database error during registration", e);
            throw new IllegalArgumentException("Cannot register: Database is not available. Please install and start MySQL server.");
        }
    }
    public Optional<Farmer> authenticate(String emailOrPhone, String password) {
        Optional<Farmer> farmerOpt;
        if (ValidationUtils.isValidEmail(emailOrPhone)) {
            farmerOpt = farmerRepository.findByEmail(emailOrPhone);
        } else if (ValidationUtils.isValidPhoneNumber(emailOrPhone)) {
            farmerOpt = farmerRepository.findByPhoneNumber(emailOrPhone);
        } else {
            return Optional.empty();
        }
        if (farmerOpt.isPresent()) {
            Farmer farmer = farmerOpt.get();
            if (!farmer.isActive()) {
                logger.warn("Inactive farmer attempted login: {}", farmer.getEmail());
                return Optional.empty();
            }
            if (ValidationUtils.verifyPassword(password, farmer.getPasswordHash())) {
                farmer.setLastLogin(LocalDateTime.now());
                farmerRepository.update(farmer);
                logger.info("Farmer authenticated successfully: {}", farmer.getFullName());
                return Optional.of(farmer);
            }
        }
        return Optional.empty();
    }
    public Optional<Farmer> getFarmerById(Long id) {
        return farmerRepository.findById(id);
    }
    public List<Farmer> getAllFarmers() {
        return farmerRepository.findAll();
    }
    public boolean updateFarmer(Farmer farmer) {
        if (farmer.getId() == null) {
            throw new IllegalArgumentException("Farmer ID is required for update");
        }
        if (!ValidationUtils.isNotEmpty(farmer.getFullName())) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (!ValidationUtils.isValidEmail(farmer.getEmail())) {
            throw new IllegalArgumentException("Invalid email address");
        }
        if (!ValidationUtils.isValidPhoneNumber(farmer.getPhoneNumber())) {
            throw new IllegalArgumentException("Invalid phone number");
        }
        return farmerRepository.update(farmer);
    }
    public boolean deactivateFarmer(Long farmerId) {
        Optional<Farmer> farmerOpt = farmerRepository.findById(farmerId);
        if (farmerOpt.isPresent()) {
            Farmer farmer = farmerOpt.get();
            farmer.setActive(false);
            return farmerRepository.update(farmer);
        }
        return false;
    }
    public boolean changePassword(Long farmerId, String oldPassword, String newPassword) {
        Optional<Farmer> farmerOpt = farmerRepository.findById(farmerId);
        if (farmerOpt.isEmpty()) {
            return false;
        }
        Farmer farmer = farmerOpt.get();
        if (!ValidationUtils.verifyPassword(oldPassword, farmer.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (!ValidationUtils.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("New password must be at least 8 characters");
        }
        String hashedPassword = ValidationUtils.hashPassword(newPassword);
        farmer.setPasswordHash(hashedPassword);
        return farmerRepository.update(farmer);
    }
}
