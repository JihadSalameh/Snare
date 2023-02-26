package com.example.snare.Entities;

import java.io.Serializable;

public class Friends {

    private String profilePic;
    private String name;
    private String status;

    public Friends(String profilePic, String name, String status) {
        this.profilePic = profilePic;
        this.name = name;
        this.status = status;
    }

    public Friends() {

    }


    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
