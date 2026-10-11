package com.college.attendance_tracker.dto;

public record SubjectProjection(
        Long subjectId,
        String subjectName,
        int currentAttended,
        int currentTotal,
        double currentPercentage,
        int minRequiredPercentage,
        int remainingClasses,
        double maxPossiblePercentage,
        int safeBunksAllowed,
        int mustAttendClasses,
        String projectedStatus // "SAFE", "WARNING", "CRITICAL_DEFICIT"
) {}
