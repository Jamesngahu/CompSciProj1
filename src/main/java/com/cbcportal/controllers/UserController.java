package com.cbcportal.controllers;

import com.cbcportal.models.User;
import com.cbcportal.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User savedUser = userService.registerUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Registration failed for {}", user.getEmail(), e);
            return ResponseEntity.badRequest().body("Registration failed. Email may already exist.");
        }
    }

    // Step 1: check credentials and email a one-time login code
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestParam String email, @RequestParam String password) {
        try {
            userService.startLogin(email, password);
            return ResponseEntity.ok().body("A verification code has been sent to your email.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // Step 2: confirm the emailed code to complete login
    @PostMapping("/login/verify")
    public ResponseEntity<?> verifyLogin(@RequestParam String email, @RequestParam String code) {
        try {
            Optional<User> user = userService.verifyLoginCode(email, code);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            }
            return ResponseEntity.status(401).body("Invalid or expired code");
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
