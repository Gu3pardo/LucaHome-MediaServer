package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import guepardoapps.library.lucahome.common.dto.TemperatureDto;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.controller.ServiceController;
import guepardoapps.library.lucahome.converter.json.JsonDataToTemperatureConverter;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.RaspPiConstants;
import guepardoapps.mediamirror.view.model.RaspberryModel;

public class TemperatureUpdater {

	private static final String TAG = TemperatureUpdater.class.getSimpleName();
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
			DownloadTemperature();
			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateReceiver onReceive");
			String[] temperatureStringArray = intent.getStringArrayExtra(Bundles.RASPBERRY_DATA_MODEL);
			if (temperatureStringArray != null) {
				SerializableList<TemperatureDto> temperatureList = JsonDataToTemperatureConverter
						.GetList(temperatureStringArray);

				RaspberryModel model = null;
				if (temperatureList != null) {
					if (temperatureList.getSize() == RaspPiConstants.SERVER_URLs.length) {
						model = new RaspberryModel(temperatureList.getValue(0).GetArea(),
								temperatureList.getValue(0).GetTemperatureString(),
								temperatureList.getValue(0).GetGraphPath());

					}
				}
				if (model == null) {
					model = new RaspberryModel("not found", "", "");
				}
				_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_RASPBERRY_DATA_MODEL,
						Bundles.RASPBERRY_DATA_MODEL, model);
			}
		}
	};

	private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_performUpdateReceiver onReceive");
			DownloadTemperature();
		}
	};

	public TemperatureUpdater(Context context) {
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
		_receiverController.RegisterReceiver(_updateReceiver,
				new String[] { Broadcasts.DOWNLOAD_TEMPERATURE_FINISHED });
		_receiverController.RegisterReceiver(_performUpdateReceiver,
				new String[] { Broadcasts.PERFORM_TEMPERATURE_UPDATE });
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

	public void DownloadTemperature() {
		_logger.Debug("startDownloadTemperature");

		_serviceController.StartRestService(RaspPiConstants.USER, RaspPiConstants.PASSWORD,
				Bundles.RASPBERRY_DATA_MODEL, RaspPiConstants.GET_TEMPERATURES,
				Broadcasts.DOWNLOAD_TEMPERATURE_FINISHED, LucaObject.TEMPERATURE, RaspberrySelection.BOTH);
	}
}
