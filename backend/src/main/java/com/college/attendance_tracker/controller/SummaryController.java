package com.college.attendance_tracker.controller;

import com.college.attendance_tracker.dto.AttendanceSummary;
import com.college.attendance_tracker.service.AttendanceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects")
public class SummaryController {

    private final AttendanceService service;

    public SummaryController(AttendanceService service) {
        this.service = service;
    }

    @GetMapping("/{id}/summary")
    public AttendanceSummary getSummary(@PathVariable Long id, Authentication auth) {
        return service.getSummary(id, auth.getName());
    }
}