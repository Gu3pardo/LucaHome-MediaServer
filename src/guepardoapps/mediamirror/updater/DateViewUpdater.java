package guepardoapps.mediamirror.updater;

import java.util.Calendar;

import android.content.Context;
import android.os.Handler;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.converter.DateConverter;
import guepardoapps.mediamirror.common.converter.TimeConverter;
import guepardoapps.mediamirror.common.converter.WeekdayConverter;
import guepardoapps.mediamirror.model.DateModel;

import guepardoapps.toolset.controller.BroadcastController;

public class DateViewUpdater {

	private static final String TAG = DateViewUpdater.class.getName();
	private SmartMirrorLogger _logger;

	private Handler _updater;

	private Context _context;
	private BroadcastController _broadcastController;

	private int _updateTime;

	private Runnable _updateRunnable = new Runnable() {
		public void run() {
			_logger.Debug("_updateRunnable run");
			UpdateDate();
			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	public DateViewUpdater(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_updater = new Handler();
		_context = context;
		_broadcastController = new BroadcastController(_context);
	}

	public void Start(int updateTime) {
		_logger.Debug("Initialize");
		_updateTime = updateTime;
		_logger.Debug("UpdateTime is: " + String.valueOf(_updateTime));
		_updateRunnable.run();
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_updater.removeCallbacks(_updateRunnable);
	}

	public void UpdateDate() {
		Calendar calendar = Calendar.getInstance();
		String weekday = WeekdayConverter.GetWeekday(calendar.get(Calendar.DAY_OF_WEEK));
		String date = DateConverter.GetDate(calendar);
		String time = TimeConverter.GetTime(calendar);

		DateModel model = new DateModel(weekday, date, time);
		_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_SHOW_DATE_MODEL, Constants.BUNDLE_DATE_MODEL,
				model);
	}
}
