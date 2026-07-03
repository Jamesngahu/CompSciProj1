package com.cbcportal.services;

import com.cbcportal.models.Feedback;
import com.cbcportal.repositories.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    // Save new feedback submitted by a user
    public Feedback submitFeedback(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    // Fetch all feedback for the system administrator
    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAllByOrderBySubmittedAtDesc();
    }
}