package guepardoapps.mediamirror.test;

import java.util.Calendar;

import android.content.Context;
import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.converter.*;
import guepardoapps.mediamirror.model.*;
import guepardoapps.toolset.controller.BroadcastController;

public class DateViewControllerTest {

	private static final String TAG = DateViewControllerTest.class.getName();
	private SmartMirrorLogger _logger;

	private Context _context;
	private BroadcastController _broadcastController;

	private Calendar _calendar;

	private static String _testWeekday = "Sunday";
	private static String _testDate = "09.January 2018";
	private static String _testTime = "09:31";

	public DateViewControllerTest(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Info("Created test for DateView");

		_context = context;
		_broadcastController = new BroadcastController(_context);

		setCalendarTestDate();
		sendTestBroadcast();
	}

	private void setCalendarTestDate() {
		_logger.Debug("setCalendarTestDate");

		_calendar = Calendar.getInstance();

		_calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

		_calendar.set(Calendar.DAY_OF_MONTH, 9);
		_calendar.set(Calendar.MONTH, Calendar.JANUARY);
		_calendar.set(Calendar.YEAR, 2018);

		_calendar.set(Calendar.HOUR_OF_DAY, 9);
		_calendar.set(Calendar.MINUTE, 31);
	}

	private void sendTestBroadcast() {
		_logger.Debug("sendTestBroadcast");

		_testWeekday = WeekdayConverter.GetWeekday(_calendar.get(Calendar.DAY_OF_WEEK));
		_testDate = DateConverter.GetDate(_calendar);
		_testTime = TimeConverter.GetTime(_calendar);

		DateModel model = new DateModel(_testWeekday, _testDate, _testTime);

		_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_SHOW_DATE_MODEL, Constants.BUNDLE_DATE_MODEL,
				model);
	}

	public boolean ValidateView(String weekday, String date, String time) {
		boolean success = true;

		if (!weekday.contains(_testWeekday)) {
			_logger.Error("weekday FAILED!" + weekday + "!=" + _testWeekday);
			success &= false;
		}
		if (!date.contains(_testDate)) {
			_logger.Error("date FAILED!" + date + "!=" + _testDate);
			success &= false;
		}
		if (!time.contains(_testTime)) {
			_logger.Error("time FAILED!" + time + "!=" + _testTime);
			success &= false;
		}

		if (success) {
			_logger.LogTest("Test SUCCEEDED!", success);
		} else {
			_logger.LogTest("Test FAILED!", success);
		}

		return success;
	}
}
