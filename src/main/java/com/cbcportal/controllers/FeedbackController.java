package com.cbcportal.controllers;

import com.cbcportal.models.Feedback;
import com.cbcportal.models.User;
import com.cbcportal.repositories.UserRepository;
import com.cbcportal.services.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserRepository userRepository;

    // Submit new feedback
    @PostMapping("/submit/{userId}")
    public ResponseEntity<?> submitFeedback(@PathVariable Long userId, @RequestBody Feedback feedback) {
        Optional<User> senderOpt = userRepository.findById(userId);

        if (senderOpt.isPresent()) {
            feedback.setSender(senderOpt.get());
            return ResponseEntity.ok(feedbackService.submitFeedback(feedback));
        }
        return ResponseEntity.badRequest().body("Invalid User ID.");
    }

    // Fetch all feedback (for Admin)
    @GetMapping("/all")
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        return ResponseEntity.ok(feedbackService.getAllFeedback());
    }
}