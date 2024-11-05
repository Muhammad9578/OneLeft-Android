package com.oneleft.app.models;

public class Player {

    private String id;
    private String name;
    private String fcmToken;

    public Player() {
    }

    public Player(String id, String name, String fcmToken) {
        this.id = id;
        this.name = name;
        this.fcmToken = fcmToken;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
