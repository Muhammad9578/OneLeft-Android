package com.oneleft.app.adapters;

public class ResultItem {

    private String playerId;
    private String name;
    private int numberOfAnswers;
    private int numberOfCorrectAnswers;

    public ResultItem() {
    }

    public ResultItem(String playerId, String name, int numberOfAnswers, int numberOfCorrectAnswers) {
        this.playerId = playerId;
        this.name = name;
        this.numberOfAnswers = numberOfAnswers;
        this.numberOfCorrectAnswers = numberOfCorrectAnswers;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfAnswers() {
        return numberOfAnswers;
    }

    public void setNumberOfAnswers(int numberOfQuestionsAttempted) {
        this.numberOfAnswers = numberOfQuestionsAttempted;
    }

    public int getNumberOfCorrectAnswers() {
        return numberOfCorrectAnswers;
    }

    public void setNumberOfCorrectAnswers(int numberOfCorrectQuestions) {
        this.numberOfCorrectAnswers = numberOfCorrectQuestions;
    }
}
