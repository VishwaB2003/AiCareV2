package com.aicare.dto;

import java.util.List;

public class LearningResponse {
    private String topic;
    private String task;
    private String explanation;
    private List<String> keyConcepts;
    private List<String> studySteps;
    private List<String> exercises;
    private List<String> youtubeSearchTerms;
    private boolean aiGenerated;
    private int dayNumber;
    private int totalDays;
    private String phase;         // Foundation / Intermediate / Advanced / Pro
    private int phasePercent;     // % through current phase
    private int totalPercent;     // % through entire topic
    private String nextPreview;   // brief look at tomorrow

    public LearningResponse() {}

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<String> getKeyConcepts() { return keyConcepts; }
    public void setKeyConcepts(List<String> keyConcepts) { this.keyConcepts = keyConcepts; }

    public List<String> getStudySteps() { return studySteps; }
    public void setStudySteps(List<String> studySteps) { this.studySteps = studySteps; }

    public List<String> getExercises() { return exercises; }
    public void setExercises(List<String> exercises) { this.exercises = exercises; }

    public List<String> getYoutubeSearchTerms() { return youtubeSearchTerms; }
    public void setYoutubeSearchTerms(List<String> youtubeSearchTerms) { this.youtubeSearchTerms = youtubeSearchTerms; }

    public boolean isAiGenerated() { return aiGenerated; }
    public void setAiGenerated(boolean aiGenerated) { this.aiGenerated = aiGenerated; }

    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }

    public int getPhasePercent() { return phasePercent; }
    public void setPhasePercent(int phasePercent) { this.phasePercent = phasePercent; }

    public int getTotalPercent() { return totalPercent; }
    public void setTotalPercent(int totalPercent) { this.totalPercent = totalPercent; }

    public String getNextPreview() { return nextPreview; }
    public void setNextPreview(String nextPreview) { this.nextPreview = nextPreview; }
}
