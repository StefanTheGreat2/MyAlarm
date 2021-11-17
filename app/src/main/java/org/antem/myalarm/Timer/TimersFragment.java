package org.antem.myalarm.Timer;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.antem.myalarm.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;


public class TimersFragment extends Fragment implements TimerAlert {


    public static final String MY_TIMERS = "MY_TIMERS";
    public static final String TAG = "timers fragment";
    Ringtone ringtone;
    private Map<String, String> ringtones;

    TimerListAdapter mListViewAdapter;
    ListView mListView;
    ArrayList<TimerRunnable> mTimerManagers = new ArrayList<>();
    Button addTimer;
    View v;

    public TimersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(() -> ringtones = getNotificationSounds()).start();
        mTimerManagers.clear();
        if (savedInstanceState != null) {
            mTimerManagers = savedInstanceState.getParcelableArrayList(MY_TIMERS);
        } else {
            loadData();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_timers, container, false);
        mListViewAdapter = new TimerListAdapter(this.getContext(), R.layout.timer_item, mTimerManagers,this);
        mListViewAdapter.setData(mTimerManagers);
        mListView = v.findViewById(R.id.mListView);
        addTimer = v.findViewById(R.id.addTimer);
        mListView.setAdapter(mListViewAdapter);
        addTimer.setOnClickListener(v -> {
            TimerData timerData = new TimerData(0, 0, 60, (int) Math.round(Math.random() * Integer.MAX_VALUE));
            timerData.setLastTimeStatus(60000);
            TimerRunnable tempTimer = new TimerRunnable(timerData,getContext());
            mTimerManagers.add(tempTimer);
            mListViewAdapter.notifyDataSetChanged();
        });
        return v;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        for (TimerRunnable timerRunnable : mTimerManagers) {
            if (timerRunnable.mCountDownTimer != null) {

                timerRunnable.mCountDownTimer.cancel();
                timerRunnable.mCountDownTimer = null;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }


    private void saveData() {

        SharedPreferences sharedPreferences = Objects.requireNonNull(requireContext()).
                getSharedPreferences(MY_TIMERS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ArrayList<TimerData> tempData = new ArrayList<>();
        for (TimerRunnable timerRunnable : mTimerManagers) {
            timerRunnable.getTimerData().setRunningStatus(timerRunnable.getRunning().get());
            timerRunnable.getTimerData().setLastTimeMillis(System.currentTimeMillis());
            tempData.add(timerRunnable.getTimerData());
            Log.i(TAG, "saveData: ");


        }
        Gson gson = new Gson();
        String json = gson.toJson(tempData);
        editor.putString(MY_TIMERS, json);
        editor.apply();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void loadData() {
        SharedPreferences sharedPreferences = Objects.requireNonNull(requireContext()).
                getSharedPreferences(MY_TIMERS, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(MY_TIMERS, null);
        ArrayList<TimerData> tempData;

        Type type = new TypeToken<ArrayList<TimerData>>() {
        }.getType();
        tempData = gson.fromJson(json, type);
        if (tempData == null) {
            mTimerManagers = new ArrayList<>();
        } else {
            for (TimerData timerData : tempData) {
                mTimerManagers.add(new TimerRunnable(timerData, getContext()));
                Log.i(TAG, "loadData: ");
            }
        }

    }


    @Override
    public void showDialog(TimerRunnable timerRunnable) {
        alertDialog(timerRunnable);
    }
    private void alertDialog(TimerRunnable currentTimer) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(requireContext(), R.style.AlertDialogCustom));
            alertDialog.setTitle("Choose Alarm Ringtone");
            Map<String, String> allTones = ringtones;
            if (allTones != null) {

                final ArrayList<String> tones = new ArrayList<>();
                for (Map.Entry<String, String> entry : allTones.entrySet()) {
                    String key = entry.getKey();
                    tones.add(key);
                }
                String[] tittles = new String[tones.size()];
                tones.toArray(tittles);

                int checkedItem = 0;
                alertDialog.setSingleChoiceItems(tittles, checkedItem, (dialog, which) -> {

                            currentTimer.getTimerData().setTimerMelody((allTones.get(tittles[which])));
                            playRingtone(allTones.get(tittles[which]));

                        }
                );
                alertDialog.setPositiveButton("Save", (dialog, which) -> {
                    if (ringtone != null) {

                        ringtone.stop();
                    }
                });
                AlertDialog alert = alertDialog.create();
                alert.show();

            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                alertDialog(currentTimer);
            }


    }


    private void playRingtone(String uri) {
        if (ringtone != null) {
            ringtone.stop();
        }
        Log.i(TAG, "playRingtone: " + uri);
        ringtone = RingtoneManager.getRingtone(getContext(), Uri.parse(uri));
        ringtone.play();
    }
    public Map<String, String> getNotificationSounds() {
        Map<String, String> list = new LinkedHashMap<>();

        RingtoneManager manager = new RingtoneManager(getContext());
        manager.setType(RingtoneManager.TYPE_ALL);
        Cursor cursor = manager.getCursor();

        while (cursor.moveToNext()) {
            String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String uri = manager.getRingtoneUri(cursor.getPosition()).toString();


            list.put(title, uri);
        }


        return list;


    }

}