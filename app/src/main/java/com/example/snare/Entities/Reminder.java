package com.example.snare.Entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.snare.Activities.ConverterType;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "reminders")
public class Reminder implements Serializable {

    @NonNull
    @PrimaryKey
    private String idFirebase;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "date_time")
    private String dateTime;

    @ColumnInfo(name = "reminder_text")
    private String reminderText;

    @ColumnInfo(name = "image_path")
    private String imagePath;

    @ColumnInfo(name = "color")
    private String color;

    @ColumnInfo(name = "year")
    private int year;

    @ColumnInfo(name = "month")
    private int month;

    @ColumnInfo(name = "day")
    private int day;

    @ColumnInfo(name = "hour")
    private int hour;

    @ColumnInfo(name = "minute")
    private int minute;

    @ColumnInfo(name = "location")
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @NonNull
    public String getIdFirebase() {
        return idFirebase;
    }

    public void setIdFirebase(@NonNull String idFirebase) {
        this.idFirebase = idFirebase;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getReminderText() {
        return reminderText;
    }

    public void setReminderText(String reminderText) {
        this.reminderText = reminderText;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    @TypeConverters(ConverterType.class)
    private List<String> group ;

    public List<String> getGroup() {
        return group;
    }

    public void setGroup(List<String> group) {
        this.group = group;
    }

    @NotNull
    @Override
    public String toString() {
        return title + " : " + dateTime + " -> " + location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return year == reminder.year && month == reminder.month && day == reminder.day && hour == reminder.hour && minute == reminder.minute&& idFirebase.equals(reminder.idFirebase) && Objects.equals(title, reminder.title) && Objects.equals(dateTime, reminder.dateTime) && Objects.equals(reminderText, reminder.reminderText) && Objects.equals(imagePath, reminder.imagePath) && Objects.equals(color, reminder.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFirebase, title, dateTime, reminderText, imagePath, color, year, month, day, hour, minute);
    }
}
