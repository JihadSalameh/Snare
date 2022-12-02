package com.example.snare;

public class User {

    private String Id;
    private String profilePic;
    private String name;
    private String dob;

    public User(String Id, String profilePic, String name, String dob) {
        this.profilePic = profilePic;
        this.name = name;
        this.Id = Id;
        this.dob = dob;
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

    @Override
    public String toString() {
        return "User{" +
                "Id='" + getId() + '\'' +
                ", profilePic='" + getProfilePic() + '\'' +
                ", name='" + getName() + '\'' +
                ", dob='" + getDob() + '\'' +
                '}';
    }
}
