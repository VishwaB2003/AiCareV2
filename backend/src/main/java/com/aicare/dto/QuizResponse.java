package com.aicare.dto;

import java.util.List;

public class QuizResponse {
    private List<QuizQuestion> questions;
    private String phase;
    private int dayNumber;

    public QuizResponse() {}

    public List<QuizQuestion> getQuestions() { return questions; }
    public void setQuestions(List<QuizQuestion> questions) { this.questions = questions; }

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }

    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }
}
