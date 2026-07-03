package com.cbcportal.controllers;

import com.cbcportal.models.EducatorProfile;
import com.cbcportal.models.SchoolProfile;
import com.cbcportal.models.User;
import com.cbcportal.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/educators/unvetted")
    public ResponseEntity<List<EducatorProfile>> getUnvettedEducators() {
        return ResponseEntity.ok(adminService.getUnvettedEducators());
    }

    @PutMapping("/educators/verify/{profileId}")
    public ResponseEntity<?> verifyEducator(@PathVariable Long profileId) {
        try {
            return ResponseEntity.ok(adminService.verifyEducator(profileId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/schools/unapproved")
    public ResponseEntity<List<SchoolProfile>> getUnapprovedSchools() {
        return ResponseEntity.ok(adminService.getUnapprovedSchools());
    }

    @PutMapping("/users/approve/{userId}")
    public ResponseEntity<?> approveUserAccount(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(adminService.approveUserAccount(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}