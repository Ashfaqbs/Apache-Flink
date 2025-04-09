package com.example.demos;
public class UserEvent {
    private String userId;
    private String action; // "CLICK", "ADD_TO_CART", etc.

    // Getters, setters, constructors


    public UserEvent() {
    }

    public UserEvent(String userId, String action) {
        this.userId = userId;
        this.action = action;
    }

    public String getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "userId='" + userId + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}
