package com.cbcportal.repositories;

import com.cbcportal.models.EducatorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EducatorProfileRepository extends JpaRepository<EducatorProfile, Long> {
    List<EducatorProfile> findByIsVettedFalse();

    // RESTORED: The missing method your service is looking for!
    Optional<EducatorProfile> findByTscRegistrationNumber(String tscRegistrationNumber);

    // NEW: Fetch profile by the base User ID
    Optional<EducatorProfile> findByUserId(Long userId);
}