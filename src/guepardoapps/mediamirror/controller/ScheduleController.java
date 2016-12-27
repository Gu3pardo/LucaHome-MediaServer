package guepardoapps.mediamirror.controller;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.converter.JsonDataToScheduleConverter;
import guepardoapps.mediamirror.model.ScheduleModel;
import guepardoapps.mediamirror.services.RESTService;

import guepardoapps.toolset.common.enums.Weekday;
import guepardoapps.toolset.controller.ReceiverController;

public class ScheduleController {

	private static final String TAG = ScheduleController.class.getName();
	private SmartMirrorLogger _logger;

	private Handler _updater;

	private Context _context;
	private ReceiverController _receiverController;

	private int _updateTime;
	private ArrayList<ScheduleModel> _scheduleList;

	private Runnable _updateRunnable = new Runnable() {
		public void run() {
			_logger.Debug("_updateRunnable run");

			Intent serviceIntent = new Intent(_context, RESTService.class);
			Bundle serviceData = new Bundle();

			serviceData.putString(Constants.BUNDLE_REST_ACTION, Constants.ACTION_GET_SCHEDULES);
			serviceData.putString(Constants.BUNDLE_REST_DATA, Constants.BUNDLE_SCHEDULE_MODEL);
			serviceData.putString(Constants.BUNDLE_REST_BROADCAST, Constants.BROADCAST_DOWNLOAD_SCHEDULE_FINISHED);

			serviceIntent.putExtras(serviceData);
			_context.startService(serviceIntent);

			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateReceiver onReceive");
			String[] scheduleStringArray = intent.getStringArrayExtra(Constants.BUNDLE_SCHEDULE_MODEL);
			if (scheduleStringArray != null) {
				ArrayList<ScheduleModel> scheduleList = JsonDataToScheduleConverter.GetList(scheduleStringArray);
				if (scheduleList != null) {
					_scheduleList = scheduleList;
				}
			}
		}
	};

	public ScheduleController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_updater = new Handler();
		_context = context;
		_receiverController = new ReceiverController(_context);
	}

	public void Start(int updateTime) {
		_logger.Debug("Initialize");

		_updateTime = updateTime;
		_logger.Debug("UpdateTime is: " + String.valueOf(_updateTime));
		_scheduleList = new ArrayList<ScheduleModel>();

		_receiverController.RegisterReceiver(_updateReceiver,
				new String[] { Constants.BROADCAST_DOWNLOAD_SCHEDULE_FINISHED });
		_updateRunnable.run();
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_updater.removeCallbacks(_updateRunnable);
		_receiverController.UnregisterReceiver(_updateReceiver);
	}

	public void UpdateSchedules() {
		_logger.Debug("UpdateSchedules");
		_updater.removeCallbacks(_updateRunnable);
		_updateRunnable.run();
	}

	public void CheckSchedules() {
		_logger.Debug("CheckSchedules");

		Calendar now = Calendar.getInstance();
		Weekday weekday = Weekday.GetById(now.get(Calendar.DAY_OF_WEEK));
		_logger.Debug("Weekday: " + weekday.toString());

		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		int timeValue = hour * 60 + minute;
		_logger.Debug("TimeValue: " + String.valueOf(timeValue));

		for (ScheduleModel entry : _scheduleList) {
			if (entry.GetWeekday() == weekday) {
				// Schedule is today!
				if (entry.GetIsActive()) {
					// Schedule is active
					if (entry.GetSocket().contains("Light") && entry.GetSocket().contains("Sleep")) {
						// Schedule is for light in sleeping room

						if (entry.GetAction()) {
							// activate socket

							// TODO check time and perform action
							// e.g. make TTS and say good morning/tell about the
							// weather
						} else if (!entry.GetAction()) {
							// deactivate socket

							// TODO check time and perform action
							// e.g. make TTS and say good morning/tell about the
							// weather
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public ScheduleModel GetNextSchedule() {
		_logger.Debug("GetNextSchedule");

		Calendar now = Calendar.getInstance();
		int searchCounts = 0;

		ArrayList<ScheduleModel> nextSchedules = new ArrayList<ScheduleModel>();
		while (nextSchedules.size() <= 0 && searchCounts < 7) {
			int weekdayId = now.get(Calendar.DAY_OF_WEEK) - searchCounts;
			if (weekdayId < 0) {
				weekdayId += 7;
			}
			Weekday weekday = Weekday.GetById(weekdayId);
			if (weekday == null) {
				_logger.Error("Weekday is null! Cancel search!");
				return null;
			}
			for (ScheduleModel entry : _scheduleList) {
				if (entry.GetWeekday() == weekday && entry.GetIsActive()) {
					nextSchedules.add(entry);
				}
			}
			searchCounts++;
		}

		if (nextSchedules.size() == 0) {
			return null;
		} else if (nextSchedules.size() == 1) {
			return nextSchedules.get(0);
		} else {
			ArrayList<Integer> timeDifferences = new ArrayList<Integer>();
			for (ScheduleModel entry : nextSchedules) {
				int minute = entry.GetTime().getMinutes();
				int hour = entry.GetTime().getHours();
				timeDifferences.add(hour * 60 + minute);
			}
			int nextScheduleId = 0;
			for (int index = 1; index < timeDifferences.size(); index++) {
				if (timeDifferences.get(index) < timeDifferences.get(index) - 1) {
					nextScheduleId = index;
				}
			}
			return nextSchedules.get(nextScheduleId);
		}
	}
}
