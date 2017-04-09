package guepardoapps.mediamirror.view.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.Enables;
import guepardoapps.mediamirror.view.model.*;
import guepardoapps.test.IpAdressViewControllerTest;

public class IpAddressViewController {

	private static final String TAG = IpAddressViewController.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;
	private boolean _screenEnabled;

	private Context _context;
	private ReceiverController _receiverController;

	private TextView _ipAdressTextView;

	private IpAdressViewControllerTest _ipAdressViewTest;

	public IpAddressViewController(Context context) {
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
		_receiverController.Dispose();
		_isInitialized = false;
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_receiverController.RegisterReceiver(_updateViewReceiver,
					new String[] { Broadcasts.SHOW_IP_ADDRESS_MODEL });
			_receiverController.RegisterReceiver(_screenEnableReceiver, new String[] { Broadcasts.SCREEN_ENABLED });
			_receiverController.RegisterReceiver(_screenDisableReceiver,
					new String[] { Broadcasts.SCREEN_OFF, Broadcasts.SCREEN_SAVER });
			_isInitialized = true;
			_logger.Debug("Initializing!");

			if (Enables.TESTING) {
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
		_receiverController.Dispose();
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
			IpAdressModel model = (IpAdressModel) intent.getSerializableExtra(Bundles.IP_ADDRESS_MODEL);
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

			if (Enables.TESTING) {
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
