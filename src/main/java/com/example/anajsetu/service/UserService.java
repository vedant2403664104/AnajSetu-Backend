package com.example.anajsetu.service;

import com.example.anajsetu.model.OtpToken;
import com.example.anajsetu.model.User;
import com.example.anajsetu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private OtpService otpService;

    @Autowired
    @Lazy
    private JwtService jwtService;

    // ── EXISTING METHODS ──────────────────────────────────────

    public User registerUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> loginUser(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return user;
        }
        return Optional.empty();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(int id) {
        return userRepository.findById(id);
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    public User verifyUser(int id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            user.get().setIsVerified(1);
            return userRepository.save(user.get());
        }
        return null;
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    // ── SIGNUP: Step 1 ────────────────────────────────────────
    public String initiateSignup(String phone) {
        if (userRepository.existsByPhone(phone)) {
            return "PHONE_EXISTS";
        }
        otpService.sendOtp(phone, OtpToken.Purpose.SIGNUP, null);
        return "OTP_SENT";
    }

    // ── SIGNUP: Step 2 ────────────────────────────────────────
    @Transactional
    public User completeSignup(String phone, String otp,
                               String name, String role, String address) {
        boolean isValid = otpService.verifyOtp(phone, otp, OtpToken.Purpose.SIGNUP);
        if (!isValid) return null;

        User newUser = new User();
        newUser.setPhone(phone);
        newUser.setName(name != null ? name : "User");
        newUser.setRole(role != null ? role : "USER");
        newUser.setAddress(address != null ? address : "");
        newUser.setEmail(phone + "@anajsetu.com");
        newUser.setPassword("OTP_USER");
        newUser.setIsVerified(1);
        return userRepository.save(newUser);
    }

    // ── LOGIN: Step 1 ─────────────────────────────────────────
    public String initiateLogin(String phone) {
        if (!userRepository.existsByPhone(phone)) {
            return "PHONE_NOT_FOUND";
        }
        otpService.sendOtp(phone, OtpToken.Purpose.LOGIN, null);
        return "OTP_SENT";
    }

    // ── LOGIN: Step 2 → returns JWT token ─────────────────────
    public Map<String, Object> loginWithOtp(String phone, String otp) {
        boolean isValid = otpService.verifyOtp(phone, otp, OtpToken.Purpose.LOGIN);
        if (!isValid) return null;

        Optional<User> optionalUser = userRepository.findByPhone(phone);
        if (optionalUser.isEmpty()) return null;

        User user = optionalUser.get();
        String token = jwtService.generateToken(
            user.getPhone(), user.getRole(), user.getId()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("token",  token);
        response.put("userId", user.getId());
        response.put("name",   user.getName());
        response.put("phone",  user.getPhone());
        response.put("role",   user.getRole());
        return response;
    }
}