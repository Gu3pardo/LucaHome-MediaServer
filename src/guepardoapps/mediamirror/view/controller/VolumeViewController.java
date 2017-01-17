package guepardoapps.mediamirror.view.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.R;

import guepardoapps.toolset.controller.ReceiverController;

public class VolumeViewController {

	private static final String TAG = VolumeViewController.class.getName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;
	private boolean _screenEnabled;

	private Context _context;
	private ReceiverController _receiverController;

	private TextView _volumeValueTextView;

	private BroadcastReceiver _volumeInfoReveicer = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctxt, Intent intent) {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_logger.Debug("_volumeInfoReveicer onReceive");
			String newVolumeText = intent.getStringExtra(Constants.BUNDLE_VOLUME_MODEL);
			if (newVolumeText != null) {
				_logger.Debug("newVolumeText: " + newVolumeText);
				_volumeValueTextView.setText(newVolumeText);
			}
		}
	};

	private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = true;
			_volumeValueTextView = (TextView) ((Activity) _context).findViewById(R.id.volumeTextView);
		}
	};

	private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = false;
		}
	};

	public VolumeViewController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_screenEnabled = true;

		_volumeValueTextView = (TextView) ((Activity) _context).findViewById(R.id.volumeTextView);
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_logger.Debug("Initializing!");
			_receiverController.RegisterReceiver(_volumeInfoReveicer,
					new String[] { Constants.BROADCAST_SHOW_VOLUME_MODEL });
			_receiverController.RegisterReceiver(_screenEnableReceiver,
					new String[] { Constants.BROADCAST_SCREEN_ENABLED });
			_receiverController.RegisterReceiver(_screenDisableReceiver,
					new String[] { Constants.BROADCAST_SCREEN_OFF, Constants.BROADCAST_SCREEN_SAVER });
			_isInitialized = true;
		} else {
			_logger.Warn("Is ALREADY initialized!");
		}
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
		_receiverController.UnregisterReceiver(_volumeInfoReveicer);
		_receiverController.UnregisterReceiver(_screenEnableReceiver);
		_receiverController.UnregisterReceiver(_screenDisableReceiver);
		_isInitialized = false;
	}
}
