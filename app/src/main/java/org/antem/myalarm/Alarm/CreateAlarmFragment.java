
package org.antem.myalarm.Alarm;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.antem.myalarm.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static org.antem.myalarm.Alarm.AlarmReceiver.TONE;
import static org.antem.myalarm.Alarm.AlarmReceiver.TONE_VOLUME;
import static org.antem.myalarm.Alarm.AlarmReceiver.VIBRATE;
import static org.antem.myalarm.Alarm.RingActivity.TAG;


public class CreateAlarmFragment extends Fragment {
    private SeekBar alarmVolume;
    private CreateAlarmViewModel mCreateAlarmViewModel;
    private ToggleButton su, mo, tu, we, th, fr, st, recurring, vibrate;
    private Button create, select, alarmTest;
    private TimePicker tp;
    private EditText alarmName;
    private Map<String, String> ringtones;
    private String toneUri;
    private Context mContext;
    private TextView toneVolume;
    private Ringtone ringtone;

    public CreateAlarmFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.requireContext();
        new Thread(() -> ringtones = getNotificationSounds()).start();
        mCreateAlarmViewModel = new ViewModelProvider(this).get(CreateAlarmViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create_alarm, container, false);

        tp = v.findViewById(R.id.timePicker);
        create = v.findViewById(R.id.createAlarm);
        alarmName = v.findViewById(R.id.alarmTime);
        toneVolume = v.findViewById(R.id.volume);
        su = v.findViewById(R.id.sun);
        mo = v.findViewById(R.id.mon);
        tu = v.findViewById(R.id.tue);
        we = v.findViewById(R.id.wed);
        th = v.findViewById(R.id.thu);
        fr = v.findViewById(R.id.fri);
        st = v.findViewById(R.id.sat);
        recurring = v.findViewById(R.id.recurring);
        select = v.findViewById(R.id.select);
        alarmTest = v.findViewById(R.id.testAlarm);
        vibrate = v.findViewById(R.id.vibrate);
        alarmVolume = v.findViewById(R.id.alarmVolume);


        tp.setIs24HourView(true);

        alarmVolume.setMax(10);
        alarmVolume.setProgress(5);
        toneVolume.setText(String.valueOf(alarmVolume.getProgress()));

        recurring.setChecked(true);
        initClickListeners();
        recurring.setActivated(true);
        su.setActivated(true);
        mo.setActivated(true);
        tu.setActivated(true);
        we.setActivated(true);
        th.setActivated(true);
        fr.setActivated(true);
        st.setActivated(true);
        vibrate.setActivated(true);
        vibrate.setChecked(true);
        return v;
    }

    private void initClickListeners() {

        alarmTest.setOnClickListener(alarmTest -> {
            Intent intent = new Intent(this.mContext, AlarmService.class);
            intent.putExtra(TONE, toneUri);
            intent.putExtra(VIBRATE, vibrate.isActivated());
            intent.putExtra(TONE_VOLUME, alarmVolume.getProgress());
            mContext.stopService(intent);
            mContext.startService(intent);
        });

        select.setOnClickListener(select -> alertDialog());

        recurring.setOnClickListener(recurring -> activateBtns(!recurring.isActivated()));

        su.setOnClickListener(su -> su.setActivated(!su.isActivated()));
        mo.setOnClickListener(mo -> mo.setActivated(!mo.isActivated()));
        tu.setOnClickListener(tu -> tu.setActivated(!tu.isActivated()));
        we.setOnClickListener(we -> we.setActivated(!we.isActivated()));
        th.setOnClickListener(th -> th.setActivated(!th.isActivated()));
        fr.setOnClickListener(fr -> fr.setActivated(!fr.isActivated()));
        st.setOnClickListener(st -> st.setActivated(!st.isActivated()));
        vibrate.setOnClickListener(vibrate -> vibrate.setActivated(!vibrate.isActivated()));

        create.setOnClickListener(v1 -> {
            scheduleAlarm();
            getParentFragmentManager().popBackStack();
        });

        alarmVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                toneVolume.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void activateBtns(boolean checked) {
        su.setActivated(checked);
        mo.setActivated(checked);
        tu.setActivated(checked);
        we.setActivated(checked);
        th.setActivated(checked);
        fr.setActivated(checked);
        st.setActivated(checked);
        su.setChecked(checked);
        mo.setChecked(checked);
        tu.setChecked(checked);
        we.setChecked(checked);
        th.setChecked(checked);
        fr.setChecked(checked);
        st.setChecked(checked);
        recurring.setActivated(checked);
    }

    private void scheduleAlarm() {
        int alarmId = new Random().nextInt(Integer.MAX_VALUE);
        Alarm alarm = new Alarm(alarmId, tp.getHour(), tp.getMinute(), alarmName.getText().toString(),
                System.currentTimeMillis(), true, recurring.isActivated(), su.isActivated(), mo.isActivated(), tu.isActivated()
                , we.isActivated(), th.isActivated(), fr.isActivated(), st.isActivated(), toneUri, alarmVolume.getProgress(), vibrate.isActivated());
        mCreateAlarmViewModel.setAlarm(alarm);

        alarm.createAlarm(mContext);

    }

    private void alertDialog() {


        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.AlertDialogCustom));
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

                        toneUri = allTones.get(tittles[which]);
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
            alertDialog();
        }

    }


    private void playRingtone(String uri) {
        if (ringtone != null) {
            ringtone.stop();
        }
        Log.i(TAG, "playRingtone: "+uri);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: create alarm fragment");
    }
}
