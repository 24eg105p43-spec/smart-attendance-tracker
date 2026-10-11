package com.college.attendance_tracker.controller;

import com.college.attendance_tracker.dto.AttendanceSummary;
import com.college.attendance_tracker.service.AttendanceService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects")
public class SummaryController {

    private final AttendanceService service;

    public SummaryController(AttendanceService service) {
        this.service = service;
    }

    @GetMapping("/{id}/summary")
    public AttendanceSummary getSummary(@PathVariable Long id) {
        return service.getSummary(id);
    }
}