package guepardoapps.mediamirror.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import guepardoapps.library.toolset.controller.DisplayController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;

public class ScreenController extends DisplayController {

	private static final String TAG = ScreenController.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private static final int BRIGHTNESS_CHANGE_STEP = 25;

	public static final int INCREASE = 1;
	public static final int DECREASE = -1;

	private boolean _isInitialized;

	private Context _context;
	private ReceiverController _receiverController;

	private BroadcastReceiver _actionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_actionReceiver onReceive");
			int action = intent.getIntExtra(Bundles.SCREEN_BRIGHTNESS, 0);
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
			int value = intent.getIntExtra(Bundles.SCREEN_BRIGHTNESS, -1);
			if (value != -1) {
				SetBrightness(value);
			}
		}
	};

	public ScreenController(Context context) {
		super(context);
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
			_receiverController.RegisterReceiver(_actionReceiver, new String[] { Broadcasts.ACTION_SCREEN_BRIGHTNESS });
			_receiverController.RegisterReceiver(_valueReceiver, new String[] { Broadcasts.VALUE_SCREEN_BRIGHTNESS });
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

		int currentBrightness = GetCurrentBrightness();
		if (currentBrightness == -1) {
			_logger.Warn("Failed to get current brightness!");
			return;
		}
		int newBrightness = currentBrightness + BRIGHTNESS_CHANGE_STEP;

		SetBrightness(newBrightness);
	}

	public void decreaseBrightness() {
		_logger.Debug("DecreaseBrightness");

		int currentBrightness = GetCurrentBrightness();
		if (currentBrightness == -1) {
			_logger.Warn("Failed to get current brightness!");
			return;
		}
		int newBrightness = currentBrightness - BRIGHTNESS_CHANGE_STEP;

		SetBrightness(newBrightness);
	}
}
