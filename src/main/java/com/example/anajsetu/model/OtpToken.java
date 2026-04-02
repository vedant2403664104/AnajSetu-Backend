package com.example.anajsetu.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_tokens")
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Column(name = "otp", nullable = false, length = 6)
    private String otp;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private Purpose purpose;

    @Column(name = "listing_id")
    private Integer listingId;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

  // ── Enum for purpose ──────────────────────────────────────
public enum Purpose {
    SIGNUP,
    DELIVERY,
    LOGIN
}
    // ── Constructors ──────────────────────────────────────────
    public OtpToken() {}

    public OtpToken(String phone, String otp, Purpose purpose, Integer listingId, LocalDateTime expiresAt) {
        this.phone      = phone;
        this.otp        = otp;
        this.purpose    = purpose;
        this.listingId  = listingId;
        this.expiresAt  = expiresAt;
        this.isUsed     = false;
        this.createdAt  = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────
    public Long getId()                     { return id; }

    public String getPhone()                { return phone; }
    public void setPhone(String phone)      { this.phone = phone; }

    public String getOtp()                  { return otp; }
    public void setOtp(String otp)          { this.otp = otp; }

    public Purpose getPurpose()             { return purpose; }
    public void setPurpose(Purpose purpose) { this.purpose = purpose; }

    public Integer getListingId()               { return listingId; }
    public void setListingId(Integer listingId) { this.listingId = listingId; }

    public boolean isUsed()                 { return isUsed; }
    public void setUsed(boolean used)       { this.isUsed = used; }

    public LocalDateTime getExpiresAt()                 { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt)   { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedAt()     { return createdAt; }
}