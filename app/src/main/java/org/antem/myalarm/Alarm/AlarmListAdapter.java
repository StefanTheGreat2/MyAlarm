package org.antem.myalarm.Alarm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.antem.myalarm.R;

import java.util.ArrayList;
import java.util.List;


interface AlarmUiUtil {
    void deleteAlarm(Alarm alarm);
    void update(Alarm alarm);
}

public class AlarmListAdapter extends ArrayAdapter<AlarmListAdapter> {
    public static final String TAG="TAG";
    private List<Alarm> mAlarms;
    private final LayoutInflater layoutInflater;
    int layoutResource;
    AlarmUiUtil mAlarmUiUtil;



    public List<Alarm> getAlarms() {
        return mAlarms;
    }

    public AlarmListAdapter(@NonNull Context context, int resource, AlarmUiUtil alarmUiUtil) {
        super(context, resource);
        layoutResource = resource;
        this.mAlarms = new ArrayList<>();
        this.layoutInflater = LayoutInflater.from(context);
        this.mAlarmUiUtil = alarmUiUtil;


    }

    public void setData(List<Alarm> alarms) {
        mAlarms = alarms;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = layoutInflater.inflate(layoutResource, parent, false);


        AlarmsViewHolder alarmsViewHolder = new AlarmsViewHolder(view, mAlarmUiUtil);
        Alarm alarm = mAlarms.get(position);
        alarmsViewHolder.init(alarm, this.getContext());

        alarmsViewHolder.alarmName.setText(alarm.getTitle());
        alarmsViewHolder.alarmTime.setText(String.format("%02d:%02d", alarm.getHour(), alarm.getMinute()));
        alarmsViewHolder.alarmDays.setText(alarm.getRecurringDaysText());



        return view;
    }

    @Override
    public int getCount() {
        return mAlarms.size();
    }
}

class AlarmsViewHolder {
    private static final String TAG ="TAG" ;
    ToggleButton alarmToggle;
    TextView alarmName, alarmTime, alarmDays;
    Button deleteBtn;
    AlarmUiUtil mAlarmUiUtil;

    public AlarmsViewHolder(View view, AlarmUiUtil alarmUiUtil) {
        alarmName = view.findViewById(R.id.alarmNameTxt);
        alarmTime = view.findViewById(R.id.alarmTime);
        alarmDays = view.findViewById(R.id.alarmDays);
        deleteBtn = view.findViewById(R.id.alarmDeleteBtn);
        alarmToggle = view.findViewById(R.id.toggleAlarm);
        mAlarmUiUtil = alarmUiUtil;

    }

    void init(Alarm alarm, Context context) {

        alarmToggle.setChecked(alarm.isStarted());
        alarmToggle.setActivated(alarm.isStarted());


        deleteBtn.setOnClickListener(delete -> {
            Log.i(TAG, "init: alarm " + alarm.getAlarmId());
            mAlarmUiUtil.deleteAlarm(alarm);


        });
        alarmToggle.setOnClickListener(alarmToggle -> {
            if (!alarmToggle.isActivated()) {
                alarmToggle.setActivated(true);
                alarm.createAlarm(context);
                mAlarmUiUtil.update(alarm);
            } else {
                alarmToggle.setActivated(false);
                alarm.cancelAlarm(context);
                mAlarmUiUtil.update(alarm);
            }
        });

    }
}

