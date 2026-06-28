package com.aicare.dto;

public class CheckRequest {
    private Long topicId;
    private int taskIndex;
    private boolean checked;

    public CheckRequest() {}

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }

    public int getTaskIndex() { return taskIndex; }
    public void setTaskIndex(int taskIndex) { this.taskIndex = taskIndex; }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}
