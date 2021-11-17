package org.antem.myalarm.Alarm;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.antem.myalarm.R;

import java.util.Calendar;
import java.util.Locale;

import static org.antem.myalarm.Alarm.AlarmReceiver.TAG;
import static org.antem.myalarm.Alarm.AlarmReceiver.TONE;
import static org.antem.myalarm.Alarm.AlarmReceiver.TONE_VOLUME;
import static org.antem.myalarm.Alarm.AlarmReceiver.VIBRATE;
import static org.antem.myalarm.App.CHANNEL_ID;


public class AlarmService extends Service {


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "onStartCommand: " + "service starting");
        boolean vibrate;
        String ringtone;
        int volume;
        if (null != intent) {
            ringtone = intent.getStringExtra(TONE);
            vibrate = intent.getBooleanExtra(VIBRATE, false);
            volume=intent.getIntExtra(TONE_VOLUME,0);
        } else {
            ringtone = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE).toString();
            vibrate = true;
            volume=5;
        }

        Intent activityIntent = new Intent(this, RingActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_RECEIVER_REPLACE_PENDING);
        activityIntent.putExtra(TONE, ringtone);
        activityIntent.putExtra(VIBRATE, vibrate);
        activityIntent.putExtra(TONE_VOLUME,volume);
        PendingIntent pendingActivity = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_ONE_SHOT);
        Notification notification;

        String timeTriggered = getConvertedText();

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Alarm")
                .setContentText("Alarm triggered at: " + timeTriggered)
                .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSilent(true)
                .setFullScreenIntent(pendingActivity, true).build();


        startForeground(100, notification);


        Log.i(TAG, "onStartCommand: id: " + 100);


        return START_STICKY;
    }


    public static String getConvertedText() {
        long millis = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        long hour = c.get(Calendar.HOUR_OF_DAY);
        long min = c.get(Calendar.MINUTE);
        long sec = (millis / 1000) % 60;

        return String
                .format(Locale.getDefault(),
                        "%02d:%02d:%02d", hour,
                        min, sec);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
