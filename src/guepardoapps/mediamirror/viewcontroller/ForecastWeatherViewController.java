package guepardoapps.mediamirror.viewcontroller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.model.*;
import guepardoapps.mediamirror.test.ForecastWeatherViewControllerTest;
import guepardoapps.mediamirror.R;

import guepardoapps.toolset.controller.ReceiverController;

public class ForecastWeatherViewController {

	private static final String TAG = ForecastWeatherViewController.class.getName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;
	private static int _forecastCount = 3;

	private Context _context;
	private ReceiverController _receiverController;

	private ImageView[] _weatherForecastConditionImageViews;
	private TextView[] _weatherForecastWeekdayTextViews;
	private TextView[] _weatherForecastDateTextViews;
	private TextView[] _weatherForecastTimeTextViews;
	private TextView[] _weatherForecastTemperatureRangeTextViews;

	private ForecastWeatherViewControllerTest _forecastWeatherViewTest;

	public ForecastWeatherViewController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_weatherForecastConditionImageViews = new ImageView[_forecastCount];
		_weatherForecastWeekdayTextViews = new TextView[_forecastCount];
		_weatherForecastDateTextViews = new TextView[_forecastCount];
		_weatherForecastTimeTextViews = new TextView[_forecastCount];
		_weatherForecastTemperatureRangeTextViews = new TextView[_forecastCount];

		_weatherForecastConditionImageViews[0] = (ImageView) ((Activity) _context)
				.findViewById(R.id.weatherForecast1Condition);
		_weatherForecastWeekdayTextViews[0] = (TextView) ((Activity) _context)
				.findViewById(R.id.weatherForecast1Weekday);
		_weatherForecastDateTextViews[0] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast1Date);
		_weatherForecastTimeTextViews[0] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast1Time);
		_weatherForecastTemperatureRangeTextViews[0] = (TextView) ((Activity) _context)
				.findViewById(R.id.weatherForecast1TemperatureRange);

		_weatherForecastConditionImageViews[1] = (ImageView) ((Activity) _context)
				.findViewById(R.id.weatherForecast2Condition);
		_weatherForecastWeekdayTextViews[1] = (TextView) ((Activity) _context)
				.findViewById(R.id.weatherForecast2Weekday);
		_weatherForecastDateTextViews[1] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast2Date);
		_weatherForecastTimeTextViews[1] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast2Time);
		_weatherForecastTemperatureRangeTextViews[1] = (TextView) ((Activity) _context)
				.findViewById(R.id.weatherForecast2TemperatureRange);

		_weatherForecastConditionImageViews[2] = (ImageView) ((Activity) _context)
				.findViewById(R.id.weatherForecast3Condition);
		_weatherForecastWeekdayTextViews[2] = (TextView) ((Activity) _context)
				.findViewById(R.id.weatherForecast3Weekday);
		_weatherForecastDateTextViews[2] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast3Date);
		_weatherForecastTimeTextViews[2] = (TextView) ((Activity) _context).findViewById(R.id.weatherForecast3Time);
		_weatherForecastTemperatureRangeTextViews[2] = (TextView) ((Activity) _context)
				.findViewById(R.id.weatherForecast3TemperatureRange);
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_receiverController.RegisterReceiver(_updateViewReceiver,
					new String[] { Constants.BROADCAST_SHOW_FORECAST_WEATHER_MODEL });
			_isInitialized = true;
			_logger.Debug("Initializing!");

			if (Constants.TESTING_ENABLED) {
				if (_forecastWeatherViewTest == null) {
					_forecastWeatherViewTest = new ForecastWeatherViewControllerTest(_context);
				}
			}
		} else {
			_logger.Warn("Is ALREADY initialized!");
		}
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
		_receiverController.UnregisterReceiver(_updateViewReceiver);
		_isInitialized = false;
	}

	private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateViewReceiver onReceive");
			ForecastWeatherModel model = (ForecastWeatherModel) intent
					.getSerializableExtra(Constants.BUNDLE_FORECAST_WEATHER_MODEL);
			if (model != null) {
				_logger.Debug(model.toString());
				if (model.GetForecasts().size() != _forecastCount) {
					_logger.Error("Forecast has the wrong size: " + String.valueOf(model.GetForecasts().size()));
				} else {
					for (int index = 0; index < _forecastCount; index++) {
						_weatherForecastConditionImageViews[index]
								.setImageResource(model.GetForecast(index).GetImageId());
						_weatherForecastWeekdayTextViews[index].setText(model.GetForecast(index).GetWeekday());
						_weatherForecastDateTextViews[index].setText(model.GetForecast(index).GetDate());
						_weatherForecastTimeTextViews[index].setText(model.GetForecast(index).GetTime());
						_weatherForecastTemperatureRangeTextViews[index]
								.setText(model.GetForecast(index).GetTemperatureRange());
					}
				}
			} else {
				_logger.Warn("model is null!");
			}
			
			if (Constants.TESTING_ENABLED) {
				_forecastWeatherViewTest.ValidateView(-1, _weatherForecastWeekdayTextViews[0].getText().toString(),
						_weatherForecastDateTextViews[0].getText().toString(),
						_weatherForecastTimeTextViews[0].getText().toString(),
						_weatherForecastTemperatureRangeTextViews[0].getText().toString(), -1,
						_weatherForecastWeekdayTextViews[1].getText().toString(),
						_weatherForecastDateTextViews[1].getText().toString(),
						_weatherForecastTimeTextViews[1].getText().toString(),
						_weatherForecastTemperatureRangeTextViews[1].getText().toString(), -1,
						_weatherForecastWeekdayTextViews[2].getText().toString(),
						_weatherForecastDateTextViews[2].getText().toString(),
						_weatherForecastTimeTextViews[2].getText().toString(),
						_weatherForecastTemperatureRangeTextViews[2].getText().toString());
			}
		}
	};
}