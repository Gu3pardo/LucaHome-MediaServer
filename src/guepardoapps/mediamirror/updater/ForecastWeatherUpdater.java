package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.Tools;
import guepardoapps.mediamirror.model.CurrentWeatherModel;
import guepardoapps.mediamirror.model.ForecastWeatherModel;

import guepardoapps.toolset.openweather.OpenWeatherController;
import guepardoapps.toolset.openweather.common.OpenWeatherConstants;
import guepardoapps.toolset.openweather.model.*;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class ForecastWeatherUpdater {

	private static final String TAG = ForecastWeatherUpdater.class.getName();
	private SmartMirrorLogger _logger;

	private Handler _updater;

	private Context _context;
	private BroadcastController _broadcastController;
	private OpenWeatherController _openWeatherController;
	private ReceiverController _receiverController;

	private int _updateTime;

	private Runnable _updateRunnable = new Runnable() {
		public void run() {
			_logger.Debug("_updateRunnable run");
			if (!Tools.IsMuteTime()) {
				_openWeatherController.loadForecastWeather();
			}
			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateReceiver onReceive");
			ForecastModel forecastWeather = (ForecastModel) intent
					.getSerializableExtra(OpenWeatherConstants.BUNDLE_EXTRA_FORECAST_MODEL);
			if (forecastWeather != null) {
				_logger.Debug("forecastWeather is: " + forecastWeather.toString());

				if (forecastWeather.GetList().size() < 3) {
					_logger.Warn("Forecast weather is smaller then three forecasts!");
					return;
				}

				guepardoapps.toolset.openweather.model.ForecastWeatherModel weather1 = forecastWeather.GetList().get(0);
				CurrentWeatherModel forecast1 = new CurrentWeatherModel("", "", "", "", "", weather1.GetIcon(), "",
						weather1.GetDate(), weather1.GetTime(), weather1.GetTempMin() + " - " + weather1.GetTempMax());
				guepardoapps.toolset.openweather.model.ForecastWeatherModel weather2 = forecastWeather.GetList().get(1);
				CurrentWeatherModel forecast2 = new CurrentWeatherModel("", "", "", "", "", weather2.GetIcon(), "",
						weather2.GetDate(), weather2.GetTime(), weather2.GetTempMin() + " - " + weather2.GetTempMax());
				guepardoapps.toolset.openweather.model.ForecastWeatherModel weather3 = forecastWeather.GetList().get(2);
				CurrentWeatherModel forecast3 = new CurrentWeatherModel("", "", "", "", "", weather3.GetIcon(), "",
						weather3.GetDate(), weather3.GetTime(), weather3.GetTempMin() + " - " + weather3.GetTempMax());

				ForecastWeatherModel model = new ForecastWeatherModel();
				model.AddForecast(forecast1);
				model.AddForecast(forecast2);
				model.AddForecast(forecast3);

				_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_SHOW_FORECAST_WEATHER_MODEL,
						Constants.BUNDLE_FORECAST_WEATHER_MODEL, model);
			} else {
				_logger.Warn("Forecast weather is null!");
			}
		}
	};

	private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_performUpdateReceiver onReceive");
			_openWeatherController.loadForecastWeather();
		}
	};

	public ForecastWeatherUpdater(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_updater = new Handler();
		_context = context;
		_broadcastController = new BroadcastController(_context);
		_openWeatherController = new OpenWeatherController(_context, Constants.CITY);
		_receiverController = new ReceiverController(_context);
	}

	public void Start(int updateTime) {
		_logger.Debug("Initialize");
		_updateTime = updateTime;
		_logger.Debug("UpdateTime is: " + String.valueOf(_updateTime));
		_receiverController.RegisterReceiver(_updateReceiver,
				new String[] { OpenWeatherConstants.BROADCAST_GET_FORECAST_WEATHER_JSON_FINISHED });
		_receiverController.RegisterReceiver(_performUpdateReceiver,
				new String[] { Constants.BROADCAST_PERFORM_FORECAST_WEATHER_UPDATE });
		_updateRunnable.run();
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_updater.removeCallbacks(_updateRunnable);
		_receiverController.UnregisterReceiver(_updateReceiver);
		_receiverController.UnregisterReceiver(_performUpdateReceiver);
	}
}
