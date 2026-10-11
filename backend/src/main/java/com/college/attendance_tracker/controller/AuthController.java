package com.college.attendance_tracker.controller;

import com.college.attendance_tracker.dto.LoginRequest;
import com.college.attendance_tracker.dto.LoginResponse;
import com.college.attendance_tracker.dto.RegisterRequest;
import com.college.attendance_tracker.dto.UserResponse;
import com.college.attendance_tracker.model.Role;
import com.college.attendance_tracker.model.User;
import com.college.attendance_tracker.repository.UserRepository;
import com.college.attendance_tracker.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.STUDENT);
        userRepository.save(user);

        return ResponseEntity.ok(new UserResponse(
                user.getId(), user.getName(), user.getEmail(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new LoginResponse(
                token, user.getName(), user.getEmail(), user.getRole().name()));
    }
}