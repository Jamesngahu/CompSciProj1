package com.cbcportal.models;

import jakarta.persistence.*;

@Entity
@Table(name = "school_profiles")
public class SchoolProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String institutionName;

    @Column(nullable = false)
    private String county;

    @Column(nullable = false, unique = true)
    private String moeNumber;

    // Default Constructor
    public SchoolProfile() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }

    public String getMoeNumber() { return moeNumber; }
    public void setMoeNumber(String moeNumber) { this.moeNumber = moeNumber; }
}