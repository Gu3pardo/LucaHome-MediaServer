package guepardoapps.mediamirror.converter;

import android.support.annotation.NonNull;

import java.util.Calendar;

import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class TimeConverter {

    private static final String TAG = TimeConverter.class.getSimpleName();

    public static String GetTime(@NonNull Calendar date) {
        SmartMirrorLogger logger = new SmartMirrorLogger(TAG);
        logger.Debug("GetTime for " + date.toString());

        int hour = date.get(Calendar.HOUR_OF_DAY);
        int minute = date.get(Calendar.MINUTE);

        String hourString = String.valueOf(hour);
        if (hourString.length() == 1) {
            hourString = "0" + hourString;
        }
        String minuteString = String.valueOf(minute);
        if (minuteString.length() == 1) {
            minuteString = "0" + minuteString;
        }

        String string = "";
        string += hourString + ":" + minuteString;

        logger.Debug("time is " + string);
        return string;
    }
}
