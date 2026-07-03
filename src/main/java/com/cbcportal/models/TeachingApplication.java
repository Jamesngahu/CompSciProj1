package com.cbcportal.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teaching_applications")
public class TeachingApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "educator_id", nullable = false)
    private User educator;

    @ManyToOne
    @JoinColumn(name = "vacancy_id", nullable = false)
    private Vacancy vacancy;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(nullable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();

    // NEW: Transient fields for displaying profile info to School Admins
    @Transient
    private String applicantTsc;

    @Transient
    private String applicantQualifications;

    public TeachingApplication() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getEducator() { return educator; }
    public void setEducator(User educator) { this.educator = educator; }

    public Vacancy getVacancy() { return vacancy; }
    public void setVacancy(Vacancy vacancy) { this.vacancy = vacancy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public String getApplicantTsc() { return applicantTsc; }
    public void setApplicantTsc(String applicantTsc) { this.applicantTsc = applicantTsc; }

    public String getApplicantQualifications() { return applicantQualifications; }
    public void setApplicantQualifications(String applicantQualifications) { this.applicantQualifications = applicantQualifications; }
}