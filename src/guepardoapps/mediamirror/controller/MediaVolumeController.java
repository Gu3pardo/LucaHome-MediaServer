package guepardoapps.mediamirror.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class MediaVolumeController {

	private static final String TAG = MediaVolumeController.class.getName();
	private SmartMirrorLogger _logger;

	private Context _context;
	private BroadcastController _broadcastController;
	private ReceiverController _receiverController;

	private static final int VOLUME_CHANGE_STEP = 1;

	private AudioManager _audioManager;
	private int _currentVolume;
	private int _maxVolume;
	private boolean _mute;

	public MediaVolumeController(Context context) {
		_logger = new SmartMirrorLogger(TAG);

		_context = context;
		_broadcastController = new BroadcastController(_context);
		_receiverController = new ReceiverController(_context);
		_receiverController.RegisterReceiver(_screenEnableReceiver, new String[] { Constants.BROADCAST_SCREEN_ENABLED });

		_audioManager = (AudioManager) _context.getSystemService(Context.AUDIO_SERVICE);
		_currentVolume = _audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		_maxVolume = _audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		_mute = _audioManager.isStreamMute(AudioManager.STREAM_MUSIC);

		sendVolumeBroadcast();
	}

	public void IncreaseVolume() {
		_logger.Debug("IncreaseVolume");
		if (_mute) {
			_logger.Warn("Audio stream is muted!");
			return;
		}
		if (_currentVolume >= _maxVolume) {
			_logger.Warn("Current volume is already _maxVolume: " + String.valueOf(_maxVolume));
			return;
		}
		int newVolume = _currentVolume + VOLUME_CHANGE_STEP;
		if (newVolume > _maxVolume) {
			newVolume = _maxVolume;
		}
		_logger.Debug("newVolume: " + String.valueOf(newVolume));
		_audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
		sendVolumeBroadcast();
	}

	public void DecreaseVolume() {
		_logger.Debug("DecreaseVolume");
		if (_mute) {
			_logger.Warn("Audio stream is muted!");
			return;
		}
		if (_currentVolume <= 0) {
			_logger.Warn("Current volume is already 0!");
			return;
		}
		int newVolume = _currentVolume - VOLUME_CHANGE_STEP;
		if (newVolume < 0) {
			newVolume = 0;
		}
		_logger.Debug("newVolume: " + String.valueOf(newVolume));
		_audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
		sendVolumeBroadcast();
	}

	public void SetVolume(int volume) {
		_logger.Debug("SetVolume: " + String.valueOf(volume));
		if (_mute) {
			_logger.Warn("Audio stream is muted!");
			UnmuteVolume();
		}
		if (_currentVolume <= 0) {
			_logger.Warn("Current volume is already 0!");
			return;
		}
		if (_currentVolume >= _maxVolume) {
			_logger.Warn("Current volume is already _maxVolume: " + String.valueOf(_maxVolume));
			return;
		}

		if (volume < 0) {
			volume = 0;
		}
		if (volume > _maxVolume) {
			volume = _maxVolume;
		}
		_logger.Debug("newVolume: " + String.valueOf(volume));
		_audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
		sendVolumeBroadcast();
	}

	@SuppressWarnings("deprecation")
	public void MuteVolume() {
		_logger.Debug("MuteVolume");
		if (_mute) {
			_logger.Warn("Audio stream is already muted!");
			return;
		}
		_audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
		_mute = _audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
		sendVolumeBroadcast();
	}

	@SuppressWarnings("deprecation")
	public void UnmuteVolume() {
		_logger.Debug("UnmuteVolume");
		if (!_mute) {
			_logger.Warn("Audio stream is already unmuted!");
			return;
		}
		_audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
		_mute = _audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
		sendVolumeBroadcast();
	}

	public int GetMaxVolume() {
		return _maxVolume;
	}

	public String GetCurrentVolume() {
		return String.valueOf(_currentVolume);
	}

	public void Dispose() {
		_receiverController.UnregisterReceiver(_screenEnableReceiver);
	}

	private void sendVolumeBroadcast() {
		_currentVolume = _audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		_mute = _audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
		String volumeText = "";
		if (_mute) {
			volumeText = "Vol.: mute";
		} else {
			volumeText = "Vol.: " + String.valueOf(_currentVolume);
		}
		_broadcastController.SendStringBroadcast(Constants.BROADCAST_SHOW_VOLUME_MODEL, Constants.BUNDLE_VOLUME_MODEL,
				volumeText);
	}

	private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			sendVolumeBroadcast();
		}
	};
}
