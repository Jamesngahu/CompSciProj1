package com.cbcportal.services;

import com.cbcportal.models.Role;
import com.cbcportal.models.User;
import com.cbcportal.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Guarantees a working SYSTEM_ADMIN account always exists, since there is no
 * UI or API path to create one otherwise. Runs once on every startup.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.full-name}")
    private String adminFullName;

    public AdminSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        User admin = userRepository.findByEmail(adminEmail).orElseGet(User::new);

        admin.setFullName(adminFullName);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.SYSTEM_ADMIN);
        admin.setApproved(true);
        admin.setLoginCode(null);
        admin.setLoginCodeExpiry(null);
        admin.setLoginCodeAttempts(0);

        userRepository.save(admin);
        log.info("Admin account ready: {}", adminEmail);
    }
}
