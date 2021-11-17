package org.antem.myalarm.Timer;

import com.google.gson.annotations.SerializedName;

public class TimerData {
    @SerializedName("hours")
    private int hours;
    @SerializedName("minutes")
    private int minutes;
    @SerializedName("seconds")
    private int seconds;
    @SerializedName("lastTimeStatus")
    private long lastTimeStatus;
    @SerializedName("currentMillis")
    private long lastTimeMillis;
    @SerializedName("runningStatus")
    private boolean runningStatus;
    @SerializedName("timerName")
    private String timerName;
    @SerializedName("alarmId")
    private final int alarmId;
    @SerializedName("timerMelody")
    String timerMelody;

    public TimerData(int hours, int minutes, int seconds,int alarmId) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.alarmId=alarmId;
    }

    public int getAlarmId() {
        return alarmId;
    }

    public void setTimerMelody(String timerMelody) {
        this.timerMelody = timerMelody;
    }

    public String getTimerMelody() {
        return timerMelody;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getSeconds() {
        return seconds;
    }

    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    public long getLastTimeStatus() {
        return lastTimeStatus;
    }

    public void setLastTimeStatus(long lastTimeStatus) {
        this.lastTimeStatus = lastTimeStatus;
    }

    public long getLastTimeMillis() {
        return lastTimeMillis;
    }

    public void setLastTimeMillis(long lastTimeMillis) {
        this.lastTimeMillis = lastTimeMillis;
    }

    public long getLongTimeMillis(){
        return (hours * 3600000) + (minutes * 60000) + (seconds * 1000);
    }

    public boolean isRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(boolean runningStatus) {
        this.runningStatus = runningStatus;
    }
}

