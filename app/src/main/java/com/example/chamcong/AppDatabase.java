package com.example.chamcong;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.migration.Migration;

@Database(entities = {TagEntity.class, EventEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "chamcong.db"
                    )
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            db.execSQL("INSERT INTO tags (name, eventType, uid, active) VALUES ('Cong vao', 'WORK_START', '53:27:B3:B1:02:00:01', 1)");
                            db.execSQL("INSERT INTO tags (name, eventType, uid, active) VALUES ('Cong ra', 'WORK_END', '53:3D:AC:B1:02:00:01', 1)");
                            db.execSQL("INSERT INTO tags (name, eventType, uid, active) VALUES ('Bat dau thiet ke', 'DESIGN_START', '53:1A:9D:B1:02:00:01', 1)");
                            db.execSQL("INSERT INTO tags (name, eventType, uid, active) VALUES ('Ket thuc thiet ke', 'DESIGN_END', '53:E6:A3:B1:02:00:01', 1)");
                            db.execSQL("INSERT INTO tags (name, eventType, uid, active) VALUES ('CNC bat dau', 'CNC_START', '53:B6:ED:B3:02:00:01', 1)");
                            db.execSQL("INSERT INTO tags (name, eventType, uid, active) VALUES ('CNC ket thuc may', 'CNC_END', '53:83:7B:B1:02:00:01', 1)");
                            db.execSQL("INSERT INTO tags (name, eventType, uid, active) VALUES ('CNC ket thuc ban', 'CNC_END', '53:F2:74:B1:02:00:01', 1)");
                        }
                    })
                    .build();
        }
        return instance;
    }

    public abstract TagDao tagDao();
    public abstract EventDao eventDao();
}