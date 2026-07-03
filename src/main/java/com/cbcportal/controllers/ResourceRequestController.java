package com.cbcportal.controllers;

import com.cbcportal.models.ResourceRequest;
import com.cbcportal.models.User;
import com.cbcportal.repositories.UserRepository;
import com.cbcportal.services.ResourceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/requests")
public class ResourceRequestController {

    @Autowired
    private ResourceRequestService requestService;

    @Autowired
    private UserRepository userRepository;

    // Endpoint for a School Admin to create a request
    @PostMapping("/create/{adminId}")
    public ResponseEntity<?> createRequest(@PathVariable Long adminId, @RequestBody ResourceRequest request) {
        Optional<User> adminOpt = userRepository.findById(adminId);

        if (adminOpt.isPresent() && "SCHOOL_ADMIN".equals(adminOpt.get().getRole().name())) {
            request.setSchoolAdmin(adminOpt.get());
            return ResponseEntity.ok(requestService.createRequest(request));
        }
        return ResponseEntity.badRequest().body("Valid School Admin account is required to post needs.");
    }

    // Endpoint for Donors to browse all requests
    @GetMapping("/all")
    public ResponseEntity<List<ResourceRequest>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    // NEW: Endpoint for a school admin to fetch their own requests
    @GetMapping("/school/{adminId}")
    public ResponseEntity<List<ResourceRequest>> getRequestsByAdmin(@PathVariable Long adminId) {
        return ResponseEntity.ok(requestService.getRequestsByAdmin(adminId));
    }
}