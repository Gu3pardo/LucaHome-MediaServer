package guepardoapps.mediamirror.view.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.model.*;
import guepardoapps.mediamirror.test.IpAdressViewControllerTest;
import guepardoapps.mediamirror.R;

import guepardoapps.toolset.controller.ReceiverController;

public class IpAdressViewController {

	private static final String TAG = IpAdressViewController.class.getName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;
	private boolean _screenEnabled;

	private Context _context;
	private ReceiverController _receiverController;

	private TextView _ipAdressTextView;

	private IpAdressViewControllerTest _ipAdressViewTest;

	public IpAdressViewController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_screenEnabled = true;
		
		_ipAdressTextView = (TextView) ((Activity) _context).findViewById(R.id.ipAdressTextView);
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_receiverController.RegisterReceiver(_updateViewReceiver,
					new String[] { Constants.BROADCAST_SHOW_IP_ADRESS_MODEL });
			_receiverController.RegisterReceiver(_screenEnableReceiver,
					new String[] { Constants.BROADCAST_SCREEN_ENABLED });
			_receiverController.RegisterReceiver(_screenDisableReceiver,
					new String[] { Constants.BROADCAST_SCREEN_OFF, Constants.BROADCAST_SCREEN_SAVER });
			_isInitialized = true;
			_logger.Debug("Initializing!");

			if (Constants.TESTING_ENABLED) {
				if (_ipAdressViewTest == null) {
					_ipAdressViewTest = new IpAdressViewControllerTest(_context);
				}
			}
		} else {
			_logger.Warn("Is ALREADY initialized!");
		}
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
		_receiverController.UnregisterReceiver(_updateViewReceiver);
		_receiverController.UnregisterReceiver(_screenEnableReceiver);
		_receiverController.UnregisterReceiver(_screenDisableReceiver);
		_isInitialized = false;
	}

	private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_logger.Debug("_updateViewReceiver onReceive");
			IpAdressModel model = (IpAdressModel) intent.getSerializableExtra(Constants.BUNDLE_IP_ADRESS_MODEL);
			if (model != null) {
				_logger.Debug(model.toString());
				if (model.GetIsVisible()) {
					_ipAdressTextView.setVisibility(View.VISIBLE);
					_ipAdressTextView.setText(model.GetIpAdress());
				} else {
					_ipAdressTextView.setVisibility(View.INVISIBLE);
				}
			} else {
				_logger.Warn("model is null!");
			}

			if (Constants.TESTING_ENABLED) {
				_ipAdressViewTest.ValidateView(_ipAdressTextView.getVisibility() == View.VISIBLE,
						_ipAdressTextView.getText().toString());
			}
		}
	};

	private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = true;
			
			_ipAdressTextView = (TextView) ((Activity) _context).findViewById(R.id.ipAdressTextView);
		}
	};

	private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = false;
		}
	};
}
