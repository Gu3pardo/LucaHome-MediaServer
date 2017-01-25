package guepardoapps.mediamirror.test;

import android.content.Context;

import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class BirthdayViewControllerTest {

	private static final String TAG = BirthdayViewControllerTest.class.getName();
	private SmartMirrorLogger _logger;

	// private Context _context;
	// private BroadcastController _broadcastController;

	private static boolean _testVisibility = true;
	private static String _testString = "Today is my 27th birthday! Yippie!";
	private static boolean _testHasBirthday = true;

	public BirthdayViewControllerTest(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Info("Created test for BirthdayView");

		// _context = context;
		// _broadcastController = new BroadcastController(_context);

		sendTestBroadcast();
	}

	private void sendTestBroadcast() {
		_logger.Debug("sendTestBroadcast");
		_logger.Warn("Temporarily disabled!");

		// BirthdayModel birthdayModel = new BirthdayModel(_testVisibility,
		// _testString, _testHasBirthday);
		// _broadcastController.SendSerializableBroadcast(Constants.BROADCAST_SHOW_BIRTHDAY_MODEL,
		// Constants.BUNDLE_BIRTHDAY_MODEL, birthdayModel);
		// Handler _sendNewBirthdayHandler = new Handler();
		// _sendNewBirthdayHandler.postDelayed(_sendNewBirthday, 5000);
	}

	private Runnable _sendNewBirthday = new Runnable() {
		public void run() {
			_logger.Debug("_sendNewBirthday");
			_logger.Warn("Temporarily disabled!");

			// BirthdayModel model = new BirthdayModel(false, "", false);
			// _broadcastController.SendSerializableBroadcast(Constants.BROADCAST_SHOW_BIRTHDAY_MODEL,
			// Constants.BUNDLE_BIRTHDAY_MODEL, model);
		}
	};

	public boolean ValidateView(boolean visibility, String text, boolean hasBirthday) {
		boolean success = true;

		if (visibility != _testVisibility) {
			_logger.Error("visibility FAILED!" + String.valueOf(visibility) + "!=" + String.valueOf(_testVisibility));
			success &= false;
		}
		if (!text.contains(_testString)) {
			_logger.Error("text FAILED!" + text + "!=" + _testString);
			success &= false;
		}
		if (hasBirthday != _testHasBirthday) {
			_logger.Error(
					"hasBirthday FAILED!" + String.valueOf(hasBirthday) + "!=" + String.valueOf(_testHasBirthday));
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
