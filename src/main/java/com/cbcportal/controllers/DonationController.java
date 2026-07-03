package com.cbcportal.controllers;

import com.cbcportal.models.Donation;
import com.cbcportal.services.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/donations")
public class DonationController {

    @Autowired
    private DonationService donationService;

    @PostMapping("/make/{donorId}/{requestId}")
    public ResponseEntity<?> makeDonation(@PathVariable Long donorId, @PathVariable Long requestId, @RequestBody Map<String, Integer> payload) {
        try {
            int quantity = payload.get("quantityDonated");
            return ResponseEntity.ok(donationService.makeDonation(donorId, requestId, quantity));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/{donorId}")
    public ResponseEntity<List<Donation>> getHistory(@PathVariable Long donorId) {
        return ResponseEntity.ok(donationService.getDonorHistory(donorId));
    }
}