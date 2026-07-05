package com.cbcportal.services;

import com.cbcportal.models.Donation;
import com.cbcportal.repositories.DonationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Integrates with Safaricom's Daraja API (M-Pesa) in sandbox mode:
 * an STK push prompts the donor to pay, and on success the payment is
 * immediately reversed so no real money is retained - this is a test/demo
 * app, not a production payment system.
 */
@Service
public class MpesaService {

    private static final Logger log = LoggerFactory.getLogger(MpesaService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private DonationService donationService;

    @Autowired
    private DonationRepository donationRepository;

    @Value("${mpesa.base-url}")
    private String baseUrl;

    @Value("${mpesa.consumer-key}")
    private String consumerKey;

    @Value("${mpesa.consumer-secret}")
    private String consumerSecret;

    @Value("${mpesa.shortcode}")
    private String shortcode;

    @Value("${mpesa.passkey}")
    private String passkey;

    @Value("${mpesa.callback-base-url}")
    private String callbackBaseUrl;

    @Value("${mpesa.initiator-name}")
    private String initiatorName;

    // Pre-encrypted via Daraja's "Test Credentials" page rather than encrypted
    // locally with a certificate - simpler and avoids bundling Safaricom's cert.
    @Value("${mpesa.security-credential}")
    private String securityCredential;

    /** Step 1: prompt the donor's phone with an STK push for the given amount. */
    public Map<String, Object> initiateStkPush(Long donorId, Long requestId, String rawPhoneNumber, int amount) {
        String phoneNumber = normalizePhoneNumber(rawPhoneNumber);
        Donation donation = donationService.createPendingMpesaDonation(donorId, requestId, phoneNumber, amount);

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String password = Base64.getEncoder().encodeToString((shortcode + passkey + timestamp).getBytes(StandardCharsets.UTF_8));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("BusinessShortCode", shortcode);
        body.put("Password", password);
        body.put("Timestamp", timestamp);
        body.put("TransactionType", "CustomerPayBillOnline");
        body.put("Amount", amount);
        body.put("PartyA", phoneNumber);
        body.put("PartyB", shortcode);
        body.put("PhoneNumber", phoneNumber);
        body.put("CallBackURL", callbackBaseUrl + "/api/mpesa/stkpush/callback");
        body.put("AccountReference", "CBCDonation" + requestId);
        body.put("TransactionDesc", "CBC Portal Donation");

        HttpHeaders headers = authHeaders();
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
                baseUrl + "/mpesa/stkpush/v1/processrequest",
                new HttpEntity<>(body, headers),
                Map.class
        );

        if (response != null) {
            donation.setMerchantRequestId((String) response.get("MerchantRequestID"));
            donation.setCheckoutRequestId((String) response.get("CheckoutRequestID"));
            donationRepository.save(donation);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("donationId", donation.getId());
        result.put("customerMessage", response != null ? response.getOrDefault("CustomerMessage", "STK push sent.") : "STK push sent.");
        result.put("checkoutRequestId", donation.getCheckoutRequestId() != null ? donation.getCheckoutRequestId() : "");
        return result;
    }

    /** Step 2: Safaricom calls this back once the donor accepts/rejects the STK prompt. */
    @SuppressWarnings("unchecked")
    public void handleStkCallback(Map<String, Object> payload) {
        Map<String, Object> body = (Map<String, Object>) payload.get("Body");
        Map<String, Object> callback = (Map<String, Object>) body.get("stkCallback");

        String checkoutRequestId = (String) callback.get("CheckoutRequestID");
        int resultCode = (int) callback.get("ResultCode");

        if (resultCode != 0) {
            log.warn("STK push failed/cancelled for {}: {}", checkoutRequestId, callback.get("ResultDesc"));
            donationService.failMpesaDonation(checkoutRequestId);
            return;
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>)
                ((Map<String, Object>) callback.get("CallbackMetadata")).get("Item");

        String mpesaReceiptNumber = null;
        for (Map<String, Object> item : items) {
            if ("MpesaReceiptNumber".equals(item.get("Name"))) {
                mpesaReceiptNumber = String.valueOf(item.get("Value"));
            }
        }

        Donation donation = donationService.completeMpesaDonation(checkoutRequestId, mpesaReceiptNumber);
        reverseTransaction(donation);
    }

    /** Refunds the donor by reversing the transaction just completed (test-mode only). */
    private void reverseTransaction(Donation donation) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("Initiator", initiatorName);
            body.put("SecurityCredential", securityCredential);
            body.put("CommandID", "TransactionReversal");
            body.put("TransactionID", donation.getMpesaReceiptNumber());
            body.put("Amount", donation.getQuantity());
            body.put("ReceiverParty", shortcode);
            body.put("RecieverIdentifierType", 4);
            body.put("ResultURL", callbackBaseUrl + "/api/mpesa/reversal/result");
            body.put("QueueTimeOutURL", callbackBaseUrl + "/api/mpesa/reversal/timeout");
            body.put("Remarks", "Test mode - automatic donation refund");
            body.put("Occasion", "CBCPortalRefund");

            restTemplate.postForObject(baseUrl + "/mpesa/reversal/v1/request", new HttpEntity<>(body, authHeaders()), Map.class);
        } catch (Exception e) {
            log.error("Failed to initiate refund reversal for donation {}", donation.getId(), e);
            donationService.markRefundFailed(donation);
        }
    }

    /** Step 3: Safaricom calls this back once the refund reversal completes. */
    @SuppressWarnings("unchecked")
    public void handleReversalResult(Map<String, Object> payload) {
        Map<String, Object> result = (Map<String, Object>) payload.get("Result");
        int resultCode = (int) result.get("ResultCode");

        String originalTransactionId = findResultParameter(result, "TransactionID")
                .or(() -> findResultParameter(result, "OriginalTransactionID"))
                .orElse(null);

        if (originalTransactionId == null) {
            log.warn("Reversal result missing original transaction id: {}", result);
            return;
        }

        donationRepository.findAll().stream()
                .filter(d -> originalTransactionId.equals(d.getMpesaReceiptNumber()))
                .findFirst()
                .ifPresent(donation -> {
                    if (resultCode == 0) {
                        donationService.markRefunded(donation, findResultParameter(result, "TransactionReceipt").orElse(originalTransactionId));
                    } else {
                        log.warn("Refund reversal failed for donation {}: {}", donation.getId(), result.get("ResultDesc"));
                        donationService.markRefundFailed(donation);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private java.util.Optional<String> findResultParameter(Map<String, Object> result, String key) {
        Object paramsObj = result.get("ResultParameters");
        if (!(paramsObj instanceof Map)) return java.util.Optional.empty();
        Object items = ((Map<String, Object>) paramsObj).get("ResultParameter");
        if (!(items instanceof List)) return java.util.Optional.empty();

        for (Object item : (List<Object>) items) {
            Map<String, Object> param = (Map<String, Object>) item;
            if (key.equals(param.get("Key"))) {
                return java.util.Optional.ofNullable(String.valueOf(param.get("Value")));
            }
        }
        return java.util.Optional.empty();
    }

    private String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        String credentials = Base64.getEncoder().encodeToString((consumerKey + ":" + consumerSecret).getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + credentials);

        Map<?, ?> response = restTemplate.exchange(
                baseUrl + "/oauth/v1/generate?grant_type=client_credentials",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        ).getBody();

        if (response == null || response.get("access_token") == null) {
            throw new RuntimeException("Failed to obtain M-Pesa access token. Check mpesa.consumer-key/secret.");
        }
        return (String) response.get("access_token");
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAccessToken());
        return headers;
    }

    private String normalizePhoneNumber(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.startsWith("0")) {
            return "254" + digits.substring(1);
        }
        if (digits.startsWith("254")) {
            return digits;
        }
        return "254" + digits;
    }
}
