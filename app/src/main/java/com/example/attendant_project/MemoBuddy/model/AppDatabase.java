package com.example.attendant_project.MemoBuddy.model;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {EventItem.class}, version = 4,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract EventDao eventDao();
    public static int coreCount = Runtime.getRuntime().availableProcessors();
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(coreCount);
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
//                    INSTANCE = Room .databaseBuilder(context.getApplicationContext(),AppDatabase.class, "app_database")
//                                    .addMigrations(AppDatabase.MIGRATION)
//                                    .build();//添加型更新

                    INSTANCE = Room .databaseBuilder(context, AppDatabase.class, "app_database")
                                    .fallbackToDestructiveMigration()
                                    .build();//破壞型更新
                }
            }
        }
        return INSTANCE;
    }


    static final Migration MIGRATION = new Migration(3,4) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {//添加行更新的更新步驟
            // 1. 建新表
            db.execSQL("CREATE TABLE new_event (id INTEGER PRIMARY KEY NOT NULL)");

            // 2. 複製資料
//            db.execSQL("INSERT INTO new_event (id, name, time) SELECT id, name, time FROM event");

            // 2.5 插入新欄位
            db.execSQL("ALTER TABLE event_table ADD COLUMN category TEXT");

            // 3. 刪除舊表
            db.execSQL("DROP TABLE event");

            // 4. 改名
            db.execSQL("ALTER TABLE new_event RENAME TO event");
        }
    };

}
