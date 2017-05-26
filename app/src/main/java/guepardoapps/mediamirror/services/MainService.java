package guepardoapps.mediamirror.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.enums.RSSFeed;
import guepardoapps.library.lucahome.common.enums.YoutubeId;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;
import guepardoapps.library.toolset.controller.TTSController;
import guepardoapps.library.toolset.scheduler.ScheduleService;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.Constants;
import guepardoapps.mediamirror.common.constants.Enables;
import guepardoapps.mediamirror.common.constants.Timeouts;
import guepardoapps.mediamirror.common.enums.AutomaticSettings;
import guepardoapps.mediamirror.controller.BatterySocketController;
import guepardoapps.mediamirror.controller.MediaVolumeController;
import guepardoapps.mediamirror.server.ServerThread;
import guepardoapps.mediamirror.updater.*;
import guepardoapps.mediamirror.view.Main;
import guepardoapps.mediamirror.view.model.CenterModel;
import guepardoapps.mediamirror.view.model.RSSModel;

public class MainService extends Service {

    private static final String TAG = MainService.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private Context _context;

    private BroadcastController _broadcastController;
    //private MediaMirrorFTPController _mediaMirrorFTPController;
    private MediaVolumeController _mediaVolumeController;
    private ReceiverController _receiverController;
    private TTSController _ttsController;

    private boolean _isInitialized;

    private ServerThread _serverThread = null;

    private BatterySocketController _batterySocketController;

    private BirthdayUpdater _birthdayUpdater;
    private CalendarViewUpdater _calendarViewUpdater;
    private CurrentWeatherUpdater _currentWeatherUpdater;
    private DateViewUpdater _dateViewUpdater;
    private ForecastWeatherUpdater _forecastWeatherUpdater;
    private IpAddressViewUpdater _ipAddressViewUpdater;
    private MenuListUpdater _menuListUpdater;
    private RSSViewUpdater _rssViewUpdater;
    private ShoppingListUpdater _shoppingListUpdater;
    private SocketListUpdater _socketListUpdater;
    private TemperatureUpdater _temperatureUpdater;

    private ScheduleService _scheduleService;

    private PowerManager _powerManager;
    private PowerManager.WakeLock _wakeLock;

