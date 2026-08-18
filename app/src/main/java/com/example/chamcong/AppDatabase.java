package com.example.chamcong;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {TagEntity.class, EventEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TagDao tagDao();
    public abstract EventDao eventDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "chamcong.db")
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    db.execSQL("INSERT INTO tags (name, eventType, uid, active) VALUES ('Bắt đầu thời gian làm việc', 'WORK_START', '53:27:B3:B1:02:00:01', 1)");
                                    db.execSQL("INSERT INTO tags (name, eventType, uid, active) VALUES ('Kết thúc thời gian làm việc', 'WORK_END', '53:3D:AC:B1:02:00:01', 1)");
                                    db.execSQL("INSERT INTO tags (name, eventType, uid, active) VALUES ('Bắt đầu thiết kế', 'DESIGN_START', '53:1A:9D:B1:02:00:01', 1)");
                                    db.execSQL("INSERT INTO tags (name, eventType, uid, active) VALUES ('Kết thúc thiết kế', 'DESIGN_END', '53:E6:A3:B1:02:00:01', 1)");
                                    db.execSQL("INSERT INTO tags (name, eventType, uid, active) VALUES ('CNC bắt đầu', 'CNC_START', '53:B6:ED:B3:02:00:01', 1)");
                                    db.execSQL("INSERT INTO tags (name, eventType, uid, active) VALUES ('CNC kết thúc máy', 'CNC_END', '53:83:7B:B1:02:00:01', 1)");
                                    db.execSQL("INSERT INTO tags (name, eventType, uid, active) VALUES ('CNC kết thúc bàn', 'CNC_END', '53:F2:74:B1:02:00:01', 1)");
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
