package com.cbcportal.controllers;

import com.cbcportal.models.SchoolProfile;
import com.cbcportal.models.User;
import com.cbcportal.repositories.SchoolProfileRepository;
import com.cbcportal.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/schools")
public class SchoolProfileController {

    @Autowired
    private SchoolProfileRepository schoolProfileRepository;

    @Autowired
    private UserRepository userRepository;

    // Endpoint to create a school profile during registration
    @PostMapping("/profile/{userId}")
    public ResponseEntity<?> createProfile(@PathVariable Long userId, @RequestBody SchoolProfile profile) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            // Prevent duplicate MOE numbers
            if (schoolProfileRepository.findByMoeNumber(profile.getMoeNumber()).isPresent()) {
                return ResponseEntity.badRequest().body("A school with this MOE Number is already registered.");
            }

            profile.setUser(userOptional.get());
            SchoolProfile savedProfile = schoolProfileRepository.save(profile);
            return ResponseEntity.ok(savedProfile);
        }

        return ResponseEntity.badRequest().body("User not found.");
    }
}