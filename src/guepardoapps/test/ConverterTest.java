package guepardoapps.test;

import java.util.ArrayList;
import java.util.Calendar;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.converter.*;

public class ConverterTest {

	private static final String TAG = BirthdayViewControllerTest.class.getSimpleName();
	private SmartMirrorLogger _logger;

	public ConverterTest() {
		_logger = new SmartMirrorLogger(TAG);
	}

	public void PerformTests() {
		testDateConverter();
		testMonthConverter();
		testTimeConverter();
		testShortWeekdayConverter();
		testWeekdayConverter();
	}

	private void testDateConverter() {
		_logger.Debug("testDateConverter");

		int testDayOfMonth = 14;
		int testMonth = 3;
		int testYear = 2013;
		String testResult = "14.April 2013";

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, testDayOfMonth);
		calendar.set(Calendar.MONTH, testMonth);
		calendar.set(Calendar.YEAR, testYear);

		String dateString = DateConverter.GetDate(calendar);

		if (dateString.contains(testResult)) {
			_logger.LogTest("Test SUCCEEDED!", true);
		} else {
			_logger.LogTest("Test FAILED!", false);
			_logger.Error(dateString + "!=" + testResult);
		}
	}

	private void testMonthConverter() {
		_logger.Debug("testMonthConverter");

		ArrayList<String> testResultList = new ArrayList<String>();
		testResultList.add("January");
		testResultList.add("February");
		testResultList.add("March");
		testResultList.add("April");
		testResultList.add("May");
		testResultList.add("June");
		testResultList.add("July");
		testResultList.add("August");
		testResultList.add("September");
		testResultList.add("October");
		testResultList.add("November");
		testResultList.add("December");
		testResultList.add("n.a.");

		boolean success = true;

		for (int index = 0; index < testResultList.size(); index++) {
			String month = MonthConverter.GetMonth(index);
			if (month.contains(testResultList.get(index))) {
				success &= true;
			} else {
				_logger.Error("FAILED for " + String.valueOf(index) + ":" + month + "!=" + testResultList.get(index));
				success &= false;
			}
		}

		if (success) {
			_logger.LogTest("Test SUCCEEDED!", success);
		} else {
			_logger.LogTest("Test FAILED!", success);
		}
	}

	private void testTimeConverter() {
		_logger.Debug("testTimeConverter");

		int testHour = 7;
		int testMinute = 1;
		String testResult = "07:01";

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, testHour);
		calendar.set(Calendar.MINUTE, testMinute);

		String timeString = TimeConverter.GetTime(calendar);

		if (timeString.contains(testResult)) {
			_logger.LogTest("Test SUCCEEDED!", true);
		} else {
			_logger.LogTest("Test FAILED!", true);
			_logger.Error(timeString + "!=" + testResult);
		}
	}

	private void testShortWeekdayConverter() {
		_logger.Debug("testShortWeekdayConverter");

		ArrayList<String> testResultList = new ArrayList<String>();
		testResultList.add("n.a.");
		testResultList.add("Su");
		testResultList.add("Mo");
		testResultList.add("Tu");
		testResultList.add("We");
		testResultList.add("Th");
		testResultList.add("Fr");
		testResultList.add("Sa");
		testResultList.add("n.a.");

		boolean success = true;

		for (int index = 0; index < testResultList.size(); index++) {
			String weekday = WeekdayConverter.GetShortWeekday(index);
			if (weekday.contains(testResultList.get(index))) {
				success &= true;
			} else {
				_logger.Error("FAILED for " + String.valueOf(index) + ":" + weekday + "!=" + testResultList.get(index));
				success &= false;
			}
		}

		if (success) {
			_logger.LogTest("Test SUCCEEDED!", success);
		} else {
			_logger.LogTest("Test FAILED!", success);
		}
	}

	private void testWeekdayConverter() {
		_logger.Debug("testWeekdayConverter");

		ArrayList<String> testResultList = new ArrayList<String>();
		testResultList.add("n.a.");
		testResultList.add("Sunday");
		testResultList.add("Monday");
		testResultList.add("Tuesday");
		testResultList.add("Wednesday");
		testResultList.add("Thursday");
		testResultList.add("Friday");
		testResultList.add("Saturday");
		testResultList.add("n.a.");

		boolean success = true;

		for (int index = 0; index < testResultList.size(); index++) {
			String weekday = WeekdayConverter.GetWeekday(index);
			if (weekday.contains(testResultList.get(index))) {
				success &= true;
			} else {
				_logger.Error("FAILED for " + String.valueOf(index) + ":" + weekday + "!=" + testResultList.get(index));
				success &= false;
			}
		}

		if (success) {
			_logger.LogTest("Test SUCCEEDED!", success);
		} else {
			_logger.LogTest("Test FAILED!", success);
		}
	}
}
