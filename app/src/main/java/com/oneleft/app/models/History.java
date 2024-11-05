package com.oneleft.app.models;

public class History {

    private String id;
    private String name;
    private String game;
    private long dateTime;
    private double rewardEarned;

    public History() {

    }

    public History(String id, String name, String game, long dateTime, double rewardEarned) {
        this.id = id;
        this.game = game;
        this.dateTime = dateTime;
        this.rewardEarned = rewardEarned;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public double getRewardEarned() {
        return rewardEarned;
    }

    public void setRewardEarned(double rewardEarned) {
        this.rewardEarned = rewardEarned;
    }
}
