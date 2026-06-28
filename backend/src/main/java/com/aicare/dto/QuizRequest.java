package com.aicare.dto;

public class QuizRequest {
    private String topic;
    private int dayNumber;
    private int totalDays;
    private double dailyHours;

    public QuizRequest() {}

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public double getDailyHours() { return dailyHours; }
    public void setDailyHours(double dailyHours) { this.dailyHours = dailyHours; }
}
