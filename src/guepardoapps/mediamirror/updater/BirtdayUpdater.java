package guepardoapps.mediamirror.updater;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.TimeHelper;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.RaspPiConstants;
import guepardoapps.mediamirror.converter.json.JsonDataToBirthdayConverter;
import guepardoapps.mediamirror.model.helper.BirthdayHelper;
import guepardoapps.mediamirror.services.RESTService;

public class BirtdayUpdater {

	private static final String TAG = BirtdayUpdater.class.getSimpleName();
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
			String[] birthdayStringArray = intent.getStringArrayExtra(Bundles.BIRTHDAY_MODEL);
			if (birthdayStringArray != null) {
				ArrayList<BirthdayHelper> loadedBirthdayList = JsonDataToBirthdayConverter.GetList(birthdayStringArray);

				ArrayList<BirthdayHelper> _nextBirthdaysList = new ArrayList<BirthdayHelper>();

				if (loadedBirthdayList != null) {
					if (loadedBirthdayList.size() == 0) {
						_logger.Warn("loadedBirthdayList size is 0!");
						return;
					}

					if (loadedBirthdayList.size() == 3) {
						_nextBirthdaysList = loadedBirthdayList;
					} else if (loadedBirthdayList.size() < 3) {
						int count = loadedBirthdayList.size();
						for (int index = 0; index < count - 1; index++) {
							_nextBirthdaysList.set(index, loadedBirthdayList.get(index));
						}
						for (int index = 2; index > count - 1; index--) {
							_nextBirthdaysList.set(index, null);
						}
					} else if (loadedBirthdayList.size() > 3) {
						Calendar today = Calendar.getInstance();
						ArrayList<BirthdayHelper> nextDateList = new ArrayList<BirthdayHelper>();
						ArrayList<BirthdayHelper> prevDateList = new ArrayList<BirthdayHelper>();

						_logger.Info("Today:");
						_logger.Info(today.toString());

						for (BirthdayHelper entry : loadedBirthdayList) {
							_logger.Info("Entry:");
							_logger.Info(entry.GetBirthday().toString());

							switch (entry.GetDateType()) {
							case UPCOMING:
							case TODAY:
								_logger.Info("Next: " + entry.GetName());
								nextDateList.add(entry);
								break;
							case PREVIOUS:
								_logger.Info("Prev:" + entry.GetName());
								prevDateList.add(entry);
								break;
							default:
								_logger.Error("Not supported BirthdayDateType: " + entry.GetDateType());
								break;
							}
						}

						int nextDateCount = nextDateList.size();
						if (nextDateCount >= 3) {
							for (int index = 0; index < 3; index++) {
								_nextBirthdaysList.add(index, nextDateList.get(index));
							}
						} else {
							for (int index = 0; index < nextDateCount; index++) {
								_nextBirthdaysList.add(index, nextDateList.get(index));
							}
							if (prevDateList.size() > 3 - nextDateCount) {
								for (int index = nextDateCount; index < 3; index++) {
									_nextBirthdaysList.add(index, prevDateList.get(index));
								}
							} else {
								for (int index = nextDateCount; index < prevDateList.size() + nextDateCount; index++) {
									_nextBirthdaysList.add(index, prevDateList.get(index));
								}
								for (int index = 2; index > prevDateList.size() + nextDateCount - 1; index--) {
									_nextBirthdaysList.add(index, null);
								}
							}
						}
					}

					for (BirthdayHelper entry : _nextBirthdaysList) {
						if (entry != null) {
							if (entry.HasBirthday()) {
								_broadcastController.SendStringBroadcast(
										guepardoapps.library.toolset.common.Broadcasts.SPEAK_TEXT,
										guepardoapps.library.toolset.common.Bundles.SPEAK_TEXT,
										entry.GetNotificationString());
								break;
							}
						}
					}
				}

				_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_BIRTHDAY_MODEL, Bundles.BIRTHDAY_MODEL,
						_nextBirthdaysList);
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
		_receiverController.RegisterReceiver(_updateReceiver, new String[] { Broadcasts.DOWNLOAD_BIRTHDAY_FINISHED });
		_receiverController.RegisterReceiver(_performUpdateReceiver,
				new String[] { Broadcasts.PERFORM_BIRTHDAY_UPDATE });
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

		if (TimeHelper.IsMuteTime()) {
			_logger.Warn("Mute time!");
			return;
		}

		Intent serviceIntent = new Intent(_context, RESTService.class);
		Bundle serviceData = new Bundle();

		serviceData.putString(RaspPiConstants.BUNDLE_REST_ACTION, RaspPiConstants.GET_BIRTHDAYS);
		serviceData.putString(RaspPiConstants.BUNDLE_REST_DATA, Bundles.BIRTHDAY_MODEL);
		serviceData.putString(RaspPiConstants.BUNDLE_REST_BROADCAST, Broadcasts.DOWNLOAD_BIRTHDAY_FINISHED);

		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}
}
