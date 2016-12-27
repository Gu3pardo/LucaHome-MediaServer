package guepardoapps.mediamirror.common.converter;

import java.util.Calendar;

import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class TimeConverter {

	private static final String TAG = TimeConverter.class.getName();
	private static SmartMirrorLogger _logger;

	public static String GetTime(Calendar date) {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Debug("GetTime for " + date.toString());

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

		_logger.Debug("time is " + string);
		return string;
	}
}
