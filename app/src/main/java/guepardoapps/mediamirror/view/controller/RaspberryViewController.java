package guepardoapps.mediamirror.view.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.controller.MediaMirrorDialogController;
import guepardoapps.mediamirror.view.model.RaspberryModel;
import guepardoapps.mediamirror.view.model.helper.RaspberryTemperatureHelper;

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

    private View _raspberryAlarmTextView;
    private TextView _raspberryNameTextView;
    private TextView _raspberryTemperatureTextView;

    private RaspberryTemperatureHelper _raspberryTemperatureHelper;

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

    private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = false;
        }
    };

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = true;

            _raspberryAlarmTextView = ((Activity) _context).findViewById(R.id.temperatureRaspberryAlarm);
            _raspberryNameTextView = ((Activity) _context).findViewById(R.id.temperatureRaspberryName);
            _raspberryTemperatureTextView = ((Activity) _context).findViewById(R.id.temperatureRaspberryValue);
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

                _raspberryAlarmTextView.setBackgroundResource(_raspberryTemperatureHelper.GetIcon(_raspberryModel.GetRaspberryTemperature()));
                _raspberryNameTextView.setText(_raspberryModel.GetRaspberryName());
                _raspberryTemperatureTextView.setText(_raspberryModel.GetRaspberryTemperature());
            } else {
                _logger.Warn("model is null!");
            }
        }
    };

    public RaspberryViewController(@NonNull Context context) {
        _logger = new SmartMirrorLogger(TAG);
        _context = context;
        _dialogController = new MediaMirrorDialogController(_context);
        _receiverController = new ReceiverController(_context);
        _raspberryTemperatureHelper = new RaspberryTemperatureHelper();
    }

    public void onCreate() {
        _logger.Debug("onCreate");

        _screenEnabled = true;

        _raspberryAlarmTextView = ((Activity) _context).findViewById(R.id.temperatureRaspberryAlarm);
        _raspberryNameTextView = ((Activity) _context).findViewById(R.id.temperatureRaspberryName);
        _raspberryTemperatureTextView = ((Activity) _context).findViewById(R.id.temperatureRaspberryValue);
    }

    public void onPause() {
        _logger.Debug("onPause");
    }

    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_menuListReceiver, new String[]{Broadcasts.MENU});
            _receiverController.RegisterReceiver(_screenDisableReceiver, new String[]{Broadcasts.SCREEN_OFF});
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_shoppingListReceiver, new String[]{Broadcasts.SHOPPING_LIST});
            _receiverController.RegisterReceiver(_socketListReceiver, new String[]{Broadcasts.SOCKET_LIST});
            _receiverController.RegisterReceiver(_updateViewReceiver, new String[]{Broadcasts.SHOW_RASPBERRY_DATA_MODEL});

            _isInitialized = true;
            _logger.Debug("Initializing!");
        } else {
            _logger.Warn("Is ALREADY initialized!");
        }
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _isInitialized = false;
    }

    public void ShowMenuListDialog(@NonNull View view) {
        _logger.Debug(String.format(Locale.getDefault(), "showMenuListDialog: %s", view));
        if (_shoppingList != null) {
            _dialogController.ShowMenuListDialog(_menu);
        } else {
            _logger.Error("_menu is null!");
            Toasty.warning(_context, "Menu is null!!", Toast.LENGTH_LONG).show();
        }
    }

    public void ShowShoppingListDialog(@NonNull View view) {
        _logger.Debug(String.format(Locale.getDefault(), "showShoppingListDialog: %s", view));
        if (_shoppingList != null) {
            _dialogController.ShowShoppingListDialog(_shoppingList);
        } else {
            _logger.Error("_shoppingList is null!");
            Toasty.warning(_context, "ShoppingList is null!!", Toast.LENGTH_LONG).show();
        }
    }

    public void ShowSocketsDialog(@NonNull View view) {
        _logger.Debug(String.format(Locale.getDefault(), "showSocketsDialog: %s", view));
        if (_socketList != null) {
            _dialogController.ShowSocketListDialog(_socketList);
        } else {
            _logger.Error("_socketList is null!");
            Toasty.warning(_context, "SocketList is null!!", Toast.LENGTH_LONG).show();
        }
    }

    public void ShowTemperatureGraph(@NonNull View view) {
        _logger.Debug(String.format(Locale.getDefault(), "showTemperatureGraph: %s", view));
        String url = _raspberryModel.GetRaspberryTemperatureGraphUrl();
        if (url.length() > 0) {
            _dialogController.ShowTemperatureGraphDialog(_raspberryModel.GetRaspberryTemperatureGraphUrl());
        } else {
            _logger.Warn("invalid URL!");
            Toasty.warning(_context, "Invalid URL!", Toast.LENGTH_LONG).show();
        }
    }
}
