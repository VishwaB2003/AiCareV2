package com.aicare.dto;

public class StatsResponse {
    private int totalScore;
    private int currentStreak;
    private int bestStreak;
    private String level;
    private String levelEmoji;
    private int pointsToNextLevel;

    public StatsResponse() {}

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getBestStreak() { return bestStreak; }
    public void setBestStreak(int bestStreak) { this.bestStreak = bestStreak; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getLevelEmoji() { return levelEmoji; }
    public void setLevelEmoji(String levelEmoji) { this.levelEmoji = levelEmoji; }

    public int getPointsToNextLevel() { return pointsToNextLevel; }
    public void setPointsToNextLevel(int pointsToNextLevel) { this.pointsToNextLevel = pointsToNextLevel; }
}
