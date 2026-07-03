package com.cbcportal.services;

import com.cbcportal.models.ResourceRequest;
import com.cbcportal.repositories.ResourceRequestRepository;
import com.cbcportal.repositories.SchoolProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceRequestService {

    @Autowired
    private ResourceRequestRepository requestRepository;

    @Autowired
    private SchoolProfileRepository schoolProfileRepository;

    public ResourceRequest createRequest(ResourceRequest request) {
        return requestRepository.save(request);
    }

    public List<ResourceRequest> getAllRequests() {
        List<ResourceRequest> requests = requestRepository.findAll();

        // NEW: Loop through requests and attach the school details
        for (ResourceRequest req : requests) {
            schoolProfileRepository.findByUserId(req.getSchoolAdmin().getId())
                    .ifPresent(profile -> {
                        req.setInstitutionName(profile.getInstitutionName());
                        req.setCounty(profile.getCounty());
                    });
        }
        return requests;
    }

    // UPDATED: Fetch requests for a specific admin and attach school details
    public List<ResourceRequest> getRequestsByAdmin(Long adminId) {
        List<ResourceRequest> requests = requestRepository.findBySchoolAdminId(adminId);
        for (ResourceRequest req : requests) {
            schoolProfileRepository.findByUserId(adminId)
                    .ifPresent(profile -> {
                        req.setInstitutionName(profile.getInstitutionName());
                        req.setCounty(profile.getCounty());
                    });
        }
        return requests;
    }
}