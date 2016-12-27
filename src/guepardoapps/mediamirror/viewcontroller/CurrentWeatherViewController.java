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
import guepardoapps.mediamirror.test.CurrentWeatherViewControllerTest;
import guepardoapps.mediamirror.R;

import guepardoapps.toolset.controller.ReceiverController;

public class CurrentWeatherViewController {

	private static final String TAG = CurrentWeatherViewController.class.getName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;

	private Context _context;
	private ReceiverController _receiverController;

	private TextView _conditionTextView;
	private TextView _temperatureTextView;
	private TextView _humidityTextView;
	private TextView _pressureTextView;
	private TextView _updatedTimeTextView;
	private ImageView _conditionImageView;

	private CurrentWeatherViewControllerTest _currentWeatherViewTest;

	public CurrentWeatherViewController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_conditionTextView = (TextView) ((Activity) _context).findViewById(R.id.weatherConditionTextView);
		_temperatureTextView = (TextView) ((Activity) _context).findViewById(R.id.weatherTemperatureTextView);
		_humidityTextView = (TextView) ((Activity) _context).findViewById(R.id.weatherHumidityTextView);
		_pressureTextView = (TextView) ((Activity) _context).findViewById(R.id.weatherPressureTextView);
		_updatedTimeTextView = (TextView) ((Activity) _context).findViewById(R.id.weatherUpdateTextView);
		_conditionImageView = (ImageView) ((Activity) _context).findViewById(R.id.weatherConditionImageView);
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_receiverController.RegisterReceiver(_updateViewReceiver,
					new String[] { Constants.BROADCAST_SHOW_CURRENT_WEATHER_MODEL });
			_isInitialized = true;
			_logger.Debug("Initializing!");

			if (Constants.TESTING_ENABLED) {
				if (_currentWeatherViewTest == null) {
					_currentWeatherViewTest = new CurrentWeatherViewControllerTest(_context);
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
			CurrentWeatherModel model = (CurrentWeatherModel) intent
					.getSerializableExtra(Constants.BUNDLE_CURRENT_WEATHER_MODEL);
			if (model != null) {
				_logger.Debug(model.toString());
				_conditionTextView.setText(model.GetCondition());
				_temperatureTextView.setText(model.GetTemperature());
				_humidityTextView.setText(model.GetHumiditiy());
				_pressureTextView.setText(model.GetPressure());
				_updatedTimeTextView.setText(model.GetUpdatedTime());
				_conditionImageView.setImageResource(model.GetImageId());
			} else {
				_logger.Warn("model is null!");
			}

			if (Constants.TESTING_ENABLED) {
				_currentWeatherViewTest.ValidateView(_conditionTextView.getText().toString(),
						_temperatureTextView.getText().toString(), _humidityTextView.getText().toString(),
						_pressureTextView.getText().toString(), _updatedTimeTextView.getText().toString(), -1);
			}
		}
	};
}
