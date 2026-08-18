package com.example.chamcong;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface EventDao {
    @Insert
    void insert(EventEntity event);

    @Update
    void update(EventEntity event);

    @Delete
    void delete(EventEntity event);

    @Query("SELECT * FROM events ORDER BY timestamp DESC")
    List<EventEntity> getAll();

    @Query("SELECT * FROM events WHERE eventType LIKE :baseTypePrefix || '%' ORDER BY timestamp DESC LIMIT 1")
    EventEntity getLastEventByType(String baseTypePrefix);
}
