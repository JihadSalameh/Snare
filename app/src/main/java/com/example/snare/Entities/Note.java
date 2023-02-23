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

@Entity(tableName = "notes")
public class Note implements Serializable {

    @NonNull
    private String idFirebase;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "date_time")
    private String dateTime;

    @ColumnInfo(name = "note_text")
    private String noteText;

    @ColumnInfo(name = "image_path")
    private String imagePath;

    @ColumnInfo(name = "color")
    private String color;

    @PrimaryKey(autoGenerate = true)
    private int count;

    @TypeConverters(ConverterType.class)
    private List<String> group ;

    public List<String> getGroup() {
        return group;
    }

    public void setGroup(List<String> group) {
        this.group = group;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
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

    @NotNull
    @Override
    public String toString() {
        return title + " : " + dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return idFirebase.equals(note.idFirebase) && Objects.equals(title, note.title) && Objects.equals(dateTime, note.dateTime) && Objects.equals(noteText, note.noteText) && Objects.equals(imagePath, note.imagePath) && Objects.equals(color, note.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFirebase, title, dateTime, noteText, imagePath, color);
    }

}
