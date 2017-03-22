package guepardoapps.mediamirror.view.controller;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;

import guepardoapps.toolset.common.classes.SerializableList;
import guepardoapps.toolset.common.dto.CalendarEntry;
import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.PermissionController;
import guepardoapps.toolset.controller.ReceiverController;

public class CalendarViewController {

	private static final String TAG = CalendarViewController.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private static final int PERMISSION_READ_CALENDAR_ID = 69;

	private boolean _isInitialized;
	private boolean _screenEnabled;
	private boolean _permissionGranted;
	private boolean _isVisible;

	private SerializableList<CalendarEntry> _calendarEntries = new SerializableList<CalendarEntry>();

	private Context _context;
	private BroadcastController _broadcastController;
	private PermissionController _permissionController;
	private ReceiverController _receiverController;

	private LinearLayout _calendarLayout;
	private View[] _calendarAlarmViewArray = new View[3];
	private TextView[] _calendarTextViewArray = new TextView[3];
	private boolean[] _isToday = new boolean[3];

	private Handler _updateAlarmHandler = new Handler();
	private int _invertTime = 1000;

	private BroadcastReceiver _dateChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_dateChangedReceiver onReceive");
			final String action = intent.getAction();
			if (action.equals(Intent.ACTION_DATE_CHANGED)) {
				_logger.Debug("ACTION_DATE_CHANGED");
				_broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_CALENDAR_UPDATE);
			}
		}
	};

	private BroadcastReceiver _permissionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_permissionReceiver onReceive");
			int permissionGrantedResult = intent
					.getIntExtra(guepardoapps.library.lucahome.common.constants.Bundles.PERMISSION_READ_CALENDAR, -1);
			_permissionGranted = permissionGrantedResult == PackageManager.PERMISSION_GRANTED;
			_logger.Info(String.format("Permission READ_CALENDAR result %s is granted %s!", permissionGrantedResult,
					_permissionGranted));
			if (_permissionGranted) {
				_broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_CALENDAR_UPDATE);
			}
		}
	};

	private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = true;
			if (_permissionGranted) {
				_broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_CALENDAR_UPDATE);
			}
			_updateAlarmHandler.postDelayed(_updateAlarmViewRunnable, _invertTime);
		}
	};

	private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = false;
			_updateAlarmHandler.removeCallbacks(_updateAlarmViewRunnable);
		}
	};

	private BroadcastReceiver _switchViewReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_switchViewReceiver onReceive");

			_isVisible = !_isVisible;

			if (_isVisible) {
				_calendarLayout.setVisibility(View.VISIBLE);
			} else {
				_calendarLayout.setVisibility(View.INVISIBLE);
			}
		}
	};

	private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			@SuppressWarnings("unchecked")
			SerializableList<CalendarEntry> calendarEntries = (SerializableList<CalendarEntry>) intent
					.getSerializableExtra(Bundles.CALENDAR_MODEL);
			if (calendarEntries != null) {
				_calendarEntries.clear();

				for (int index = 0; index < calendarEntries.getSize(); index++) {
					CalendarEntry entry = calendarEntries.getValue(index);
					if (entry.BeginIsAfterNow()) {
						_logger.Debug(entry.toString() + ": begin is after now!");
						_calendarEntries.addValue(entry);
					}
				}

				for (int index = 0; index < 3; index++) {
					_isToday[index] = false;
				}

				for (int index = 0; index < 3; index++) {
					if (index < _calendarEntries.getSize()) {
						CalendarEntry entry = _calendarEntries.getValue(index);
						if (entry.IsToday()) {
							_logger.Debug(entry.toString() + " is today!");
							_isToday[index] = true;
							_calendarAlarmViewArray[index].setVisibility(View.VISIBLE);
						} else {
							_logger.Debug(entry.toString() + " is not today!");
							_isToday[index] = false;
							_calendarAlarmViewArray[index].setVisibility(View.INVISIBLE);
						}
						_calendarTextViewArray[index].setText(entry.GetMirrorText());
					} else {
						_calendarTextViewArray[index].setVisibility(View.INVISIBLE);
						_calendarAlarmViewArray[index].setVisibility(View.INVISIBLE);
					}
				}

				_updateAlarmHandler.removeCallbacks(_updateAlarmViewRunnable);
				_updateAlarmHandler.postDelayed(_updateAlarmViewRunnable, _invertTime);
			}
		}
	};

	private Runnable _updateAlarmViewRunnable = new Runnable() {
		private boolean _invert;

		public void run() {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			for (int index = 0; index < 3; index++) {
				if (_isToday[index]) {
					if (_invert) {
						_calendarAlarmViewArray[index].setBackgroundResource(R.drawable.circle_red);
					} else {
						_calendarAlarmViewArray[index].setBackgroundResource(R.drawable.circle_yellow);
					}
				}
			}

			_invert = !_invert;
			_updateAlarmHandler.postDelayed(this, _invertTime);
		}
	};

	public CalendarViewController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
		_broadcastController = new BroadcastController(_context);
		_permissionController = new PermissionController(_context);
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_screenEnabled = true;

		_calendarLayout = (LinearLayout) ((Activity) _context).findViewById(R.id.calendarLinearLayout);
		_calendarAlarmViewArray[0] = (View) ((Activity) _context).findViewById(R.id.calendar1AlarmView);
		_calendarTextViewArray[0] = (TextView) ((Activity) _context).findViewById(R.id.calendar1TextView);
		_calendarAlarmViewArray[1] = (View) ((Activity) _context).findViewById(R.id.calendar2AlarmView);
		_calendarTextViewArray[1] = (TextView) ((Activity) _context).findViewById(R.id.calendar2TextView);
		_calendarAlarmViewArray[2] = (View) ((Activity) _context).findViewById(R.id.calendar3AlarmView);
		_calendarTextViewArray[2] = (TextView) ((Activity) _context).findViewById(R.id.calendar3TextView);
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_logger.Debug("Initializing!");

			_receiverController.RegisterReceiver(_dateChangedReceiver, new String[] { Intent.ACTION_DATE_CHANGED });
			_receiverController.RegisterReceiver(_permissionReceiver, new String[] {
					guepardoapps.library.lucahome.common.constants.Broadcasts.PERMISSION_READ_CALENDAR });
			_receiverController.RegisterReceiver(_screenEnableReceiver, new String[] { Broadcasts.SCREEN_ENABLED });
			_receiverController.RegisterReceiver(_screenDisableReceiver,
					new String[] { Broadcasts.SCREEN_OFF, Broadcasts.SCREEN_SAVER });
			_receiverController.RegisterReceiver(_switchViewReceiver,
					new String[] { Broadcasts.SWITCH_BIRTHDAY_CALENDAR });
			_receiverController.RegisterReceiver(_updateViewReceiver, new String[] { Broadcasts.SHOW_CALENDAR_MODEL });

			_isInitialized = true;

			if (_screenEnabled) {
				_permissionController.CheckPermissions(PERMISSION_READ_CALENDAR_ID, Manifest.permission.READ_CALENDAR);
			}
		} else {
			_logger.Warn("Is ALREADY initialized!");
		}
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");

		_receiverController.UnregisterReceiver(_dateChangedReceiver);
		_receiverController.UnregisterReceiver(_permissionReceiver);
		_receiverController.UnregisterReceiver(_screenEnableReceiver);
		_receiverController.UnregisterReceiver(_screenDisableReceiver);
		_receiverController.UnregisterReceiver(_switchViewReceiver);
		_receiverController.UnregisterReceiver(_updateViewReceiver);

		_isInitialized = false;
	}
}
