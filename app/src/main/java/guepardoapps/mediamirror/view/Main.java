package guepardoapps.mediamirror.view;

import com.google.android.youtube.player.YouTubeBaseActivity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.PermissionController;
import guepardoapps.library.toolset.controller.SharedPrefController;
import guepardoapps.library.toolset.controller.TTSController;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Constants;
import guepardoapps.mediamirror.common.constants.Enables;
import guepardoapps.mediamirror.common.constants.RaspPiConstants;
import guepardoapps.mediamirror.controller.ScreenController;
import guepardoapps.mediamirror.services.*;
import guepardoapps.mediamirror.view.controller.*;

public class Main extends YouTubeBaseActivity {

    private static final String TAG = Main.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private Context _context;
    private BroadcastController _broadcastController;

    private CenterViewController _centerViewController;
    private CurrentWeatherViewController _currentWeatherViewController;
    private DateViewController _dateViewController;
    private EventViewController _eventViewController;
    private ForecastWeatherViewController _forecastWeatherViewController;
    private GameViewController _gameViewController;
    private InfoViewController _infoViewController;
    private LayoutController _layoutController;
    private RaspberryViewController _raspberryViewController;
    private RSSViewController _rssViewController;

    private ScreenController _screenController;

    private TTSController _ttsController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _logger = new SmartMirrorLogger(TAG);
        _logger.Debug("onCreate");

        _context = this;
        _broadcastController = new BroadcastController(_context);

        install();

        PermissionController permissionController = new PermissionController(_context);
        permissionController.CheckPermissions(
                Constants.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE_ID,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        permissionController.CheckPermissions(
                Constants.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_ID,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionController.CheckPermissions(
                Constants.PERMISSION_REQUEST_WRITE_SETTINGS_ID,
                Manifest.permission.WRITE_SETTINGS);

        initializeController();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main_touch);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        // | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        _centerViewController.onCreate(_context);
        _currentWeatherViewController.onCreate();
        _dateViewController.onCreate();
        _eventViewController.onCreate();
        _forecastWeatherViewController.onCreate();
        _gameViewController.onCreate();
        _infoViewController.onCreate();
        _layoutController.onCreate();
        _raspberryViewController.onCreate();
        _rssViewController.onCreate();

        _screenController.onCreate();

        _ttsController.Init();
    }

    private void install() {
        _logger.Debug("install");
        SharedPrefController sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);

        if (!sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.SHARED_PREF_INSTALLED)) {
            _logger.Info("Installing shared preferences!");

            sharedPrefController.SaveStringValue(SharedPrefConstants.USER_NAME, RaspPiConstants.USER);
            sharedPrefController.SaveStringValue(SharedPrefConstants.USER_PASSPHRASE, RaspPiConstants.PASSWORD);
            sharedPrefController.SaveBooleanValue(SharedPrefConstants.SHARED_PREF_INSTALLED, true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        _logger.Debug("onResume");

        _centerViewController.onResume();
        _currentWeatherViewController.onResume();
        _dateViewController.onResume();
        _eventViewController.onResume();
        _forecastWeatherViewController.onResume();
        _gameViewController.onResume();
        _infoViewController.onResume();
        _layoutController.onResume();
        _raspberryViewController.onResume();
        _rssViewController.onResume();

        _screenController.onResume();

        startServices();
    }

    @Override
    public void onPause() {
        super.onPause();
        _logger.Debug("onPause");

        _centerViewController.onPause();
        _currentWeatherViewController.onPause();
        _dateViewController.onPause();
        _eventViewController.onPause();
        _forecastWeatherViewController.onPause();
        _gameViewController.onPause();
        _infoViewController.onPause();
        _layoutController.onPause();
        _raspberryViewController.onPause();
        _rssViewController.onPause();

        _screenController.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _logger.Debug("onDestroy");

        _centerViewController.onDestroy();
        _currentWeatherViewController.onDestroy();
        _dateViewController.onDestroy();
        _eventViewController.onDestroy();
        _forecastWeatherViewController.onDestroy();
        _gameViewController.onDestroy();
        _infoViewController.onDestroy();
        _layoutController.onDestroy();
        _raspberryViewController.onDestroy();
        _rssViewController.onDestroy();

        _screenController.onDestroy();

        _ttsController.Dispose();
    }

    @Override
    public void onRequestPermissionsResult(int callbackId, @NonNull String permissions[], @NonNull int[] grantResults) {
        _logger.Debug(String.format("onRequestPermissionsResult with id %s for permissions %s has result %s", callbackId, permissions, grantResults));
        int index = 0;
        for (String permission : permissions) {
            _logger.Info(String.format("Permission %s has been granted: %s", permission, grantResults[index]));

            switch (permission) {
                case Manifest.permission.READ_CALENDAR:
                    _broadcastController.SendIntBroadcast(
                            Broadcasts.PERMISSION_READ_CALENDAR,
                            Bundles.PERMISSION_READ_CALENDAR,
                            grantResults[index]);
                    break;

                case Manifest.permission.READ_EXTERNAL_STORAGE:
                    _broadcastController.SendIntBroadcast(
                            Broadcasts.PERMISSION_READ_EXTERNAL_STORAGE,
                            Bundles.PERMISSION_READ_EXTERNAL_STORAGE,
                            grantResults[index]);
                    break;

                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    _broadcastController.SendIntBroadcast(
                            Broadcasts.PERMISSION_WRITE_EXTERNAL_STORAGE,
                            Bundles.PERMISSION_WRITE_EXTERNAL_STORAGE,
                            grantResults[index]);
                    break;

                case Manifest.permission.WRITE_SETTINGS:
                    _broadcastController.SendIntBroadcast(
                            Broadcasts.PERMISSION_WRITE_SETTINGS,
                            Bundles.PERMISSION_WRITE_SETTINGS,
                            grantResults[index]);
                    break;
                default:
                    _logger.Info(String.format("Received request for permission %s, but this is not handled here!", permission));
                    break;
            }
            index++;
        }
    }

    public void ShowMenuListDialog(View view) {
        _raspberryViewController.ShowMenuListDialog(view);
    }

    public void ShowShoppingListDialog(View view) {
        _raspberryViewController.ShowShoppingListDialog(view);
    }

    public void ShowSocketsDialog(View view) {
        _raspberryViewController.ShowSocketsDialog(view);
    }

    public void ShowTemperatureGraph(View view) {
        _raspberryViewController.ShowTemperatureGraph(view);
    }

    public void ShowUpdateAvailableDialog(View view) {
        _infoViewController.ShowUpdateAvailableDialog(view);
    }

    private void initializeController() {
        _centerViewController = CenterViewController.getInstance();
        _currentWeatherViewController = new CurrentWeatherViewController(_context);
        _dateViewController = new DateViewController(_context);
        _eventViewController = new EventViewController(_context);
        _forecastWeatherViewController = new ForecastWeatherViewController(_context);
        _gameViewController = new GameViewController(_context);
        _infoViewController = new InfoViewController(_context);
        _layoutController = new LayoutController(_context);
        _raspberryViewController = new RaspberryViewController(_context);
        _rssViewController = new RSSViewController(_context);

        _screenController = new ScreenController(_context);

        _ttsController = new TTSController(_context, Enables.TTS);
    }

    private void startServices() {
        startService(new Intent(_context, MainService.class));
        startService(new Intent(_context, ControlServiceStateService.class));
    }
}