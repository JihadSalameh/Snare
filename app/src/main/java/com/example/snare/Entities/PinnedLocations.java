package com.example.snare.Entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pinnedLocations")
public class PinnedLocations {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "lat")
    private String lat;

    @ColumnInfo(name = "lng")
    private String lng;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "PinnedLocations{" +
                "name='" + name + '\'' +
                ", lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                '}';
    }

}
