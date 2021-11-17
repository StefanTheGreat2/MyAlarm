package org.antem.myalarm.Alarm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;


public class CreateAlarmViewModel extends AndroidViewModel {
    private final AlarmRepository alarmRepository;

    public CreateAlarmViewModel(@NonNull Application application) {
        super(application);

        alarmRepository = new AlarmRepository(application);
    }

    public void setAlarm(Alarm alarm) {
        alarmRepository.insert(alarm);
    }
}
