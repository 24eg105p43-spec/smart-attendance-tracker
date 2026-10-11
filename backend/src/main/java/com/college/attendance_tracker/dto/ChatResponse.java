package com.college.attendance_tracker.dto;

import java.util.List;

public record ChatResponse(
        String reply,
        List<String> suggestedActions
) {}
