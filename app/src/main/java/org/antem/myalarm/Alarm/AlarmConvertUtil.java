package org.antem.myalarm.Alarm;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class AlarmConvertUtil {
    static String getConvertedText(long millis){
        NumberFormat f = new DecimalFormat("00");
        long hour = (millis / 3600000);
        long min = (millis / 60000) % 60;


        String time;
        time = f.format(hour) + ":" + f.format(min);


        return time;
    }
}
