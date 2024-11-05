package com.oneleft.app.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;

public class Room {

    private String id;
    private String status;
    private long lastActivityTime;
    private long creationTime;
    private long gameScheduleTime;
    private boolean gameStarted;
    private boolean gameEnded;
    private int totalPlayersJoined = 1;
    private HashMap<String, Player> userList;

    public Room() {

    }

    public Room(String id, String status, long lastActivityTime, long creationTime, long gameScheduleTime, HashMap<String, Player> userList) {
        this.id = id;
        this.status = status;
        this.lastActivityTime = lastActivityTime;
        this.creationTime = creationTime;
        this.gameScheduleTime = gameScheduleTime;
        this.userList = userList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public HashMap<String, Player> getUserList() {
        return userList;
    }

    public void setUserList(HashMap<String, Player> userList) {
        this.userList = userList;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(long lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getGameScheduleTime() {
        return gameScheduleTime;
    }

    public void setGameScheduleTime(long gameScheduleTime) {
        this.gameScheduleTime = gameScheduleTime;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public int getTotalPlayersJoined() {
        return totalPlayersJoined;
    }

    public void setTotalPlayersJoined(int totalPlayersJoined) {
        this.totalPlayersJoined = totalPlayersJoined;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public void setGameEnded(boolean gameEnded) {
        this.gameEnded = gameEnded;
    }

    @Exclude
    public ArrayList<Player> getPlayerList() {
        ArrayList<Player> players = new ArrayList<>();
        for (String key : userList.keySet()) {
            players.add(userList.get(key));
        }
        return players;
    }

}