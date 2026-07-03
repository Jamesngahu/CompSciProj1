package com.cbcportal.controllers;

import com.cbcportal.models.EducatorProfile;
import com.cbcportal.models.User;
import com.cbcportal.repositories.EducatorProfileRepository;
import com.cbcportal.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/educators")
public class EducatorProfileController {

    @Autowired
    private EducatorProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/profile/{userId}")
    public ResponseEntity<?> createProfile(@PathVariable Long userId, @RequestBody EducatorProfile profile) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            profile.setUser(userOpt.get());
            profile.setVetted(false);
            return ResponseEntity.ok(profileRepository.save(profile));
        }
        return ResponseEntity.badRequest().body("User not found.");
    }

    // NEW: Fetch an educator's profile for their dashboard
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        Optional<EducatorProfile> opt = profileRepository.findByUserId(userId);
        if (opt.isPresent()) {
            return ResponseEntity.ok(opt.get());
        }
        return ResponseEntity.notFound().build();
    }

    // NEW: Update an educator's profile
    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable Long userId, @RequestBody EducatorProfile updatedProfile) {
        Optional<EducatorProfile> opt = profileRepository.findByUserId(userId);
        if (opt.isPresent()) {
            EducatorProfile profile = opt.get();
            profile.setTscRegistrationNumber(updatedProfile.getTscRegistrationNumber());
            profile.setQualifications(updatedProfile.getQualifications());
            return ResponseEntity.ok(profileRepository.save(profile));
        }
        return ResponseEntity.badRequest().body("Profile not found.");
    }
}