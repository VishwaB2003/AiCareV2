package com.aicare.dto;

import com.aicare.model.StudyTopic;
import java.util.List;
import java.util.Map;

public class DashboardResponse {
    private List<StudyTopic> topics;
    private Map<Long, List<Integer>> checkedTasks; // topicId -> [taskIndexes]
    private StatsResponse stats;
    private SummaryResponse pendingSummary; // non-null if new day just started

    public DashboardResponse() {}

    public List<StudyTopic> getTopics() { return topics; }
    public void setTopics(List<StudyTopic> topics) { this.topics = topics; }

    public Map<Long, List<Integer>> getCheckedTasks() { return checkedTasks; }
    public void setCheckedTasks(Map<Long, List<Integer>> checkedTasks) { this.checkedTasks = checkedTasks; }

    public StatsResponse getStats() { return stats; }
    public void setStats(StatsResponse stats) { this.stats = stats; }

    public SummaryResponse getPendingSummary() { return pendingSummary; }
    public void setPendingSummary(SummaryResponse pendingSummary) { this.pendingSummary = pendingSummary; }
}
