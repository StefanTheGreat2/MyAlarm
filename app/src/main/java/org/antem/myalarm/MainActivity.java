package org.antem.myalarm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.antem.myalarm.Alarm.AlarmListFragment;
import org.antem.myalarm.StopWatch.StopWatchFragment;
import org.antem.myalarm.Timer.TimersFragment;


public class MainActivity extends AppCompatActivity {
    public static final String FRAGMENT = "FRAGMENT";
    private String currentFragment;
    Fragment alarm, timer, stopwatch;
    BottomNavigationView mBottomNavigationView;
  

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBottomNavigationView = findViewById(R.id.bottom_nav);

        alarm = new AlarmListFragment();
        timer = new TimersFragment();
        stopwatch = new StopWatchFragment();


        mBottomNavigationView.setOnItemSelectedListener(v -> {

            int destination = v.getItemId();
            if (destination != mBottomNavigationView.getSelectedItemId()) {
                if (destination == R.id.alarmListFragment) {
                    removeFragment();
                    currentFragment = "ALARM";
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, alarm).commit();
                } else if (destination == R.id.timersFragment) {
                    removeFragment();
                    currentFragment = "TIMER";
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, timer).commit();
                } else if (destination == R.id.stopWatchFragment) {
                    removeFragment();
                    currentFragment = "STOPWATCH";
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, stopwatch).commit();
                }
            }
            return true;
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (this::onSystemUiVisibilityChange);
        currentFragment = getSharedPreferences(FRAGMENT, MODE_PRIVATE).getString(FRAGMENT, "ALARM");
        switch (currentFragment) {
            case "ALARM":
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, alarm).commit();
                mBottomNavigationView.setSelectedItemId(R.id.alarmListFragment);
                break;
            case "TIMER":
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, timer).commit();
                mBottomNavigationView.setSelectedItemId(R.id.timersFragment);
                break;
            case "STOPWATCH":
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, stopwatch).commit();
                mBottomNavigationView.setSelectedItemId(R.id.stopWatchFragment);
                break;
        }


    }


    private void removeFragment() {

        getFragment(currentFragment).onDestroy();
    }

    public Fragment getFragment(String fragmentName) {
        switch (fragmentName) {
            case "ALARM":
                return alarm;

            case "TIMER":
                return timer;

            case "STOPWATCH":
                return stopwatch;

        }
        return null;
    }


    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getSharedPreferences(FRAGMENT, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(FRAGMENT, currentFragment).apply();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {

            hideSystemUI();
        }
    }


    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


    private void onSystemUiVisibilityChange(int v) {

        hideSystemUI();


    }
}