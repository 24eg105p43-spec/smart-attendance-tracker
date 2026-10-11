package com.college.attendance_tracker.controller;

import com.college.attendance_tracker.dto.SemesterSummaryResponse;
import com.college.attendance_tracker.service.SemesterService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/semester")
public class SemesterController {

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @GetMapping("/summary")
    public SemesterSummaryResponse getSummary(Authentication auth) {
        return semesterService.getSemesterSummary(auth.getName());
    }
}
