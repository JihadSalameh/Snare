package com.example.snare.Utills;

public class Friends {

    private String profilePic;
    private String name;

    public Friends(String profilePic, String name) {
        this.profilePic = profilePic;
        this.name = name;
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
}
