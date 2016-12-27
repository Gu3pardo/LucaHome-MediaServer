package guepardoapps.mediamirror.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.services.RESTService;

public class BatterySocketController {

	private static final String TAG = BatterySocketController.class.getName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;
	private boolean _activatedSocket;
	private boolean _deactivatedSocket;

	private Context _context;

	private BroadcastReceiver _batteryInfoReveicer = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			if (level > 95) {
				disableBatterySocket();
			} else if (level < 10) {
				enableBatterySocket();
			}
		}
	};

	public BatterySocketController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Info("ScreenController created");
		_context = context;
	}

	public void Start() {
		_logger.Debug("Start");
		if (!_isInitialized) {
			_logger.Debug("Initializing!");
			_context.registerReceiver(_batteryInfoReveicer, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			_isInitialized = true;
		} else {
			_logger.Warn("Is ALREADY initialized!");
		}
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_context.unregisterReceiver(_batteryInfoReveicer);
		_isInitialized = false;
	}

	private void enableBatterySocket() {
		if (_activatedSocket) {
			_logger.Warn("Already activated socket!");
			return;
		}

		_activatedSocket = true;
		_deactivatedSocket = false;

		_logger.Debug("enableBatterySocket");
		setBatterySocket(true);
	}

	private void disableBatterySocket() {
		if (_deactivatedSocket) {
			_logger.Warn("Already deactivated socket!");
			return;
		}

		_activatedSocket = false;
		_deactivatedSocket = true;

		_logger.Debug("disableBatterySocket");
		setBatterySocket(false);
	}

	private void setBatterySocket(boolean enable) {
		_logger.Debug("setBatterySocket " + Constants.SOCKET_NAME + " to "
				+ ((enable) ? Constants.SOCKET_STATE_ON : Constants.SOCKET_STATE_OFF));

		Intent serviceIntent = new Intent(_context, RESTService.class);
		Bundle serviceData = new Bundle();

		serviceData.putString(Constants.BUNDLE_REST_ACTION, Constants.ACTION_SET_SOCKET + Constants.SOCKET_NAME
				+ ((enable) ? Constants.SOCKET_STATE_ON : Constants.SOCKET_STATE_OFF));
		serviceData.putString(Constants.BUNDLE_REST_DATA, "");
		serviceData.putString(Constants.BUNDLE_REST_BROADCAST, "");

		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}
}
