package com.example.anajsetu.repository;

import com.example.anajsetu.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    // Find the latest unused OTP for a phone + purpose
    // Used when verifying OTP during signup or delivery
    Optional<OtpToken> findTopByPhoneAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(
        String phone,
        OtpToken.Purpose purpose
    );

    // Delete all OTPs for a phone (cleanup after successful verify)
    void deleteAllByPhoneAndPurpose(String phone, OtpToken.Purpose purpose);
}
