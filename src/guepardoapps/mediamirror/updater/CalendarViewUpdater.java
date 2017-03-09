package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;

import guepardoapps.toolset.common.classes.SerializableList;
import guepardoapps.toolset.common.dto.CalendarEntry;
import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.CalendarController;
import guepardoapps.toolset.controller.ReceiverController;

public class CalendarViewUpdater {

	private static final String TAG = CalendarViewUpdater.class.getName();
	private SmartMirrorLogger _logger;

	private Handler _updater;

	private Context _context;
	private BroadcastController _broadcastController;
	private CalendarController _calendarController;
	private ReceiverController _receiverController;

	private int _updateTime;

	private Runnable _updateRunnable = new Runnable() {
		public void run() {
			_logger.Debug("_updateRunnable run");

			SerializableList<CalendarEntry> calendarList = _calendarController.ReadCalendar();
			_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_SHOW_CALENDAR_MODEL,
					Constants.BUNDLE_CALENDAR_MODEL, calendarList);

			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_performUpdateReceiver onReceive");

			SerializableList<CalendarEntry> calendarList = _calendarController.ReadCalendar();
			_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_SHOW_CALENDAR_MODEL,
					Constants.BUNDLE_CALENDAR_MODEL, calendarList);
		}
	};

	public CalendarViewUpdater(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_updater = new Handler();
		_context = context;
		_broadcastController = new BroadcastController(_context);
		_calendarController = new CalendarController(_context);
		_receiverController = new ReceiverController(_context);
	}

	public void Start(int updateTime) {
		_logger.Debug("Initialize");
		_updateTime = updateTime;
		_logger.Debug("UpdateTime is: " + String.valueOf(_updateTime));
		_receiverController.RegisterReceiver(_performUpdateReceiver,
				new String[] { Constants.BROADCAST_PERFORM_CALENDAR_UPDATE });
		_updateRunnable.run();
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_updater.removeCallbacks(_updateRunnable);
		_receiverController.UnregisterReceiver(_performUpdateReceiver);
	}
}
