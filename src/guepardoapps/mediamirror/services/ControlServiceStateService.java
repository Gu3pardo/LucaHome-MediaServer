package guepardoapps.mediamirror.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import guepardoapps.library.toolset.controller.AndroidSystemController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.view.Main;

public class ControlServiceStateService extends Service {

	private static final String TAG = ControlServiceStateService.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;
	private int _errorCount;

	private Context _context;
	private AndroidSystemController _systemController;

	private Handler _checkServicesHandler;

	private Runnable _checkServices = new Runnable() {
		public void run() {
			_logger.Debug("_checkServices");

			if (!_systemController.isServiceRunning(MainService.class)) {
				_logger.Warn("MainService not running! Restarting!");
				_errorCount++;
				_logger.Warn("_errorCount: " + String.valueOf(_errorCount));
				if (_errorCount >= 5) {
					// TODO: send warning mail to me!
					_logger.Info("TODO: send warning mail to me!");
				} else {
					Intent serviceIntent = new Intent(_context, MainService.class);
					startService(serviceIntent);
				}
			} else {
				_errorCount = 0;
			}

			if (!_systemController.isServiceRunning(TimeListenerService.class)) {
				_logger.Warn("TimeListenerService not running! Restarting!");
				_errorCount++;
				_logger.Warn("_errorCount: " + String.valueOf(_errorCount));
				if (_errorCount >= 5) {
					// TODO: send warning mail to me!
					_logger.Info("TODO: send warning mail to me!");
				} else {
					Intent serviceIntent = new Intent(_context, TimeListenerService.class);
					startService(serviceIntent);
				}
			} else {
				_errorCount = 0;
			}

			if (!_systemController.isBaseActivityRunning()) {
				_logger.Warn("MainActivity not running! Restarting!");
				_errorCount++;
				_logger.Warn("_errorCount: " + String.valueOf(_errorCount));
				if (_errorCount >= 5) {
					// TODO: send warning mail to me!
					_logger.Info("TODO: send warning mail to me!");
				} else {
					Intent intent = new Intent(_context, Main.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
			} else {
				_errorCount = 0;
			}

			_checkServicesHandler.postDelayed(_checkServices, 60 * 1000);
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (!_isInitialized) {
			_logger = new SmartMirrorLogger(TAG);

			_isInitialized = true;
			_errorCount = 0;

			_context = this;
			_systemController = new AndroidSystemController(_context);

			_checkServicesHandler = new Handler();
			_checkServices.run();
		}

		_logger.Debug("onStartCommand");

		return 0;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
