package guepardoapps.mediamirror.view.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.model.*;
import guepardoapps.mediamirror.model.helper.RaspberryTemperatureHelper;
import guepardoapps.mediamirror.test.RaspberryViewControllerTest;
import guepardoapps.mediamirror.R;

import guepardoapps.toolset.controller.ReceiverController;

public class RaspberryViewController {

	private static final String TAG = RaspberryViewController.class.getName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;
	private boolean _screenEnabled;

	private Context _context;
	private ReceiverController _receiverController;

	private View _raspberryAlarm1TextView;
	private TextView _raspberryName1TextView;
	private TextView _raspberryTemperature1TextView;

	private RaspberryTemperatureHelper _raspberryTemperatureHelper;
	private RaspberryViewControllerTest _raspberryViewTest;

	public RaspberryViewController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
		_receiverController = new ReceiverController(_context);
		_raspberryTemperatureHelper = new RaspberryTemperatureHelper();
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_screenEnabled = true;

		_raspberryAlarm1TextView = (View) ((Activity) _context).findViewById(R.id.temperatureRaspberry1Alarm);
		_raspberryName1TextView = (TextView) ((Activity) _context).findViewById(R.id.temperatureRaspberry1Name);
		_raspberryTemperature1TextView = (TextView) ((Activity) _context).findViewById(R.id.temperatureRaspberry1Value);
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_receiverController.RegisterReceiver(_updateViewReceiver,
					new String[] { Constants.BROADCAST_SHOW_RASPBERRY_DATA_MODEL });
			_receiverController.RegisterReceiver(_screenEnableReceiver,
					new String[] { Constants.BROADCAST_SCREEN_ENABLED });
			_receiverController.RegisterReceiver(_screenDisableReceiver,
					new String[] { Constants.BROADCAST_SCREEN_OFF, Constants.BROADCAST_SCREEN_SAVER });
			_isInitialized = true;
			_logger.Debug("Initializing!");

			if (Constants.TESTING_ENABLED) {
				if (_raspberryViewTest == null) {
					_raspberryViewTest = new RaspberryViewControllerTest(_context);
				}
			}
		} else {
			_logger.Warn("Is ALREADY initialized!");
		}
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
		_receiverController.UnregisterReceiver(_updateViewReceiver);
		_receiverController.UnregisterReceiver(_screenEnableReceiver);
		_receiverController.UnregisterReceiver(_screenDisableReceiver);
		_isInitialized = false;
	}

	private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_logger.Debug("_updateViewReceiver onReceive");
			RaspberryModel model = (RaspberryModel) intent.getSerializableExtra(Constants.BUNDLE_RASPBERRY_DATA_MODEL);
			if (model != null) {
				_logger.Debug(model.toString());

				_raspberryAlarm1TextView
						.setBackgroundResource(_raspberryTemperatureHelper.GetIcon(model.GetRaspberry1Temperature()));
				_raspberryName1TextView.setText(model.GetRaspberry1Name());
				_raspberryTemperature1TextView.setText(model.GetRaspberry1Temperature());
			} else {
				_logger.Warn("model is null!");
			}

			if (Constants.TESTING_ENABLED) {
				_raspberryViewTest.ValidateView(_raspberryName1TextView.getText().toString(),
						_raspberryTemperature1TextView.getText().toString());
			}
		}
	};

	private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = true;

			_raspberryAlarm1TextView = (View) ((Activity) _context).findViewById(R.id.temperatureRaspberry1Alarm);
			_raspberryName1TextView = (TextView) ((Activity) _context).findViewById(R.id.temperatureRaspberry1Name);
			_raspberryTemperature1TextView = (TextView) ((Activity) _context).findViewById(R.id.temperatureRaspberry1Value);
		}
	};

	private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = false;
		}
	};
}
