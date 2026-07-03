package com.cbcportal.services;

import com.cbcportal.models.Role;
import com.cbcportal.models.User;
import com.cbcportal.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    // At least 8 characters, one uppercase, one lowercase, one digit and one special character
    private static final Pattern STRONG_PASSWORD =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$");

    private static final int LOGIN_CODE_EXPIRY_MINUTES = 5;
    private static final int MAX_LOGIN_CODE_ATTEMPTS = 5;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public User registerUser(User user) {
        if (!STRONG_PASSWORD.matcher(user.getPassword()).matches()) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters and include an uppercase letter, "
                            + "a lowercase letter, a digit and a special character.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setApproved(user.getRole() == Role.DONOR);
        return userRepository.save(user);
    }

    /**
     * Step 1 of login: verify credentials and, if valid, email a one-time code.
     * Returns true if a code was sent.
     */
    public boolean startLogin(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.isApproved() && (user.getRole() == Role.EDUCATOR || user.getRole() == Role.SCHOOL_ADMIN)) {
            throw new RuntimeException("Account pending administrator verification.");
        }

        String code = generateCode();
        user.setLoginCode(code);
        user.setLoginCodeExpiry(LocalDateTime.now().plusMinutes(LOGIN_CODE_EXPIRY_MINUTES));
        user.setLoginCodeAttempts(0);
        userRepository.save(user);

        emailService.sendLoginCode(user.getEmail(), code);
        return true;
    }

    /**
     * Step 2 of login: confirm the emailed code and log the user in.
     */
    public Optional<User> verifyLoginCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or code"));

        if (user.getLoginCode() == null || user.getLoginCodeExpiry() == null) {
            throw new RuntimeException("Invalid or expired code");
        }

        if (LocalDateTime.now().isAfter(user.getLoginCodeExpiry())) {
            user.setLoginCode(null);
            user.setLoginCodeExpiry(null);
            userRepository.save(user);
            throw new RuntimeException("Code has expired. Please log in again.");
        }

        if (user.getLoginCodeAttempts() >= MAX_LOGIN_CODE_ATTEMPTS) {
            user.setLoginCode(null);
            user.setLoginCodeExpiry(null);
            userRepository.save(user);
            throw new RuntimeException("Too many incorrect attempts. Please log in again to get a new code.");
        }

        if (!user.getLoginCode().equals(code)) {
            user.setLoginCodeAttempts(user.getLoginCodeAttempts() + 1);
            userRepository.save(user);
            throw new RuntimeException("Invalid or expired code");
        }

        user.setLoginCode(null);
        user.setLoginCodeExpiry(null);
        user.setLoginCodeAttempts(0);
        userRepository.save(user);

        return Optional.of(user);
    }

    private String generateCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
