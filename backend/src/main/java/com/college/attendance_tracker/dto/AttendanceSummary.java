package com.college.attendance_tracker.dto;

public record AttendanceSummary(
        String subject,
        int totalClasses,
        int attended,
        double percentage,
        int minRequired,
        int canSkip,
        int needToAttend,
        String status
) {}