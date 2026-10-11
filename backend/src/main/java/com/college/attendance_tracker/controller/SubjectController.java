package com.college.attendance_tracker.controller;

import com.college.attendance_tracker.model.Subject;
import com.college.attendance_tracker.model.User;
import com.college.attendance_tracker.repository.SubjectRepository;
import com.college.attendance_tracker.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectRepository repository;
    private final UserRepository userRepository;

    public SubjectController(SubjectRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public Subject addSubject(@RequestBody Subject subject, Authentication auth) {
        User owner = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        subject.setId(null);
        subject.setOwner(owner);
        return repository.save(subject);
    }

    @GetMapping
    public List<Subject> getMySubjects(Authentication auth) {
        return repository.findByOwnerEmail(auth.getName());
    }
}