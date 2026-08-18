package com.example.chamcong;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TagDao {
    @Query("SELECT * FROM tags WHERE uid = :uid AND active = 1 LIMIT 1")
    TagEntity findByUid(String uid);

    @Query("SELECT * FROM tags ORDER BY id ASC")
    List<TagEntity> getAll();

    @Update
    void update(TagEntity tag);
}
