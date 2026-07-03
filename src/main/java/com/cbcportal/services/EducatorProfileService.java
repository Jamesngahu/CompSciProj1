package com.cbcportal.services;

import com.cbcportal.models.EducatorProfile;
import com.cbcportal.repositories.EducatorProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EducatorProfileService {

    @Autowired
    private EducatorProfileRepository profileRepository;

    // Save a new educator profile linking to a user account
    public EducatorProfile saveEducatorProfile(EducatorProfile profile) {
        return profileRepository.save(profile);
    }

    // Lookup an educator by their TSC number (useful for the Admin Module later)
    public Optional<EducatorProfile> getProfileByTscNumber(String tscNumber) {
        return profileRepository.findByTscRegistrationNumber(tscNumber);
    }
}
