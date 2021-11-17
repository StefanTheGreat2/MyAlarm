package org.antem.myalarm.Alarm;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Alarm.class}, version = 2, exportSchema = false)

abstract class AlarmDatabase extends RoomDatabase {
    public abstract AlarmDao alarmDao();

    private static volatile AlarmDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);


    static AlarmDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AlarmDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.createDeviceProtectedStorageContext(),
                            AlarmDatabase.class,
                            "alarm_database"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
