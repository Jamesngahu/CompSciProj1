package com.cbcportal.controllers;

import com.cbcportal.services.MpesaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mpesa")
public class MpesaController {

    private static final Logger log = LoggerFactory.getLogger(MpesaController.class);

    @Autowired
    private MpesaService mpesaService;

    // Donor taps "Pay with M-Pesa" - triggers the STK push prompt on their phone
    @PostMapping("/stkpush/{donorId}/{requestId}")
    public ResponseEntity<?> stkPush(@PathVariable Long donorId, @PathVariable Long requestId, @RequestBody Map<String, Object> payload) {
        try {
            String phoneNumber = String.valueOf(payload.get("phoneNumber"));
            int amount = Integer.parseInt(String.valueOf(payload.get("amount")));
            return ResponseEntity.ok(mpesaService.initiateStkPush(donorId, requestId, phoneNumber, amount));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin manually retries a refund that previously failed (e.g. after a config fix)
    @PostMapping("/reversal/retry/{donationId}")
    public ResponseEntity<?> retryRefund(@PathVariable Long donationId) {
        try {
            mpesaService.retryRefund(donationId);
            return ResponseEntity.ok(Map.of("message", "Refund retry submitted."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Safaricom calls this once the donor accepts/rejects/times out on the STK prompt
    @PostMapping("/stkpush/callback")
    public ResponseEntity<?> stkCallback(@RequestBody Map<String, Object> payload) {
        try {
            mpesaService.handleStkCallback(payload);
        } catch (Exception e) {
            log.error("Error processing STK callback", e);
        }
        return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
    }

    // Safaricom calls this once the automatic refund reversal completes
    @PostMapping("/reversal/result")
    public ResponseEntity<?> reversalResult(@RequestBody Map<String, Object> payload) {
        try {
            mpesaService.handleReversalResult(payload);
        } catch (Exception e) {
            log.error("Error processing reversal result", e);
        }
        return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
    }

    // Safaricom calls this if the reversal request times out in its queue
    @PostMapping("/reversal/timeout")
    public ResponseEntity<?> reversalTimeout(@RequestBody Map<String, Object> payload) {
        log.warn("M-Pesa reversal timed out: {}", payload);
        return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
    }
}
