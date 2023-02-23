package com.example.snare.Entities;

public class WrappingFriends {

    private Friends friends ;
    private String id;

    public WrappingFriends(Friends friends, String id) {
        this.friends = friends;
        this.id = id;
    }

    public Friends getFriends() {
        return friends;
    }

    public void setFriends(Friends friends) {
        this.friends = friends;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
