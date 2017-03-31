package guepardoapps.mediamirror.converter.json;

import java.sql.Time;
import java.util.ArrayList;

import guepardoapps.library.toolset.common.StringHelper;
import guepardoapps.library.toolset.common.enums.Weekday;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.model.ScheduleModel;

public final class JsonDataToScheduleConverter {

	private static final String TAG = JsonDataToScheduleConverter.class.getSimpleName();
	private static SmartMirrorLogger _logger;

	private static String _searchParameter = "{schedule:";

	public static ArrayList<ScheduleModel> GetList(String[] stringArray) {
		if (StringHelper.StringsAreEqual(stringArray)) {
			return ParseStringToList(stringArray[0]);
		} else {
			String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
			return ParseStringToList(usedEntry);
		}
	}

	public static ScheduleModel Get(String value) {
		if (StringHelper.GetStringCount(value, _searchParameter) == 1) {
			if (value.contains(_searchParameter)) {
				value = value.replace(_searchParameter, "").replace("};};", "");

				String[] data = value.split("\\};");
				ScheduleModel newValue = ParseStringToValue(data);
				if (newValue != null) {
					return newValue;
				}
			}
		}

		if (_logger == null) {
			_logger = new SmartMirrorLogger(TAG);
		}
		_logger.Error(value + " has an error!");

		return null;
	}

	private static ArrayList<ScheduleModel> ParseStringToList(String value) {
		if (StringHelper.GetStringCount(value, _searchParameter) > 0) {
			if (value.contains(_searchParameter)) {
				ArrayList<ScheduleModel> list = new ArrayList<ScheduleModel>();

				String[] entries = value.split("\\" + _searchParameter);
				for (String entry : entries) {
					entry = entry.replace(_searchParameter, "").replace("};};", "");

					String[] data = entry.split("\\};");
					ScheduleModel newValue = ParseStringToValue(data);
					if (newValue != null) {
						list.add(newValue);
					}
				}
				return list;
			}
		}

		if (_logger == null) {
			_logger = new SmartMirrorLogger(TAG);
		}
		_logger.Error(value + " has an error!");

		return null;
	}

	private static ScheduleModel ParseStringToValue(String[] data) {
		if (data.length == 11) {
			if (data[0].contains("{Name:") && data[1].contains("{Socket:") && data[2].contains("{Gpio:")
					&& data[3].contains("{Weekday:") && data[4].contains("{Hour:") && data[5].contains("{Minute:")
					&& data[6].contains("{OnOff:") && data[7].contains("{IsTimer:") && data[8].contains("{PlaySound:")
					&& data[9].contains("{Raspberry:") && data[10].contains("{State:")) {

				String Name = data[0].replace("{Name:", "").replace("};", "");

				String socketName = data[1].replace("{Socket:", "").replace("};", "");

				String WeekdayString = data[3].replace("{Weekday:", "").replace("};", "");
				int weekdayInteger = Integer.parseInt(WeekdayString);
				Weekday weekday = Weekday.GetById(weekdayInteger);

				String HourString = data[4].replace("{Hour:", "").replace("};", "");
				int Hour = Integer.parseInt(HourString);
				String MinuteString = data[5].replace("{Minute:", "").replace("};", "");
				int Minute = Integer.parseInt(MinuteString);
				@SuppressWarnings("deprecation")
				Time time = new Time(Hour, Minute, 0);

				String ActionString = data[6].replace("{OnOff:", "").replace("};", "");
				boolean action = ActionString.contains("1");

				String IsTimerString = data[7].replace("{IsTimer:", "").replace("};", "");
				boolean isTimer = IsTimerString.contains("1");

				String IsActiveString = data[10].replace("{State:", "").replace("};", "");
				boolean isActive = IsActiveString.contains("1");

				if (!isTimer) {
					ScheduleModel newValue = new ScheduleModel(Name, socketName, weekday, time, action, isTimer,
							isActive);
					return newValue;
				} else {
					return null;
				}
			}
		}

		if (_logger == null) {
			_logger = new SmartMirrorLogger(TAG);
		}
		_logger.Error("Data has an error!");

		return null;
	}
}