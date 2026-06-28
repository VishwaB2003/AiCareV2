package com.aicare.dto;

public class DailyTasksRequest {
    private String topic;
    private int dayNumber;
    private int totalDays;
    private double dailyHours;
    private int numTasks;  // how many tasks to generate

    public DailyTasksRequest() {}

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public double getDailyHours() { return dailyHours; }
    public void setDailyHours(double dailyHours) { this.dailyHours = dailyHours; }

    public int getNumTasks() { return numTasks; }
    public void setNumTasks(int numTasks) { this.numTasks = numTasks; }
}
