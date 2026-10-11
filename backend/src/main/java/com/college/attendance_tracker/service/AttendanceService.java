package com.college.attendance_tracker.service;

import com.college.attendance_tracker.dto.AttendanceSummary;
import com.college.attendance_tracker.model.Attendance;
import com.college.attendance_tracker.model.Subject;
import com.college.attendance_tracker.repository.AttendanceRepository;
import com.college.attendance_tracker.repository.SubjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SubjectRepository subjectRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             SubjectRepository subjectRepository) {
        this.attendanceRepository = attendanceRepository;
        this.subjectRepository = subjectRepository;
    }

    public AttendanceSummary getSummary(Long subjectId, String email) {
        Subject subject = subjectRepository.findByIdAndOwnerEmail(subjectId, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        List<Attendance> records = attendanceRepository.findBySubjectId(subjectId);
        int total = records.size();
        int attended = (int) records.stream().filter(Attendance::isPresent).count();

        return calculate(subject.getName(), attended, total, subject.getMinPercentage());
    }

    // Pure calculation: no database, so it is easy to test
    public static AttendanceSummary calculate(String name, int attended, int total, int min) {
        double percentage = total == 0 ? 0 : Math.round(attended * 10000.0 / total) / 100.0;

        int canSkip = Math.max(0, (attended * 100) / min - total);

        int needToAttend = 0;
        if (attended * 100 < min * total) {
            needToAttend = (min >= 100)
                    ? -1
                    : Math.ceilDiv(min * total - 100 * attended, 100 - min);
        }

        String status = percentage >= min ? "SAFE" : "LOW";
        return new AttendanceSummary(name, total, attended, percentage, min, canSkip, needToAttend, status);
    }
}