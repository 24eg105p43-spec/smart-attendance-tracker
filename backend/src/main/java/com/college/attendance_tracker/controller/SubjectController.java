package com.college.attendance_tracker.controller;

import com.college.attendance_tracker.model.Subject;
import com.college.attendance_tracker.repository.SubjectRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectRepository repository;

    public SubjectController(SubjectRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Subject addSubject(@RequestBody Subject subject) {
        return repository.save(subject);
    }

    @GetMapping
    public List<Subject> getAllSubjects() {
        return repository.findAll();
    }
}