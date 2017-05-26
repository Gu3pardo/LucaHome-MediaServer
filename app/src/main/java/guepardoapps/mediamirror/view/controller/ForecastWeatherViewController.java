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

public class ForecastWeatherViewController {

    private static final String TAG = ForecastWeatherViewController.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private boolean _isInitialized;
    private boolean _screenEnabled;

    private static int _forecastCount = 3;

    private Context _context;
    private ReceiverController _receiverController;

    private ImageView[] _weatherForecastConditionImageViews;
    private TextView[] _weatherForecastWeekdayTextViews;
    private TextView[] _weatherForecastDateTextViews;
    private TextView[] _weatherForecastTimeTextViews;
    private TextView[] _weatherForecastTemperatureRangeTextViews;

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

            _weatherForecastConditionImageViews = new ImageView[_forecastCount];
            _weatherForecastWeekdayTextViews = new TextView[_forecastCount];
            _weatherForecastDateTextViews = new TextView[_forecastCount];
            _weatherForecastTimeTextViews = new TextView[_forecastCount];
            _weatherForecastTemperatureRangeTextViews = new TextView[_forecastCount];

            _weatherForecastConditionImageViews[0] = (ImageView) ((Activity) _context).findViewById(R.id.weatherForecast1Condition);
            _weatherForecastWeekdayTextViews[0] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast1Weekday);
            _weatherForecastDateTextViews[0] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast1Date);
            _weatherForecastTimeTextViews[0] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast1Time);
            _weatherForecastTemperatureRangeTextViews[0] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast1TemperatureRange);

            _weatherForecastConditionImageViews[1] = (ImageView) ((Activity) _context).findViewById(R.id.weatherForecast2Condition);
            _weatherForecastWeekdayTextViews[1] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast2Weekday);
            _weatherForecastDateTextViews[1] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast2Date);
            _weatherForecastTimeTextViews[1] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast2Time);
            _weatherForecastTemperatureRangeTextViews[1] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast2TemperatureRange);

            _weatherForecastConditionImageViews[2] = (ImageView) ((Activity) _context).findViewById(R.id.weatherForecast3Condition);
            _weatherForecastWeekdayTextViews[2] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast3Weekday);
            _weatherForecastDateTextViews[2] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast3Date);
            _weatherForecastTimeTextViews[2] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast3Time);
            _weatherForecastTemperatureRangeTextViews[2] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast3TemperatureRange);
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
            ForecastWeatherModel model = (ForecastWeatherModel) intent.getSerializableExtra(Bundles.FORECAST_WEATHER_MODEL);

            if (model != null) {
                _logger.Debug(model.toString());

                if (model.GetForecasts().size() != _forecastCount) {
                    _logger.Error("Forecast has the wrong size: " + String.valueOf(model.GetForecasts().size()));
                } else {
                    for (int index = 0; index < _forecastCount; index++) {
                        _weatherForecastConditionImageViews[index].setImageResource(model.GetForecast(index).GetImageId());
                        _weatherForecastWeekdayTextViews[index].setText(model.GetForecast(index).GetWeekday());
                        _weatherForecastDateTextViews[index].setText(model.GetForecast(index).GetDate());
                        _weatherForecastTimeTextViews[index].setText(model.GetForecast(index).GetTime());
                        _weatherForecastTemperatureRangeTextViews[index].setText(model.GetForecast(index).GetTemperatureRange());
                    }
                }
            } else {
                _logger.Warn("model is null!");
            }
        }
    };

    public ForecastWeatherViewController(@NonNull Context context) {
        _logger = new SmartMirrorLogger(TAG);
        _context = context;
        _receiverController = new ReceiverController(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");

        _screenEnabled = true;

        _weatherForecastConditionImageViews = new ImageView[_forecastCount];
        _weatherForecastWeekdayTextViews = new TextView[_forecastCount];
        _weatherForecastDateTextViews = new TextView[_forecastCount];
        _weatherForecastTimeTextViews = new TextView[_forecastCount];
        _weatherForecastTemperatureRangeTextViews = new TextView[_forecastCount];

        _weatherForecastConditionImageViews[0] = (ImageView) ((Activity) _context).findViewById(R.id.weatherForecast1Condition);
        _weatherForecastWeekdayTextViews[0] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast1Weekday);
        _weatherForecastDateTextViews[0] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast1Date);
        _weatherForecastTimeTextViews[0] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast1Time);
        _weatherForecastTemperatureRangeTextViews[0] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast1TemperatureRange);

        _weatherForecastConditionImageViews[1] = (ImageView) ((Activity) _context).findViewById(R.id.weatherForecast2Condition);
        _weatherForecastWeekdayTextViews[1] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast2Weekday);
        _weatherForecastDateTextViews[1] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast2Date);
        _weatherForecastTimeTextViews[1] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast2Time);
        _weatherForecastTemperatureRangeTextViews[1] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast2TemperatureRange);

        _weatherForecastConditionImageViews[2] = (ImageView) ((Activity) _context).findViewById(R.id.weatherForecast3Condition);
        _weatherForecastWeekdayTextViews[2] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast3Weekday);
        _weatherForecastDateTextViews[2] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast3Date);
        _weatherForecastTimeTextViews[2] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast3Time);
        _weatherForecastTemperatureRangeTextViews[2] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast3TemperatureRange);
    }

    public void onPause() {
        _logger.Debug("onPause");
    }

    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_screenDisableReceiver, new String[]{Broadcasts.SCREEN_OFF});
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_updateViewReceiver, new String[]{Broadcasts.SHOW_FORECAST_WEATHER_MODEL});
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
