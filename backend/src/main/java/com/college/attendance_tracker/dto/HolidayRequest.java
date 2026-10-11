package com.college.attendance_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record HolidayRequest(
        @NotBlank(message = "Holiday name is required")
        String name,

        @NotNull(message = "Holiday date is required")
        LocalDate date
) {}
