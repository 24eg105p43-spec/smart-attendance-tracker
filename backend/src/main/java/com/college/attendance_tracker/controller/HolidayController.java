package com.college.attendance_tracker.controller;

import com.college.attendance_tracker.dto.HolidayRequest;
import com.college.attendance_tracker.model.Holiday;
import com.college.attendance_tracker.model.User;
import com.college.attendance_tracker.repository.HolidayRepository;
import com.college.attendance_tracker.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    private final HolidayRepository holidayRepository;
    private final UserRepository userRepository;

    public HolidayController(HolidayRepository holidayRepository, UserRepository userRepository) {
        this.holidayRepository = holidayRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Holiday> getHolidays(Authentication auth) {
        return holidayRepository.findAllForUser(auth.getName());
    }

    @PostMapping
    public Holiday addHoliday(@Valid @RequestBody HolidayRequest request, Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Holiday holiday = new Holiday(request.name().trim(), request.date(), user);
        return holidayRepository.save(holiday);
    }

    @DeleteMapping("/{id}")
    public void deleteHoliday(@PathVariable Long id, Authentication auth) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Holiday not found"));

        if (holiday.getUser() != null && !holiday.getUser().getEmail().equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to delete this holiday");
        }

        holidayRepository.delete(holiday);
    }
}
