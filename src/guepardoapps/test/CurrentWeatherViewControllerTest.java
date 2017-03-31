package guepardoapps.test;

import android.content.Context;

import guepardoapps.library.toolset.controller.BroadcastController;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.model.*;

public class CurrentWeatherViewControllerTest {

	private static final String TAG = CurrentWeatherViewControllerTest.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private Context _context;
	private BroadcastController _broadcastController;

	private static String _empty = "";

	private static String _testCondition = "Clear";
	private static String _testTemperature = "19.3°C";
	private static String _testHumidity = "54%";
	private static String _testPressure = "989mbar";
	private static String _testUpdatedTime = "22:14";
	private static int _testImageId = R.drawable.weather_clear;

	public CurrentWeatherViewControllerTest(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Info("Created test for CurrentWeatherView");

		_context = context;
		_broadcastController = new BroadcastController(_context);

		sendTestBroadcast();
	}

	private void sendTestBroadcast() {
		_logger.Debug("sendTestBroadcast");

		CurrentWeatherModel model = new CurrentWeatherModel(_testCondition, _testTemperature, _testHumidity,
				_testPressure, _testUpdatedTime, _testImageId, _empty, _empty, _empty, _empty);
		_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CURRENT_WEATHER_MODEL,
				Bundles.CURRENT_WEATHER_MODEL, model);
	}

	public boolean ValidateView(String condition, String temperature, String humidity, String pressure,
			String updatedTime, int imageId) {
		boolean success = true;

		if (!condition.contains(_testCondition)) {
			_logger.Error("condition FAILED!" + condition + "!=" + _testCondition);
			success &= false;
		}
		if (!temperature.contains(_testTemperature)) {
			_logger.Error("temperature FAILED!" + temperature + "!=" + _testTemperature);
			success &= false;
		}
		if (!humidity.contains(_testHumidity)) {
			_logger.Error("humidity FAILED!" + humidity + "!=" + _testHumidity);
			success &= false;
		}
		if (!pressure.contains(_testPressure)) {
			_logger.Error("pressure FAILED!" + pressure + "!=" + _testPressure);
			success &= false;
		}
		if (!updatedTime.contains(_testUpdatedTime)) {
			_logger.Error("updatedTime FAILED!" + updatedTime + "!=" + _testUpdatedTime);
			success &= false;
		}
		if (imageId != _testImageId) {
			_logger.Error("imageId FAILED!" + String.valueOf(imageId) + "!=" + String.valueOf(_testImageId));
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
