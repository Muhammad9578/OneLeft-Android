package com.oneleft.app.models;

public class Card {

    private String id;
    private String number;
    private String name;
    private String token;
    private String type;

    public Card() {
    }

    public Card(String id, String number, String name, String token, String type) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.token = token;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}