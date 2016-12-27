package guepardoapps.mediamirror.view.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.model.*;
import guepardoapps.mediamirror.test.BirthdayViewControllerTest;
import guepardoapps.mediamirror.R;

import guepardoapps.toolset.controller.ReceiverController;

public class BirthdayViewController {

	private static final String TAG = BirthdayViewController.class.getName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;

	private Context _context;
	private ReceiverController _receiverController;

	private RelativeLayout _birthdayLayout;
	private View _birthdayAlarmView;
	private TextView _birthdayTextView;

	private Handler _updateAlarmHandler = new Handler();
	private int _invertTime = 1000;

	private BirthdayViewControllerTest _birthdayViewTest;

	public BirthdayViewController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_birthdayLayout = (RelativeLayout) ((Activity) _context).findViewById(R.id.birthdayLayout);
		_birthdayLayout.setVisibility(View.GONE);
		_birthdayAlarmView = (View) ((Activity) _context).findViewById(R.id.birthdayAlarmView);
		_birthdayTextView = (TextView) ((Activity) _context).findViewById(R.id.birthdayTextView);
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_receiverController.RegisterReceiver(_updateViewReceiver,
					new String[] { Constants.BROADCAST_SHOW_BIRTHDAY_MODEL });
			_isInitialized = true;
			_logger.Debug("Initializing!");

			if (Constants.TESTING_ENABLED) {
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
		_receiverController.UnregisterReceiver(_updateViewReceiver);
		_isInitialized = false;
	}

	private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateViewReceiver onReceive");
			BirthdayModel model = (BirthdayModel) intent.getSerializableExtra(Constants.BUNDLE_BIRTHDAY_MODEL);
			if (model != null) {
				_logger.Debug(model.toString());
				if (model.GetIsVisible()) {
					_birthdayLayout.setVisibility(View.VISIBLE);
					_birthdayTextView.setText(model.GetText());
					_updateAlarmHandler.postDelayed(_updateAlarmViewRunnable, _invertTime);
				} else {
					_birthdayLayout.setVisibility(View.INVISIBLE);
					_updateAlarmHandler.removeCallbacks(_updateAlarmViewRunnable);
				}
			} else {
				_logger.Warn("model is null!");
			}

			if (Constants.TESTING_ENABLED) {
				boolean hasBirthday = false;
				if (model != null) {
					hasBirthday = model.GetHasBirthday();
				}
				_birthdayViewTest.ValidateView(_birthdayLayout.getVisibility() == View.VISIBLE,
						_birthdayTextView.getText().toString(), hasBirthday);
			}
		}
	};

	private Runnable _updateAlarmViewRunnable = new Runnable() {
		private boolean invert;

		public void run() {
			_logger.Debug("Inverting birthday alarm view!");
			if (invert) {
				_birthdayAlarmView.setBackgroundResource(R.drawable.circle_red);
			} else {
				_birthdayAlarmView.setBackgroundResource(R.drawable.circle_yellow);
			}
			invert = !invert;
			_updateAlarmHandler.postDelayed(this, _invertTime);
		}
	};
}
