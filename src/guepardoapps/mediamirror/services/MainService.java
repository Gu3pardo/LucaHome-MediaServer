package guepardoapps.mediamirror.services;

import android.os.IBinder;
import android.os.PowerManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.Enables;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.enums.RSSFeed;
import guepardoapps.mediamirror.common.enums.YoutubeId;
import guepardoapps.mediamirror.controller.BatterySocketController;
import guepardoapps.mediamirror.model.CenterModel;
import guepardoapps.mediamirror.model.RSSModel;
import guepardoapps.mediamirror.server.ServerThread;
import guepardoapps.mediamirror.test.ConverterTest;
import guepardoapps.mediamirror.tts.TTSService;
import guepardoapps.mediamirror.updater.*;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class MainService extends Service {

	private static final String TAG = MainService.class.getName();
	private SmartMirrorLogger _logger;

	private Context _context;
	private BroadcastController _broadcastController;
	private ReceiverController _receiverController;

	private boolean _isInitialized;

	private ServerThread _serverThread = null;

	private BatterySocketController _batterySocketController;

	private BirtdayUpdater _birthdayUpdater;
	private CurrentWeatherUpdater _currentWeatherUpdater;
	private DateViewUpdater _dateViewUpdater;
	private ForecastWeatherUpdater _forecastWeatherUpdater;
	private IpAdressViewUpdater _ipAdressViewUpdater;
	private RSSViewUpdater _rssViewUpdater;
	private SocketListUpdater _socketListUpdater;
	private TemperatureUpdater _temperatureUpdater;

	private TTSService _ttsService;

	private ConverterTest _converterTest;

	private PowerManager _powerManager;
	private PowerManager.WakeLock _wakeLock;

	private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_birthdayUpdater.DownloadBirthdays();
			_currentWeatherUpdater.DownloadWeather();
			_dateViewUpdater.UpdateDate();
			_forecastWeatherUpdater.DownloadWeather();
			_ipAdressViewUpdater.GetCurrentLocalIpAddress();
			_rssViewUpdater.LoadRss();
			_socketListUpdater.DownloadSocketList();
			_temperatureUpdater.DownloadTemperature();
		}
	};

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

			if (_receiverController == null) {
				_receiverController = new ReceiverController(_context);

				_receiverController.RegisterReceiver(_screenEnableReceiver,
						new String[] { Constants.BROADCAST_SCREEN_ENABLED });
			}

			if (_serverThread == null) {
				_serverThread = new ServerThread(Constants.SERVERPORT, _context);
				_serverThread.Start();
			}

			if (_batterySocketController == null) {
				_batterySocketController = new BatterySocketController(_context);
				_batterySocketController.Start();
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
				_dateViewUpdater.Start();
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

			if (_socketListUpdater == null) {
				_socketListUpdater = new SocketListUpdater(_context);
				_socketListUpdater.Start(Constants.SOCKET_LIST_UPDATE_TIMEOUT);
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

			_powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
			_wakeLock = _powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MediaServerMainService");
			_wakeLock.acquire();

			if (Enables.TESTING_ENABLED) {
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

		_receiverController.UnregisterReceiver(_screenEnableReceiver);

		_serverThread.Dispose();

		_batterySocketController.Dispose();

		_birthdayUpdater.Dispose();
		_currentWeatherUpdater.Dispose();
		_dateViewUpdater.Dispose();
		_forecastWeatherUpdater.Dispose();
		_ipAdressViewUpdater.Dispose();
		_rssViewUpdater.Dispose();
		_socketListUpdater.Dispose();
		_temperatureUpdater.Dispose();

		_ttsService.Dispose();

		_wakeLock.release();
	}
}