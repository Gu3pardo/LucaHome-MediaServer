package guepardoapps.mediamirror.test;

import android.content.Context;
import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.model.*;
import guepardoapps.toolset.controller.BroadcastController;

public class RaspberryViewControllerTest {

	private static final String TAG = RaspberryViewControllerTest.class.getName();
	private SmartMirrorLogger _logger;

	private Context _context;
	private BroadcastController _broadcastController;

	private static String _rasperryPi1Name = "RPi1";
	private static String _rasperryPi1Temperature = "21.6°C";

	public RaspberryViewControllerTest(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Info("Created test for RaspberryView");

		_context = context;
		_broadcastController = new BroadcastController(_context);

		sendTestBroadcast();
	}

	private void sendTestBroadcast() {
		_logger.Debug("sendTestBroadcast");

		RaspberryModel model = new RaspberryModel(_rasperryPi1Name, _rasperryPi1Temperature);
		_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_SHOW_RASPBERRY_DATA_MODEL,
				Constants.BUNDLE_RASPBERRY_DATA_MODEL, model);
	}

	public boolean ValidateView(String rasperryPi1Name, String rasperryPi1Temperature) {
		boolean success = true;

		if (!rasperryPi1Name.contains(_rasperryPi1Name)) {
			_logger.Error("rasperryPi1Name FAILED!" + rasperryPi1Name + "!=" + _rasperryPi1Name);
			success &= false;
		}
		if (!rasperryPi1Temperature.contains(_rasperryPi1Temperature)) {
			_logger.Error("rasperryPi1Temperature FAILED!" + rasperryPi1Temperature + "!=" + _rasperryPi1Temperature);
			success &= false;
		}

		if (success) {
			_logger.LogTest("Test SUCCEEDED!", success);
		} else {
			_logger.LogTest("Test FAILED!", success);
		}

		return success;
	}
}
