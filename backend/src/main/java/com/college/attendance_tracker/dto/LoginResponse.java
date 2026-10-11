package com.college.attendance_tracker.dto;

public record LoginResponse(String token, String name, String email, String role) {}