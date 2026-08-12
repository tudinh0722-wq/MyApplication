package com.example.chamcong;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tags")
public class TagEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String eventType;
    public String uid;
    public boolean active;
}