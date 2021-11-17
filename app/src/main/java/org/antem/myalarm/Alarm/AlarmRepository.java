package org.antem.myalarm.Alarm;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

import static org.antem.myalarm.Alarm.RingActivity.TAG;

public class AlarmRepository {
    private final AlarmDao alarmDao;
    private final LiveData<List<Alarm>> alarmsLiveData;



    public AlarmRepository(Application application) {
        AlarmDatabase db = AlarmDatabase.getDatabase(application);
        alarmDao = db.alarmDao();
        alarmsLiveData = alarmDao.getAlarms();


    }

    public void insert(Alarm alarm) {
        AlarmDatabase.databaseWriteExecutor.execute(() -> alarmDao.insert(alarm));
    }

    public void update(Alarm alarm) {
        AlarmDatabase.databaseWriteExecutor.execute(() -> alarmDao.update(alarm));
    }

    public void delete(Alarm alarm) {
        Log.i(TAG, "delete: alarm deleted");
        AlarmDatabase.databaseWriteExecutor.execute(() -> alarmDao.deleteAlarm(alarm));
    }

    public LiveData<List<Alarm>> getAlarmsLiveData() {
        return alarmsLiveData;
    }

    public void deleteAll() {
        AlarmDatabase.databaseWriteExecutor.execute(alarmDao::deleteAll);
    }
}
