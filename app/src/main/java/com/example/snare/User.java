package com.example.snare;

public class User {

    private String profilePic;
    private String name;
    private String dob;
    private String phoneNumber;

    public User(String profilePic, String name, String dob, String phoneNumber) {
        this.profilePic = profilePic;
        this.name = name;
        this.dob = dob;
        this.phoneNumber = phoneNumber;
    }

    public User() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "User{" +
                ", profilePic='" + profilePic + '\'' +
                ", name='" + name + '\'' +
                ", dob='" + dob + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
