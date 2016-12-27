package guepardoapps.mediamirror.viewcontroller;

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

	private Context _context;
	private ReceiverController _receiverController;

	private View _raspberryAlarm1TextView;
	private View _raspberryAlarm2TextView;
	private TextView _raspberryName1TextView;
	private TextView _raspberryName2TextView;
	private TextView _raspberryTemperature1TextView;
	private TextView _raspberryTemperature2TextView;

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

		_raspberryAlarm1TextView = (View) ((Activity) _context).findViewById(R.id.temperatureRaspberry1Alarm);
		_raspberryAlarm2TextView = (View) ((Activity) _context).findViewById(R.id.temperatureRaspberry2Alarm);
		_raspberryName1TextView = (TextView) ((Activity) _context).findViewById(R.id.temperatureRaspberry1Name);
		_raspberryName2TextView = (TextView) ((Activity) _context).findViewById(R.id.temperatureRaspberry2Name);
		_raspberryTemperature1TextView = (TextView) ((Activity) _context).findViewById(R.id.temperatureRaspberry1Value);
		_raspberryTemperature2TextView = (TextView) ((Activity) _context).findViewById(R.id.temperatureRaspberry2Value);
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_receiverController.RegisterReceiver(_updateViewReceiver,
					new String[] { Constants.BROADCAST_SHOW_RASPBERRY_DATA_MODEL });
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
		_isInitialized = false;
	}

	private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateViewReceiver onReceive");
			RaspberryModel model = (RaspberryModel) intent.getSerializableExtra(Constants.BUNDLE_RASPBERRY_DATA_MODEL);
			if (model != null) {
				_logger.Debug(model.toString());

				_raspberryAlarm1TextView
						.setBackgroundResource(_raspberryTemperatureHelper.GetIcon(model.GetRaspberry1Temperature()));
				_raspberryAlarm2TextView
						.setBackgroundResource(_raspberryTemperatureHelper.GetIcon(model.GetRaspberry2Temperature()));
				_raspberryName1TextView.setText(model.GetRaspberry1Name());
				_raspberryName2TextView.setText(model.GetRaspberry2Name());
				_raspberryTemperature1TextView.setText(model.GetRaspberry1Temperature());
				_raspberryTemperature2TextView.setText(model.GetRaspberry2Temperature());
			} else {
				_logger.Warn("model is null!");
			}

			if (Constants.TESTING_ENABLED) {
				_raspberryViewTest.ValidateView(_raspberryName1TextView.getText().toString(),
						_raspberryName2TextView.getText().toString(),
						_raspberryTemperature1TextView.getText().toString(),
						_raspberryTemperature2TextView.getText().toString());
			}
		}
	};
}
