package guepardoapps.mediamirror.view.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;

import guepardoapps.library.toastview.ToastView;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.Enables;
import guepardoapps.mediamirror.controller.MediaMirrorDialogController;
import guepardoapps.mediamirror.model.RaspberryModel;
import guepardoapps.mediamirror.model.helper.RaspberryTemperatureHelper;

import guepardoapps.test.RaspberryViewControllerTest;

public class RaspberryViewController {

	private static final String TAG = RaspberryViewController.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;
	private boolean _screenEnabled;

	private RaspberryModel _raspberryModel;
	private SerializableList<MenuDto> _menu;
	private SerializableList<ShoppingEntryDto> _shoppingList;
	private SerializableList<WirelessSocketDto> _socketList;

	private Context _context;
	private MediaMirrorDialogController _dialogController;
	private ReceiverController _receiverController;

	private View _raspberryAlarm1TextView;
	private TextView _raspberryName1TextView;
	private TextView _raspberryTemperature1TextView;

	private RaspberryTemperatureHelper _raspberryTemperatureHelper;
	private RaspberryViewControllerTest _raspberryViewTest;

	private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_logger.Debug("_updateViewReceiver onReceive");
			RaspberryModel model = (RaspberryModel) intent.getSerializableExtra(Bundles.RASPBERRY_DATA_MODEL);
			if (model != null) {
				_logger.Debug(model.toString());
				_raspberryModel = model;

				_raspberryAlarm1TextView.setBackgroundResource(
						_raspberryTemperatureHelper.GetIcon(_raspberryModel.GetRaspberry1Temperature()));
				_raspberryName1TextView.setText(_raspberryModel.GetRaspberry1Name());
				_raspberryTemperature1TextView.setText(_raspberryModel.GetRaspberry1Temperature());
			} else {
				_logger.Warn("model is null!");
			}

			if (Enables.TESTING) {
				_raspberryViewTest.ValidateView(_raspberryName1TextView.getText().toString(),
						_raspberryTemperature1TextView.getText().toString());
			}
		}
	};

	private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = true;

			_raspberryAlarm1TextView = (View) ((Activity) _context).findViewById(R.id.temperatureRaspberry1Alarm);
			_raspberryName1TextView = (TextView) ((Activity) _context).findViewById(R.id.temperatureRaspberry1Name);
			_raspberryTemperature1TextView = (TextView) ((Activity) _context)
					.findViewById(R.id.temperatureRaspberry1Value);
		}
	};

	private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = false;
		}
	};

	private BroadcastReceiver _menuListReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			@SuppressWarnings("unchecked")
			SerializableList<MenuDto> menu = (SerializableList<MenuDto>) intent.getSerializableExtra(Bundles.MENU);
			if (menu != null) {
				_menu = menu;
			}
		}
	};

	private BroadcastReceiver _shoppingListReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			@SuppressWarnings("unchecked")
			SerializableList<ShoppingEntryDto> shoppingList = (SerializableList<ShoppingEntryDto>) intent
					.getSerializableExtra(Bundles.SHOPPING_LIST);
			if (shoppingList != null) {
				_shoppingList = shoppingList;
			}
		}
	};

	private BroadcastReceiver _socketListReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			@SuppressWarnings("unchecked")
			SerializableList<WirelessSocketDto> socketList = (SerializableList<WirelessSocketDto>) intent
					.getSerializableExtra(Bundles.SOCKET_LIST);
			if (socketList != null) {
				_socketList = socketList;
			}
		}
	};

	public RaspberryViewController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
		_dialogController = new MediaMirrorDialogController(_context);
		_receiverController = new ReceiverController(_context);
		_raspberryTemperatureHelper = new RaspberryTemperatureHelper();
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_screenEnabled = true;

		_raspberryAlarm1TextView = (View) ((Activity) _context).findViewById(R.id.temperatureRaspberry1Alarm);
		_raspberryName1TextView = (TextView) ((Activity) _context).findViewById(R.id.temperatureRaspberry1Name);
		_raspberryTemperature1TextView = (TextView) ((Activity) _context).findViewById(R.id.temperatureRaspberry1Value);
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_receiverController.RegisterReceiver(_updateViewReceiver,
					new String[] { Broadcasts.SHOW_RASPBERRY_DATA_MODEL });
			_receiverController.RegisterReceiver(_screenEnableReceiver, new String[] { Broadcasts.SCREEN_ENABLED });
			_receiverController.RegisterReceiver(_screenDisableReceiver,
					new String[] { Broadcasts.SCREEN_OFF, Broadcasts.SCREEN_SAVER });
			_receiverController.RegisterReceiver(_menuListReceiver, new String[] { Broadcasts.MENU });
			_receiverController.RegisterReceiver(_shoppingListReceiver, new String[] { Broadcasts.SHOPPING_LIST });
			_receiverController.RegisterReceiver(_socketListReceiver, new String[] { Broadcasts.SOCKET_LIST });

			_isInitialized = true;
			_logger.Debug("Initializing!");

			if (Enables.TESTING) {
				if (_raspberryViewTest == null) {
					_raspberryViewTest = new RaspberryViewControllerTest(_context);
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
		_receiverController.UnregisterReceiver(_menuListReceiver);
		_receiverController.UnregisterReceiver(_shoppingListReceiver);
		_receiverController.UnregisterReceiver(_socketListReceiver);

		_isInitialized = false;
	}

	public void showMenuListDialog(View view) {
		_logger.Debug("showMenuListDialog");
		if (_shoppingList != null) {
			_dialogController.ShowMenuListDialog(_menu);
		} else {
			_logger.Error("_menu is null!");
			ToastView.warning(_context, "Menu is null!!", Toast.LENGTH_LONG).show();
		}
	}

	public void showShoppingListDialog(View view) {
		_logger.Debug("showShoppingListDialog");
		if (_shoppingList != null) {
			_dialogController.ShowShoppingListDialog(_shoppingList);
		} else {
			_logger.Error("_shoppingList is null!");
			ToastView.warning(_context, "ShoppingList is null!!", Toast.LENGTH_LONG).show();
		}
	}

	public void showSocketsDialog(View view) {
		_logger.Debug("showSocketsDialog");
		if (_socketList != null) {
			_dialogController.ShowSocketListDialog(_socketList);
		} else {
			_logger.Error("_socketList is null!");
			ToastView.warning(_context, "SocketList is null!!", Toast.LENGTH_LONG).show();
		}
	}

	public void showTemperatureGraph(View view) {
		_logger.Debug("showTemperatureGraph");
		String url = _raspberryModel.GetRaspberry1TemperatureGraphUrl();
		if (url.length() > 0) {
			_dialogController.ShowTemperatureGraphDialog(_raspberryModel.GetRaspberry1TemperatureGraphUrl());
		} else {
			_logger.Warn("invalid URL!");
			ToastView.warning(_context, "Invalid URL!", Toast.LENGTH_LONG).show();
		}
	}
}
