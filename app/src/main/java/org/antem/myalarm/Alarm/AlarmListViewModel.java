package org.antem.myalarm.Alarm;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import static org.antem.myalarm.Alarm.RingActivity.TAG;


public class AlarmListViewModel extends AndroidViewModel {

    private AlarmRepository alarmRepository;
    private LiveData<List<Alarm>> alarmsLiveData;


    public AlarmListViewModel(@NonNull Application application) {
        super(application);

        alarmRepository = new AlarmRepository(application);
        alarmsLiveData = alarmRepository.getAlarmsLiveData();
    }

    public void update(Alarm alarm) {
        alarmRepository.update(alarm);
    }

    public void delete(Alarm alarm) {
        alarmRepository.delete(alarm);
        Log.i(TAG, "deleteAlarm: " );
    }

    public void deleteAll(){
        alarmRepository.deleteAll();
    }

    public LiveData<List<Alarm>> getAlarmsLiveData() {
        return alarmsLiveData;
    }


}
