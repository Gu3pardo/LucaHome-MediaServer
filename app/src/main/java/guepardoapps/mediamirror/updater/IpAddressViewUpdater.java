package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;
import guepardoapps.library.toolset.controller.UserInformationController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.view.model.IpAddressModel;

public class IpAddressViewUpdater {

    private static final String TAG = IpAddressViewUpdater.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private Handler _updater;

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;
    private UserInformationController _userInformationController;

    private int _updateTime;
    private boolean _isRunning;

    private Runnable _updateRunnable = new Runnable() {
        public void run() {
            _logger.Debug("_updateRunnable run");
            GetCurrentLocalIpAddress();
            _updater.postDelayed(_updateRunnable, _updateTime);
        }
    };

    private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_performUpdateReceiver onReceive");
            GetCurrentLocalIpAddress();
        }
    };

    public IpAddressViewUpdater(@NonNull Context context) {
        _logger = new SmartMirrorLogger(TAG);
        _updater = new Handler();
        _broadcastController = new BroadcastController(context);
        _receiverController = new ReceiverController(context);
        _userInformationController = new UserInformationController(context);
    }

    public void Start(int updateTime) {
        _logger.Debug("Initialize");

        if (_isRunning) {
            _logger.Warn("Already running!");
            return;
        }

        _updateTime = updateTime;
        _logger.Debug("UpdateTime is: " + String.valueOf(_updateTime));
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_IP_ADDRESS_UPDATE});
        _updateRunnable.run();

        _isRunning = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _updater.removeCallbacks(_updateRunnable);
        _receiverController.Dispose();
        _isRunning = false;
    }

    public IpAddressModel GetCurrentLocalIpAddress() {
        _logger.Debug("getCurrentLocalIpAddress");

        String ip = _userInformationController.GetIp();
        _logger.Debug("IP address is: " + ip);

        IpAddressModel model = new IpAddressModel(true, ip);
        _broadcastController.SendSerializableBroadcast(
                Broadcasts.SHOW_IP_ADDRESS_MODEL,
                Bundles.IP_ADDRESS_MODEL,
                model);

        return model;
    }
}
