package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.converter.json.JsonDataToShoppingListConverter;

import guepardoapps.library.toastview.ToastView;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.RaspPiConstants;
import guepardoapps.mediamirror.services.RESTService;

public class ShoppingListUpdater {

	private static final String TAG = ShoppingListUpdater.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private Handler _updater;

	private Context _context;
	private BroadcastController _broadcastController;
	private ReceiverController _receiverController;

	private int _updateTime;

	private Runnable _updateRunnable = new Runnable() {
		public void run() {
			_logger.Debug("_updateRunnable run");
			DownloadShoppingList();
			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateReceiver onReceive");
			String[] shoppingListStringArray = intent.getStringArrayExtra(Bundles.SHOPPING_LIST);
			if (shoppingListStringArray != null) {
				SerializableList<ShoppingEntryDto> shoppingList = JsonDataToShoppingListConverter
						.GetList(shoppingListStringArray);
				if (shoppingList != null) {
					_broadcastController.SendSerializableBroadcast(Broadcasts.SHOPPING_LIST, Bundles.SHOPPING_LIST,
							shoppingList);
				} else {
					ToastView.error(_context, "Failed to convert shopping list from string array!", Toast.LENGTH_LONG)
							.show();
				}
			}
		}
	};

	private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_performUpdateReceiver onReceive");
			DownloadShoppingList();
		}
	};

	private BroadcastReceiver _reloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_reloadReceiver onReceive");
			DownloadShoppingList();
		}
	};

	public ShoppingListUpdater(Context context) {
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
				new String[] { Broadcasts.DOWNLOAD_SHOPPING_LIST_FINISHED });
		_receiverController.RegisterReceiver(_performUpdateReceiver,
				new String[] { Broadcasts.PERFORM_SHOPPING_LIST_UPDATE });
		_receiverController.RegisterReceiver(_reloadReceiver,
				new String[] { guepardoapps.library.lucahome.common.constants.Broadcasts.RELOAD_SHOPPING_LIST });
		_updateRunnable.run();
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_updater.removeCallbacks(_updateRunnable);
		_receiverController.UnregisterReceiver(_updateReceiver);
		_receiverController.UnregisterReceiver(_performUpdateReceiver);
		_receiverController.UnregisterReceiver(_reloadReceiver);
	}

	public void DownloadShoppingList() {
		_logger.Debug("startDownloadShoppingList");

		Intent serviceIntent = new Intent(_context, RESTService.class);
		Bundle serviceData = new Bundle();

		serviceData.putString(RaspPiConstants.BUNDLE_REST_ACTION, RaspPiConstants.GET_SHOPPING_LIST);
		serviceData.putString(RaspPiConstants.BUNDLE_REST_DATA, Bundles.SHOPPING_LIST);
		serviceData.putString(RaspPiConstants.BUNDLE_REST_BROADCAST, Broadcasts.DOWNLOAD_SHOPPING_LIST_FINISHED);

		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}
}
