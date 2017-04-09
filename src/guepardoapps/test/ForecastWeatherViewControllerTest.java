package guepardoapps.test;

import android.content.Context;

import guepardoapps.library.toolset.controller.BroadcastController;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.view.model.*;

public class ForecastWeatherViewControllerTest {

	private static final String TAG = ForecastWeatherViewControllerTest.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private Context _context;
	private BroadcastController _broadcastController;

	private static String _empty = "";

	private static int _testForecast1ImageId = R.drawable.weather_clear;
	private static String _testForecast1ShortWeekday = "Tu";
	private static String _testForecast1Date = "11.07.2013";
	private static String _testForecast1Time = "15:00";
	private static String _testForecast1TemperatureRange = "31.7°C-33.9°C";

	private static int _testForecast2ImageId = R.drawable.weather_cloud;
	private static String _testForecast2ShortWeekday = "Tu";
	private static String _testForecast2Date = "11.07.2013";
	private static String _testForecast2Time = "18:00";
	private static String _testForecast2TemperatureRange = "25.4°C-26.1°C";

	private static int _testForecast3ImageId = R.drawable.weather_rain;
	private static String _testForecast3ShortWeekday = "Tu";
	private static String _testForecast3Date = "11.07.2013";
	private static String _testForecast3Time = "21:00";
	private static String _testForecast3TemperatureRange = "22.1°C-23.7°C";

	public ForecastWeatherViewControllerTest(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Info("Created test for ForecastWeatherView");

		_context = context;
		_broadcastController = new BroadcastController(_context);

		sendTestBroadcast();
	}

	private void sendTestBroadcast() {
		_logger.Debug("sendTestBroadcast");

		CurrentWeatherModel forecast1 = new CurrentWeatherModel(_empty, _empty, _empty, _empty, _empty,
				_testForecast1ImageId, _testForecast1ShortWeekday, _testForecast1Date, _testForecast1Time,
				_testForecast1TemperatureRange);
		CurrentWeatherModel forecast2 = new CurrentWeatherModel(_empty, _empty, _empty, _empty, _empty,
				_testForecast2ImageId, _testForecast2ShortWeekday, _testForecast2Date, _testForecast2Time,
				_testForecast2TemperatureRange);
		CurrentWeatherModel forecast3 = new CurrentWeatherModel(_empty, _empty, _empty, _empty, _empty,
				_testForecast3ImageId, _testForecast3ShortWeekday, _testForecast3Date, _testForecast3Time,
				_testForecast3TemperatureRange);

		ForecastWeatherModel model = new ForecastWeatherModel();
		model.AddForecast(forecast1);
		model.AddForecast(forecast2);
		model.AddForecast(forecast3);

		_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_FORECAST_WEATHER_MODEL,
				Bundles.FORECAST_WEATHER_MODEL, model);
	}

	public boolean ValidateView(int imageId1, String weekday1, String date1, String time1, String temperatureRange1,
			int imageId2, String weekday2, String date2, String time2, String temperatureRange2, int imageId3,
			String weekday3, String date3, String time3, String temperatureRange3) {
		boolean success = true;

		if (imageId1 != _testForecast1ImageId) {
			_logger.Error("imageId1 FAILED!" + String.valueOf(imageId1) + "!=" + String.valueOf(_testForecast1ImageId));
			success &= false;
		}
		if (!weekday1.contains(_testForecast1ShortWeekday)) {
			_logger.Error("weekday1 FAILED!" + weekday1 + "!=" + _testForecast1ShortWeekday);
			success &= false;
		}
		if (!date1.contains(_testForecast1Date)) {
			_logger.Error("date1 FAILED!" + date1 + "!=" + _testForecast1Date);
			success &= false;
		}
		if (!time1.contains(_testForecast1Time)) {
			_logger.Error("time1 FAILED!" + time1 + "!=" + _testForecast1Time);
			success &= false;
		}
		if (!temperatureRange1.contains(_testForecast1TemperatureRange)) {
			_logger.Error("temperatureRange1 FAILED!" + temperatureRange1 + "!=" + _testForecast1TemperatureRange);
			success &= false;
		}

		if (imageId2 != _testForecast2ImageId) {
			_logger.Error("imageId2 FAILED!" + String.valueOf(imageId2) + "!=" + String.valueOf(_testForecast2ImageId));
			success &= false;
		}
		if (!weekday2.contains(_testForecast2ShortWeekday)) {
			_logger.Error("weekday2 FAILED!" + weekday2 + "!=" + _testForecast2ShortWeekday);
			success &= false;
		}
		if (!date2.contains(_testForecast2Date)) {
			_logger.Error("date2 FAILED!" + date2 + "!=" + _testForecast2Date);
			success &= false;
		}
		if (!time2.contains(_testForecast2Time)) {
			_logger.Error("time2 FAILED!" + time2 + "!=" + _testForecast2Time);
			success &= false;
		}
		if (!temperatureRange2.contains(_testForecast2TemperatureRange)) {
			_logger.Error("temperatureRange2 FAILED!" + temperatureRange2 + "!=" + _testForecast2TemperatureRange);
			success &= false;
		}

		if (imageId3 != _testForecast3ImageId) {
			_logger.Error("imageId3 FAILED!" + String.valueOf(imageId3) + "!=" + String.valueOf(_testForecast3ImageId));
			success &= false;
		}
		if (!weekday3.contains(_testForecast3ShortWeekday)) {
			_logger.Error("weekday3 FAILED!" + weekday3 + "!=" + _testForecast3ShortWeekday);
			success &= false;
		}
		if (!date3.contains(_testForecast3Date)) {
			_logger.Error("date3 FAILED!" + date3 + "!=" + _testForecast3Date);
			success &= false;
		}
		if (!time3.contains(_testForecast3Time)) {
			_logger.Error("time3 FAILED!" + time3 + "!=" + _testForecast3Time);
			success &= false;
		}
		if (!temperatureRange3.contains(_testForecast3TemperatureRange)) {
			_logger.Error("temperatureRange3 FAILED!" + temperatureRange3 + "!=" + _testForecast3TemperatureRange);
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
