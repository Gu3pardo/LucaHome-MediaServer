package guepardoapps.mediamirror.services;

import android.os.IBinder;

import java.util.Calendar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.controller.*;

import guepardoapps.toolset.controller.BroadcastController;

public class TimeListenerService extends Service {

	private static final String TAG = TimeListenerService.class.getName();
	private SmartMirrorLogger _logger;

	private Context _context;
	private BroadcastController _broadcastController;

	private MediaVolumeController _mediaVolumeController;
	private ScreenController _screenController;
	private ScheduleController _scheduleController;

	private boolean _isInitialized;

	private final BroadcastReceiver _timeChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(Intent.ACTION_TIME_CHANGED)) {
				checkCurrentTime();
			} else if (action.equals(Intent.ACTION_DATE_CHANGED)) {
				_broadcastController.SendSimpleBroadcast(Constants.BROADCAST_PERFORM_BIRTHDAY_UPDATE);
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		if (!_isInitialized) {
			_logger = new SmartMirrorLogger(TAG);

			IntentFilter timeIntentFilter = new IntentFilter();
			timeIntentFilter.addAction(Intent.ACTION_TIME_TICK);
			timeIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
			timeIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
			registerReceiver(_timeChangedReceiver, timeIntentFilter);

			_context = this;

			if (_broadcastController == null) {
				_broadcastController = new BroadcastController(_context);
			}
			if (_mediaVolumeController == null) {
				_mediaVolumeController = new MediaVolumeController(_context);
			}
			if (_screenController == null) {
				_screenController = new ScreenController(_context);
			}
			if (_scheduleController == null) {
				_scheduleController = new ScheduleController(_context);
				_scheduleController.Start(Constants.SCHEDULE_UPDATE_TIMEOUT);
			}

			_isInitialized = true;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (_logger != null) {
			_logger.Debug("onStartCommand");
		}
		return 0;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		if (_logger != null) {
			_logger.Debug("onBind");
		}
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (_logger != null) {
			_logger.Debug("onDestroy");
		}
		_scheduleController.Dispose();
		unregisterReceiver(_timeChangedReceiver);
	}

	private void checkCurrentTime() {
		_logger.Debug("checkCurrentTime");

		Calendar now = Calendar.getInstance();
		int second = now.get(Calendar.SECOND);
		if (second == 0) {

			int minute = now.get(Calendar.MINUTE);
			if (minute == 0) {

				int hour = now.get(Calendar.HOUR_OF_DAY);
				int weekday = now.get(Calendar.DAY_OF_WEEK);

				switch (hour) {
				case 6:
					// weekend days
					if (weekday == Calendar.SUNDAY || weekday == Calendar.SATURDAY) {

					}
					// working days
					else {
						_mediaVolumeController.SetVolume((int) (_mediaVolumeController.GetMaxVolume() * (30 / 100)));
						_broadcastController.SendIntBroadcast(Constants.BROADCAST_VALUE_SCREEN_BRIGHTNESS,
								Constants.BUNDLE_SCREEN_BRIGHTNESS, 20);
					}
					break;
				case 8:
					// weekend days
					if (weekday == Calendar.SUNDAY || weekday == Calendar.SATURDAY) {
						_mediaVolumeController.SetVolume((int) (_mediaVolumeController.GetMaxVolume() * (30 / 100)));
						_broadcastController.SendIntBroadcast(Constants.BROADCAST_VALUE_SCREEN_BRIGHTNESS,
								Constants.BUNDLE_SCREEN_BRIGHTNESS, 40);
					}
					// working days
					else {
						_mediaVolumeController.SetVolume((int) (_mediaVolumeController.GetMaxVolume() * (10 / 100)));
						_broadcastController.SendIntBroadcast(Constants.BROADCAST_VALUE_SCREEN_BRIGHTNESS,
								Constants.BUNDLE_SCREEN_BRIGHTNESS, 10);
					}
					break;
				case 12:
					// weekend days
					if (weekday == Calendar.SUNDAY || weekday == Calendar.SATURDAY) {
						_broadcastController.SendIntBroadcast(Constants.BROADCAST_VALUE_SCREEN_BRIGHTNESS,
								Constants.BUNDLE_SCREEN_BRIGHTNESS, 70);
					}
					// working days
					else {

					}
					break;
				case 18:
					// weekend days
					if (weekday == Calendar.SUNDAY || weekday == Calendar.SATURDAY) {
						_mediaVolumeController.SetVolume((int) (_mediaVolumeController.GetMaxVolume() * (75 / 100)));
						_broadcastController.SendIntBroadcast(Constants.BROADCAST_VALUE_SCREEN_BRIGHTNESS,
								Constants.BUNDLE_SCREEN_BRIGHTNESS, 40);
					}
					// working days
					else {
						_broadcastController.SendIntBroadcast(Constants.BROADCAST_VALUE_SCREEN_BRIGHTNESS,
								Constants.BUNDLE_SCREEN_BRIGHTNESS, 40);
					}
					break;
				case 22:
					// weekend days
					if (weekday == Calendar.SUNDAY || weekday == Calendar.SATURDAY) {
						_mediaVolumeController.SetVolume((int) (_mediaVolumeController.GetMaxVolume() * (50 / 100)));
						_broadcastController.SendIntBroadcast(Constants.BROADCAST_VALUE_SCREEN_BRIGHTNESS,
								Constants.BUNDLE_SCREEN_BRIGHTNESS, 30);
					}
					// working days
					else {
						_mediaVolumeController.SetVolume((int) (_mediaVolumeController.GetMaxVolume() * (30 / 100)));
						_broadcastController.SendIntBroadcast(Constants.BROADCAST_VALUE_SCREEN_BRIGHTNESS,
								Constants.BUNDLE_SCREEN_BRIGHTNESS, 10);
					}
					break;
				case 23:
					// weekend days
					if (weekday == Calendar.SUNDAY || weekday == Calendar.SATURDAY) {
						_mediaVolumeController.SetVolume((int) (_mediaVolumeController.GetMaxVolume() * (10 / 100)));
						_broadcastController.SendIntBroadcast(Constants.BROADCAST_VALUE_SCREEN_BRIGHTNESS,
								Constants.BUNDLE_SCREEN_BRIGHTNESS, 10);
					}
					// working days
					else {
						_mediaVolumeController.SetVolume((int) (_mediaVolumeController.GetMaxVolume() * (10 / 100)));
					}
					break;
				default:
					break;
				}
			}
		}
	}
}