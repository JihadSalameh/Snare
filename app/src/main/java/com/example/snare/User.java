package com.example.snare;

public class User {

    private String Id;
    private String profilePic;
    private String name;
    private String dob;
    private String phoneNumber;

    public User(String Id, String profilePic, String name, String dob, String phoneNumber) {
        this.profilePic = profilePic;
        this.name = name;
        this.Id = Id;
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

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
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
                "Id='" + Id + '\'' +
                ", profilePic='" + profilePic + '\'' +
                ", name='" + name + '\'' +
                ", dob='" + dob + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
