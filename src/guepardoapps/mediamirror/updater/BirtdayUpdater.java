package guepardoapps.mediamirror.updater;

import java.util.ArrayList;
import java.util.Comparator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.Tools;
import guepardoapps.mediamirror.common.converter.JsonDataToBirthdayConverter;
import guepardoapps.mediamirror.model.helper.BirthdayHelper;
import guepardoapps.mediamirror.services.RESTService;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class BirtdayUpdater {

	private static final String TAG = BirtdayUpdater.class.getName();
	private SmartMirrorLogger _logger;

	private Handler _updater;

	private Context _context;
	private BroadcastController _broadcastController;
	private ReceiverController _receiverController;

	private int _updateTime;

	private Runnable _updateRunnable = new Runnable() {
		public void run() {
			_logger.Debug("_updateRunnable run");
			DownloadBirthdays();
			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateReceiver onReceive");
			String[] birthdayStringArray = intent.getStringArrayExtra(Constants.BUNDLE_BIRTHDAY_MODEL);
			if (birthdayStringArray != null) {
				ArrayList<BirthdayHelper> _nextBirthdaysList = new ArrayList<BirthdayHelper>();
				ArrayList<BirthdayHelper> nextBirthdaysList = JsonDataToBirthdayConverter.GetList(birthdayStringArray);

				if (nextBirthdaysList != null) {
					if (nextBirthdaysList.size() == 3) {
						_nextBirthdaysList = nextBirthdaysList;
					} else if (nextBirthdaysList.size() < 3) {
						int count = nextBirthdaysList.size();
						for (int index = 0; index < count - 1; index++) {
							_nextBirthdaysList.set(index, nextBirthdaysList.get(index));
						}
						for (int index = 2; index > count - 1; index--) {
							_nextBirthdaysList.set(index, null);
						}
					} else if (nextBirthdaysList.size() > 3) {
						nextBirthdaysList.sort(new Comparator<BirthdayHelper>() {
							@Override
							public int compare(BirthdayHelper entry0, BirthdayHelper entry1) {
								return entry0.GetBirthday().compareTo(entry1.GetBirthday());
							}
						});

						Calendar today = Calendar.getInstance();
						ArrayList<BirthdayHelper> nextDateList = new ArrayList<BirthdayHelper>();
						ArrayList<BirthdayHelper> prevDateList = new ArrayList<BirthdayHelper>();

						for (BirthdayHelper entry : nextBirthdaysList) {
							if (entry.GetBirthday().get(Calendar.MONTH) >= today.get(Calendar.MONTH)) {
								if (entry.GetBirthday().get(Calendar.DAY_OF_MONTH) >= today
										.get(Calendar.DAY_OF_MONTH)) {
									nextDateList.add(entry);
								} else {
									prevDateList.add(entry);
								}
							} else {
								prevDateList.add(entry);
							}
						}

						int nextDateCount = nextDateList.size();
						if (nextDateCount >= 3) {
							for (int index = 0; index < 3; index++) {
								_nextBirthdaysList.set(index, nextDateList.get(index));
							}
						} else {
							for (int index = 0; index < nextDateCount; index++) {
								_nextBirthdaysList.set(index, nextDateList.get(index));
							}
							if (prevDateList.size() > 3 - nextDateCount) {
								for (int index = nextDateCount; index < 3; index++) {
									_nextBirthdaysList.set(index, prevDateList.get(index));
								}
							} else {
								for (int index = nextDateCount; index < prevDateList.size() + nextDateCount; index++) {
									_nextBirthdaysList.set(index, prevDateList.get(index));
								}
								for (int index = 2; index > prevDateList.size() + nextDateCount - 1; index--) {
									_nextBirthdaysList.set(index, null);
								}
							}
						}
					}

					for (BirthdayHelper entry : _nextBirthdaysList) {
						if (entry != null) {
							if (entry.HasBirthday()) {
								_broadcastController.SendStringBroadcast(Constants.BROADCAST_SPEAK_TEXT,
										Constants.BUNDLE_SPEAK_TEXT, entry.GetNotificationString());
								break;
							}
						}
					}
				}

				_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_SHOW_BIRTHDAY_MODEL,
						Constants.BUNDLE_BIRTHDAY_MODEL, _nextBirthdaysList);
			}
		}
	};

	private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_performUpdateReceiver onReceive");
			DownloadBirthdays();
		}
	};

	public BirtdayUpdater(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_updater = new Handler();
		_context = context;
		_broadcastController = new BroadcastController(_context);
		_receiverController = new ReceiverController(_context);
	}

	public void Start(int updateTime) {
		_logger.Debug("Initialize");
		_updateTime = updateTime;
		_logger.Debug("UpdateTime is: " + String.valueOf(_updateTime));
		_receiverController.RegisterReceiver(_updateReceiver,
				new String[] { Constants.BROADCAST_DOWNLOAD_BIRTHDAY_FINISHED });
		_receiverController.RegisterReceiver(_performUpdateReceiver,
				new String[] { Constants.BROADCAST_PERFORM_BIRTHDAY_UPDATE });
		_updateRunnable.run();
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_updater.removeCallbacks(_updateRunnable);
		_receiverController.UnregisterReceiver(_updateReceiver);
		_receiverController.UnregisterReceiver(_performUpdateReceiver);
	}

	public void DownloadBirthdays() {
		_logger.Debug("startDownloadBirthdays");

		if (Tools.IsMuteTime()) {
			_logger.Warn("Mute time!");
			return;
		}

		Intent serviceIntent = new Intent(_context, RESTService.class);
		Bundle serviceData = new Bundle();

		serviceData.putString(Constants.BUNDLE_REST_ACTION, Constants.ACTION_GET_BIRTHDAYS);
		serviceData.putString(Constants.BUNDLE_REST_DATA, Constants.BUNDLE_BIRTHDAY_MODEL);
		serviceData.putString(Constants.BUNDLE_REST_BROADCAST, Constants.BROADCAST_DOWNLOAD_BIRTHDAY_FINISHED);

		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}
}
