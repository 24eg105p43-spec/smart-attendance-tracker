package com.college.attendance_tracker.dto;

import java.time.LocalDate;
import java.util.List;

public record SemesterSummaryResponse(
        LocalDate semesterEndDate,
        int totalRemainingClassDays,
        int totalHolidays,
        List<SubjectProjection> projections,
        List<AlertItem> alerts
) {}
