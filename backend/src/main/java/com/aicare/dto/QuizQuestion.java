package com.aicare.dto;

import java.util.List;

public class QuizQuestion {
    private String question;
    private List<String> options;   // exactly 4 options
    private int correctIndex;       // 0-3
    private String explanation;

    public QuizQuestion() {}

    public QuizQuestion(String question, List<String> options, int correctIndex, String explanation) {
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
        this.explanation = explanation;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
