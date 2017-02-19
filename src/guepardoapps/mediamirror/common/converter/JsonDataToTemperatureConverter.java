package guepardoapps.mediamirror.common.converter;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.model.helper.TemperatureHelper;

public final class JsonDataToTemperatureConverter {

	private static final String TAG = JsonDataToTemperatureConverter.class.getName();
	private static SmartMirrorLogger _logger;

	private static String _searchParameter = "{temperature:";

	public static ArrayList<TemperatureHelper> GetList(String[] stringArray) {
		ArrayList<TemperatureHelper> temperatureList = new ArrayList<TemperatureHelper>();
		for (String entry : stringArray) {
			if (entry == null || entry.length() == 0) {
				continue;
			}

			if (entry.contains(_searchParameter)) {
				entry = entry.replace(_searchParameter, "").replace("};};", "");

				String[] data = entry.split("\\};");
				TemperatureHelper newValue = ParseStringArrayToValue(data);
				if (newValue != null) {
					temperatureList.add(newValue);
				}
			}
		}

		return temperatureList;
	}

	public static TemperatureHelper Get(String value) {
		if (value.contains(_searchParameter)) {
			value = value.replace(_searchParameter, "").replace("};};", "");

			String[] data = value.split("\\};");
			TemperatureHelper newValue = ParseStringArrayToValue(data);
			if (newValue != null) {
				return newValue;
			}
		}

		if (_logger == null) {
			_logger = new SmartMirrorLogger(TAG);
		}
		_logger.Error(value + " has an error!");

		return null;
	}

	private static TemperatureHelper ParseStringArrayToValue(String[] data) {
		if (data.length == 4) {
			if (data[0].contains("{value:") 
					&& data[1].contains("{area:") 
					&& data[2].contains("{sensorPath:")
					&& data[3].contains("{graphPath:")) {

				String tempString = data[0].replace("{value:", "").replace("};", "");
				Double temperature = Double.parseDouble(tempString);

				String area = data[1].replace("{area:", "").replace("};", "");

				String graphPath = data[3].replace("{graphPath:", "").replace("};", "");

				Calendar calendar = Calendar.getInstance();
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				int minute = calendar.get(Calendar.MINUTE);
				int second = calendar.get(Calendar.SECOND);
				@SuppressWarnings("deprecation")
				Time time = new Time(hour, minute, second);

				TemperatureHelper newValue = new TemperatureHelper(temperature, area, time, graphPath);
				return newValue;
			}
		}

		if (_logger == null) {
			_logger = new SmartMirrorLogger(TAG);
		}
		_logger.Error("Data has an error!");

		return null;
	}
}