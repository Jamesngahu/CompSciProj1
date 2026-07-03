package com.cbcportal.repositories;

import com.cbcportal.models.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
    List<Vacancy> findByIsActiveTrue();

    // NEW: Fetch vacancies posted by a specific School Admin
    List<Vacancy> findBySchoolAdminId(Long adminId);
}