package org.antem.myalarm.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.antem.myalarm.Timer.TimerData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static org.antem.myalarm.Timer.TimersFragment.MY_TIMERS;


public class AlarmReceiver extends BroadcastReceiver {
    public static final String TAG = "TAG";
    public static final String ALARM_ID = "ALARM_ID";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String MONDAY = "MONDAY";
    public static final String TUESDAY = "TUESDAY";
    public static final String WEDNESDAY = "WEDNESDAY";
    public static final String THURSDAY = "THURSDAY";
    public static final String FRIDAY = "FRIDAY";
    public static final String SATURDAY = "SATURDAY";
    public static final String SUNDAY = "SUNDAY";
    public static final String RECURRING = "RECURRING";
    public static final String TITLE = "TITLE";
    public static final String VIBRATE = "VIBRATE";
    public static final String TONE = "TONE";
    public static final String TONE_VOLUME = "VOLUME";
    public static final String RUNNING = "RUNNING";
    public static final String TIMER_ID = "TIMER_ID";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null) {
            if (Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())) {


                Context encryptedContext = context.createDeviceProtectedStorageContext();
                encryptedContext.startForegroundService(new Intent(encryptedContext, RescheduleAlarmService.class));

            }
            if ("snoozeWakeUp".equals(intent.getAction())) {
                cancelNotification(context);
                context.stopService(intent);
                Intent snoozeWakeUpI = new Intent(context, AlarmService.class);
                Log.i(TAG, "onReceive: " + intent.getStringExtra(TONE));
                snoozeWakeUpI.putExtra(TONE, intent.getStringExtra(TONE));
                snoozeWakeUpI.putExtra(TONE_VOLUME, intent.getIntExtra(TONE_VOLUME, 0));
                context.startService(snoozeWakeUpI);
            }
            if ("alarmWakeUp".equals(intent.getAction())) {
                Log.i(TAG, "onReceive: " + intent.getIntExtra(ALARM_ID, 0));
                if (intent.getBooleanExtra(RECURRING, false)) {
                    Alarm nextAlarm = recurringAlarm(intent);
                    Log.i(TAG, "alarm: " + "recurring alarm Created " + nextAlarm.getRecurringDaysText());
                    nextAlarm.createAlarm(context);
                } else {
                    updateAlarm(context, intent);
                }
                if (alarmIsToday(intent)) {
                    cancelNotification(context);
                    Intent alarmWakeUpi = new Intent(context, AlarmService.class);
                    String toastText = "Alarm Received";
                    Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
                    String path = intent.getStringExtra(TONE);
                    Log.i(TAG, "alarmWakeUp: " + path);
                    alarmWakeUpi.putExtra(TONE, path);
                    alarmWakeUpi.putExtra(TITLE, intent.getStringExtra(TITLE));
                    alarmWakeUpi.putExtra(TONE_VOLUME, intent.getIntExtra(TONE_VOLUME, 0));
                    context.stopService(alarmWakeUpi);
                    context.startService(alarmWakeUpi);

                }
            }
            if (intent.getAction().equals("timer")) {
                Intent timer = new Intent(context, AlarmService.class);
                String toastText = "Timer Received";
                Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
                String path = intent.getStringExtra(TONE);
                Log.i(TAG, "alarmWakeUp: timer " + intent.getBooleanExtra(RUNNING, false));
                timer.putExtra(TONE, path);
                Log.i(TAG, "onReceive: "+path);
                timer.putExtra(TITLE, intent.getStringExtra(TITLE));
                timer.putExtra(TONE_VOLUME, intent.getIntExtra(TONE_VOLUME, 0));
                boolean running = intent.getBooleanExtra(RUNNING, false);
                if (running) {
                    context.stopService(timer);
                    context.startService(timer);
                }
                SharedPreferences sharedPreferences = context.getSharedPreferences(MY_TIMERS, MODE_PRIVATE);


                Gson gson = new Gson();
                String json = sharedPreferences.getString(MY_TIMERS, null);
                ArrayList<TimerData> tempData;

                Type type = new TypeToken<ArrayList<TimerData>>() {
                }.getType();
                tempData = gson.fromJson(json, type);
                for (TimerData timerData : tempData) {
                    if (timerData.getAlarmId() == intent.getIntExtra(TIMER_ID, 0)) {

                        timerData.setRunningStatus(false);
                        timerData.setLastTimeStatus(60000);
                        timerData.setSeconds(60);
                    }
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                json = gson.toJson(tempData);
                editor.putString(MY_TIMERS, json);
                editor.apply();

            }


        }

    }

    private void updateAlarm(Context context, Intent intent) {
        AlarmDatabase db = AlarmDatabase.getDatabase(context.getApplicationContext());
        AlarmDao alarmDao = db.alarmDao();
        int id = intent.getIntExtra(ALARM_ID, 0);
        new Thread(() -> {
            List<Alarm> alarms = alarmDao.getAll();
            for (Alarm alarm : alarms) {
                if (alarm.getAlarmId() == id) {
                    alarm.cancelAlarm(context);
                    alarmDao.update(alarm);
                }
            }
        }).start();
    }


    private Alarm recurringAlarm(Intent intent) {
        return new Alarm((int) (Math.random() * Integer.MAX_VALUE), intent.getIntExtra(HOUR, 0), intent.getIntExtra(MINUTE, 0),
                intent.getStringExtra(TITLE), System.currentTimeMillis(), true, true, intent.getBooleanExtra(SUNDAY, false),
                intent.getBooleanExtra(MONDAY, false), intent.getBooleanExtra(TUESDAY, false),
                intent.getBooleanExtra(WEDNESDAY, false), intent.getBooleanExtra(THURSDAY, false),
                intent.getBooleanExtra(FRIDAY, false), intent.getBooleanExtra(SATURDAY, false),
                intent.getStringExtra(TONE), intent.getIntExtra(TONE_VOLUME, 0), intent.getBooleanExtra(VIBRATE, false));
    }


    private boolean alarmIsToday(Intent intent) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int today = calendar.get(Calendar.DAY_OF_WEEK);

        switch (today) {
            case Calendar.SUNDAY:
                return intent.getBooleanExtra(SUNDAY, false);
            case Calendar.MONDAY:
                return intent.getBooleanExtra(MONDAY, false);
            case Calendar.TUESDAY:
                return intent.getBooleanExtra(TUESDAY, false);
            case Calendar.WEDNESDAY:
                return intent.getBooleanExtra(WEDNESDAY, false);
            case Calendar.THURSDAY:
                return intent.getBooleanExtra(THURSDAY, false);
            case Calendar.FRIDAY:
                return intent.getBooleanExtra(FRIDAY, false);
            case Calendar.SATURDAY:
                return intent.getBooleanExtra(SATURDAY, false);
        }
        return false;
    }

    private void cancelNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(100);

    }

}

