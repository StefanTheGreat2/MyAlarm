package org.antem.myalarm.StopWatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.antem.myalarm.R;

import java.util.Locale;

import static android.content.ContentValues.TAG;


public class StopWatchFragment extends Fragment {
    public static final String SAVED_PREFS = "saved prefs";
    public static final String SECONDS = "seconds";
    public static final String RUNNING = "was running";
    public static final String CURRENT_TIME = "current time";

    Handler myHandler;
    Button start, reset, stop;

    TextView mTextView;
    ProgressBar sProgressBar,mProgressBar,hProgressBar;

    private int seconds = 0;
    private String time = "00:00:00";
    private boolean running = false;
    private boolean wasRunning ;
    private long currentTimeMillis;

    Thread mThread = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myHandler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_stop_watch, container, false);
        sProgressBar=view.findViewById(R.id.progressBarSeconds);
        mProgressBar=view.findViewById(R.id.progressBarMinutes );
        hProgressBar=view.findViewById(R.id.progressBarHours );
        sProgressBar.setMax(59);
        mProgressBar.setMax(59);
        hProgressBar.setMax(23);
        start = view.findViewById(R.id.stopWatchStart);
        stop = view.findViewById(R.id.stopWatchPause);
        reset = view.findViewById(R.id.stopWatchStop);
        mTextView = view.findViewById(R.id.stopWatchTv);

        start.setOnClickListener(mOnClickListener());
        stop.setOnClickListener(mOnClickListener());
        reset.setOnClickListener(mOnClickListener());
        Log.i(TAG, "onCreateView: " + " save instance");

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE);
        seconds = sharedPreferences.getInt(SECONDS, 0);
        wasRunning = sharedPreferences.getBoolean(RUNNING, true);
        if (wasRunning) {
            Log.i(TAG, "onCreateView: was running"+" running"+running);
            wasRunning=false;
            running=true;
            if (seconds > 0) {
                seconds= ((int) ( (System.currentTimeMillis()-sharedPreferences.getLong(CURRENT_TIME, 0)+(seconds*1000)))/1000);

            }
            runTimer();


        }else {
            getTimeString();
            mTextView.setText(time);
        }



        return view;

    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();

        Log.i(TAG, "onPause: " + "saving");

        if (running){
            wasRunning=true;
            running=false;
        }
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;

        }
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SECONDS, seconds).putLong(CURRENT_TIME, System.currentTimeMillis()).
                putBoolean(RUNNING, wasRunning).apply();


    }


    View.OnClickListener mOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.stopWatchStart) {
                    if (!running) {
                        running = true;
                        runTimer();

                    }
                } else if (id == R.id.stopWatchStop) {
                    seconds = 0;
                    time = "00:00:00";
                    mTextView.setText(time);
                    if (mThread != null) {
                        mThread.interrupt();

                    }
                    running = false;
                } else if (id == R.id.stopWatchPause) {
                    running = false;
                }
            }
        };
    }

    private void runTimer() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (running) {
                    Log.i(TAG, "run: ");
                    getTimeString();
                    myHandler.post(() -> {

                        mTextView.setText(time);
                    });


                    try {
                        sProgressBar.setProgress(seconds%60);
                        mProgressBar.setProgress(seconds/60%60);
                        hProgressBar.setProgress(seconds/60/60%24);

                        seconds++;
                        Thread.sleep(1000);

                    } catch (InterruptedException in) {
                        Log.d(TAG, "run: " + in);
                    }
                }
            }

        });
        mThread.start();
    }
            private void getTimeString() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                time
                        = String
                        .format(Locale.getDefault(),
                                "%02d:%02d:%02d", hours,
                                minutes, secs);
            }
}
