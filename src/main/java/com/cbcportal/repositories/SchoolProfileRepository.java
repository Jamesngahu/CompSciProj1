package com.cbcportal.repositories;

import com.cbcportal.models.SchoolProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolProfileRepository extends JpaRepository<SchoolProfile, Long> {
    Optional<SchoolProfile> findByUserId(Long userId);
    Optional<SchoolProfile> findByMoeNumber(String moeNumber);
    List<SchoolProfile> findByUserIsApprovedFalse();
}