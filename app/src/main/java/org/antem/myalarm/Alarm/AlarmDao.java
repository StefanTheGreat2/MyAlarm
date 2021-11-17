package org.antem.myalarm.Alarm;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlarmDao {
    @Insert
    void insert(Alarm alarm);

    @Query("DELETE FROM alarm_table")
    void deleteAll();

    @Query("SELECT * FROM alarm_table ORDER BY hour ASC")
    LiveData<List<Alarm>> getAlarms();

   @Delete
    void deleteAlarm(Alarm alarm);

   @Query("SELECT * FROM alarm_table")
   List<Alarm> getAll();


    @Update
    void update(Alarm alarm);


}
