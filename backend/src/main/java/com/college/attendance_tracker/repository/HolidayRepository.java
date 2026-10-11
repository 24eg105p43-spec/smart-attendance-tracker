package com.college.attendance_tracker.repository;

import com.college.attendance_tracker.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    @Query("SELECT h FROM Holiday h WHERE h.user.email = :email OR h.user IS NULL ORDER BY h.date ASC")
    List<Holiday> findAllForUser(@Param("email") String email);

    List<Holiday> findByUserEmail(String email);

    boolean existsByDateAndUserEmail(LocalDate date, String email);
}
