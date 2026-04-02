package com.example.anajsetu.service;

import com.example.anajsetu.model.OtpToken;
import com.example.anajsetu.repository.OtpTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    // ── Put your Fast2SMS API key in application.properties ──
    @Value("${fast2sms.api.key}")
    private String fast2smsApiKey;

    // ─────────────────────────────────────────────────────────
    // 1. GENERATE — creates a random 6-digit OTP
    // ─────────────────────────────────────────────────────────
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // always 6 digits
        return String.valueOf(otp);
    }

    // ─────────────────────────────────────────────────────────
    // 2. SEND — saves OTP to DB + sends via Fast2SMS
    // ─────────────────────────────────────────────────────────
    @Transactional
    public void sendOtp(String phone, OtpToken.Purpose purpose, Integer listingId) {

        // Step A: Delete any old unused OTPs for this phone+purpose
        otpTokenRepository.deleteAllByPhoneAndPurpose(phone, purpose);

        // Step B: Generate a fresh OTP
        String otp = generateOtp();

        // Step C: Set expiry to 10 minutes from now
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        // Step D: Save to database
        OtpToken token = new OtpToken(phone, otp, purpose, listingId, expiresAt);
        otpTokenRepository.save(token);

        // Step E: Send via Fast2SMS
        sendViaSms(phone, otp);
    }

    // ─────────────────────────────────────────────────────────
    // 3. VERIFY — checks if OTP matches and is still valid
    // ─────────────────────────────────────────────────────────
    @Transactional
    public boolean verifyOtp(String phone, String otp, OtpToken.Purpose purpose) {

        // Step A: Find the latest unused OTP for this phone+purpose
        Optional<OtpToken> optionalToken =
            otpTokenRepository.findTopByPhoneAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(phone, purpose);

        // Step B: If no OTP found → invalid
        if (optionalToken.isEmpty()) {
            return false;
        }

        OtpToken token = optionalToken.get();

        // Step C: Check if OTP is expired
        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            return false;
        }

        // Step D: Check if OTP matches
        if (!token.getOtp().equals(otp)) {
            return false;
        }

        // Step E: Mark OTP as used so it can't be reused
        token.setUsed(true);
        otpTokenRepository.save(token);

        return true; // ✅ OTP is valid!
    }

    // ─────────────────────────────────────────────────────────
    // 4. SEND SMS — calls Fast2SMS REST API
    // ─────────────────────────────────────────────────────────
    private void sendViaSms(String phone, String otp) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Build the URL with OTP message
            String url = "https://www.fast2sms.com/dev/bulkV2" +
                    "?authorization=" + fast2smsApiKey +
                    "&route=otp" +
                    "&variables_values=" + otp +
                    "&flash=0" +
                    "&numbers=" + phone;

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("cache-control", "no-cache");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make GET request to Fast2SMS
            ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            System.out.println("SMS sent! Response: " + response.getBody());

        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
            // Don't throw — we don't want SMS failure to crash the whole request
        }
    }
}