package com.college.attendance_tracker.dto;

public record AlertItem(
        String severity, // "CRITICAL", "WARNING", "INFO"
        String title,
        String message,
        Long subjectId
) {}
