package com.cbcportal.controllers;

import com.cbcportal.models.TeachingApplication;
import com.cbcportal.models.User;
import com.cbcportal.models.Vacancy;
import com.cbcportal.repositories.UserRepository;
import com.cbcportal.repositories.VacancyRepository;
import com.cbcportal.services.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
public class TeachingApplicationController {

    @Autowired
    private VacancyService vacancyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VacancyRepository vacancyRepository;

    @PostMapping("/apply/{vacancyId}/{userId}")
    public ResponseEntity<?> applyForVacancy(@PathVariable Long vacancyId, @PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Vacancy> vacOpt = vacancyRepository.findById(vacancyId);

        if (userOpt.isPresent() && vacOpt.isPresent()) {
            TeachingApplication app = new TeachingApplication();
            app.setEducator(userOpt.get());
            app.setVacancy(vacOpt.get());
            try {
                return ResponseEntity.ok(vacancyService.applyForVacancy(app));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.badRequest().body("Invalid User or Vacancy ID.");
    }

    // Endpoint to fetch applications for the School Admin
    @GetMapping("/school/{adminId}")
    public ResponseEntity<List<TeachingApplication>> getApplicationsForSchool(@PathVariable Long adminId) {
        return ResponseEntity.ok(vacancyService.getApplicationsForSchoolAdmin(adminId));
    }

    // Endpoint to Accept/Reject an application
    @PutMapping("/{applicationId}/status")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long applicationId, @RequestParam String status) {
        try {
            return ResponseEntity.ok(vacancyService.updateApplicationStatus(applicationId, status));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint to fetch applications for an Educator
    @GetMapping("/educator/{educatorId}")
    public ResponseEntity<List<TeachingApplication>> getApplicationsForEducator(@PathVariable Long educatorId) {
        return ResponseEntity.ok(vacancyService.getApplicationsForEducator(educatorId));
    }
}