package com.cbcportal.models;

import jakarta.persistence.*;

@Entity
@Table(name = "vacancies")
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1500)
    private String description;

    @Column(nullable = false)
    private String subjectArea; // e.g., Math, Science, Arts

    @Column(nullable = false)
    private boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "school_admin_id", nullable = false)
    private User schoolAdmin;

    // NEW: Transient fields for frontend display
    @Transient
    private String institutionName;

    @Transient
    private String county;

    // Default Constructor
    public Vacancy() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubjectArea() {
        return subjectArea;
    }

    public void setSubjectArea(String subjectArea) {
        this.subjectArea = subjectArea;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public User getSchoolAdmin() {
        return schoolAdmin;
    }

    public void setSchoolAdmin(User schoolAdmin) {
        this.schoolAdmin = schoolAdmin;
    }

    // Add these new Getters and Setters at the bottom:
    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }
}