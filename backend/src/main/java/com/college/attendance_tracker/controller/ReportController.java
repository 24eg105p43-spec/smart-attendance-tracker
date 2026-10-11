package com.college.attendance_tracker.controller;

import com.college.attendance_tracker.dto.AttendanceSummary;
import com.college.attendance_tracker.model.Subject;
import com.college.attendance_tracker.repository.SubjectRepository;
import com.college.attendance_tracker.service.AttendanceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final SubjectRepository subjectRepository;
    private final AttendanceService attendanceService;

    public ReportController(SubjectRepository subjectRepository, AttendanceService attendanceService) {
        this.subjectRepository = subjectRepository;
        this.attendanceService = attendanceService;
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportCsv(Authentication auth) {
        List<Subject> subjects = subjectRepository.findByOwnerEmail(auth.getName());

        StringBuilder csv = new StringBuilder();
        csv.append("Subject,Total Classes,Attended,Absent,Percentage (%),Min Required (%),Status,Can Skip,Need To Attend\n");

        for (Subject s : subjects) {
            AttendanceSummary sum = attendanceService.getSummary(s.getId(), auth.getName());
            int absent = sum.totalClasses() - sum.attended();
            csv.append(escape(sum.subject())).append(",")
               .append(sum.totalClasses()).append(",")
               .append(sum.attended()).append(",")
               .append(absent).append(",")
               .append(sum.percentage()).append(",")
               .append(sum.minRequired()).append(",")
               .append(sum.status()).append(",")
               .append(sum.canSkip()).append(",")
               .append(sum.needToAttend() == -1 ? "N/A" : sum.needToAttend()).append("\n");
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        String filename = "attendance_report_" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
