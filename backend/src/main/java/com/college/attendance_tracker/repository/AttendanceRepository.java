package com.college.attendance_tracker.repository;

import com.college.attendance_tracker.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findBySubjectId(Long subjectId);
}