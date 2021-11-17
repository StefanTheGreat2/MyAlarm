package org.antem.myalarm.Alarm;


import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

public class RescheduleAlarmService extends JobIntentService {
    AlarmRepository alarmRepository;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        alarmRepository = new AlarmRepository(getApplication());
        alarmRepository.getAlarmsLiveData().observeForever(alarms -> {
            for (Alarm a : alarms) {
                if (a.isStarted()) {
                    a.createAlarm(getApplicationContext());
                }
            }
        });
        Log.i("alarm service", "onStartCommand: alarms restored");
        return START_STICKY;
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return super.onBind(intent);
    }

}
