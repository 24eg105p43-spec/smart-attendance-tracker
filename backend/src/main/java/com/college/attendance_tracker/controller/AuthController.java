package com.college.attendance_tracker.controller;

import com.college.attendance_tracker.dto.RegisterRequest;
import com.college.attendance_tracker.dto.UserResponse;
import com.college.attendance_tracker.model.Role;
import com.college.attendance_tracker.model.User;
import com.college.attendance_tracker.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        user.setRole(Role.STUDENT); // everyone registers as a student
        userRepository.save(user);

        return ResponseEntity.ok(new UserResponse(
                user.getId(), user.getName(), user.getEmail(), user.getRole().name()));
    }
}