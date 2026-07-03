package com.cbcportal.services;

import com.cbcportal.models.Notification;
import com.cbcportal.models.TeachingApplication;
import com.cbcportal.models.Vacancy;
import com.cbcportal.repositories.EducatorProfileRepository;
import com.cbcportal.repositories.NotificationRepository;
import com.cbcportal.repositories.SchoolProfileRepository;
import com.cbcportal.repositories.TeachingApplicationRepository;
import com.cbcportal.repositories.VacancyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VacancyService {

    @Autowired
    private VacancyRepository vacancyRepository;

    @Autowired
    private TeachingApplicationRepository applicationRepository;

    @Autowired
    private SchoolProfileRepository schoolProfileRepository;

    @Autowired
    private EducatorProfileRepository educatorProfileRepository; // NEW

    @Autowired
    private NotificationRepository notificationRepository;

    public Vacancy createVacancy(Vacancy vacancy) {
        return vacancyRepository.save(vacancy);
    }

    public List<Vacancy> getActiveVacancies() {
        List<Vacancy> vacancies = vacancyRepository.findByIsActiveTrue();
        for (Vacancy vac : vacancies) {
            schoolProfileRepository.findByUserId(vac.getSchoolAdmin().getId())
                    .ifPresent(profile -> {
                        vac.setInstitutionName(profile.getInstitutionName());
                        vac.setCounty(profile.getCounty());
                    });
        }
        return vacancies;
    }

    public List<Vacancy> getVacanciesByAdmin(Long adminId) {
        List<Vacancy> vacancies = vacancyRepository.findBySchoolAdminId(adminId);
        for (Vacancy vac : vacancies) {
            schoolProfileRepository.findByUserId(adminId)
                    .ifPresent(profile -> {
                        vac.setInstitutionName(profile.getInstitutionName());
                        vac.setCounty(profile.getCounty());
                    });
        }
        return vacancies;
    }

    public TeachingApplication applyForVacancy(TeachingApplication application) {
        Optional<TeachingApplication> existing = applicationRepository.findByVacancyIdAndEducatorId(
                application.getVacancy().getId(),
                application.getEducator().getId()
        );
        if (existing.isPresent()) {
            throw new RuntimeException("You have already applied for this vacancy.");
        }
        application.setStatus("PENDING");
        return applicationRepository.save(application);
    }

    // UPDATED: Attach the educator's profile data so the school admin can review it
    public List<TeachingApplication> getApplicationsForSchoolAdmin(Long adminId) {
        List<TeachingApplication> apps = applicationRepository.findByVacancySchoolAdminId(adminId);
        for (TeachingApplication app : apps) {
            educatorProfileRepository.findByUserId(app.getEducator().getId())
                    .ifPresent(profile -> {
                        app.setApplicantTsc(profile.getTscRegistrationNumber());
                        app.setApplicantQualifications(profile.getQualifications());
                    });
        }
        return apps;
    }

    public List<TeachingApplication> getApplicationsForEducator(Long educatorId) {
        return applicationRepository.findByEducatorId(educatorId);
    }

    // UPDATED: Handle auto-closing vacancies and auto-rejecting competing applications
    // UPDATED: Added @Transactional to ensure all auto-rejects save properly
    @Transactional
    public TeachingApplication updateApplicationStatus(Long applicationId, String status) {
        Optional<TeachingApplication> appOpt = applicationRepository.findById(applicationId);
        if (appOpt.isPresent()) {
            TeachingApplication app = appOpt.get();
            app.setStatus(status);
            TeachingApplication savedApp = applicationRepository.save(app);

            // Notify the primary applicant of their exact status
            Notification notif = new Notification();
            notif.setRecipient(app.getEducator());
            notif.setMessage("Your teaching application for '" + app.getVacancy().getTitle() + "' has been " + status + ".");
            notificationRepository.save(notif);

            // NEW: If this person was ACCEPTED, close the vacancy and reject everyone else
            if ("ACCEPTED".equals(status)) {
                Vacancy vacancy = app.getVacancy();
                vacancy.setActive(false); // Removes it from the active board
                vacancyRepository.save(vacancy);

                List<TeachingApplication> otherApps = applicationRepository.findByVacancyId(vacancy.getId());
                for (TeachingApplication otherApp : otherApps) {
                    // Find all other applications that are still PENDING
                    if (!otherApp.getId().equals(applicationId) && "PENDING".equals(otherApp.getStatus())) {
                        otherApp.setStatus("REJECTED");
                        applicationRepository.save(otherApp);

                        // Send an automatic polite rejection notification
                        Notification rejectNotif = new Notification();
                        rejectNotif.setRecipient(otherApp.getEducator());
                        rejectNotif.setMessage("Status Update: The teaching vacancy for '" + vacancy.getTitle() + "' has been successfully filled. Your application has been automatically closed.");
                        notificationRepository.save(rejectNotif);
                    }
                }
            }

            return savedApp;
        }
        throw new RuntimeException("Application not found.");
    }
}