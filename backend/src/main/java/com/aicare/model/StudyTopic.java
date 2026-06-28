package com.aicare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_topics")
public class StudyTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "total_days")
    private int totalDays;

    @Column(name = "daily_hours")
    private double dailyHours;

    @Column(length = 20)
    private String color;

    @Column(name = "sub_tasks", columnDefinition = "TEXT")
    private String subTasks; // JSON array e.g. ["Task 1","Task 2"]

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public StudyTopic() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public double getDailyHours() { return dailyHours; }
    public void setDailyHours(double dailyHours) { this.dailyHours = dailyHours; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSubTasks() { return subTasks; }
    public void setSubTasks(String subTasks) { this.subTasks = subTasks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
