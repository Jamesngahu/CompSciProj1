package com.cbcportal.controllers;

import com.cbcportal.models.User;
import com.cbcportal.models.Vacancy;
import com.cbcportal.repositories.UserRepository;
import com.cbcportal.services.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vacancies")
public class VacancyController {

    @Autowired
    private VacancyService vacancyService;

    @Autowired
    private UserRepository userRepository;

    // Endpoint for a School Admin to create a vacancy
    @PostMapping("/create/{adminId}")
    public ResponseEntity<?> createVacancy(@PathVariable Long adminId, @RequestBody Vacancy vacancy) {
        Optional<User> adminOpt = userRepository.findById(adminId);

        if (adminOpt.isPresent() && "SCHOOL_ADMIN".equals(adminOpt.get().getRole().name())) {
            vacancy.setSchoolAdmin(adminOpt.get());
            return ResponseEntity.ok(vacancyService.createVacancy(vacancy));
        }
        return ResponseEntity.badRequest().body("Valid School Admin account is required to post vacancies.");
    }

    // Endpoint for Educators to browse active vacancies
    @GetMapping("/active")
    public ResponseEntity<List<Vacancy>> getActiveVacancies() {
        return ResponseEntity.ok(vacancyService.getActiveVacancies());
    }

    // NEW: Endpoint for a school admin to fetch their own vacancies
    @GetMapping("/school/{adminId}")
    public ResponseEntity<List<Vacancy>> getVacanciesByAdmin(@PathVariable Long adminId) {
        return ResponseEntity.ok(vacancyService.getVacanciesByAdmin(adminId));
    }
}