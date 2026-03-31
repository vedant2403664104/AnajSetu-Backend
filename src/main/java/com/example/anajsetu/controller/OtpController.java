package com.example.anajsetu.controller;

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

    @PostMapping("/generate")
    public ResponseEntity<?> generateOtp(@RequestBody Map<String, String> request) {
        String identifier = request.get("identifier");
        
        if (identifier == null || identifier.isEmpty()) {
            return ResponseEntity.badRequest().body("Identifier is required");
        }
        
        String otp = otpService.generateOtp(identifier);
        
        return ResponseEntity.ok(Map.of("message", "OTP generated successfully", "otp", otp));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String identifier = request.get("identifier");
        String submittedOtp = request.get("otp");

        if (identifier == null || submittedOtp == null) {
            return ResponseEntity.badRequest().body("Identifier and OTP are required");
        }

        boolean isValid = otpService.verifyOtp(identifier, submittedOtp);

        if (isValid) {
            return ResponseEntity.ok(Map.of("success", true, "message", "OTP verified successfully!"));
        } else {
            return ResponseEntity.status(400).body(Map.of("success", false, "message", "Invalid or expired OTP"));
        }
    }
}