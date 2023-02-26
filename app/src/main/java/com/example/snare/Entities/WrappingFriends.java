package com.example.snare.Entities;

import java.io.Serializable;
import java.util.Objects;

public class WrappingFriends implements Serializable {

    private Friends friends ;
    private String id;

    public WrappingFriends(Friends friends, String id) {
        this.friends = friends;
        this.id = id;
    }

    public WrappingFriends(){

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

    @Override
    public boolean equals(Object o) {
        return id.equals(((String)o).toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(friends, id);
    }
}
