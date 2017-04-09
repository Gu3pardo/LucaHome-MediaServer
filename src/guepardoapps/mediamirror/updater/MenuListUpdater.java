package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.controller.ServiceController;
import guepardoapps.library.lucahome.converter.json.JsonDataToMenuConverter;

import guepardoapps.library.toastview.ToastView;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.RaspPiConstants;

public class MenuListUpdater {

	private static final String TAG = MenuListUpdater.class.getSimpleName();
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
			DownloadMenuList();
			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateReceiver onReceive");
			String[] menuStringArray = intent.getStringArrayExtra(Bundles.MENU);
			if (menuStringArray != null) {
				SerializableList<MenuDto> menu = JsonDataToMenuConverter.GetList(menuStringArray);
				if (menu != null) {
					_broadcastController.SendSerializableBroadcast(Broadcasts.MENU, Bundles.MENU, menu);
				} else {
					ToastView.error(_context, "Failed to convert menu from string array!", Toast.LENGTH_LONG).show();
				}
			}
		}
	};

	private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_performUpdateReceiver onReceive");
			DownloadMenuList();
		}
	};

	public MenuListUpdater(Context context) {
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
		_receiverController.RegisterReceiver(_updateReceiver, new String[] { Broadcasts.DOWNLOAD_MENU_FINISHED });
		_receiverController.RegisterReceiver(_performUpdateReceiver, new String[] { Broadcasts.PERFORM_MENU_UPDATE });
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

	public void DownloadMenuList() {
		_logger.Debug("startDownloadMenuList");

		_serviceController.StartRestService(RaspPiConstants.USER, RaspPiConstants.PASSWORD, Bundles.MENU,
				RaspPiConstants.GET_MENU, Broadcasts.DOWNLOAD_MENU_FINISHED, LucaObject.MENU, RaspberrySelection.BOTH);
	}
}
