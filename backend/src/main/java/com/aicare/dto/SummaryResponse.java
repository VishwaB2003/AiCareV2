package com.aicare.dto;

import java.time.LocalDate;
import java.util.List;

public class SummaryResponse {
    private LocalDate date;
    private int completedTasks;
    private int totalTasks;
    private double completionRate;
    private int dailyScore;
    private int streakChange;   // +1 or 0 (reset)
    private int currentStreak;
    private List<TopicSummary> breakdown;

    public SummaryResponse() {}

    public static class TopicSummary {
        private String topicName;
        private int completed;
        private int total;

        public TopicSummary(String topicName, int completed, int total) {
            this.topicName = topicName;
            this.completed = completed;
            this.total = total;
        }

        public String getTopicName() { return topicName; }
        public int getCompleted() { return completed; }
        public int getTotal() { return total; }
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

    public int getDailyScore() { return dailyScore; }
    public void setDailyScore(int dailyScore) { this.dailyScore = dailyScore; }

    public int getStreakChange() { return streakChange; }
    public void setStreakChange(int streakChange) { this.streakChange = streakChange; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public List<TopicSummary> getBreakdown() { return breakdown; }
    public void setBreakdown(List<TopicSummary> breakdown) { this.breakdown = breakdown; }
}
