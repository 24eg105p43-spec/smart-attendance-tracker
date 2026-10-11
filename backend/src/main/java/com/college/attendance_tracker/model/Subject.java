package com.college.attendance_tracker.model;

import jakarta.persistence.*;

@Entity
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int minPercentage = 75;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMinPercentage() { return minPercentage; }
    public void setMinPercentage(int minPercentage) { this.minPercentage = minPercentage; }
}