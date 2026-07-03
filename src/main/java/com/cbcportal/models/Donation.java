package com.cbcportal.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private ResourceRequest resourceRequest;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private LocalDateTime donatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationType donationType = DonationType.ITEM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.COMPLETED;

    // M-Pesa specific fields - only populated for DonationType.MONEY
    private String phoneNumber;
    private String merchantRequestId;
    private String checkoutRequestId;
    private String mpesaReceiptNumber;

    // This is a test/demo app: successful M-Pesa payments are automatically
    // reversed so no real money is retained from testers.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus refundStatus = RefundStatus.NOT_APPLICABLE;

    private String refundReceiptNumber;

    public Donation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getDonor() { return donor; }
    public void setDonor(User donor) { this.donor = donor; }

    public ResourceRequest getResourceRequest() { return resourceRequest; }
    public void setResourceRequest(ResourceRequest resourceRequest) { this.resourceRequest = resourceRequest; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getDonatedAt() { return donatedAt; }
    public void setDonatedAt(LocalDateTime donatedAt) { this.donatedAt = donatedAt; }

    public DonationType getDonationType() { return donationType; }
    public void setDonationType(DonationType donationType) { this.donationType = donationType; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getMerchantRequestId() { return merchantRequestId; }
    public void setMerchantRequestId(String merchantRequestId) { this.merchantRequestId = merchantRequestId; }

    public String getCheckoutRequestId() { return checkoutRequestId; }
    public void setCheckoutRequestId(String checkoutRequestId) { this.checkoutRequestId = checkoutRequestId; }

    public String getMpesaReceiptNumber() { return mpesaReceiptNumber; }
    public void setMpesaReceiptNumber(String mpesaReceiptNumber) { this.mpesaReceiptNumber = mpesaReceiptNumber; }

    public RefundStatus getRefundStatus() { return refundStatus; }
    public void setRefundStatus(RefundStatus refundStatus) { this.refundStatus = refundStatus; }

    public String getRefundReceiptNumber() { return refundReceiptNumber; }
    public void setRefundReceiptNumber(String refundReceiptNumber) { this.refundReceiptNumber = refundReceiptNumber; }
}
