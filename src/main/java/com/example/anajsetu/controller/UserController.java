package com.example.anajsetu.controller;

import com.example.anajsetu.model.User;
import com.example.anajsetu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserService userService;

    // ── REGISTER ──────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        return new ResponseEntity<>(userService.registerUser(user), HttpStatus.CREATED);
    }

    // ── LOGIN (old email-based) ───────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> credentials) {
        String email    = credentials.get("email");
        String password = credentials.get("password");
        Optional<User> foundUser = userService.loginUser(email, password);
        if (foundUser.isPresent()) {
            User user = foundUser.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id",    user.getId());
            response.put("name",  user.getName());
            response.put("email", user.getEmail());
            response.put("role",  user.getRole());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(Map.of("message", "Invalid email or password"), HttpStatus.UNAUTHORIZED);
    }

    // ── GET ALL USERS ─────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    // ── GET USER BY ID ────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) return new ResponseEntity<>(user.get(), HttpStatus.OK);
        return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
    }

    // ── GET USERS BY ROLE ─────────────────────────────────────
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        return new ResponseEntity<>(userService.getUsersByRole(role), HttpStatus.OK);
    }

    // ── VERIFY USER ───────────────────────────────────────────
    @PutMapping("/{id}/verify")
    public ResponseEntity<?> verifyUser(@PathVariable int id) {
        User user = userService.verifyUser(id);
        if (user != null) return new ResponseEntity<>(user, HttpStatus.OK);
        return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
    }

    // ── UPDATE USER ───────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody User user) {
        user.setId(id);
        return new ResponseEntity<>(userService.updateUser(user), HttpStatus.OK);
    }

    // ── DELETE USER ───────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
    }

    // ── SIGNUP: Step 1 ────────────────────────────────────────
    @PostMapping("/signup/initiate")
    public ResponseEntity<?> initiateSignup(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        if (phone == null || phone.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "Phone number is required"));

        String result = userService.initiateSignup(phone);
        if (result.equals("PHONE_EXISTS"))
            return ResponseEntity.badRequest().body(Map.of("message", "Phone already registered. Please login."));

        return ResponseEntity.ok(Map.of("message", "OTP sent successfully to " + phone));
    }

    // ── SIGNUP: Step 2 ────────────────────────────────────────
    @PostMapping("/signup/complete")
    public ResponseEntity<?> completeSignup(@RequestBody Map<String, String> request) {
        String phone   = request.get("phone");
        String otp     = request.get("otp");
        String name    = request.get("name");
        String role    = request.get("role");
        String address = request.get("address");

        if (phone == null || otp == null || name == null || role == null)
            return ResponseEntity.badRequest().body(Map.of("message", "phone, otp, name and role are required"));

        User newUser = userService.completeSignup(phone, otp, name, role, address);
        if (newUser == null)
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP"));

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message", "Account created successfully! 🎉",
            "userId",  newUser.getId(),
            "name",    newUser.getName(),
            "phone",   newUser.getPhone(),
            "role",    newUser.getRole()
        ));
    }

    // ── LOGIN: Step 1 ─────────────────────────────────────────
    @PostMapping("/login/initiate")
    public ResponseEntity<?> initiateLogin(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        if (phone == null || phone.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "Phone number is required"));

        String result = userService.initiateLogin(phone);
        if (result.equals("PHONE_NOT_FOUND"))
            return ResponseEntity.badRequest().body(Map.of("message", "No account found. Please signup first."));

        return ResponseEntity.ok(Map.of("message", "OTP sent successfully to " + phone));
    }

    // ── LOGIN: Step 2 → JWT token ─────────────────────────────
    @PostMapping("/login/complete")
    public ResponseEntity<?> completeLogin(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String otp   = request.get("otp");

        if (phone == null || otp == null)
            return ResponseEntity.badRequest().body(Map.of("message", "phone and otp are required"));

        Map<String, Object> result = userService.loginWithOtp(phone, otp);
        if (result == null)
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP"));

        return ResponseEntity.ok(result);
    }
}