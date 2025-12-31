package com.agriminds.service;

import com.agriminds.model.Expert;
import com.agriminds.repository.ExpertRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ExpertService {
    private static final Logger logger = LoggerFactory.getLogger(ExpertService.class);
    private ExpertRepository expertRepository;

    public ExpertService() {
        this.expertRepository = new ExpertRepository();
    }

    public Optional<Expert> authenticate(String email, String password) {
        Optional<Expert> expertOpt = expertRepository.findByEmail(email);

        if (expertOpt.isPresent()) {
            Expert expert = expertOpt.get();
            if (BCrypt.checkpw(password, expert.getPasswordHash())) {
                logger.info("Expert authenticated successfully: {}", email);
                return Optional.of(expert);
            }
        }

        logger.warn("Authentication failed for expert: {}", email);
        return Optional.empty();
    }

    public Long register(Expert expert, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        expert.setPasswordHash(hashedPassword);
        expert.setActive(true);
        expert.setVerified(false);

        Long id = expertRepository.save(expert);
        if (id != null) {
            logger.info("Expert registered successfully: {}", expert.getEmail());
        }
        return id;
    }

    public Optional<Expert> findById(Long id) {
        return expertRepository.findById(id);
    }

    public Optional<Expert> findByEmail(String email) {
        return expertRepository.findByEmail(email);
    }
}
