package guepardoapps.test;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import guepardoapps.library.lucahome.common.enums.YoutubeId;

import guepardoapps.library.toolset.controller.BroadcastController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.view.model.*;

public class CenterViewControllerTest {

	private static final String TAG = CenterViewControllerTest.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private Context _context;
	private BroadcastController _broadcastController;

	private static String EMPTY = "";
	private static boolean NOT_VISIBLE = false;

	private static int TEST = 0;

	private static boolean _testInformationVisibility = true;
	private static String _testInformationText = "Hello Jonas! How are you?";

	private static boolean _testYoutubeVisibility = true;
	private static String _testYoutubeId = YoutubeId.LUCKY_CHOPS_LIVE_DANZA.GetYoutubeId();

	private Runnable _sendStopVideo = new Runnable() {
		public void run() {
			_logger.Debug("_sendStopVideo");
			_context.sendBroadcast(new Intent(Broadcasts.STOP_VIDEO));

			Handler _sendStartVideoHandler = new Handler();
			_sendStartVideoHandler.postDelayed(_sendStartVideo, 5000);
		}
	};

	private Runnable _sendStartVideo = new Runnable() {
		public void run() {
			_logger.Debug("_sendStartVideo");
			_context.sendBroadcast(new Intent(Broadcasts.PLAY_VIDEO));
		}
	};

	private Runnable _sendPlayVideo = new Runnable() {
		public void run() {
			_logger.Debug("_sendPlayVideo");
			_context.sendBroadcast(new Intent(Broadcasts.PLAY_VIDEO));

			CenterModel model = new CenterModel(NOT_VISIBLE, EMPTY, _testYoutubeVisibility, _testYoutubeId, NOT_VISIBLE,
					EMPTY);
			_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL, Bundles.CENTER_MODEL, model);

			Handler _sendStopVideoHandler = new Handler();
			_sendStopVideoHandler.postDelayed(_sendStopVideo, 10000);
		}
	};

	public CenterViewControllerTest(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Info("Created test for CenterView");

		_context = context;
		_broadcastController = new BroadcastController(_context);

		if (TEST == 0) {
			sendTextTestBroadcast();
		} else if (TEST == 1) {
			sendVideoTestBroadcast();
		} else {
			_logger.Warn("Test not found: " + String.valueOf(TEST));
		}
	}

	private void sendTextTestBroadcast() {
		_logger.Debug("sendTextTestBroadcast");

		CenterModel model = new CenterModel(_testInformationVisibility, _testInformationText, NOT_VISIBLE, EMPTY,
				NOT_VISIBLE, EMPTY);
		_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL, Bundles.CENTER_MODEL, model);

		Handler _sendPlayVideoHandler = new Handler();
		_sendPlayVideoHandler.postDelayed(_sendPlayVideo, 10000);
	}

	private void sendVideoTestBroadcast() {
		_logger.Debug("sendVideoTestBroadcast");
		_sendPlayVideo.run();
	}

	public boolean ValidateView(boolean informationVisibility, String informationText, boolean youtubeVisibility,
			String youtubeId, boolean webviewVisibility, String webviewUrl) {
		if (TEST == 0) {
			return validateInformationText(informationVisibility, informationText, youtubeVisibility, youtubeId,
					webviewVisibility, webviewUrl);
		} else if (TEST == 1) {
			return validateInformationVideo(informationVisibility, informationText, youtubeVisibility, youtubeId,
					webviewVisibility, webviewUrl);
		} else {
			_logger.Warn("Test not found: " + String.valueOf(TEST));
			return false;
		}
	}

	private boolean validateInformationText(boolean informationVisibility, String informationText,
			boolean youtubeVisibility, String youtubeId, boolean webviewVisibility, String webviewUrl) {
		boolean success = true;

		if (informationVisibility != _testInformationVisibility) {
			_logger.Error("informationVisibility FAILED!" + String.valueOf(informationVisibility) + "!="
					+ String.valueOf(_testInformationVisibility));
			success &= false;
		}
		if (!informationText.contains(_testInformationText)) {
			_logger.Error("informationText FAILED!" + informationText + "!=" + _testInformationText);
			success &= false;
		}
		if (youtubeVisibility != NOT_VISIBLE) {
			_logger.Error("youtubeVisibility FAILED!" + String.valueOf(youtubeVisibility) + "!="
					+ String.valueOf(NOT_VISIBLE));
			success &= false;
		}
		if (youtubeId != null) {
			_logger.Error("youtubeId FAILED!" + youtubeId + "!=null");
			success &= false;
		}
		if (webviewVisibility != NOT_VISIBLE) {
			_logger.Error("webviewVisibility FAILED!" + String.valueOf(webviewVisibility) + "!="
					+ String.valueOf(NOT_VISIBLE));
			success &= false;
		}
		if (!webviewUrl.contains(EMPTY)) {
			_logger.Error("webviewUrl FAILED!" + webviewUrl + "!=" + EMPTY);
			success &= false;
		}

		if (success) {
			_logger.LogTest("Test SUCCEEDED!", success);
		} else {
			_logger.LogTest("Test FAILED!", success);
		}

		return success;
	}

	private boolean validateInformationVideo(boolean informationVisibility, String informationText,
			boolean youtubeVisibility, String youtubeId, boolean webviewVisibility, String webviewUrl) {
		boolean success = true;

		if (informationVisibility != NOT_VISIBLE) {
			_logger.Error("informationVisibility FAILED!" + String.valueOf(informationVisibility) + "!="
					+ String.valueOf(NOT_VISIBLE));
			success &= false;
		}
		if (!informationText.contains(EMPTY)) {
			_logger.Error("informationText FAILED!" + informationText + "!=" + EMPTY);
			success &= false;
		}
		if (youtubeVisibility != _testYoutubeVisibility) {
			_logger.Error("youtubeVisibility FAILED!" + String.valueOf(youtubeVisibility) + "!="
					+ String.valueOf(_testYoutubeVisibility));
			success &= false;
		}
		if (youtubeId != _testYoutubeId) {
			_logger.Error("youtubeId FAILED!" + youtubeId + "!=" + _testYoutubeId);
			success &= false;
		}
		if (webviewVisibility != NOT_VISIBLE) {
			_logger.Error("webviewVisibility FAILED!" + String.valueOf(webviewVisibility) + "!="
					+ String.valueOf(NOT_VISIBLE));
			success &= false;
		}
		if (!webviewUrl.contains(EMPTY)) {
			_logger.Error("webviewUrl FAILED!" + webviewUrl + "!=" + EMPTY);
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
