package com.college.attendance_tracker.repository;

import com.college.attendance_tracker.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByOwnerEmail(String email);
    Optional<Subject> findByIdAndOwnerEmail(Long id, String email);
}