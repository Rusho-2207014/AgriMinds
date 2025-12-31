package com.agriminds.service;

import com.agriminds.model.Buyer;
import com.agriminds.repository.BuyerRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class BuyerService {
    private static final Logger logger = LoggerFactory.getLogger(BuyerService.class);
    private BuyerRepository buyerRepository;

    public BuyerService() {
        this.buyerRepository = new BuyerRepository();
    }

    public Optional<Buyer> authenticate(String email, String password) {
        Optional<Buyer> buyerOpt = buyerRepository.findByEmail(email);

        if (buyerOpt.isPresent()) {
            Buyer buyer = buyerOpt.get();
            if (BCrypt.checkpw(password, buyer.getPasswordHash())) {
                logger.info("Buyer authenticated successfully: {}", email);
                return Optional.of(buyer);
            }
        }

        logger.warn("Authentication failed for buyer: {}", email);
        return Optional.empty();
    }

    public Long register(Buyer buyer, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        buyer.setPasswordHash(hashedPassword);
        buyer.setActive(true);
        buyer.setVerified(false);

        Long id = buyerRepository.save(buyer);
        if (id != null) {
            logger.info("Buyer registered successfully: {}", buyer.getEmail());
        }
        return id;
    }

    public Optional<Buyer> findById(Long id) {
        return buyerRepository.findById(id);
    }

    public Optional<Buyer> findByEmail(String email) {
        return buyerRepository.findByEmail(email);
    }
}
