package com.cbcportal.services;

import com.cbcportal.models.Donation;
import com.cbcportal.models.DonationType;
import com.cbcportal.models.PaymentStatus;
import com.cbcportal.models.RefundStatus;
import com.cbcportal.models.ResourceRequest;
import com.cbcportal.models.User;
import com.cbcportal.repositories.DonationRepository;
import com.cbcportal.repositories.ResourceRequestRepository;
import com.cbcportal.repositories.SchoolProfileRepository;
import com.cbcportal.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonationService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private ResourceRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchoolProfileRepository schoolProfileRepository;

    public Donation makeDonation(Long donorId, Long requestId, int quantity) {
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        ResourceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getDonationType() != DonationType.ITEM) {
            throw new RuntimeException("This request only accepts M-Pesa donations.");
        }

        if (request.getQuantityFulfilled() + quantity > request.getQuantityRequested()) {
            throw new RuntimeException("Donation exceeds requested amount.");
        }

        request.setQuantityFulfilled(request.getQuantityFulfilled() + quantity);
        requestRepository.save(request);

        Donation donation = new Donation();
        donation.setDonor(donor);
        donation.setResourceRequest(request);
        donation.setQuantity(quantity);
        return donationRepository.save(donation);
    }

    /** Creates a PENDING M-Pesa donation record before the STK push is sent. */
    public Donation createPendingMpesaDonation(Long donorId, Long requestId, String phoneNumber, int amount) {
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        ResourceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getDonationType() != DonationType.MONEY) {
            throw new RuntimeException("This request does not accept monetary donations.");
        }

        if (request.getQuantityFulfilled() + amount > request.getQuantityRequested()) {
            throw new RuntimeException("Donation exceeds the amount requested.");
        }

        Donation donation = new Donation();
        donation.setDonor(donor);
        donation.setResourceRequest(request);
        donation.setQuantity(amount);
        donation.setDonationType(DonationType.MONEY);
        donation.setStatus(PaymentStatus.PENDING);
        donation.setPhoneNumber(phoneNumber);
        donation.setRefundStatus(RefundStatus.NOT_APPLICABLE);
        return donationRepository.save(donation);
    }

    public Donation completeMpesaDonation(String checkoutRequestId, String mpesaReceiptNumber) {
        Donation donation = donationRepository.findByCheckoutRequestId(checkoutRequestId)
                .orElseThrow(() -> new RuntimeException("Donation not found for checkout request " + checkoutRequestId));

        donation.setStatus(PaymentStatus.COMPLETED);
        donation.setMpesaReceiptNumber(mpesaReceiptNumber);
        donation.setRefundStatus(RefundStatus.PENDING);

        ResourceRequest request = donation.getResourceRequest();
        request.setQuantityFulfilled(request.getQuantityFulfilled() + donation.getQuantity());
        requestRepository.save(request);

        return donationRepository.save(donation);
    }

    public void failMpesaDonation(String checkoutRequestId) {
        donationRepository.findByCheckoutRequestId(checkoutRequestId).ifPresent(donation -> {
            donation.setStatus(PaymentStatus.FAILED);
            donationRepository.save(donation);
        });
    }

    public void markRefunded(Donation donation, String refundReceiptNumber) {
        donation.setRefundStatus(RefundStatus.REFUNDED);
        donation.setRefundReceiptNumber(refundReceiptNumber);
        donationRepository.save(donation);
    }

    public void markRefundFailed(Donation donation) {
        donation.setRefundStatus(RefundStatus.FAILED);
        donationRepository.save(donation);
    }

    public List<Donation> getDonorHistory(Long donorId) {
        List<Donation> history = donationRepository.findByDonorIdOrderByDonatedAtDesc(donorId);

        for (Donation d : history) {
            schoolProfileRepository.findByUserId(d.getResourceRequest().getSchoolAdmin().getId())
                    .ifPresent(profile -> {
                        d.getResourceRequest().setInstitutionName(profile.getInstitutionName());
                    });
        }
        return history;
    }
}
