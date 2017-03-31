package guepardoapps.mediamirror.view.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.library.verticalseekbarview.VerticalSeekbarView;
import guepardoapps.library.verticalseekbarview.enums.VerticalSeekbarStyle;
import guepardoapps.library.verticalseekbarview.interfaces.OnVerticalSeebarMoveListener;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.controller.MediaVolumeController;

public class VolumeViewController {

	private static final String TAG = VolumeViewController.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;
	private boolean _screenEnabled;

	private int _maxVolume;

	private static final long LOOP_INTERVAL = 250;

	private Context _context;
	private MediaVolumeController _mediaVolumeController;
	private ReceiverController _receiverController;

	private TextView _volumeValueTextView;
	private boolean _volumeEnabled = true;
	private VerticalSeekbarView _volumeControl;

	private BroadcastReceiver _volumeInfoReveicer = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_logger.Debug("_volumeInfoReveicer onReceive");
			String newVolumeText = intent.getStringExtra(Bundles.VOLUME_MODEL);
			if (newVolumeText != null) {
				_logger.Debug("newVolumeText: " + newVolumeText);
				_volumeValueTextView.setText("Vol.: " + newVolumeText);

				if (!newVolumeText.contains("mute")) {
					int currentVolume = -1;
					try {
						currentVolume = Integer.parseInt(newVolumeText);
						_logger.Debug("currentVolume: " + String.valueOf(currentVolume));
						_maxVolume = _mediaVolumeController.GetMaxVolume();
						_logger.Debug("_maxVolume is: " + String.valueOf(_maxVolume));
						int percentageY = (currentVolume * 100) / _maxVolume;
						_volumeEnabled = false;
						_volumeControl.SetPositionY(percentageY);
						_volumeEnabled = true;
					} catch (Exception ex) {
						_logger.Error(ex.toString());
					} finally {
						_logger.Debug("Setting _mediaVolumeController currentVolume to: " + currentVolume);
						_mediaVolumeController.SetCurrentVolume(currentVolume);
					}
				}
			}
		}
	};

	private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = true;

			_maxVolume = _mediaVolumeController.GetMaxVolume();

			_volumeValueTextView = (TextView) ((Activity) _context).findViewById(R.id.volumeTextView);
			_volumeControl = (VerticalSeekbarView) ((Activity) _context).findViewById(R.id.volumeSlider);
			_volumeControl.setStyle(VerticalSeekbarStyle.VOLUME_SLIDER);
			_volumeControl.setOnVerticalSeebarMoveListener(new OnVerticalSeebarMoveListener() {
				@Override
				public void onValueChanged(int volumePercentage) {
					_logger.Debug(String.format("VolumePercentage: %s", volumePercentage));
					if (volumePercentage < 0) {
						volumePercentage *= -1;
					}

					if (!_volumeEnabled) {
						_logger.Warn("VolumeControl is disabled!");
						return;
					}

					_mediaVolumeController.SetVolume((int) (_maxVolume * volumePercentage / 100));
				}
			}, LOOP_INTERVAL);
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
		_mediaVolumeController = MediaVolumeController.getInstance();
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_screenEnabled = true;

		_maxVolume = _mediaVolumeController.GetMaxVolume();

		_volumeValueTextView = (TextView) ((Activity) _context).findViewById(R.id.volumeTextView);
		_volumeValueTextView.setText(String.valueOf(_mediaVolumeController.GetCurrentVolume()));
		_volumeControl = (VerticalSeekbarView) ((Activity) _context).findViewById(R.id.volumeSlider);
		_volumeControl.setStyle(VerticalSeekbarStyle.VOLUME_SLIDER);
		_volumeControl.setOnVerticalSeebarMoveListener(new OnVerticalSeebarMoveListener() {
			@Override
			public void onValueChanged(int volumePercentage) {
				_logger.Debug(String.format("VolumePercentage: %s", volumePercentage));
				if (volumePercentage < 0) {
					volumePercentage *= -1;
				}
				_mediaVolumeController.SetVolume((int) (_maxVolume * volumePercentage / 100));
			}
		}, LOOP_INTERVAL);
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_logger.Debug("Initializing!");
			_receiverController.RegisterReceiver(_volumeInfoReveicer, new String[] { Broadcasts.SHOW_VOLUME_MODEL });
			_receiverController.RegisterReceiver(_screenEnableReceiver, new String[] { Broadcasts.SCREEN_ENABLED });
			_receiverController.RegisterReceiver(_screenDisableReceiver,
					new String[] { Broadcasts.SCREEN_OFF, Broadcasts.SCREEN_SAVER });
			_mediaVolumeController.initialize(_context);
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

		_mediaVolumeController.Dispose();

		_isInitialized = false;
	}
}
