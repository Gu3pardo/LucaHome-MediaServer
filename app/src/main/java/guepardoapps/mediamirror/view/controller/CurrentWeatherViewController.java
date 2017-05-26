package guepardoapps.mediamirror.view.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.view.model.*;

public class CurrentWeatherViewController {

    private static final String TAG = CurrentWeatherViewController.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private boolean _isInitialized;
    private boolean _screenEnabled;

    private Context _context;
    private ReceiverController _receiverController;

    private TextView _conditionTextView;
    private TextView _temperatureTextView;
    private TextView _humidityTextView;
    private TextView _pressureTextView;
    private TextView _updatedTimeTextView;
    private ImageView _conditionImageView;

    private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = false;
        }
    };

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = true;

            _conditionTextView = (TextView) ((Activity) _context).findViewById(R.id.weatherConditionTextView);
            _temperatureTextView = (TextView) ((Activity) _context).findViewById(R.id.weatherTemperatureTextView);
            _humidityTextView = (TextView) ((Activity) _context).findViewById(R.id.weatherHumidityTextView);
            _pressureTextView = (TextView) ((Activity) _context).findViewById(R.id.weatherPressureTextView);
            _updatedTimeTextView = (TextView) ((Activity) _context).findViewById(R.id.weatherUpdateTextView);
            _conditionImageView = (ImageView) ((Activity) _context).findViewById(R.id.weatherConditionImageView);
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
            CurrentWeatherModel model = (CurrentWeatherModel) intent.getSerializableExtra(Bundles.CURRENT_WEATHER_MODEL);

            if (model != null) {
                _logger.Debug(model.toString());
                _conditionTextView.setText(model.GetCondition());
                _temperatureTextView.setText(model.GetTemperature());
                _humidityTextView.setText(model.GetHumidity());
                _pressureTextView.setText(model.GetPressure());
                _updatedTimeTextView.setText(model.GetUpdatedTime());
                _conditionImageView.setImageResource(model.GetImageId());
            } else {
                _logger.Warn("model is null!");
            }
        }
    };

    public CurrentWeatherViewController(@NonNull Context context) {
        _logger = new SmartMirrorLogger(TAG);
        _context = context;
        _receiverController = new ReceiverController(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");

        _screenEnabled = true;

        _conditionTextView = (TextView) ((Activity) _context).findViewById(R.id.weatherConditionTextView);
        _temperatureTextView = (TextView) ((Activity) _context).findViewById(R.id.weatherTemperatureTextView);
        _humidityTextView = (TextView) ((Activity) _context).findViewById(R.id.weatherHumidityTextView);
        _pressureTextView = (TextView) ((Activity) _context).findViewById(R.id.weatherPressureTextView);
        _updatedTimeTextView = (TextView) ((Activity) _context).findViewById(R.id.weatherUpdateTextView);
        _conditionImageView = (ImageView) ((Activity) _context).findViewById(R.id.weatherConditionImageView);
    }

    public void onPause() {
        _logger.Debug("onPause");
        _receiverController.Dispose();
        _isInitialized = false;
    }

    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_screenDisableReceiver, new String[]{Broadcasts.SCREEN_OFF});
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_updateViewReceiver, new String[]{Broadcasts.SHOW_CURRENT_WEATHER_MODEL});
            _isInitialized = true;
            _logger.Debug("Initializing!");
        } else {
            _logger.Warn("Is ALREADY initialized!");
        }
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _isInitialized = false;
    }
}
