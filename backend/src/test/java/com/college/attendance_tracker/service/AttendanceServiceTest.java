package com.college.attendance_tracker.service;

import com.college.attendance_tracker.dto.AttendanceSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceServiceTest {

    @Test
    @DisplayName("Calculate with 0 total classes returns 0 percentage")
    void testZeroTotalClasses() {
        AttendanceSummary summary = AttendanceService.calculate("Math", 0, 0, 75);
        assertEquals(0.0, summary.percentage());
        assertEquals("LOW", summary.status());
        assertEquals(0, summary.canSkip());
        assertEquals(0, summary.needToAttend());
    }

    @Test
    @DisplayName("Calculate with 100% attendance allows skipping classes")
    void testFullAttendanceCanSkip() {
        // 10 attended out of 10 total with 75% requirement
        // (10 * 100) / 75 = 13 total max classes allowed -> 13 - 10 = 3 classes can skip
        AttendanceSummary summary = AttendanceService.calculate("Physics", 10, 10, 75);
        assertEquals(100.0, summary.percentage());
        assertEquals("SAFE", summary.status());
        assertEquals(3, summary.canSkip());
        assertEquals(0, summary.needToAttend());
    }

    @Test
    @DisplayName("Calculate with low attendance correctly computes classes needed to recover")
    void testLowAttendanceRecovery() {
        // 5 attended out of 10 total with 75% requirement (50%)
        // Formula: ceil((75 * 10 - 100 * 5) / (100 - 75)) = ceil(250 / 25) = 10 classes
        AttendanceSummary summary = AttendanceService.calculate("Chemistry", 5, 10, 75);
        assertEquals(50.0, summary.percentage());
        assertEquals("LOW", summary.status());
        assertEquals(0, summary.canSkip());
        assertEquals(10, summary.needToAttend());
    }

    @Test
    @DisplayName("Calculate with exact 75% requirement is SAFE with 0 canSkip")
    void testExactRequirementThreshold() {
        // 15 attended out of 20 total = 75.0%
        AttendanceSummary summary = AttendanceService.calculate("Biology", 15, 20, 75);
        assertEquals(75.0, summary.percentage());
        assertEquals("SAFE", summary.status());
        assertEquals(0, summary.canSkip());
        assertEquals(0, summary.needToAttend());
    }
}
