package com.oneleft.app.models;

public class Answer {

    private String questionId;
    private String selectedOption;
    private String correctOption;

    public Answer() {
    }

    public Answer(String questionId, String selectedOption, String correctOption) {
        this.questionId = questionId;
        this.selectedOption = selectedOption;
        this.correctOption = correctOption;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }
}
