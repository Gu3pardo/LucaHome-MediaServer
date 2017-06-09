package guepardoapps.mediamirror.view.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.library.verticalseekbarview.VerticalSeekBarView;
import guepardoapps.library.verticalseekbarview.enums.VerticalSeekBarStyle;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.controller.MediaMirrorDialogController;
import guepardoapps.mediamirror.controller.MediaVolumeController;
import guepardoapps.mediamirror.view.model.IpAddressModel;

public class InfoViewController {

    private static final String TAG = InfoViewController.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private static final int BAT_LVL_LOW = 15;
    private static final int BAT_LVL_CRITICAL = 5;

    private static final long LOOP_INTERVAL = 250;

    private boolean _isInitialized;
    private boolean _screenEnabled;
    private boolean _setVolumeEnabled = true;

    private int _maxVolume;

    private String _updateFilePath = null;

    private Context _context;
    private MediaMirrorDialogController _dialogController;
    private MediaVolumeController _mediaVolumeController;
    private ReceiverController _receiverController;

    private View _batteryAlarmView;
    private TextView _batteryValueTextView;

    private TextView _ipAddressTextView;

    private TextView _volumeValueTextView;
    private VerticalSeekBarView _volumeControl;

    private ImageView _updateAvailableView;

    private BroadcastReceiver _batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            _batteryValueTextView.setText(String.format("%s%%", level));

