package org.antem.myalarm.Timer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.antem.myalarm.R;

import java.util.ArrayList;

interface TimerAlert {
    void showDialog(TimerRunnable timerRunnable);
}

public class TimerListAdapter extends ArrayAdapter<TimerListAdapter.ViewHolder> {
    private final int layoutResource;
    private final LayoutInflater layoutInflater;
    private ArrayList<TimerRunnable> timersList;
    public static final String TAG = "adapterTag";
    Context mContext;
    TimerAlert mTimerAlert;


    Handler mHandler = new Handler(Looper.getMainLooper());

    public void setData(ArrayList<TimerRunnable> timers) {
        timersList = timers;
        notifyDataSetChanged();
    }


    public TimerListAdapter(Context context, int resource, ArrayList<TimerRunnable> timersList, TimerAlert timerAlert) {
        super(context, resource);
        this.mContext = context;
        this.layoutResource = resource;
        this.layoutInflater = LayoutInflater.from(context);
        this.timersList = timersList;
        this.mTimerAlert = timerAlert;


    }

    @NonNull
    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        convertView = layoutInflater.inflate(layoutResource, parent, false);
        viewHolder = new ViewHolder(convertView);

        init(position, viewHolder);


        return convertView;
    }

    private void init(int position, ViewHolder viewHolder) {
        TimerRunnable currentTimer = timersList.get(position);
        TimerData timerData = currentTimer.getTimerData();
        if (timerData.getLastTimeStatus() != 60000) {
            viewHolder.resetTv.setTextColor(Color.parseColor("#0099FF"));
        }

        String timerName;
        if (null == currentTimer.getTimerData().getTimerName()) {
            timerName = "Timer Name";
        } else {
            timerName = currentTimer.getTimerData().getTimerName();
        }
        viewHolder.timerNameTV.setText(timerName);
        viewHolder.timerNameET.setText(timerName);
        viewHolder.bindView(timersList.get(position));
        currentTimer.setHandler(mHandler);

        Log.i(TAG, "getView: " + currentTimer.getTimerData().isRunningStatus());
        if (currentTimer.getTimerData().isRunningStatus()) {

            long time = System.currentTimeMillis() - timerData.getLastTimeMillis();
            Log.i(TAG, "getView: " + time);

            time = timerData.getLastTimeStatus() - time;


            if (time >= 0) {

                currentTimer.startTimer(time, getContext());
                viewHolder.progressUpdate(timerData.getLastTimeStatus());

            }
        } else {

            viewHolder.progressUpdate(timerData.getLastTimeStatus());
        }
    }


    @Override
    public int getCount() {
        return timersList.size();
    }


    class ViewHolder implements UIUpdate {
        TextView timeTv, timerNameTV, resetTv, melodyTv;
        EditText timerNameET;
        Button start, pause, delete, reset, melodyBtn;
        SeekBar sSeekBar, mSeekBar, hSeekBar;
        View timerLayout;
        TimerRunnable mTimerRunnable;


        ViewHolder(View v) {
            this.timeTv = v.findViewById(R.id.timerTv);
            this.start = v.findViewById(R.id.startBtn);
            this.pause = v.findViewById(R.id.pauseBtn);
            this.delete = v.findViewById(R.id.deleteBtn);
            this.timerNameET = v.findViewById(R.id.timerName);
            this.timerLayout = v.findViewById(R.id.timerBackground);
            this.timerNameTV = v.findViewById(R.id.timerNameTv);
            this.hSeekBar = v.findViewById(R.id.hoursSeek);
            this.mSeekBar = v.findViewById(R.id.minutesSeek);
            this.sSeekBar = v.findViewById(R.id.secondsSeek);
            this.melodyTv = v.findViewById(R.id.melody);
            this.melodyBtn = v.findViewById(R.id.melodyBtn);
            this.reset = v.findViewById(R.id.resetTimer);
            this.resetTv = v.findViewById(R.id.reset);
            timerNameET.setVisibility(View.INVISIBLE);
            pause.setVisibility(View.INVISIBLE);


        }

        public void bindView(TimerRunnable currentTimer) {
            currentTimer.setUIUpdate(this);
            mTimerRunnable = currentTimer;
            timeTv.setText(currentTimer.getConvertedText(currentTimer.getTimerData().getLastTimeStatus()));


            timerNameTV.setOnClickListener((v) ->
            {
                timerNameTV.setVisibility(View.INVISIBLE);
                timerNameET.setVisibility(View.VISIBLE);
            });


            timerLayout.setOnClickListener((v) -> {
                timerNameET.setVisibility(View.INVISIBLE);

                String timerName = timerNameET.getText().toString();
                timerNameTV.setText(timerName);

                currentTimer.getTimerData().setTimerName(timerName);


                timerNameTV.setVisibility(View.VISIBLE);
            });


            start.setOnClickListener(v -> {
                Log.i(TAG, "bindView: start id" + currentTimer.getTimerData().getAlarmId());
                if (!currentTimer.getRunning().get()) {
                    currentTimer.startTimer(currentTimer.getTimerData().getLastTimeStatus(), getContext());
                    currentTimer.createTimerAlarm(getContext(), currentTimer.getTimerData().getLongTimeMillis(), currentTimer.getTimerData().getAlarmId());

                }


            });

            pause.setOnClickListener(v -> {
                currentTimer.stopTimer();
                progressUpdate(currentTimer.getTimerData().getLastTimeStatus());

            });
            reset.setOnClickListener(v -> {
                resetTv.setTextColor(Color.parseColor("#AD617587"));
                currentTimer.resetTimer();
                progressUpdate(currentTimer.getTimerData().getLastTimeStatus());


            });

            delete.setOnClickListener(v -> {
                currentTimer.stopTimer();
                timersList.remove(currentTimer);
             notifyDataSetChanged();
            });


            hSeekBar.setMax(23);
            mSeekBar.setMax(59);
            sSeekBar.setMax(60);
            SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    if (progress >= 0) {
                        if (seekBar.getId() == R.id.secondsSeek) {
                            currentTimer.getTimerData().setSeconds(progress);
                            tvSetText(currentTimer);
                        }
                        if (seekBar.getId() == R.id.minutesSeek) {
                            currentTimer.getTimerData().setMinutes(progress);
                            tvSetText(currentTimer);
                        }
                        if (seekBar.getId() == R.id.hoursSeek) {
                            currentTimer.getTimerData().setHours(progress);
                            tvSetText(currentTimer);
                        }

                    }
                }


                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if (currentTimer.getRunning().get()) {
                        currentTimer.stopTimer();
                    }

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    currentTimer.startTimer(currentTimer.getTimerData().getLongTimeMillis(), getContext());
                    start.setVisibility(View.INVISIBLE);
                    pause.setVisibility(View.VISIBLE);


                }
            };

            hSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
            mSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
            sSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

            melodyBtn.setOnClickListener(v -> mTimerAlert.showDialog(currentTimer));

        }

        private void tvSetText(TimerRunnable currentTimer) {
            String s = String.valueOf(currentTimer.getConvertedText(currentTimer.getTimerData().getLongTimeMillis()));
            timeTv.setText(s);
        }

        @Override
        public void progressUpdate(long progress) {
            mHandler.post(() -> {
                int hour = (int) ((progress / 3600000) % 24);
                int min = (int) (progress / 60000) % 60;
                int sec = (int) (progress / 1000) % 60;
                hSeekBar.setProgress(hour);
                mSeekBar.setProgress(min);
                sSeekBar.setProgress(sec);
                timeTv.setText(mTimerRunnable.getConvertedText(progress));
                if (mTimerRunnable.getRunning().get()) {
                    start.setVisibility(View.INVISIBLE);
                    pause.setVisibility(View.VISIBLE);
                    resetTv.setTextColor(Color.parseColor("#0099FF"));
                } else {
                    start.setVisibility(View.VISIBLE);
                    pause.setVisibility(View.INVISIBLE);
                }
            });
        }

        @Override
        public void dataSetUpdate() {
         notifyDataSetChanged();
        }



    }
}
