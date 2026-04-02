package com.example.anajsetu.controller;

import com.example.anajsetu.model.OtpToken;
import com.example.anajsetu.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@CrossOrigin(origins = "http://localhost:3000")
public class OtpController {

    @Autowired
    private OtpService otpService;

    // ── SEND OTP ──────────────────────────────────────────────
    // POST /api/otp/send
    // Body: { "phone": "9321677269", "purpose": "LOGIN" }
    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String phone   = request.get("phone");
        String purpose = request.get("purpose");

        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Phone number is required"));
        }

        if (purpose == null || purpose.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Purpose is required (SIGNUP / LOGIN / DELIVERY)"));
        }

        // Convert string to enum
        OtpToken.Purpose otpPurpose;
        try {
            otpPurpose = OtpToken.Purpose.valueOf(purpose.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Invalid purpose. Use: SIGNUP, LOGIN, or DELIVERY"));
        }

        otpService.sendOtp(phone, otpPurpose, null);

        return ResponseEntity.ok(
            Map.of("message", "OTP sent successfully to " + phone)
        );
    }

    // ── VERIFY OTP ────────────────────────────────────────────
    // POST /api/otp/verify
    // Body: { "phone": "9321677269", "otp": "123456", "purpose": "LOGIN" }
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String phone   = request.get("phone");
        String otp     = request.get("otp");
        String purpose = request.get("purpose");

        if (phone == null || otp == null || purpose == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "phone, otp and purpose are required"));
        }

        // Convert string to enum
        OtpToken.Purpose otpPurpose;
        try {
            otpPurpose = OtpToken.Purpose.valueOf(purpose.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Invalid purpose. Use: SIGNUP, LOGIN, or DELIVERY"));
        }

        boolean isValid = otpService.verifyOtp(phone, otp, otpPurpose);

        if (!isValid) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "valid",   false,
                    "message", "Invalid or expired OTP"
                ));
        }

        return ResponseEntity.ok(
            Map.of(
                "valid",   true,
                "message", "OTP verified successfully ✅"
            )
        );
    }
}