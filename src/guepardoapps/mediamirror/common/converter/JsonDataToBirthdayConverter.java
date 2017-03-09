package guepardoapps.mediamirror.common.converter;

import java.util.ArrayList;
import java.util.Calendar;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.model.helper.BirthdayHelper;

import guepardoapps.toolset.common.StringHelper;

public final class JsonDataToBirthdayConverter {

	private static final String TAG = JsonDataToBirthdayConverter.class.getName();
	private static SmartMirrorLogger _logger;

	private static String _searchParameter = "{birthday:";

	public static ArrayList<BirthdayHelper> GetList(String[] stringArray) {
		if (_logger == null) {
			_logger = new SmartMirrorLogger(TAG);
		}

		if (StringHelper.StringsAreEqual(stringArray)) {
			if (stringArray[0] != null) {
				return ParseStringToList(stringArray[0]);
			} else {
				_logger.Warn("Entry 1 is null! Checking 2nd");

				if (stringArray.length != 2) {
					_logger.Error("StringArray has no 2nd entry!");
					return new ArrayList<BirthdayHelper>();
				}

				if (stringArray[1] != null) {
					return ParseStringToList(stringArray[1]);
				} else {
					_logger.Warn("Entry 2 is null!");
					return new ArrayList<BirthdayHelper>();
				}
			}
		} else {
			String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
			if (usedEntry != null) {
				return ParseStringToList(usedEntry);
			} else {
				_logger.Warn("usedEntry is null!");
				return new ArrayList<BirthdayHelper>();
			}
		}
	}

	public static BirthdayHelper Get(String value) {
		if (_logger == null) {
			_logger = new SmartMirrorLogger(TAG);
		}

		if (!value.contains("Error")) {
			if (StringHelper.GetStringCount(value, _searchParameter) == 1) {
				if (value.contains(_searchParameter)) {
					value = value.replace(_searchParameter, "").replace("};};", "");

					String[] data = value.split("\\};");
					BirthdayHelper newValue = ParseStringToValue(data);
					if (newValue != null) {
						return newValue;
					}
				}
			}
		}
		_logger.Error(value + " has an error!");

		return null;
	}

	private static ArrayList<BirthdayHelper> ParseStringToList(String value) {
		if (_logger == null) {
			_logger = new SmartMirrorLogger(TAG);
		}

		if (!value.contains("Error")) {
			if (StringHelper.GetStringCount(value, _searchParameter) > 1) {
				if (value.contains(_searchParameter)) {
					ArrayList<BirthdayHelper> list = new ArrayList<BirthdayHelper>();

					String[] entries = value.split("\\" + _searchParameter);
					for (String entry : entries) {
						entry = entry.replace(_searchParameter, "").replace("};};", "");

						String[] data = entry.split("\\};");
						BirthdayHelper newValue = ParseStringToValue(data);
						if (newValue != null) {
							list.add(newValue);
						}
					}

					return list;
				}
			}
		}

		_logger.Error(value + " has an error!");

		return null;
	}

	private static BirthdayHelper ParseStringToValue(String[] data) {
		if (_logger == null) {
			_logger = new SmartMirrorLogger(TAG);
		}

		if (data.length == 5) {
			if (data[0].contains("{id:") && data[1].contains("{name:") && data[2].contains("{day:")
					&& data[3].contains("{month:") && data[4].contains("{year:")) {

				String idString = data[0].replace("{id:", "").replace("};", "");
				int id = Integer.parseInt(idString);

				String name = data[1].replace("{name:", "").replace("};", "");

				String dayString = data[2].replace("{day:", "").replace("};", "");
				int day = Integer.parseInt(dayString);
				String monthString = data[3].replace("{month:", "").replace("};", "");
				int month = Integer.parseInt(monthString);
				String yearString = data[4].replace("{year:", "").replace("};", "");
				int year = Integer.parseInt(yearString);
				Calendar birthday = Calendar.getInstance();
				birthday.set(Calendar.DAY_OF_MONTH, day);
				birthday.set(Calendar.MONTH, month - 1);
				birthday.set(Calendar.YEAR, year);

				BirthdayHelper newValue = new BirthdayHelper(name, birthday, id);
				return newValue;
			}
		}

		_logger.Error("Data has an error!");

		return null;
	}
}