package com.cbcportal.repositories;

import com.cbcportal.models.TeachingApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeachingApplicationRepository extends JpaRepository<TeachingApplication, Long> {
    Optional<TeachingApplication> findByVacancyIdAndEducatorId(Long vacancyId, Long educatorId);

    List<TeachingApplication> findByVacancySchoolAdminId(Long adminId);

    List<TeachingApplication> findByEducatorId(Long educatorId);

    // NEW: Fetch all applications for a specific vacancy so we can auto-reject the others
    List<TeachingApplication> findByVacancyId(Long vacancyId);
}