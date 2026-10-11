package com.college.attendance_tracker.service;

import com.college.attendance_tracker.dto.AlertItem;
import com.college.attendance_tracker.dto.SemesterSummaryResponse;
import com.college.attendance_tracker.dto.SubjectProjection;
import com.college.attendance_tracker.model.Attendance;
import com.college.attendance_tracker.model.Holiday;
import com.college.attendance_tracker.model.Subject;
import com.college.attendance_tracker.repository.AttendanceRepository;
import com.college.attendance_tracker.repository.HolidayRepository;
import com.college.attendance_tracker.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SemesterService {

    private final SubjectRepository subjectRepository;
    private final AttendanceRepository attendanceRepository;
    private final HolidayRepository holidayRepository;

    @Value("${semester.end-date:2026-12-20}")
    private String semesterEndDateStr;

    @Value("${semester.class-days:MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY}")
    private String classDaysStr;

    public SemesterService(SubjectRepository subjectRepository,
                           AttendanceRepository attendanceRepository,
                           HolidayRepository holidayRepository) {
        this.subjectRepository = subjectRepository;
        this.attendanceRepository = attendanceRepository;
        this.holidayRepository = holidayRepository;
    }

    public SemesterSummaryResponse getSemesterSummary(String userEmail) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = LocalDate.parse(semesterEndDateStr.trim());

        Set<DayOfWeek> validDays = Arrays.stream(classDaysStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());

        List<Holiday> holidays = holidayRepository.findAllForUser(userEmail);
        Set<LocalDate> holidayDates = holidays.stream()
                .map(Holiday::getDate)
                .collect(Collectors.toSet());

        // Count remaining active class days
        int remainingClassDays = 0;
        LocalDate cursor = today.plusDays(1);
        while (!cursor.isAfter(endDate)) {
            if (validDays.contains(cursor.getDayOfWeek()) && !holidayDates.contains(cursor)) {
                remainingClassDays++;
            }
            cursor = cursor.plusDays(1);
        }

        List<Subject> subjects = subjectRepository.findByOwnerEmail(userEmail);
        List<SubjectProjection> projections = new ArrayList<>();
        List<AlertItem> alerts = new ArrayList<>();

        for (Subject s : subjects) {
            List<Attendance> records = attendanceRepository.findBySubjectId(s.getId());
            int currentTotal = records.size();
            int currentAttended = (int) records.stream().filter(Attendance::isPresent).count();
            int minReq = s.getMinPercentage();

            double currentPercentage = currentTotal == 0 ? 0.0 :
                    Math.round(currentAttended * 10000.0 / currentTotal) / 100.0;

            int finalTotal = currentTotal + remainingClassDays;
            double maxPossible = finalTotal == 0 ? 100.0 :
                    Math.round((currentAttended + remainingClassDays) * 10000.0 / finalTotal) / 100.0;

            int requiredTotalAttended = (int) Math.ceil((minReq * finalTotal) / 100.0);
            int mustAttend = Math.max(0, requiredTotalAttended - currentAttended);
            int safeBunksAllowed = 0;

            String status;
            if (maxPossible < minReq) {
                status = "CRITICAL_DEFICIT";
                mustAttend = remainingClassDays;
                alerts.add(new AlertItem(
                        "CRITICAL",
                        "Detention Risk: " + s.getName(),
                        "Even attending all " + remainingClassDays + " upcoming classes will only reach " + maxPossible + "% (below required " + minReq + "%). Contact your instructor immediately.",
                        s.getId()
                ));
            } else if (currentTotal > 0 && currentPercentage < minReq) {
                status = "WARNING";
                safeBunksAllowed = Math.max(0, remainingClassDays - mustAttend);
                alerts.add(new AlertItem(
                        "WARNING",
                        "Low Attendance: " + s.getName(),
                        "Current attendance is " + currentPercentage + "%. You must attend at least " + mustAttend + " of the " + remainingClassDays + " remaining classes to reach " + minReq + "%.",
                        s.getId()
                ));
            } else {
                status = "SAFE";
                safeBunksAllowed = Math.max(0, remainingClassDays - mustAttend);
                if (safeBunksAllowed <= 1 && currentTotal > 0) {
                    alerts.add(new AlertItem(
                        "INFO",
                        "Bunk Warning: " + s.getName(),
                        "You only have " + safeBunksAllowed + " safe bunk(s) remaining for the entire semester.",
                        s.getId()
                    ));
                }
            }

            projections.add(new SubjectProjection(
                    s.getId(),
                    s.getName(),
                    currentAttended,
                    currentTotal,
                    currentPercentage,
                    minReq,
                    remainingClassDays,
                    maxPossible,
                    safeBunksAllowed,
                    mustAttend,
                    status
            ));
        }

        return new SemesterSummaryResponse(
                endDate,
                remainingClassDays,
                holidays.size(),
                projections,
                alerts
        );
    }
}
