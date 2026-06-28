package com.aicare.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_stats")
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "total_score")
    private int totalScore;

    @Column(name = "current_streak")
    private int currentStreak;

    @Column(name = "best_streak")
    private int bestStreak;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Column(length = 50)
    private String level;

    public UserStats() {}

    public UserStats(Long userId) {
        this.userId = userId;
        this.totalScore = 0;
        this.currentStreak = 0;
        this.bestStreak = 0;
        this.level = "Seedling";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getBestStreak() { return bestStreak; }
    public void setBestStreak(int bestStreak) { this.bestStreak = bestStreak; }

    public LocalDate getLastActiveDate() { return lastActiveDate; }
    public void setLastActiveDate(LocalDate lastActiveDate) { this.lastActiveDate = lastActiveDate; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
