package com.cbcportal.repositories;

import com.cbcportal.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    // Get all feedback ordered by newest first
    List<Feedback> findAllByOrderBySubmittedAtDesc();
}