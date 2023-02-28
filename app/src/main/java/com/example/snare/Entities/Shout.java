package com.example.snare.Entities;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public class Shout {

    private String text;
    private String name;
    private LatLng loc;

    public Shout(String text, String name, LatLng loc) {
        this.text = text;
        this.name = name;
        this.loc = loc;
    }

    public Shout() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLoc() {
        return loc;
    }

    public void setLoc(LatLng loc) {
        this.loc = loc;
    }

    @NonNull
    @Override
    public String toString() {
        return "Shout{" +
                "text='" + text + '\'' +
                ", name='" + name + '\'' +
                ", loc='" + loc + '\'' +
                '}';
    }

}
