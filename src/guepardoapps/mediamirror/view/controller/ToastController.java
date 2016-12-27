package guepardoapps.mediamirror.view.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;

import guepardoapps.toolset.controller.ReceiverController;

public class ToastController {

	private static final String TAG = ToastController.class.getName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;

	private Context _context;
	private ReceiverController _receiverController;

	private BroadcastReceiver _toastReveicer = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_toastReveicer onReceive");
			String toastText = intent.getStringExtra(Constants.BUNDLE_TOAST_TEXT);
			if (toastText != null) {
				_logger.Debug("toastText: " + toastText);
				Toast.makeText(_context, toastText, Toast.LENGTH_LONG).show();
			} else {
				_logger.Warn("toastText is null!");
			}
		}
	};

	public ToastController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
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
			if (_receiverController == null) {
				_receiverController = new ReceiverController(_context);
			}
			_receiverController.RegisterReceiver(_toastReveicer, new String[] { Constants.BROADCAST_TOAST_TEXT });
			_isInitialized = true;
		} else {
			_logger.Warn("Is ALREADY initialized!");
		}
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
		_receiverController.UnregisterReceiver(_toastReveicer);
		_isInitialized = false;
	}
}
