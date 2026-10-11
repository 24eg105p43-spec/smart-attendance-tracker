package com.college.attendance_tracker.controller;

import com.college.attendance_tracker.model.Attendance;
import com.college.attendance_tracker.model.Subject;
import com.college.attendance_tracker.repository.AttendanceRepository;
import com.college.attendance_tracker.repository.SubjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final SubjectRepository subjectRepository;

    public AttendanceController(AttendanceRepository attendanceRepository,
                                SubjectRepository subjectRepository) {
        this.attendanceRepository = attendanceRepository;
        this.subjectRepository = subjectRepository;
    }

    @PostMapping("/{subjectId}")
    public Attendance markAttendance(@PathVariable Long subjectId,
                                     @RequestParam boolean present,
                                     @RequestParam(required = false) String date,
                                     Authentication auth) {
        Subject subject = subjectRepository.findByIdAndOwnerEmail(subjectId, auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        Attendance record = new Attendance();
        record.setSubject(subject);
        record.setPresent(present);
        record.setDate(date == null ? LocalDate.now() : LocalDate.parse(date));
        return attendanceRepository.save(record);
    }
}