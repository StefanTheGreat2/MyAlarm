package org.antem.myalarm.Timer;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import org.antem.myalarm.Alarm.AlarmReceiver;
import org.antem.myalarm.Alarm.AlarmService;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.antem.myalarm.Alarm.AlarmReceiver.RUNNING;
import static org.antem.myalarm.Alarm.AlarmReceiver.TIMER_ID;
import static org.antem.myalarm.Alarm.AlarmReceiver.TONE;
import static org.antem.myalarm.Alarm.AlarmReceiver.TONE_VOLUME;
import static org.antem.myalarm.Timer.TimersFragment.TAG;

interface UIUpdate {
    void progressUpdate(long progress);

    void dataSetUpdate();
}

public class TimerRunnable implements Parcelable {

    private AtomicBoolean running;
    private final TimerData mTimerData;
    private Handler mHandler;
    CountDownTimer mCountDownTimer;
    UIUpdate mUIUpdate;
    Context mContext;
    private boolean started;


    public TimerRunnable(TimerData timerData, Context context) {
        this.running = new AtomicBoolean(false);
        mTimerData = timerData;
        this.mContext = context;
    }

    protected TimerRunnable(Parcel in) {
        setRunning(in.readInt() == 1);
        long timeInMillis = in.readLong();
        int hour = (int) ((timeInMillis / 3600000) % 24);
        int min = (int) (timeInMillis / 60000) % 60;
        int sec = (int) (timeInMillis / 1000) % 60;
        this.mTimerData = new TimerData(hour, min, sec, in.readInt()); }


    public AtomicBoolean getRunning() {
        return running;
    }


    public void setRunning(boolean running) {
        this.running.set(running);
    }

    public TimerData getTimerData() {
        return mTimerData;
    }


    public void startTimer(long millisToRun, Context context) {

        if (!running.get()) {


            createTimerAlarm(context, millisToRun, getTimerData().getAlarmId());
            running.set(true);
            getTimerData().setRunningStatus(true);
            Log.i(TAG, "startTimer: " + millisToRun);

            createTimerAlarm(mContext, millisToRun, getTimerData().getAlarmId());
            mCountDownTimer = new CountDownTimer(millisToRun, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {

                    mHandler.post(() -> {
                        getTimerData().setLastTimeStatus(millisUntilFinished);
                        mUIUpdate.progressUpdate(millisUntilFinished);
                    });


                }

                @Override
                public void onFinish() {
                    running.set(false);
                    cancelTimerAlarm(context, getTimerData().getAlarmId());
                    Intent intent = new Intent(context, AlarmService.class);
                    intent.putExtra(TONE_VOLUME, 5);
                    intent.putExtra(RUNNING, running.get());
                    intent.putExtra(TONE,getTimerData().timerMelody);
                    new Thread(() -> {
                        context.stopService(intent);
                        context.startService(intent);

                    }).start();
                    mUIUpdate.progressUpdate(60);
                    resetTimer();
                    mUIUpdate.dataSetUpdate();

                }
            }.start();
        }


    }


    public String getConvertedText(Long millis) {
        long hour = (millis / 3600000) % 25;
        long min = (millis / 60000) % 60;
        long sec = (millis / 1000) % 60;

        return String
                .format(Locale.getDefault(),
                        "%02d:%02d:%02d", hour,
                        min, sec);
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }



    public void setUIUpdate(UIUpdate UIUpdate) {
        mUIUpdate = UIUpdate;
    }

    public void stopTimer(){
       getRunning().set(false);
       getTimerData().setRunningStatus(false);
       cancelTimerAlarm(mContext,getTimerData().getAlarmId());
        if (null != mCountDownTimer) {
           mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }


    public void resetTimer() {
        stopTimer();
        getTimerData().setHours(0);
        getTimerData().setMinutes(0);
        getTimerData().setSeconds(60);
        getTimerData().setRunningStatus(false);
        getTimerData().setLastTimeStatus(60000);
        setRunning(false);
    }

    public void createTimerAlarm(Context context, long millis, int id) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.started = true;

        Intent timer = new Intent(context, AlarmReceiver.class);
        timer.setAction("timer");
        timer.putExtra(TONE_VOLUME, 5);
        timer.putExtra(RUNNING, started);
        timer.putExtra(TIMER_ID, getTimerData().getAlarmId());
        timer.putExtra(TONE,getTimerData().getTimerMelody());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, (int) (millis / 1000));
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context,
                id, timer, 0);

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                alarmPendingIntent
        );

    }

    public void cancelTimerAlarm(Context context, int id) {
        Intent timer = new Intent(context, AlarmReceiver.class);
        timer.setAction("timer");
        timer.putExtra(TONE_VOLUME, 5);
        timer.putExtra(RUNNING, started);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, id, timer, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmPendingIntent);
        alarmPendingIntent.cancel();
        this.started = false;


    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (running.get()) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        dest.writeLong(getTimerData().getLongTimeMillis());
        dest.writeLong(getTimerData().getLastTimeStatus());
        dest.writeInt(getTimerData().getAlarmId());
    }

    public static final Creator<TimerRunnable> CREATOR = new Creator<TimerRunnable>() {
        @Override
        public TimerRunnable createFromParcel(Parcel in) {
            return new TimerRunnable(in);
        }

        @Override
        public TimerRunnable[] newArray(int size) {
            return new TimerRunnable[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
}
