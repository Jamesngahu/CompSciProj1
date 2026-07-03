package com.cbcportal.models;

import jakarta.persistence.*;

@Entity
@Table(name = "resource_requests")
public class ResourceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String itemType; // e.g., Textbooks, Lab Equipment, Desks

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationType donationType = DonationType.ITEM;

    // For DonationType.MONEY this is a KES amount rather than an item count
    @Column(nullable = false)
    private int quantityRequested;

    @Column(nullable = false)
    private int quantityFulfilled = 0;

    @ManyToOne
    @JoinColumn(name = "school_admin_id", nullable = false)
    private User schoolAdmin;

    // NEW: Transient fields for frontend display
    @Transient
    private String institutionName;

    @Transient
    private String county;

    // Default Constructor
    public ResourceRequest() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public DonationType getDonationType() { return donationType; }
    public void setDonationType(DonationType donationType) { this.donationType = donationType; }

    public int getQuantityRequested() { return quantityRequested; }
    public void setQuantityRequested(int quantityRequested) { this.quantityRequested = quantityRequested; }

    public int getQuantityFulfilled() { return quantityFulfilled; }
    public void setQuantityFulfilled(int quantityFulfilled) { this.quantityFulfilled = quantityFulfilled; }

    public User getSchoolAdmin() { return schoolAdmin; }
    public void setSchoolAdmin(User schoolAdmin) { this.schoolAdmin = schoolAdmin; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }
}