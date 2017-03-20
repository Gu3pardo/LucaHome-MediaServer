package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import guepardoapps.library.openweather.common.OWBroadcasts;
import guepardoapps.library.openweather.common.OWBundles;
import guepardoapps.library.openweather.common.enums.ForecastListType;
import guepardoapps.library.openweather.common.model.ForecastModel;
import guepardoapps.library.openweather.controller.OpenWeatherController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.TimeHelper;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.Constants;
import guepardoapps.mediamirror.model.CurrentWeatherModel;
import guepardoapps.mediamirror.model.ForecastWeatherModel;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class ForecastWeatherUpdater {

	private static final String TAG = ForecastWeatherUpdater.class.getSimpleName();
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
			DownloadWeather();
			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateReceiver onReceive");
			ForecastModel forecastWeather = (ForecastModel) intent.getSerializableExtra(OWBundles.EXTRA_FORECAST_MODEL);
			if (forecastWeather != null) {
				_logger.Debug("forecastWeather is: " + forecastWeather.toString());

				int listSize = forecastWeather.GetList().size();

				if (listSize < 3) {
					_logger.Warn("Forecast weather is smaller then three forecasts!");
					return;
				}

				int entryIndex = 0;
				guepardoapps.library.openweather.common.model.ForecastWeatherModel weather1 = forecastWeather.GetList()
						.get(entryIndex);
				entryIndex++;
				if (entryIndex > listSize - 1) {
					_logger.Error(
							String.format("EntryIndex %s is bigger then listSize-1 %s!", entryIndex, listSize - 1));
					return;
				}

				int tries = 0;
				while (weather1.GetForecastListType() == ForecastListType.DATE_DIVIDER && tries < 5) {
					tries++;

					weather1 = forecastWeather.GetList().get(entryIndex);
					entryIndex++;
					if (entryIndex > listSize - 1) {
						_logger.Error(
								String.format("EntryIndex %s is bigger then listSize-1 %s!", entryIndex, listSize - 1));
						return;
					}
				}

				if (tries == 5) {
					_logger.Error("Error selecting forecastweather for entry 1!");
					return;
				}

				CurrentWeatherModel forecast1 = new CurrentWeatherModel("", "", "", "", "", weather1.GetIcon(), "",
						weather1.GetDate(), weather1.GetTime(), weather1.GetTempMin() + " - " + weather1.GetTempMax());

				guepardoapps.library.openweather.common.model.ForecastWeatherModel weather2 = forecastWeather.GetList()
						.get(entryIndex);
				entryIndex++;
				if (entryIndex > listSize - 1) {
					_logger.Error(
							String.format("EntryIndex %s is bigger then listSize-1 %s!", entryIndex, listSize - 1));
					return;
				}

				tries = 0;
				while (weather2.GetForecastListType() == ForecastListType.DATE_DIVIDER && tries < 5) {
					tries++;

					weather2 = forecastWeather.GetList().get(entryIndex);
					entryIndex++;
					if (entryIndex > listSize - 1) {
						_logger.Error(
								String.format("EntryIndex %s is bigger then listSize-1 %s!", entryIndex, listSize - 1));
						return;
					}
				}

				if (tries == 5) {
					_logger.Error("Error selecting forecastweather for entry 2!");
					return;
				}

				CurrentWeatherModel forecast2 = new CurrentWeatherModel("", "", "", "", "", weather2.GetIcon(), "",
						weather2.GetDate(), weather2.GetTime(), weather2.GetTempMin() + " - " + weather2.GetTempMax());

				guepardoapps.library.openweather.common.model.ForecastWeatherModel weather3 = forecastWeather.GetList()
						.get(entryIndex);
				entryIndex++;
				if (entryIndex > listSize - 1) {
					_logger.Error(
							String.format("EntryIndex %s is bigger then listSize-1 %s!", entryIndex, listSize - 1));
					return;
				}

				tries = 0;
				while (weather3.GetForecastListType() == ForecastListType.DATE_DIVIDER && tries < 5) {
					tries++;

					weather3 = forecastWeather.GetList().get(entryIndex);
					entryIndex++;
					if (entryIndex > listSize - 1) {
						_logger.Error(
								String.format("EntryIndex %s is bigger then listSize-1 %s!", entryIndex, listSize - 1));
						return;
					}
				}

				if (tries == 5) {
					_logger.Error("Error selecting forecastweather for entry 3!");
					return;
				}

				CurrentWeatherModel forecast3 = new CurrentWeatherModel("", "", "", "", "", weather3.GetIcon(), "",
						weather3.GetDate(), weather3.GetTime(), weather3.GetTempMin() + " - " + weather3.GetTempMax());

				ForecastWeatherModel model = new ForecastWeatherModel();
				model.AddForecast(forecast1);
				model.AddForecast(forecast2);
				model.AddForecast(forecast3);

				_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_FORECAST_WEATHER_MODEL,
						Bundles.FORECAST_WEATHER_MODEL, model);
			} else {
				_logger.Warn("Forecast weather is null!");
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
				new String[] { OWBroadcasts.FORECAST_WEATHER_JSON_FINISHED });
		_receiverController.RegisterReceiver(_performUpdateReceiver,
				new String[] { Broadcasts.PERFORM_FORECAST_WEATHER_UPDATE });
		_updateRunnable.run();
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_updater.removeCallbacks(_updateRunnable);
		_receiverController.UnregisterReceiver(_updateReceiver);
		_receiverController.UnregisterReceiver(_performUpdateReceiver);
	}

	public void DownloadWeather() {
		_logger.Debug("DownloadWeather");

		if (TimeHelper.IsMuteTime()) {
			_logger.Warn("Mute time!");
			return;
		}

		_openWeatherController.loadForecastWeather();
	}
}
