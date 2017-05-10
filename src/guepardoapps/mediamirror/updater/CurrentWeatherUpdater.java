package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import guepardoapps.library.openweather.common.OWBroadcasts;
import guepardoapps.library.openweather.common.OWBundles;
import guepardoapps.library.openweather.common.enums.WeatherCondition;
import guepardoapps.library.openweather.common.model.WeatherModel;
import guepardoapps.library.openweather.controller.OpenWeatherController;
import guepardoapps.library.openweather.converter.WeatherConverter;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.Constants;
import guepardoapps.mediamirror.view.model.CurrentWeatherModel;

public class CurrentWeatherUpdater {

	private static final String TAG = CurrentWeatherUpdater.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private Handler _updater;

	private Context _context;
	private BroadcastController _broadcastController;
	private OpenWeatherController _openWeatherController;
	private ReceiverController _receiverController;

	private int _updateTime;
	private boolean _isRunning;

	private Runnable _updateRunnable = new Runnable() {
		public void run() {
			_logger.Debug("_updateRunnable run");
			DownloadWeather();
			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateReceiver onReceive");
			WeatherModel currentWeather = (WeatherModel) intent.getSerializableExtra(OWBundles.EXTRA_WEATHER_MODEL);
			if (currentWeather != null) {
				_logger.Debug("currentWeather is: " + currentWeather.toString());

				String description = currentWeather.GetDescription();
				_logger.Debug("Description: " + description);

				WeatherCondition condition = WeatherConverter.GetWeatherCondition(description);
				int iconId = condition.GetIcon();

				_logger.Debug("WeatherCondition: " + condition.toString());
				_logger.Debug("IconId: " + String.valueOf(iconId));

				CurrentWeatherModel model = new CurrentWeatherModel(condition.toString(),
						currentWeather.GetTemperatureString(), currentWeather.GetHumidity(),
						currentWeather.GetPressure(), currentWeather.GetLastUpdate().toString(), iconId, "", "", "",
						"");
				_logger.Debug("CurrentWeatherModel: " + model.toString());

				_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CURRENT_WEATHER_MODEL,
						Bundles.CURRENT_WEATHER_MODEL, model);
			} else {
				_logger.Warn("Current weather is null!");
			}
		}
	};

	private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_performUpdateReceiver onReceive");
			DownloadWeather();
		}
	};

	public CurrentWeatherUpdater(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_updater = new Handler();
		_context = context;
		_broadcastController = new BroadcastController(_context);
		_openWeatherController = new OpenWeatherController(_context, Constants.CITY);
		_receiverController = new ReceiverController(_context);
	}

	public void Start(int updateTime) {
		_logger.Debug("Initialize");
		if (_isRunning) {
			_logger.Warn("Already running!");
			return;
		}
		_updateTime = updateTime;
		_logger.Debug("UpdateTime is: " + String.valueOf(_updateTime));
		_receiverController.RegisterReceiver(_updateReceiver,
				new String[] { OWBroadcasts.CURRENT_WEATHER_JSON_FINISHED });
		_receiverController.RegisterReceiver(_performUpdateReceiver,
				new String[] { Broadcasts.PERFORM_CURRENT_WEATHER_UPDATE });
		_updateRunnable.run();
		_isRunning = true;
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_updater.removeCallbacks(_updateRunnable);
		_receiverController.UnregisterReceiver(_updateReceiver);
		_receiverController.UnregisterReceiver(_performUpdateReceiver);
		_isRunning = false;
	}

	public void DownloadWeather() {
		_logger.Debug("DownloadWeather");

		_openWeatherController.loadCurrentWeather();
	}
}