            if (level > BAT_LVL_LOW) {
                _batteryAlarmView.setBackgroundResource(R.drawable.circle_green);
            } else if (level <= BAT_LVL_LOW && level > BAT_LVL_CRITICAL) {
                _batteryAlarmView.setBackgroundResource(R.drawable.circle_yellow);
            } else {
                _batteryAlarmView.setBackgroundResource(R.drawable.circle_red);
            }
        }
    };

    private BroadcastReceiver _downloadUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String downloadedFilePath = intent.getStringExtra(Bundles.FILE_PATH);
            if (downloadedFilePath.isEmpty()
                    || downloadedFilePath.length() == 0) {
                _updateAvailableView.setVisibility(View.GONE);
                _updateFilePath = null;
            } else {
                _logger.Info(String.format(Locale.GERMAN, "Update available at path %s", downloadedFilePath));
                _updateAvailableView.setVisibility(View.VISIBLE);
                _updateFilePath = downloadedFilePath;
            }
        }
    };

    private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = false;
        }
    };

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initializeView();
        }
    };

    private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_updateViewReceiver onReceive");
            IpAddressModel model = (IpAddressModel) intent.getSerializableExtra(Bundles.IP_ADDRESS_MODEL);

            if (model != null) {
                _logger.Debug(model.toString());
                if (model.GetIsVisible()) {
                    _ipAddressTextView.setVisibility(View.VISIBLE);
                    _ipAddressTextView.setText(model.GetIpAddress());
                } else {
                    _ipAddressTextView.setVisibility(View.INVISIBLE);
                }
            } else {
                _logger.Warn("model is null!");
            }
        }
    };

    private BroadcastReceiver _volumeInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_volumeInfoReceiver onReceive");
            String newVolumeText = intent.getStringExtra(Bundles.VOLUME_MODEL);

            if (newVolumeText != null) {
                _logger.Debug("newVolumeText: " + newVolumeText);
                _volumeValueTextView.setText(String.format(Locale.GERMAN, "Vol.: %s", newVolumeText));

                if (!newVolumeText.contains("mute")) {
                    int currentVolume = -1;
                    try {
                        currentVolume = Integer.parseInt(newVolumeText);
                        _logger.Debug("currentVolume: " + String.valueOf(currentVolume));
                        _maxVolume = _mediaVolumeController.GetMaxVolume();

                        _logger.Debug("_maxVolume is: " + String.valueOf(_maxVolume));
                        int percentageY = (currentVolume * 100) / _maxVolume;

                        _setVolumeEnabled = false;
                        _volumeControl.SetPositionY(percentageY);
                        _setVolumeEnabled = true;
                    } catch (Exception ex) {
                        _logger.Error(ex.toString());
                    } finally {
                        _logger.Debug("Setting _mediaVolumeController currentVolume to: " + currentVolume);
                        _mediaVolumeController.SetCurrentVolume(currentVolume);
                    }
                }
            }
        }
    };

    public InfoViewController(@NonNull Context context) {
        _logger = new SmartMirrorLogger(TAG);
        _context = context;
        _dialogController = new MediaMirrorDialogController(_context);
        _mediaVolumeController = MediaVolumeController.getInstance();
        _receiverController = new ReceiverController(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");
        initializeView();
    }

    public void onPause() {
        _logger.Debug("onPause");
    }

    public void onResume() {
        _logger.Debug("onResume");

        if (!_isInitialized) {
            _logger.Debug("Initializing!");

            _receiverController.RegisterReceiver(_batteryInfoReceiver, new String[]{Intent.ACTION_BATTERY_CHANGED});
            _receiverController.RegisterReceiver(_downloadUpdateFinishedReceiver, new String[]{Broadcasts.FTP_FILE_UPDATE_DOWNLOAD_FINISHED});
            _receiverController.RegisterReceiver(_screenDisableReceiver, new String[]{Broadcasts.SCREEN_OFF});
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_updateViewReceiver, new String[]{Broadcasts.SHOW_IP_ADDRESS_MODEL});
            _receiverController.RegisterReceiver(_volumeInfoReceiver, new String[]{Broadcasts.SHOW_VOLUME_MODEL});

            _mediaVolumeController.Initialize(_context);

            _isInitialized = true;
        } else {
            _logger.Warn("Is ALREADY initialized!");
        }
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
        _dialogController.Dispose();
        _receiverController.Dispose();
        _mediaVolumeController.Dispose();
        _isInitialized = false;
    }

    public void ShowUpdateAvailableDialog(@NonNull View view) {
        _logger.Debug(String.format(Locale.GERMAN, "ShowUpdateAvailableDialog at view: %s", view));
        if (_updateFilePath == null) {
            _logger.Error("UpdateFilePath is null!");
            return;
        }

        _dialogController.ShowUpdateApkDialog(_updateFilePath);
    }

    private void initializeView() {
        _logger.Debug("initializeView");

        _screenEnabled = true;

        _batteryAlarmView = ((Activity) _context).findViewById(R.id.batteryAlarm);
        _batteryValueTextView = (TextView) ((Activity) _context).findViewById(R.id.batteryTextView);

        _ipAddressTextView = (TextView) ((Activity) _context).findViewById(R.id.ipAddressTextView);

        _maxVolume = _mediaVolumeController.GetMaxVolume();

        _volumeValueTextView = (TextView) ((Activity) _context).findViewById(R.id.volumeTextView);
        _volumeValueTextView.setText(String.valueOf(_mediaVolumeController.GetCurrentVolume()));
        _volumeControl = (VerticalSeekBarView) ((Activity) _context).findViewById(R.id.volumeSlider);
        _volumeControl.SetStyle(VerticalSeekBarStyle.VOLUME_SLIDER);
        _volumeControl.setOnVerticalSeebarMoveListener(volumePercentage -> {
            _logger.Debug(String.format("VolumePercentage: %s", volumePercentage));
            if (volumePercentage < 0) {
                volumePercentage *= -1;
            }

            if (!_setVolumeEnabled) {
                _logger.Warn("VolumeControl is disabled!");
                return;
            }

            _mediaVolumeController.SetVolume((_maxVolume * volumePercentage / 100));
        }, LOOP_INTERVAL);

        TextView serverVersionTextView = (TextView) ((Activity) _context).findViewById(R.id.serverVersionTextView);
        String version;
        try {
            PackageInfo packageInfo = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            _logger.Error(e.toString());
            version = "Error loading version...";
        }
        serverVersionTextView.setText(version);

        _updateAvailableView = (ImageView) ((Activity) _context).findViewById(R.id.updateAvailableView);
    }
}
