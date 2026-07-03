package com.cbcportal.repositories;

import com.cbcportal.models.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    // Fetch history for a specific donor, newest first
    List<Donation> findByDonorIdOrderByDonatedAtDesc(Long donorId);

    // Used to match an incoming M-Pesa STK callback back to its donation
    Optional<Donation> findByCheckoutRequestId(String checkoutRequestId);
}