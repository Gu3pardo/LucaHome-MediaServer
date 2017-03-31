package guepardoapps.mediamirror.updater;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.converter.DateConverter;
import guepardoapps.mediamirror.converter.TimeConverter;
import guepardoapps.mediamirror.converter.WeekdayConverter;
import guepardoapps.mediamirror.model.DateModel;

public class DateViewUpdater {

	private static final String TAG = DateViewUpdater.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private Context _context;
	private BroadcastController _broadcastController;
	private ReceiverController _receiverController;

	private BroadcastReceiver _timeTickReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			_logger.Debug("action: " + action);

			if (action.equals(Intent.ACTION_TIME_TICK)) {
				UpdateDate();
			}
		}
	};

	public DateViewUpdater(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
		_broadcastController = new BroadcastController(_context);
		_receiverController = new ReceiverController(_context);
	}

	public void Start() {
		_logger.Debug("Initialize");
		_receiverController.RegisterReceiver(_timeTickReceiver, new String[] { Intent.ACTION_TIME_TICK });
		UpdateDate();
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_receiverController.UnregisterReceiver(_timeTickReceiver);
	}

	public void UpdateDate() {
		Calendar calendar = Calendar.getInstance();
		String weekday = WeekdayConverter.GetWeekday(calendar.get(Calendar.DAY_OF_WEEK));
		String date = DateConverter.GetDate(calendar);
		String time = TimeConverter.GetTime(calendar);

		DateModel model = new DateModel(weekday, date, time);
		_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_DATE_MODEL, Bundles.DATE_MODEL, model);
	}
}
