package guepardoapps.mediamirror.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;

import guepardoapps.toolset.controller.ReceiverController;

public class ScreenController {

	private static final String TAG = ScreenController.class.getName();
	private SmartMirrorLogger _logger;

	private static final int BRIGHTNESS_CHANGE_STEP = 25;
	private static final int BRIGHTNESS_MAX_LEVEL = 250;
	private static final int BRIGHTNESS_MIN_LEVEL = 25;

	public static final int INCREASE = 1;
	public static final int DECREASE = -1;

	private boolean _isInitialized;

	private Context _context;
	private ReceiverController _receiverController;

	private BroadcastReceiver _actionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_actionReceiver onReceive");
			int action = intent.getIntExtra(Constants.BUNDLE_SCREEN_BRIGHTNESS, 0);
			if (action == INCREASE) {
				increaseBrightness();
			} else if (action == DECREASE) {
				decreaseBrightness();
			} else {
				_logger.Warn("Action not supported! " + String.valueOf(action));
			}
		}
	};

	private BroadcastReceiver _valueReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_valueReceiver onReceive");
			int value = intent.getIntExtra(Constants.BUNDLE_SCREEN_BRIGHTNESS, -1);
			if (value != -1) {
				setBrighntess(value);
			}
		}
	};

	public ScreenController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Info("ScreenController created");
		_context = context;
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_logger.Debug("Initializing!");
			_receiverController.RegisterReceiver(_actionReceiver,
					new String[] { Constants.BROADCAST_ACTION_SCREEN_BRIGHTNESS });
			_receiverController.RegisterReceiver(_valueReceiver,
					new String[] { Constants.BROADCAST_VALUE_SCREEN_BRIGHTNESS });
			_isInitialized = true;
		} else {
			_logger.Warn("Is ALREADY initialized!");
		}
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
		_receiverController.UnregisterReceiver(_actionReceiver);
		_receiverController.UnregisterReceiver(_valueReceiver);
		_isInitialized = false;
	}

	public void increaseBrightness() {
		_logger.Debug("IncreaseBrightness");

		int currentBrightness = getCurrentBrightness();
		if (currentBrightness == -1) {
			_logger.Warn("Failed to get current brightness!");
			return;
		}
		int newBrightness = currentBrightness + BRIGHTNESS_CHANGE_STEP;

		setBrighntess(newBrightness);
	}

	public void decreaseBrightness() {
		_logger.Debug("DecreaseBrightness");

		int currentBrightness = getCurrentBrightness();
		if (currentBrightness == -1) {
			_logger.Warn("Failed to get current brightness!");
			return;
		}
		int newBrightness = currentBrightness - BRIGHTNESS_CHANGE_STEP;

		setBrighntess(newBrightness);
	}

	private void setBrighntess(int brightness) {
		_logger.Debug("SetBrighntess");

		if (brightness > BRIGHTNESS_MAX_LEVEL) {
			_logger.Error("Brightness to high! Set to maximum level!");
			brightness = BRIGHTNESS_MAX_LEVEL;
		}

		if (brightness < BRIGHTNESS_MIN_LEVEL) {
			_logger.Error("Brightness to low! Set to minimum level!");
			brightness = BRIGHTNESS_MIN_LEVEL;
		}

		ContentResolver contentResolver = _context.getContentResolver();
		Window window = ((Activity) _context).getWindow();

		try {
			Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
			LayoutParams layoutParams = window.getAttributes();
			layoutParams.screenBrightness = brightness / (float) 255;
			window.setAttributes(layoutParams);
		} catch (Exception e) {
			_logger.Error(e.toString());
			Toast.makeText(_context, "Failed to set brightness!", Toast.LENGTH_SHORT).show();
		}

	}

	private int getCurrentBrightness() {
		_logger.Debug("getCurrentBrightness");

		ContentResolver contentResolver = _context.getContentResolver();
		int brightness = -1;

		try {
			Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
					Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			_logger.Error(e.toString());
		}

		return brightness;
	}
}
