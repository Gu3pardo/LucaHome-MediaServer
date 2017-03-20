package guepardoapps.test;

import android.content.Context;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.model.*;

import guepardoapps.toolset.controller.BroadcastController;

public class IpAdressViewControllerTest {

	private static final String TAG = IpAdressViewControllerTest.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private Context _context;
	private BroadcastController _broadcastController;

	private static boolean _testVisibility = true;
	private static String _testIpAdress = "192.168.178.99";

	public IpAdressViewControllerTest(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Info("Created test for IpAdressView");

		_context = context;
		_broadcastController = new BroadcastController(_context);

		sendTestBroadcast();
	}

	private void sendTestBroadcast() {
		_logger.Debug("sendTestBroadcast");

		IpAdressModel model = new IpAdressModel(_testVisibility, _testIpAdress);
		_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_IP_ADDRESS_MODEL, Bundles.IP_ADDRESS_MODEL,
				model);
	}

	public boolean ValidateView(boolean visibility, String ipAdress) {
		boolean success = true;

		if (visibility != _testVisibility) {
			_logger.Error("visibility FAILED!" + String.valueOf(visibility) + "!=" + String.valueOf(_testVisibility));
			success &= false;
		}
		if (!ipAdress.contains(_testIpAdress)) {
			_logger.Error("ipAdress FAILED!" + ipAdress + "!=" + _testIpAdress);
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
