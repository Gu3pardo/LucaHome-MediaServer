package guepardoapps.mediamirror.common;

import java.util.Calendar;

import guepardoapps.mediamirror.common.constants.Constants;

public class TimeHelper {
	public static boolean IsMuteTime() {
		Calendar now = Calendar.getInstance();
		int hour = now.get(Calendar.HOUR_OF_DAY);
		if (hour >= Constants.START_MUTE_TIME || hour < Constants.END_MUTE_TIME) {
			return true;
		}
		return false;
	}
}
