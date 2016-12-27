package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.Tools;
import guepardoapps.mediamirror.model.IpAdressModel;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;
import guepardoapps.toolset.controller.UserInformationController;

public class IpAdressViewUpdater {

	private static final String TAG = IpAdressViewUpdater.class.getName();
	private SmartMirrorLogger _logger;

	private Handler _updater;

	private Context _context;
	private BroadcastController _broadcastController;
	private ReceiverController _receiverController;
	private UserInformationController _userInformationController;

	private int _updateTime;

	private Runnable _updateRunnable = new Runnable() {
		public void run() {
			_logger.Debug("_updateRunnable run");
			if (!Tools.IsMuteTime()) {
				getCurrentLocalIpAddress();
			}
			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_performUpdateReceiver onReceive");
			getCurrentLocalIpAddress();
		}
	};

	public IpAdressViewUpdater(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_updater = new Handler();
		_context = context;
		_broadcastController = new BroadcastController(_context);
		_receiverController = new ReceiverController(_context);
		_userInformationController = new UserInformationController(_context);
	}

	public void Start(int updateTime) {
		_logger.Debug("Initialize");
		_updateTime = updateTime;
		_logger.Debug("UpdateTime is: " + String.valueOf(_updateTime));
		_receiverController.RegisterReceiver(_performUpdateReceiver,
				new String[] { Constants.BROADCAST_PERFORM_IP_ADDRESS_UPDATE });
		_updateRunnable.run();
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_updater.removeCallbacks(_updateRunnable);
		_receiverController.UnregisterReceiver(_performUpdateReceiver);
	}

	private void getCurrentLocalIpAddress() {
		_logger.Debug("getCurrentLocalIpAddress");

		String ip = _userInformationController.GetIp();
		_logger.Debug("IP adress is: " + ip);

		IpAdressModel model = new IpAdressModel(true, ip);
		_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_SHOW_IP_ADRESS_MODEL,
				Constants.BUNDLE_IP_ADRESS_MODEL, model);
	}
}
