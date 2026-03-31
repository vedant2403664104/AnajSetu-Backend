package com.example.anajsetu.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    public String generateOtp(String identifier) {
        Random random = new Random();
        String otp = String.format("%04d", random.nextInt(10000));
        otpStorage.put(identifier, otp);

        return otp; 
    }

    public boolean verifyOtp(String identifier, String submittedOtp) {
        String savedOtp = otpStorage.get(identifier);
        
        if (savedOtp != null && savedOtp.equals(submittedOtp)) {

            otpStorage.remove(identifier);
            return true;
        }
        return false;
    }
}