package guepardoapps.mediamirror.updater;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.RaspPiConstants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.Tools;
import guepardoapps.mediamirror.common.converter.JsonDataToTemperatureConverter;
import guepardoapps.mediamirror.model.RaspberryModel;
import guepardoapps.mediamirror.model.helper.TemperatureHelper;
import guepardoapps.mediamirror.services.RESTService;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class TemperatureUpdater {

	private static final String TAG = TemperatureUpdater.class.getName();
	private SmartMirrorLogger _logger;

	private Handler _updater;

	private Context _context;
	private BroadcastController _broadcastController;
	private ReceiverController _receiverController;

	private int _updateTime;

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
			String[] temperatureStringArray = intent.getStringArrayExtra(Constants.BUNDLE_RASPBERRY_DATA_MODEL);
			if (temperatureStringArray != null) {
				ArrayList<TemperatureHelper> temperatureList = JsonDataToTemperatureConverter
						.GetList(temperatureStringArray);

				RaspberryModel model = null;
				if (temperatureList != null) {
					if (temperatureList.size() == RaspPiConstants.SERVER_URLs.length) {
						model = new RaspberryModel(temperatureList.get(0).GetArea(),
								temperatureList.get(0).GetTemperatureString(), temperatureList.get(0).GetGraphUrl());

					}
				}
				if (model == null) {
					model = new RaspberryModel("not found", "", "");
				}
				_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_SHOW_RASPBERRY_DATA_MODEL,
						Constants.BUNDLE_RASPBERRY_DATA_MODEL, model);
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
	}

	public void Start(int updateTime) {
		_logger.Debug("Initialize");
		_updateTime = updateTime;
		_logger.Debug("UpdateTime is: " + String.valueOf(_updateTime));
		_receiverController.RegisterReceiver(_updateReceiver,
				new String[] { Constants.BROADCAST_DOWNLOAD_TEMPERATURE_FINISHED });
		_receiverController.RegisterReceiver(_performUpdateReceiver,
				new String[] { Constants.BROADCAST_PERFORM_TEMPERATURE_UPDATE });
		_updateRunnable.run();
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_updater.removeCallbacks(_updateRunnable);
		_receiverController.UnregisterReceiver(_updateReceiver);
		_receiverController.UnregisterReceiver(_performUpdateReceiver);
	}

	public void DownloadTemperature() {
		_logger.Debug("startDownloadTemperature");

		if (Tools.IsMuteTime()) {
			_logger.Warn("Mute time!");
			return;
		}

		Intent serviceIntent = new Intent(_context, RESTService.class);
		Bundle serviceData = new Bundle();

		serviceData.putString(RaspPiConstants.BUNDLE_REST_ACTION, Constants.ACTION_GET_TEMPERATURES);
		serviceData.putString(RaspPiConstants.BUNDLE_REST_DATA, Constants.BUNDLE_RASPBERRY_DATA_MODEL);
		serviceData.putString(RaspPiConstants.BUNDLE_REST_BROADCAST, Constants.BROADCAST_DOWNLOAD_TEMPERATURE_FINISHED);

		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}
}
