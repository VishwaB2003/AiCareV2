package com.aicare.dto;

public class LearningRequest {
    private String topic;
    private String task;
    private int dayNumber;   // current day in the plan (1-based)
    private int totalDays;   // total planned days
    private double dailyHours;

    public LearningRequest() {}

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }

    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public double getDailyHours() { return dailyHours; }
    public void setDailyHours(double dailyHours) { this.dailyHours = dailyHours; }
}
