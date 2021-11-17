package org.antem.myalarm.Alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Calendar;

import static org.antem.myalarm.Alarm.AlarmReceiver.ALARM_ID;
import static org.antem.myalarm.Alarm.AlarmReceiver.FRIDAY;
import static org.antem.myalarm.Alarm.AlarmReceiver.HOUR;
import static org.antem.myalarm.Alarm.AlarmReceiver.MINUTE;
import static org.antem.myalarm.Alarm.AlarmReceiver.MONDAY;
import static org.antem.myalarm.Alarm.AlarmReceiver.RECURRING;
import static org.antem.myalarm.Alarm.AlarmReceiver.RUNNING;
import static org.antem.myalarm.Alarm.AlarmReceiver.SATURDAY;
import static org.antem.myalarm.Alarm.AlarmReceiver.SUNDAY;
import static org.antem.myalarm.Alarm.AlarmReceiver.THURSDAY;
import static org.antem.myalarm.Alarm.AlarmReceiver.TITLE;
import static org.antem.myalarm.Alarm.AlarmReceiver.TONE;
import static org.antem.myalarm.Alarm.AlarmReceiver.TONE_VOLUME;
import static org.antem.myalarm.Alarm.AlarmReceiver.TUESDAY;
import static org.antem.myalarm.Alarm.AlarmReceiver.VIBRATE;
import static org.antem.myalarm.Alarm.AlarmReceiver.WEDNESDAY;

@Entity(tableName = "alarm_table")
public class Alarm {


    @PrimaryKey
    private final int alarmId;

    private int hour, minute;
    private boolean started, recurring, vibrate;
    private boolean sunday, tuesday, wednesday, thursday, friday, saturday, monday;
    private String title;
    private long created;
    private long timeTillAlarm;
    private String tone;
    private int toneVolume;


    public Alarm(int alarmId, int hour, int minute, String title,
                 long created, boolean started, boolean recurring,
                 boolean sunday, boolean monday, boolean tuesday,
                 boolean wednesday, boolean thursday, boolean friday,
                 boolean saturday, String tone, int toneVolume, boolean vibrate) {
        this.alarmId = alarmId;
        this.hour = hour;
        this.minute = minute;
        this.started = started;

        this.recurring = recurring;

        this.sunday = sunday;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;

        this.tone = tone;
        this.toneVolume = toneVolume;
        this.title = title;
        this.vibrate = vibrate;

        this.created = created;
    }

    @Ignore
    public Alarm() {
        this.alarmId = (int) (Math.random() * Integer.MAX_VALUE);
    }

    public void setTimeTillAlarm(long timeTillAlarm) {
        this.timeTillAlarm = timeTillAlarm;
    }


    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public int getToneVolume() {
        return toneVolume;
    }

    public void setToneVolume(int toneVolume) {
        this.toneVolume = toneVolume;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public boolean isStarted() {
        return started;
    }

    public int getAlarmId() {
        return alarmId;
    }


    public boolean isRecurring() {
        return recurring;
    }

    public boolean isMonday() {
        return monday;
    }

    public boolean isTuesday() {
        return tuesday;
    }

    public boolean isWednesday() {
        return wednesday;
    }

    public boolean isThursday() {
        return thursday;
    }

    public boolean isFriday() {
        return friday;
    }

    public boolean isSaturday() {
        return saturday;
    }

    public boolean isSunday() {
        return sunday;
    }


    public long getTimeTillAlarm() {
        return timeTillAlarm;
    }

    public String getTitle() {
        return title;
    }

    public long getCreated() {
        return created;
    }


    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public void createAlarm(Context context) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("alarmWakeUp");
        intent.putExtra(HOUR, hour);
        intent.putExtra(MINUTE, minute);
        intent.putExtra(RECURRING, recurring);
        intent.putExtra(MONDAY, monday);
        intent.putExtra(TUESDAY, tuesday);
        intent.putExtra(WEDNESDAY, wednesday);
        intent.putExtra(THURSDAY, thursday);
        intent.putExtra(FRIDAY, friday);
        intent.putExtra(SATURDAY, saturday);
        intent.putExtra(SUNDAY, sunday);
        intent.putExtra(TITLE, title);
        intent.putExtra(TONE, tone);
        intent.putExtra(TONE_VOLUME, toneVolume);
        intent.putExtra(ALARM_ID, alarmId);
        intent.putExtra(VIBRATE, vibrate);

        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        checkPassedAlarm(calendar);

        String toastText;
        toastText = String.format("%s scheduled for  %s at %02d:%02d", title, getRecurringDaysText(), hour, minute);

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                alarmPendingIntent
        );
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();

        this.started = true;
    }

    private void checkPassedAlarm(Calendar calendar) {
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        }
    }


    public String getRecurringDaysText() {
        if (!recurring) {
            return "One Time Alarm";
        }
        String days = "";
        int numDays = 0;
        if (sunday) {
            days += "Sun ";
            numDays++;
        }
        if (monday) {
            days += "Mon ";
            numDays++;
        }
        if (tuesday) {
            days += "Tue ";
            numDays++;
        }
        if (wednesday) {
            days += "Wed ";
            numDays++;
        }
        if (thursday) {
            days += "Thu ";
            numDays++;
        }
        if (friday) {
            days += "Fri ";
            numDays++;
        }
        if (saturday) {
            days += "Sat ";
            numDays++;
        }
        if (numDays > 6) {
            return "Every Day";
        }

        return days;
    }


    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("alarmWakeUp");
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmPendingIntent);
        alarmPendingIntent.cancel();
        this.started = false;
//        String toastText = String.format("Alarm cancelled for %02d:%02d with id %d", hour, minute, alarmId);
//        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
//        Log.i("cancel", toastText);
    }

    public String getTimeLeftTillAlarm() {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        checkPassedAlarm(c);
        long alarmTime = c.getTimeInMillis();
        long millis = System.currentTimeMillis();
        c.setTimeInMillis(millis);
        return (AlarmConvertUtil.getConvertedText(alarmTime - System.currentTimeMillis()));
    }



}
