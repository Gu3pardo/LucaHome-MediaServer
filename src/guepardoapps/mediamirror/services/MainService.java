package guepardoapps.mediamirror.services;

import android.os.IBinder;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.enums.RSSFeed;
import guepardoapps.mediamirror.common.enums.YoutubeId;
import guepardoapps.mediamirror.model.CenterModel;
import guepardoapps.mediamirror.model.RSSModel;
import guepardoapps.mediamirror.server.ServerThread;
import guepardoapps.mediamirror.test.ConverterTest;
import guepardoapps.mediamirror.tts.TTSService;
import guepardoapps.mediamirror.updater.*;
import guepardoapps.toolset.controller.BroadcastController;

public class MainService extends Service {

	private static final String TAG = MainService.class.getName();
	private SmartMirrorLogger _logger;

	private Context _context;
	private BroadcastController _broadcastController;

	private boolean _isInitialized;

	private ServerThread _serverThread = null;

	private BirtdayUpdater _birthdayUpdater;
	private CurrentWeatherUpdater _currentWeatherUpdater;
	private DateViewUpdater _dateViewUpdater;
	private ForecastWeatherUpdater _forecastWeatherUpdater;
	private IpAdressViewUpdater _ipAdressViewUpdater;
	private RSSViewUpdater _rssViewUpdater;
	private TemperatureUpdater _temperatureUpdater;

	private TTSService _ttsService;

	private ConverterTest _converterTest;

	@Override
	public void onCreate() {
		super.onCreate();

		if (!_isInitialized) {
			_isInitialized = true;

			_logger = new SmartMirrorLogger(TAG);

			_context = this;
			if (_broadcastController == null) {
				_broadcastController = new BroadcastController(_context);
			}

			if (_serverThread == null) {
				_serverThread = new ServerThread(Constants.SERVERPORT, _context);
				_serverThread.Start();
			}

			if (_birthdayUpdater == null) {
				_birthdayUpdater = new BirtdayUpdater(_context);
				_birthdayUpdater.Start(Constants.BIRTHDAY_UPDATE_TIMEOUT);
			}

			if (_currentWeatherUpdater == null) {
				_currentWeatherUpdater = new CurrentWeatherUpdater(_context);
				_currentWeatherUpdater.Start(Constants.CURRENT_WEATHER_UPDATE_TIMEOUT);
			}

			if (_dateViewUpdater == null) {
				_dateViewUpdater = new DateViewUpdater(_context);
				_dateViewUpdater.Start(Constants.DATE_UPDATE_TIMEOUT);
			}

			if (_forecastWeatherUpdater == null) {
				_forecastWeatherUpdater = new ForecastWeatherUpdater(_context);
				_forecastWeatherUpdater.Start(Constants.FORECAST_WEATHER_UPDATE_TIMEOUT);
			}

			if (_ipAdressViewUpdater == null) {
				_ipAdressViewUpdater = new IpAdressViewUpdater(_context);
				_ipAdressViewUpdater.Start(Constants.IP_ADRESS_UPDATE_TIMEOUT);
			}

			if (_rssViewUpdater == null) {
				_rssViewUpdater = new RSSViewUpdater(_context);
				_rssViewUpdater.Start(Constants.RSS_UPDATE_TIMEOUT);
			}

			if (_temperatureUpdater == null) {
				_temperatureUpdater = new TemperatureUpdater(_context);
				_temperatureUpdater.Start(Constants.TEMPERATURE_UPDATE_TIMEOUT);
			}

			if (_ttsService == null) {
				_ttsService = new TTSService(_context);
				_ttsService.Init();
			}

			CenterModel centerModel = new CenterModel(false, "", true, YoutubeId.THE_GOOD_LIFE_STREAM.GetYoutubeId(),
					false, "");
			_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_SHOW_CENTER_MODEL,
					Constants.BUNDLE_CENTER_MODEL, centerModel);

			RSSModel rssModel = new RSSModel(RSSFeed.DEFAULT, true);
			_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_SHOW_RSS_DATA_MODEL,
					Constants.BUNDLE_RSS_DATA_MODEL, rssModel);

			if (Constants.TESTING_ENABLED) {
				_converterTest = new ConverterTest();
				_converterTest.PerformTests();
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (_logger != null) {
			_logger.Debug("onStartCommand");
		}
		return 0;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		if (_logger != null) {
			_logger.Debug("onBind");
		}
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (_logger != null) {
			_logger.Debug("onDestroy");
		}

		_serverThread.Dispose();

		_birthdayUpdater.Dispose();
		_currentWeatherUpdater.Dispose();
		_dateViewUpdater.Dispose();
		_forecastWeatherUpdater.Dispose();
		_ipAdressViewUpdater.Dispose();
		_rssViewUpdater.Dispose();
		_temperatureUpdater.Dispose();

		_ttsService.Dispose();
	}
}