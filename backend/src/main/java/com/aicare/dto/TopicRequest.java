package com.aicare.dto;

import jakarta.validation.constraints.NotBlank;

public class TopicRequest {

    @NotBlank(message = "Topic name is required")
    private String name;
    private String description;
    private int totalDays;
    private double dailyHours;
    private String color;
    private String subTasks; // JSON array string

    public TopicRequest() {}

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
}
