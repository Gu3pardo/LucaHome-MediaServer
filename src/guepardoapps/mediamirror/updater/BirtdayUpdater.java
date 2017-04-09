package guepardoapps.mediamirror.updater;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import guepardoapps.library.lucahome.common.dto.BirthdayDto;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.controller.ServiceController;
import guepardoapps.library.lucahome.converter.json.JsonDataToBirthdayConverter;
import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.RaspPiConstants;

public class BirtdayUpdater {

	private static final String TAG = BirtdayUpdater.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private Handler _updater;

	private Context _context;
	private BroadcastController _broadcastController;
	private ReceiverController _receiverController;
	private ServiceController _serviceController;

	private int _updateTime;
	private boolean _isRunning;

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
				SerializableList<BirthdayDto> loadedBirthdayList = JsonDataToBirthdayConverter
						.GetList(birthdayStringArray);

				SerializableList<BirthdayDto> _nextBirthdaysList = new SerializableList<BirthdayDto>();

				if (loadedBirthdayList != null) {
					if (loadedBirthdayList.getSize() == 0) {
						_logger.Warn("loadedBirthdayList size is 0!");
						return;
					}

					if (loadedBirthdayList.getSize() == 3) {
						_nextBirthdaysList = loadedBirthdayList;
					} else if (loadedBirthdayList.getSize() < 3) {
						int count = loadedBirthdayList.getSize();
						for (int index = 0; index < count - 1; index++) {
							_nextBirthdaysList.setValue(index, loadedBirthdayList.getValue(index));
						}
						for (int index = 2; index > count - 1; index--) {
							_nextBirthdaysList.setValue(index, null);
						}
					} else if (loadedBirthdayList.getSize() > 3) {
						Calendar today = Calendar.getInstance();
						ArrayList<BirthdayDto> nextDateList = new ArrayList<BirthdayDto>();
						ArrayList<BirthdayDto> prevDateList = new ArrayList<BirthdayDto>();

						_logger.Info("Today:");
						_logger.Info(today.toString());

						for (int index = 0; index < loadedBirthdayList.getSize(); index++) {
							BirthdayDto entry = loadedBirthdayList.getValue(index);

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
								_nextBirthdaysList.setValue(index, nextDateList.get(index));
							}
						} else {
							for (int index = 0; index < nextDateCount; index++) {
								_nextBirthdaysList.setValue(index, nextDateList.get(index));
							}
							if (prevDateList.size() > 3 - nextDateCount) {
								for (int index = nextDateCount; index < 3; index++) {
									_nextBirthdaysList.setValue(index, prevDateList.get(index));
								}
							} else {
								for (int index = nextDateCount; index < prevDateList.size() + nextDateCount; index++) {
									_nextBirthdaysList.setValue(index, prevDateList.get(index));
								}
								for (int index = 2; index > prevDateList.size() + nextDateCount - 1; index--) {
									_nextBirthdaysList.setValue(index, null);
								}
							}
						}
					}

					for (int index = 0; index < _nextBirthdaysList.getSize(); index++) {
						BirthdayDto entry = _nextBirthdaysList.getValue(index);
						if (entry != null) {
							if (entry.HasBirthday()) {
								_broadcastController.SendStringBroadcast(
										guepardoapps.library.toolset.common.Broadcasts.SPEAK_TEXT,
										guepardoapps.library.toolset.common.Bundles.SPEAK_TEXT,
										entry.GetNotificationBody(entry.GetAge()));
								break;
							}
						}
					}
				} else {
					_logger.Warn("loadedBirthdayList is null!");
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
		_serviceController = new ServiceController(_context);
	}

	public void Start(int updateTime) {
		_logger.Debug("Initialize");
		if (_isRunning) {
			_logger.Warn("Already running!");
			return;
		}
		_updateTime = updateTime;
		_logger.Debug("UpdateTime is: " + String.valueOf(_updateTime));
		_receiverController.RegisterReceiver(_updateReceiver, new String[] { Broadcasts.DOWNLOAD_BIRTHDAY_FINISHED });
		_receiverController.RegisterReceiver(_performUpdateReceiver,
				new String[] { Broadcasts.PERFORM_BIRTHDAY_UPDATE });
		_updateRunnable.run();
		_isRunning = true;
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_updater.removeCallbacks(_updateRunnable);
		_receiverController.UnregisterReceiver(_updateReceiver);
		_receiverController.UnregisterReceiver(_performUpdateReceiver);
		_isRunning = false;
	}

	public void DownloadBirthdays() {
		_logger.Debug("startDownloadBirthdays");

		_serviceController.StartRestService(RaspPiConstants.USER, RaspPiConstants.PASSWORD, Bundles.BIRTHDAY_MODEL,
				RaspPiConstants.GET_BIRTHDAYS, Broadcasts.DOWNLOAD_BIRTHDAY_FINISHED, LucaObject.BIRTHDAY,
				RaspberrySelection.BOTH);
	}
}
