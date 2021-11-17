package org.antem.myalarm.Alarm;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import org.antem.myalarm.R;

import java.util.Calendar;

import static org.antem.myalarm.Alarm.AlarmReceiver.TITLE;
import static org.antem.myalarm.Alarm.AlarmReceiver.TONE;
import static org.antem.myalarm.Alarm.AlarmReceiver.TONE_VOLUME;
import static org.antem.myalarm.Alarm.AlarmReceiver.VIBRATE;


public class RingActivity extends AppCompatActivity {
    public static final String TAG = "tag";
    private ImageView clock;
    private Ringtone ringtone;
    private Vibrator vibrator;
    private boolean vibrate;
    private float fVolume;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ring_activity);

        setShowWhenLocked(true);
        setTurnScreenOn(true);

        Intent intent = getIntent();
        vibrate = intent.getBooleanExtra(VIBRATE, false);
        int volume = intent.getIntExtra(TONE_VOLUME, 5);
        fVolume = volume * 0.1f;

        startRingtone(intent.getStringExtra(TONE));

        Button dismiss = findViewById(R.id.ringDismiss);
        Button snooze = findViewById(R.id.ringSnooze);
        clock = findViewById(R.id.imageView);


        dismiss.setOnClickListener(v -> {
            stopService();
            cancelNotification(this);
            finish();
        });
        snooze.setOnClickListener(v -> {
            snooze(this, intent);
            stopService();
            finish();
        });
        animateClock();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            this.setTurnScreenOn(true);
            this.setShowWhenLocked(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null)
                keyguardManager.requestDismissKeyguard(this, null);

        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void stopService() {
        Intent intentService = new Intent(getApplicationContext(), AlarmService.class);
        getApplicationContext().stopService(intentService);
    }

    private void animateClock() {
        ObjectAnimator rotateAnimation = ObjectAnimator.ofFloat(clock, "rotation", 0f, 20f, 0f, -20f, 0f);
        rotateAnimation.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimation.setDuration(800);
        rotateAnimation.start();
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    void startRingtone(String data) {
        Uri soundUri;
        long[] pattern = {0, 100, 1000};
        if (null != data) {
            soundUri = Uri.parse(data);
        } else {
            soundUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
        }


        ringtone = RingtoneManager.getRingtone(getApplicationContext(), soundUri);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        ringtone.setVolume(fVolume);


        ringtone.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());


        if (ringtone.isPlaying()) {
            ringtone.stop();
            vibrator.cancel();
        }

        ringtone.setLooping(true);
        ringtone.play();
        if (vibrate) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));

        }


    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ringtone.stop();
        vibrator.cancel();
        stopService();
    }

    public void snooze(Context context, Intent intent) {
        stopService();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 20);

        Intent snoozeIntent = new Intent(context, AlarmReceiver.class);
        snoozeIntent.setAction("snoozeWakeUp");
        snoozeIntent.putExtra(TONE, intent.getStringExtra(TONE));
        snoozeIntent.putExtra(TITLE, "SNOOZE");

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        Toast.makeText(context, "Snooze Activated", Toast.LENGTH_LONG).show();
    }

    private void cancelNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(100);
    }
}