    private WifiManager _wifiManager;
    private WifiLock _wifiLock;

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _birthdayUpdater.DownloadBirthdays();
            _currentWeatherUpdater.DownloadWeather();
            _dateViewUpdater.UpdateDate();
            _forecastWeatherUpdater.DownloadWeather();
            _ipAddressViewUpdater.GetCurrentLocalIpAddress();
            _menuListUpdater.DownloadMenuList();
            _rssViewUpdater.LoadRss();
            _shoppingListUpdater.DownloadShoppingList();
            _socketListUpdater.DownloadSocketList();
            _temperatureUpdater.DownloadTemperature();
        }
    };

    private final BroadcastReceiver _timeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_CHANGED)) {
                checkCurrentTime();
            } else if (action.equals(Intent.ACTION_DATE_CHANGED)) {
                _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_BIRTHDAY_UPDATE);
            }
        }
    };

    /*private Runnable _checkForUpdateRunnable = new Runnable() {
        public void run() {
            _logger.Debug("_checkForUpdateRunnable run");
            checkForApplicationUpdate();
        }
    };*/

    @Override
    public void onCreate() {
        super.onCreate();

        if (!_isInitialized) {
            _isInitialized = true;

            _logger = new SmartMirrorLogger(TAG);

            _context = this;

            _broadcastController = new BroadcastController(_context);
            //_mediaMirrorFTPController = new MediaMirrorFTPController(_context);
            _receiverController = new ReceiverController(_context);

            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_timeChangedReceiver, new String[]{Intent.ACTION_TIME_TICK, Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_TIME_CHANGED});

            _batterySocketController = new BatterySocketController(_context);

            _birthdayUpdater = new BirthdayUpdater(_context);
            _calendarViewUpdater = new CalendarViewUpdater(_context);
            _currentWeatherUpdater = new CurrentWeatherUpdater(_context);
            _dateViewUpdater = new DateViewUpdater(_context);
            _forecastWeatherUpdater = new ForecastWeatherUpdater(_context);
            _ipAddressViewUpdater = new IpAddressViewUpdater(_context);
            _mediaVolumeController = MediaVolumeController.getInstance();
            _menuListUpdater = new MenuListUpdater(_context);
            _rssViewUpdater = new RSSViewUpdater(_context);
            _shoppingListUpdater = new ShoppingListUpdater(_context);
            _socketListUpdater = new SocketListUpdater(_context);
            _temperatureUpdater = new TemperatureUpdater(_context);

            _ttsController = new TTSController(_context, Enables.TTS);
            _ttsController.Init();

            _scheduleService = ScheduleService.getInstance();

            _serverThread = new ServerThread(Constants.SERVER_PORT, _context);
            _serverThread.Start();

            _batterySocketController.Start();
            _birthdayUpdater.Start(Timeouts.BIRTHDAY_UPDATE);
            _calendarViewUpdater.Start(Timeouts.CALENDAR_UPDATE);
            _currentWeatherUpdater.Start(Timeouts.CURRENT_WEATHER_UPDATE);
            _dateViewUpdater.Start();
            _forecastWeatherUpdater.Start(Timeouts.FORECAST_WEATHER_UPDATE);
            _ipAddressViewUpdater.Start(Timeouts.IP_ADDRESS_UPDATE);
            _menuListUpdater.Start(Timeouts.MENU_UPDATE);
            _rssViewUpdater.Start(Timeouts.RSS_UPDATE);
            _shoppingListUpdater.Start(Timeouts.SHOPPING_LIST_UPDATE);
            _socketListUpdater.Start(Timeouts.SOCKET_LIST_UPDATE);
            _temperatureUpdater.Start(Timeouts.TEMPERATURE_UPDATE);

            /*_scheduleService.DeleteSchedule("CheckForUpdateRunnable");
            _scheduleService.AddSchedule(
                    "CheckForUpdateRunnable",
                    _checkForUpdateRunnable,
                    Timeouts.CHECK_FOR_UPDATE,
                    true);*/

            CenterModel centerModel = new CenterModel(
                    false,
                    "",
                    true,
                    YoutubeId.THE_GOOD_LIFE_STREAM.GetYoutubeId(),
                    false,
                    "");
            _broadcastController.SendSerializableBroadcast(
                    Broadcasts.SHOW_CENTER_MODEL,
                    Bundles.CENTER_MODEL,
                    centerModel);

            RSSModel rssModel = new RSSModel(RSSFeed.DEFAULT, true);
            _broadcastController.SendSerializableBroadcast(
                    Broadcasts.SHOW_RSS_DATA_MODEL,
                    Bundles.RSS_DATA_MODEL,
                    rssModel);

            setLocks();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (_logger != null) {
            _logger.Debug("onStartCommand");
        }

        if (_isInitialized) {
            if (_batterySocketController != null) {
                _batterySocketController.Start();
            }

            if (_birthdayUpdater != null) {
                _birthdayUpdater.Start(Timeouts.BIRTHDAY_UPDATE);
            }

            if (_calendarViewUpdater != null) {
                _calendarViewUpdater.Start(Timeouts.CALENDAR_UPDATE);
            }

            if (_currentWeatherUpdater != null) {
                _currentWeatherUpdater.Start(Timeouts.CURRENT_WEATHER_UPDATE);
            }

            if (_dateViewUpdater != null) {
                _dateViewUpdater.Start();
            }

            if (_forecastWeatherUpdater != null) {
                _forecastWeatherUpdater.Start(Timeouts.FORECAST_WEATHER_UPDATE);
            }

            if (_ipAddressViewUpdater != null) {
                _ipAddressViewUpdater.Start(Timeouts.CHECK_FOR_UPDATE);
            }

            if (_menuListUpdater != null) {
                _menuListUpdater.Start(Timeouts.MENU_UPDATE);
            }

            if (_rssViewUpdater != null) {
                _rssViewUpdater.Start(Timeouts.RSS_UPDATE);
            }

            if (_shoppingListUpdater != null) {
                _shoppingListUpdater.Start(Timeouts.SHOPPING_LIST_UPDATE);
            }

            if (_socketListUpdater != null) {
                _socketListUpdater.Start(Timeouts.SOCKET_LIST_UPDATE);
            }

            if (_temperatureUpdater != null) {
                _temperatureUpdater.Start(Timeouts.TEMPERATURE_UPDATE);
            }

            /*if (_scheduleService != null) {
                _scheduleService.DeleteSchedule("CheckForUpdateRunnable");
                _scheduleService.AddSchedule(
                        "CheckForUpdateRunnable",
                        _checkForUpdateRunnable,
                        Timeouts.CHECK_FOR_UPDATE,
                        true);
            }*/

            if (_broadcastController != null) {
                CenterModel centerModel = new CenterModel(
                        false,
                        "",
                        true,
                        YoutubeId.THE_GOOD_LIFE_STREAM.GetYoutubeId(),
                        false,
                        "");

                _broadcastController.SendSerializableBroadcast(
                        Broadcasts.SHOW_CENTER_MODEL,
                        Bundles.CENTER_MODEL,
                        centerModel);

                _broadcastController.SendSerializableBroadcast(
                        Broadcasts.SHOW_RSS_DATA_MODEL,
                        Bundles.RSS_DATA_MODEL,
                        new RSSModel(RSSFeed.DEFAULT, true));
            }

            setLocks();
        }

        return Service.START_STICKY;
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

        _receiverController.Dispose();
        _serverThread.Dispose();

        _batterySocketController.Dispose();

        _birthdayUpdater.Dispose();
        _calendarViewUpdater.Dispose();
        _currentWeatherUpdater.Dispose();
        _dateViewUpdater.Dispose();
        _forecastWeatherUpdater.Dispose();
        _ipAddressViewUpdater.Dispose();
        _menuListUpdater.Dispose();
        _rssViewUpdater.Dispose();
        _shoppingListUpdater.Dispose();
        _socketListUpdater.Dispose();
        _temperatureUpdater.Dispose();

        _ttsController.Dispose();

        _wakeLock.release();
        _wifiLock.release();

        _scheduleService.Dispose();

        restartActivity();
    }

    private void checkCurrentTime() {
        Calendar now = Calendar.getInstance();

        int second = now.get(Calendar.SECOND);
        int minute = now.get(Calendar.MINUTE);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int weekday = now.get(Calendar.DAY_OF_WEEK);

        boolean isWeekend = weekday == Calendar.SUNDAY || weekday == Calendar.SATURDAY;

        _logger.Debug(String.format(Locale.GERMAN,
                "checkCurrentTime at %02d:%02d:%02d on %d",
                hour, minute, second, weekday));

        if ((second >= 0 && second < 5) && minute == 0) {
            for (AutomaticSettings entry : AutomaticSettings.values()) {
                if (entry.GetEnableHour() == hour) {
                    if ((entry.IsWeekend() && isWeekend) || (!entry.IsWeekend() && !isWeekend)) {
                        int maxVolume = _mediaVolumeController.GetMaxVolume();
                        double currentVolume = (entry.GetSoundVolumePercentage() / 100) * maxVolume;
                        setSettings(currentVolume, (int) entry.GetScreenBrightnessPercentage());
                        break;
                    }
                }
            }
        }
    }

    /*private void checkForApplicationUpdate() {
        _logger.Debug("checkForApplicationUpdate");
        if (_mediaMirrorFTPController.IsUpdateAvailable()) {
            Toasty.info(_context, "Update available", Toast.LENGTH_LONG).show();
        }
    }*/

    private void restartActivity() {
        _logger.Info("Restarting activity Main.class!");
        Toasty.info(_context, "Restarting activity Main.class!", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(_context, Main.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    private void setLocks() {
        _logger.Debug("setLocks");

        if (_powerManager == null) {
            _powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        }
        _wakeLock = _powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "MediaServerMainService_WakeLock");
        _wakeLock.acquire();

        if (_wifiManager == null) {
            _wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        }
        _wifiLock = _wifiManager.createWifiLock(
                WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                "MediaServerMainService_WifiLock");
        _wifiLock.acquire();
    }

    private void setSettings(double currentVolume, int screenBrightness) {
        _mediaVolumeController.SetVolume((int) currentVolume);
        _broadcastController.SendIntBroadcast(
                Broadcasts.VALUE_SCREEN_BRIGHTNESS,
                Bundles.SCREEN_BRIGHTNESS,
                screenBrightness);
    }
}