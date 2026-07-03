package com.cbcportal.models;

import jakarta.persistence.*;

@Entity
@Table(name = "educator_profiles")
public class EducatorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String tscRegistrationNumber;

    @Column(length = 1000)
    private String qualifications;

    private boolean isVetted = false;

    // Default Constructor
    public EducatorProfile() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTscRegistrationNumber() { return tscRegistrationNumber; }
    public void setTscRegistrationNumber(String tscRegistrationNumber) { this.tscRegistrationNumber = tscRegistrationNumber; }

    public String getQualifications() { return qualifications; }
    public void setQualifications(String qualifications) { this.qualifications = qualifications; }

    public boolean isVetted() { return isVetted; }
    public void setVetted(boolean vetted) { isVetted = vetted; }
}