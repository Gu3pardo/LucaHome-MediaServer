package guepardoapps.mediamirror.view.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.controller.ScreenController;

public class LayoutController {

	private static final String TAG = LayoutController.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;

	private Context _context;
	private BroadcastController _broadcastController;
	private ReceiverController _receiverController;
	private ScreenController _screenController;

	private BroadcastReceiver _screenNormalReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_screenNormalReceiver onReceive");
			((Activity) _context).setContentView(R.layout.main_touch);
			_broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_ENABLED);
		}
	};

	private BroadcastReceiver _screenSaverReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_screenSaverReceiver onReceive");
			((Activity) _context).setContentView(R.layout.blackscreen);
		}
	};

	private BroadcastReceiver _screenOnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_screenOnReceiver onReceive");
			_screenController.ScreenOn(new int[] { WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
					WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD, WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
					WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON },
					new int[] { View.SYSTEM_UI_FLAG_LAYOUT_STABLE,
							// View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION,
							View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN,
							// View.SYSTEM_UI_FLAG_HIDE_NAVIGATION,
							View.SYSTEM_UI_FLAG_FULLSCREEN, View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY });
			_broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_ENABLED);
		}
	};

	private BroadcastReceiver _screenOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_screenOffReceiver onReceive");
			_screenController.ScreenOff(new int[] { WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
					WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD, WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
					WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON });
		}
	};

	public LayoutController(Context context) {
		_logger = new SmartMirrorLogger(TAG);

		_context = context;
		_broadcastController = new BroadcastController(_context);
		_receiverController = new ReceiverController(_context);
		_screenController = new ScreenController(_context);

		((Activity) _context).setContentView(R.layout.main_touch);
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
			_receiverController.RegisterReceiver(_screenNormalReceiver, new String[] { Broadcasts.SCREEN_NORMAL });
			_receiverController.RegisterReceiver(_screenSaverReceiver, new String[] { Broadcasts.SCREEN_SAVER });
			_receiverController.RegisterReceiver(_screenOnReceiver, new String[] { Broadcasts.SCREEN_ON });
			_receiverController.RegisterReceiver(_screenOffReceiver, new String[] { Broadcasts.SCREEN_OFF });
			_isInitialized = true;
		} else {
			_logger.Warn("Is ALREADY initialized!");
		}
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");

		_receiverController.UnregisterReceiver(_screenNormalReceiver);
		_receiverController.UnregisterReceiver(_screenSaverReceiver);
		_receiverController.UnregisterReceiver(_screenOnReceiver);
		_receiverController.UnregisterReceiver(_screenOffReceiver);

		_isInitialized = false;
	}
}
