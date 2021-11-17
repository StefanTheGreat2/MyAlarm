package org.antem.myalarm.Alarm;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.antem.myalarm.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class AlarmListFragment extends Fragment implements AlarmUiUtil {
    private Context mContext;
    private AlarmListAdapter mAlarmListAdapter;
    private AlarmListViewModel alarmsListViewModel;
    private int pressedButton = 0;
    private Button addAlarm, deleteAllBtn, yesBtn, noBtn;
    private TextView areYouSureTv, nextAlarmTimeTv;

    private final AtomicBoolean keepUpdate = new AtomicBoolean();
    private Thread uiUpdater;
    public static final String TAG = "alarmlist";

    public AlarmListFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_alarm_list, container, false);
        ListView myList = v.findViewById(R.id.lalarmList);
        myList.setAdapter(mAlarmListAdapter);
        addAlarm = v.findViewById(R.id.addAlarmBtn);
        deleteAllBtn = v.findViewById(R.id.deleteAllBtn);
        yesBtn = v.findViewById(R.id.yesBtn);
        noBtn = v.findViewById(R.id.noBtn);
        areYouSureTv = v.findViewById(R.id.sureTv);
        yesBtn.setVisibility(View.GONE);
        noBtn.setVisibility((View.GONE));
        areYouSureTv.setVisibility(View.GONE);
        areYouSureTv.setText(R.string.sureTvText);
        nextAlarmTimeTv = v.findViewById(R.id.nextAlarmTimeTv);

        keepUpdate.set(true);

        init();
        uiUpdater = new Thread(this::run);
        if (!uiUpdater.isAlive()) {
            uiUpdater.start();
        }


        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.requireContext();
        mAlarmListAdapter = new AlarmListAdapter(this.requireContext(), R.layout.single_alarm, this);
        alarmsListViewModel = new ViewModelProvider(this).get(AlarmListViewModel.class);
        alarmsListViewModel.getAlarmsLiveData().observe(this, alarms -> {

            if (alarms != null) {
                mAlarmListAdapter.setData(sortAlarms(alarms));
                mAlarmListAdapter.notifyDataSetChanged();

                nextAlarmTimeTv.setText(getNextAlarm(alarms));

            }
        });

    }

    private void init() {
        addAlarm.setOnClickListener(addAlarm ->
                getParentFragmentManager().beginTransaction()
                        .addToBackStack("create_alarm").replace(R.id.nav_host_fragment, new CreateAlarmFragment()).commit());

        deleteAllBtn.setOnClickListener(delete ->

        {
            deleteAllBtn.setSelected(true);
            if (pressedButton != (deleteAllBtn.getId())) {
                pressedButton = deleteAllBtn.getId();
                addAlarm.animate().translationXBy(-350).setDuration(200).start();
                deleteAllBtn.animate().translationXBy(1000).setDuration(100).start();
            }
            yesBtn.setVisibility(View.VISIBLE);
            noBtn.setVisibility(View.VISIBLE);
            areYouSureTv.setVisibility(View.VISIBLE);

        });

        yesBtn.setOnClickListener((yes) ->

        {
            if (pressedButton != yesBtn.getId()) {
                pressedButton = yesBtn.getId();
                addAlarm.animate().translationXBy(350).setDuration(100).start();
                deleteAllBtn.animate().translationXBy(-1000).setDuration(700).start();
                yesBtn.setVisibility(View.GONE);
                noBtn.setVisibility((View.GONE));
                areYouSureTv.setVisibility(View.GONE);
                deleteAll();


            }

        });
        noBtn.setOnClickListener(no ->

        {
            if (pressedButton != noBtn.getId()) {
                pressedButton = noBtn.getId();
                addAlarm.animate().translationXBy(350).setDuration(100).start();
                yesBtn.setVisibility(View.GONE);
                noBtn.setVisibility((View.GONE));
                areYouSureTv.setVisibility(View.GONE);
                deleteAllBtn.animate().translationXBy(-1000).setDuration(600).start();
            }
        });
    }

    private String getNextAlarm(List<Alarm> alarms) {
        List<Alarm> currentAlarms = new ArrayList<>(alarms);
        currentAlarms.sort((a, b) -> a.getTimeLeftTillAlarm().compareTo(b.getTimeLeftTillAlarm()));
        for (Alarm alarm : currentAlarms) {
            if (alarm.isStarted()) {
                return alarm.getTimeLeftTillAlarm();
            }
        }
        return "No alarms running";
    }


    private List<Alarm> sortAlarms(@NonNull List<Alarm> alarms) {
        for (int j = 0; j < alarms.size() - 1; j++) {
            for (int i = 0; i < alarms.size() - 1; i++) {

                Alarm tempAlarm = alarms.get(i);
                int oneAlarm = tempAlarm.getHour();
                int secondAlarm = alarms.get(j + 1).getHour();

                if (oneAlarm > secondAlarm) {
                    alarms.set(i, alarms.get(j + 1));
                    alarms.set(j + 1, tempAlarm);

                } else if (oneAlarm == secondAlarm) {
                    if (tempAlarm.getMinute() > alarms.get(j + 1).getMinute()) {
                        alarms.set(i, alarms.get(j + 1));
                        alarms.set(j + 1, tempAlarm);
                    }
                }
            }
        }
        return alarms;
    }

    @Override
    public void deleteAlarm(Alarm alarm) {
        alarm.cancelAlarm(mContext);
        alarmsListViewModel.delete(alarm);
        mAlarmListAdapter.notifyDataSetChanged();
    }

    @Override
    public void update(Alarm alarm) {
        alarmsListViewModel.update(alarm);
    }

    private void deleteAll() {
        for (Alarm alarm : mAlarmListAdapter.getAlarms()) {
            alarm.cancelAlarm(mContext);
        }
        alarmsListViewModel.deleteAll();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        keepUpdate.set(false);
        if (uiUpdater != null) {
            uiUpdater.interrupt();
            uiUpdater=null;
        }
        Log.i(TAG, "onDestroy: alarm list fragment");
    }

    private void run() {
        while (keepUpdate.get()) {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (!mAlarmListAdapter.getAlarms().isEmpty()) {
                    nextAlarmTimeTv.setText(getNextAlarm(mAlarmListAdapter.getAlarms()));
                }
                Log.i(TAG, "run: ");
            });
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                Log.i(TAG, "run: interuppted");
            }

        }
    }
}