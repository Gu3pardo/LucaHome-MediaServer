package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.converter.json.JsonDataToSocketConverter;

import guepardoapps.library.toastview.ToastView;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.RaspPiConstants;
import guepardoapps.mediamirror.services.RESTService;

import guepardoapps.toolset.common.classes.SerializableList;
import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class SocketListUpdater {

	private static final String TAG = SocketListUpdater.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private Handler _updater;

	private Context _context;
	private BroadcastController _broadcastController;
	private ReceiverController _receiverController;

	private int _updateTime;

	private Runnable _updateRunnable = new Runnable() {
		public void run() {
			_logger.Debug("_updateRunnable run");
			DownloadSocketList();
			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateReceiver onReceive");
			String[] socketStringArray = intent.getStringArrayExtra(Bundles.SOCKET_LIST);
			if (socketStringArray != null) {
				SerializableList<WirelessSocketDto> socketList = JsonDataToSocketConverter.GetList(socketStringArray);
				if (socketList != null) {
					_broadcastController.SendSerializableBroadcast(Broadcasts.SOCKET_LIST, Bundles.SOCKET_LIST,
							socketList);
				} else {
					ToastView.error(_context, "Failed to convert socket list from string array!", Toast.LENGTH_LONG)
							.show();
				}
			}
		}
	};

	private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_performUpdateReceiver onReceive");
			DownloadSocketList();
		}
	};

	public SocketListUpdater(Context context) {
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
		_receiverController.RegisterReceiver(_updateReceiver, new String[] { Broadcasts.DOWNLOAD_SOCKET_FINISHED });
		_receiverController.RegisterReceiver(_performUpdateReceiver, new String[] { Broadcasts.PERFORM_SOCKET_UPDATE });
		_updateRunnable.run();
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_updater.removeCallbacks(_updateRunnable);
		_receiverController.UnregisterReceiver(_updateReceiver);
		_receiverController.UnregisterReceiver(_performUpdateReceiver);
	}

	public void DownloadSocketList() {
		_logger.Debug("startDownloadSocketList");

		Intent serviceIntent = new Intent(_context, RESTService.class);
		Bundle serviceData = new Bundle();

		serviceData.putString(RaspPiConstants.BUNDLE_REST_ACTION, RaspPiConstants.GET_SOCKETS);
		serviceData.putString(RaspPiConstants.BUNDLE_REST_DATA, Bundles.SOCKET_LIST);
		serviceData.putString(RaspPiConstants.BUNDLE_REST_BROADCAST, Broadcasts.DOWNLOAD_SOCKET_FINISHED);

		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}
}
