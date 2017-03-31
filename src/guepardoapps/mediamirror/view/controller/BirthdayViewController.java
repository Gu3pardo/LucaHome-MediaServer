package guepardoapps.mediamirror.view.controller;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.Enables;
import guepardoapps.mediamirror.model.helper.BirthdayHelper;

import guepardoapps.test.BirthdayViewControllerTest;

public class BirthdayViewController {

	private static final String TAG = BirthdayViewController.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;
	private boolean _screenEnabled;
	private boolean _isVisible = true;

	private Context _context;
	private BroadcastController _broadcastController;
	private ReceiverController _receiverController;

	private LinearLayout _birthdayLayout;
	private View[] _birthdayAlarmViewArray = new View[3];
	private TextView[] _birthdayTextViewArray = new TextView[3];
	private boolean[] _hasBirthday = new boolean[3];

	private Handler _updateAlarmHandler = new Handler();
	private int _invertTime = 1000;

	private BirthdayViewControllerTest _birthdayViewTest;

	private BroadcastReceiver _dateChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_dateChangedReceiver onReceive");
			final String action = intent.getAction();
			if (action.equals(Intent.ACTION_DATE_CHANGED)) {
				_logger.Debug("ACTION_DATE_CHANGED");
				_broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_BIRTHDAY_UPDATE);
			}
		}
	};

	private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = false;
			_updateAlarmHandler.removeCallbacks(_updateAlarmViewRunnable);
		}
	};

	private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = true;

			_birthdayAlarmViewArray[0] = (View) ((Activity) _context).findViewById(R.id.birthday1AlarmView);
			_birthdayTextViewArray[0] = (TextView) ((Activity) _context).findViewById(R.id.birthday1TextView);
			_birthdayAlarmViewArray[1] = (View) ((Activity) _context).findViewById(R.id.birthday2AlarmView);
			_birthdayTextViewArray[1] = (TextView) ((Activity) _context).findViewById(R.id.birthday2TextView);
			_birthdayAlarmViewArray[2] = (View) ((Activity) _context).findViewById(R.id.birthday3AlarmView);
			_birthdayTextViewArray[2] = (TextView) ((Activity) _context).findViewById(R.id.birthday3TextView);

			_updateAlarmHandler.postDelayed(_updateAlarmViewRunnable, _invertTime);
		}
	};

	private BroadcastReceiver _switchViewReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_switchViewReceiver onReceive");

			_isVisible = !_isVisible;

			if (_isVisible) {
				_birthdayLayout.setVisibility(View.VISIBLE);
			} else {
				_birthdayLayout.setVisibility(View.INVISIBLE);
			}
		}
	};

	private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_logger.Debug("_updateViewReceiver onReceive");
			ArrayList<BirthdayHelper> birthdayList = (ArrayList<BirthdayHelper>) intent
					.getSerializableExtra(Bundles.BIRTHDAY_MODEL);
			if (birthdayList != null) {
				_logger.Debug(birthdayList.toString());
				for (int index = 0; index < birthdayList.size(); index++) {
					BirthdayHelper entry = birthdayList.get(index);
					if (entry != null) {
						if (entry.HasBirthday()) {
							_hasBirthday[index] = true;
							_birthdayTextViewArray[index].setText(entry.GetNotificationString());
							_birthdayAlarmViewArray[index].setVisibility(View.VISIBLE);
							checkPlayBirthdaySong(entry);
						} else {
							_hasBirthday[index] = false;
							_birthdayTextViewArray[index].setText(entry.GetName() + ": " + entry.GetBirthdayString());
							_birthdayAlarmViewArray[index].setVisibility(View.INVISIBLE);
						}
					} else {
						_hasBirthday[index] = false;
						_birthdayTextViewArray[index].setText("");
						_birthdayAlarmViewArray[index].setVisibility(View.INVISIBLE);
					}
				}

				_updateAlarmHandler.removeCallbacks(_updateAlarmViewRunnable);
				_updateAlarmHandler.postDelayed(_updateAlarmViewRunnable, _invertTime);
			} else {
				_logger.Warn("birthdayList is null!");
			}

			/*
			 * if (Constants.TESTING_ENABLED) { boolean hasBirthday = false; if
			 * (model != null) { hasBirthday = model.GetHasBirthday(); }
			 * _birthdayViewTest.ValidateView(_birthdayLayout.getVisibility() ==
			 * View.VISIBLE, _birthdayTextView.getText().toString(),
			 * hasBirthday); }
			 */
		}

		private void checkPlayBirthdaySong(BirthdayHelper entry) {
			if (entry.GetName().contains("Sandra Huber") || entry.GetName().contains("Jonas Schubert")) {
				_broadcastController.SendSimpleBroadcast(Broadcasts.PLAY_BIRTHDAY_SONG);
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
				if (_hasBirthday[index]) {
					if (_invert) {
						_birthdayAlarmViewArray[index].setBackgroundResource(R.drawable.circle_red);
					} else {
						_birthdayAlarmViewArray[index].setBackgroundResource(R.drawable.circle_yellow);
					}
				}
			}

			_invert = !_invert;
			_updateAlarmHandler.postDelayed(this, _invertTime);
		}
	};

	public BirthdayViewController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
		_broadcastController = new BroadcastController(_context);
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_screenEnabled = true;

		_birthdayLayout = (LinearLayout) ((Activity) _context).findViewById(R.id.birthdayLinearLayout);
		_birthdayAlarmViewArray[0] = (View) ((Activity) _context).findViewById(R.id.birthday1AlarmView);
		_birthdayTextViewArray[0] = (TextView) ((Activity) _context).findViewById(R.id.birthday1TextView);
		_birthdayAlarmViewArray[1] = (View) ((Activity) _context).findViewById(R.id.birthday2AlarmView);
		_birthdayTextViewArray[1] = (TextView) ((Activity) _context).findViewById(R.id.birthday2TextView);
		_birthdayAlarmViewArray[2] = (View) ((Activity) _context).findViewById(R.id.birthday3AlarmView);
		_birthdayTextViewArray[2] = (TextView) ((Activity) _context).findViewById(R.id.birthday3TextView);
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_receiverController.RegisterReceiver(_dateChangedReceiver, new String[] { Intent.ACTION_DATE_CHANGED });
			_receiverController.RegisterReceiver(_screenEnableReceiver, new String[] { Broadcasts.SCREEN_ENABLED });
			_receiverController.RegisterReceiver(_screenDisableReceiver,
					new String[] { Broadcasts.SCREEN_OFF, Broadcasts.SCREEN_SAVER });
			_receiverController.RegisterReceiver(_switchViewReceiver,
					new String[] { Broadcasts.SWITCH_BIRTHDAY_CALENDAR });
			_receiverController.RegisterReceiver(_updateViewReceiver, new String[] { Broadcasts.SHOW_BIRTHDAY_MODEL });

			_isInitialized = true;
			_logger.Debug("Initializing!");

			if (Enables.TESTING) {
				if (_birthdayViewTest == null) {
					_birthdayViewTest = new BirthdayViewControllerTest(_context);
				}
			}
		} else {
			_logger.Warn("Is ALREADY initialized!");
		}
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");

		_receiverController.UnregisterReceiver(_dateChangedReceiver);
		_receiverController.UnregisterReceiver(_screenEnableReceiver);
		_receiverController.UnregisterReceiver(_screenDisableReceiver);
		_receiverController.UnregisterReceiver(_switchViewReceiver);
		_receiverController.UnregisterReceiver(_updateViewReceiver);

		_isInitialized = false;
	}
}
