package com.example.chamcong;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "events",
    foreignKeys = @ForeignKey(
        entity = TagEntity.class,
        parentColumns = "id",
        childColumns = "tagId",
        onDelete = ForeignKey.RESTRICT
    )
)
public class EventEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int tagId;
    public String eventType;
    public long timestamp;
}
