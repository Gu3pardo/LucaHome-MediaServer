package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.format.DateUtils;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.dto.CalendarEntryDto;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.CalendarController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;

public class CalendarViewUpdater {

	private static final String TAG = CalendarViewUpdater.class.getSimpleName();
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

			SerializableList<CalendarEntryDto> calendarList = _calendarController
					.ReadCalendar(DateUtils.YEAR_IN_MILLIS * 10000);
			_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CALENDAR_MODEL, Bundles.CALENDAR_MODEL,
					calendarList);

			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_performUpdateReceiver onReceive");

			SerializableList<CalendarEntryDto> calendarList = _calendarController
					.ReadCalendar(DateUtils.YEAR_IN_MILLIS * 10000);
			_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CALENDAR_MODEL, Bundles.CALENDAR_MODEL,
					calendarList);
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
				new String[] { Broadcasts.PERFORM_CALENDAR_UPDATE });
		_updateRunnable.run();
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_updater.removeCallbacks(_updateRunnable);
		_receiverController.UnregisterReceiver(_performUpdateReceiver);
	}
}
