package com.college.attendance_tracker.service;

import com.college.attendance_tracker.dto.AttendanceSummary;
import com.college.attendance_tracker.model.Attendance;
import com.college.attendance_tracker.model.Subject;
import com.college.attendance_tracker.repository.AttendanceRepository;
import com.college.attendance_tracker.repository.SubjectRepository;
import org.springframework.stereotype.Service;

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

    public AttendanceSummary getSummary(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        List<Attendance> records = attendanceRepository.findBySubjectId(subjectId);
        int total = records.size();
        int attended = (int) records.stream().filter(Attendance::isPresent).count();

        return calculate(subject.getName(), attended, total, subject.getMinPercentage());
    }

    // Pure calculation: no database, so it is easy to test
    public static AttendanceSummary calculate(String name, int attended, int total, int min) {
        double percentage = total == 0 ? 0 : Math.round(attended * 10000.0 / total) / 100.0;

        // Most classes you can still miss: attended / (total + x) >= min/100
        int canSkip = Math.max(0, (attended * 100) / min - total);

        // Classes to attend in a row to get back to min:
        // (attended + y) / (total + y) >= min/100
        int needToAttend = 0;
        if (attended * 100 < min * total) {
            needToAttend = (min >= 100)
                    ? -1  // impossible to recover at 100%
                    : Math.ceilDiv(min * total - 100 * attended, 100 - min);
        }

        String status = percentage >= min ? "SAFE" : "LOW";
        return new AttendanceSummary(name, total, attended, percentage, min, canSkip, needToAttend, status);
    }
}