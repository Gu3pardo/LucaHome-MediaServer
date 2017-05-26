package guepardoapps.mediamirror.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.Locale;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.MediaMirrorSelection;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.controller.ServiceController;

import guepardoapps.library.toolset.controller.NetworkController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.RaspPiConstants;

public class BatterySocketController {

    private static final String TAG = BatterySocketController.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private static final int UPPER_BATTERY_LIMIT = 90;
    private static final int LOWER_BATTERY_LIMIT = 10;

    private boolean _isInitialized;
    private boolean _isSocketActive = false;

    private Context _context;
    private NetworkController _networkController;
    private ReceiverController _receiverController;
    private ServiceController _serviceController;

    private BroadcastReceiver _batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            _logger.Debug(String.format(Locale.GERMAN, "Received new battery level %d", level));
            if (level > UPPER_BATTERY_LIMIT) {
                disableBatterySocket();
            } else if (level < LOWER_BATTERY_LIMIT) {
                enableBatterySocket();
            }
        }
    };

    public BatterySocketController(@NonNull Context context) {
        _logger = new SmartMirrorLogger(TAG);
        _logger.Info(TAG + " created");
        _context = context;
        _receiverController = new ReceiverController(_context);
        _networkController = new NetworkController(_context, null);
        _serviceController = new ServiceController(_context);
    }

    public void Start() {
        _logger.Debug("Start");
        if (!_isInitialized) {
            _logger.Debug("Initializing!");
            _receiverController.RegisterReceiver(_batteryInfoReceiver, new String[]{Intent.ACTION_BATTERY_CHANGED});
            _isInitialized = true;
        } else {
            _logger.Warn("Is ALREADY initialized!");
        }
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _receiverController.UnregisterReceiver(_batteryInfoReceiver);
        _isInitialized = false;
    }

    private void enableBatterySocket() {
        if (_isSocketActive) {
            _logger.Warn("Already activated socket!");
            return;
        }

        _logger.Debug("enableBatterySocket");
        setBatterySocket(true);
    }

    private void disableBatterySocket() {
        if (!_isSocketActive) {
            _logger.Warn("Already deactivated socket!");
            return;
        }

        _logger.Debug("disableBatterySocket");
        setBatterySocket(false);
    }

    private void setBatterySocket(boolean enable) {
        String localIp = _networkController.GetIpAddress().replace("SiteLocalAddress: ", "").replace("\n", "");
        _logger.Debug("Local ip is " + localIp);

        if (localIp != null) {
            try {
                MediaMirrorSelection mediaMirrorSelection = MediaMirrorSelection.GetByIp(localIp);
                _logger.Debug(String.format("MediaMirrorSelection is %s", mediaMirrorSelection));

                String localSocket = mediaMirrorSelection.GetSocket();
                if (localSocket != null) {
                    _logger.Debug(String.format(Locale.GERMAN, "setBatterySocket %s to %s", localSocket, ((enable) ? RaspPiConstants.SOCKET_STATE_ON : RaspPiConstants.SOCKET_STATE_OFF)));

                    _serviceController.StartRestService(
                            RaspPiConstants.USER,
                            RaspPiConstants.PASSWORD,
                            "",
                            RaspPiConstants.SET_SOCKET + localSocket + ((enable) ? RaspPiConstants.SOCKET_STATE_ON : RaspPiConstants.SOCKET_STATE_OFF),
                            "",
                            LucaObject.WIRELESS_SOCKET,
                            RaspberrySelection.BOTH);

                    _isSocketActive = enable;

                    Toasty.success(_context,
                            String.format("Set socket %s to %s", localSocket, _isSocketActive ? "ON" : "OFF"),
                            Toast.LENGTH_LONG).show();
                } else {
                    _logger.Error("Did not found socket for " + localIp);
                    Toasty.error(_context, "Did not found socket for " + localIp, Toast.LENGTH_LONG).show();
                }
            } catch (Exception ex) {
                _logger.Error(ex.toString());
                Toasty.error(_context, "Did not found socket for " + localIp, Toast.LENGTH_LONG).show();
            }
        }
    }
}
