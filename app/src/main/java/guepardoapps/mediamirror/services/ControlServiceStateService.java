package guepardoapps.mediamirror.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import guepardoapps.library.toolset.controller.AndroidSystemController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class ControlServiceStateService extends Service {

    private static final String TAG = ControlServiceStateService.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private static final int TIMEOUT_CHECK = 60 * 1000;

    private boolean _isInitialized;

    private Context _context;
    private AndroidSystemController _systemController;

    private Handler _checkServicesHandler;

    private Runnable _checkServices = new Runnable() {
        public void run() {
            _logger.Debug("_checkServices");

            if (!_systemController.IsServiceRunning(MainService.class)) {
                _logger.Warn("MainService not running! Restarting!");
                startService(new Intent(_context, MainService.class));
            }

            _checkServicesHandler.postDelayed(_checkServices, TIMEOUT_CHECK);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!_isInitialized) {
            _logger = new SmartMirrorLogger(TAG);

            _isInitialized = true;

            _context = this;
            _systemController = new AndroidSystemController(_context);

            _checkServicesHandler = new Handler();
            _checkServices.run();
        }

        _logger.Debug("onStartCommand");

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
