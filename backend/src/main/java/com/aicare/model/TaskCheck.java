package com.aicare.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "task_checks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "topic_id", "task_index", "check_date"})
})
public class TaskCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = "task_index", nullable = false)
    private int taskIndex;

    @Column(name = "check_date", nullable = false)
    private LocalDate checkDate;

    public TaskCheck() {}

    public TaskCheck(Long userId, Long topicId, int taskIndex, LocalDate checkDate) {
        this.userId = userId;
        this.topicId = topicId;
        this.taskIndex = taskIndex;
        this.checkDate = checkDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }

    public int getTaskIndex() { return taskIndex; }
    public void setTaskIndex(int taskIndex) { this.taskIndex = taskIndex; }

    public LocalDate getCheckDate() { return checkDate; }
    public void setCheckDate(LocalDate checkDate) { this.checkDate = checkDate; }
}
