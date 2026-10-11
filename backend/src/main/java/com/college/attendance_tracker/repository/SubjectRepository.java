package com.college.attendance_tracker.repository;

import com.college.attendance_tracker.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}