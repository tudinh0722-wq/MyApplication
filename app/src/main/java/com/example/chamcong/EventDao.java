package com.example.chamcong;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EventDao {
    @Insert
    void insert(EventEntity event);

    @Query("SELECT * FROM events ORDER BY timestamp DESC")
    List<EventEntity> getAll();
}