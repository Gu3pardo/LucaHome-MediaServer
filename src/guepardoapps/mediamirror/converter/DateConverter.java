package guepardoapps.mediamirror.converter;

import java.util.Calendar;

import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class DateConverter {

	private static final String TAG = DateConverter.class.getSimpleName();
	private static SmartMirrorLogger _logger;

	public static String GetDate(Calendar date) {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Debug("GetDate for " + date.toString());

		int day = date.get(Calendar.DAY_OF_MONTH);
		int month = date.get(Calendar.MONTH);
		int year = date.get(Calendar.YEAR);

		String dayString = String.valueOf(day);
		if (dayString.length() == 1) {
			dayString = "0" + dayString;
		}
		String monthString = MonthConverter.GetMonth(month);

		String string = "";
		string += dayString + "." + monthString + " " + String.valueOf(year);

		_logger.Debug("date is " + string);
		return string;
	}
}
