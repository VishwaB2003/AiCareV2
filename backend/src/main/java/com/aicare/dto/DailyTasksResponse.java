package com.aicare.dto;

import java.util.List;

public class DailyTasksResponse {
    private List<String> tasks;
    private int dayNumber;
    private String phase;
    private String dailyFocus; // one-line summary of today's theme

    public DailyTasksResponse() {}

    public List<String> getTasks() { return tasks; }
    public void setTasks(List<String> tasks) { this.tasks = tasks; }

    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }

    public String getDailyFocus() { return dailyFocus; }
    public void setDailyFocus(String dailyFocus) { this.dailyFocus = dailyFocus; }
}
