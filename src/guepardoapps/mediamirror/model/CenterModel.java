package guepardoapps.mediamirror.model;

import java.io.Serializable;

import guepardoapps.library.lucahome.common.enums.YoutubeId;

import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class CenterModel implements Serializable {

	private static final long serialVersionUID = -4643092994785889917L;

	private static final String TAG = CenterModel.class.getName();
	private SmartMirrorLogger _logger;

	private static String DEFAULT_CENTER_TEXT = "Hello, this is your media mirror!";
	private static String DEFAULT_YOUTUBE_VIDEO_ID = YoutubeId.DEFAULT.GetYoutubeId();
	private static String DEFAULT_WEBVIEW_URL = "http://imgur.com/";

	private boolean _centerVisibility;
	private String _centerText;
	private boolean _youtubeVisibility;
	private String _youtubeId;
	private boolean _webviewVisibility;
	private String _webviewUrl;

	public CenterModel(boolean centerVisibility, String centerText, boolean youtubeVisibility, String youtubeId,
			boolean webviewVisibility, String webviewUrl) {
		_logger = new SmartMirrorLogger(TAG);

		_centerVisibility = centerVisibility;
		_centerText = centerText;
		_youtubeVisibility = youtubeVisibility;
		_youtubeId = youtubeId;
		_webviewVisibility = webviewVisibility;
		_webviewUrl = webviewUrl;

		checkPlausibility();
	}

	public boolean GetCenterVisibility() {
		return _centerVisibility;
	}

	public String GetCenterText() {
		return _centerText;
	}

	public boolean GetYoutubeVisibility() {
		return _youtubeVisibility;
	}

	public String GetYoutubeId() {
		return _youtubeId;
	}

	public boolean GetWebViewVisibility() {
		return _webviewVisibility;
	}

	public String GetWebViewUrl() {
		return _webviewUrl;
	}

	private void checkPlausibility() {
		if ((_centerVisibility && _youtubeVisibility) || (_youtubeVisibility && _webviewVisibility)
				|| (_webviewVisibility && _centerVisibility)) {

			_logger.Warn("Invalid visibilities!");

			if (_centerText.length() > 0) {
				_centerVisibility = true;
				_logger.Warn("Resetting video and webview!");
				_youtubeVisibility = false;
				_youtubeId = null;
				_webviewVisibility = false;
				_webviewUrl = "";
			}
			if (_webviewUrl.length() > 0) {
				_youtubeVisibility = true;
				_logger.Warn("Resetting webview and center!");
				_webviewVisibility = false;
				_webviewUrl = "";
				_centerVisibility = false;
				_centerText = "";
			}
			if (_youtubeId != null) {
				_webviewVisibility = true;
				_logger.Warn("Resetting center and video!");
				_centerVisibility = false;
				_centerText = "";
				_youtubeVisibility = false;
				_youtubeId = null;
			}
		}

		if (_centerVisibility) {
			if (_centerText.length() == 0) {
				_logger.Warn("Setting center to default: " + DEFAULT_CENTER_TEXT);
				_centerText = DEFAULT_CENTER_TEXT;
			}
		} else if (_youtubeVisibility) {
			if (_youtubeId == null) {
				_logger.Warn("Setting videourl to default!");
				_youtubeId = DEFAULT_YOUTUBE_VIDEO_ID;
			}
		} else if (_webviewVisibility) {
			if (_webviewUrl.length() == 0) {
				_logger.Warn("Setting webviewurl to default: " + DEFAULT_WEBVIEW_URL);
				_webviewUrl = DEFAULT_WEBVIEW_URL;
			}
		} else {
			_centerVisibility = true;
			if (_centerText.length() == 0) {
				_logger.Warn("Setting center to default!");
				_centerText = DEFAULT_CENTER_TEXT;
			}
		}
	}

	@Override
	public String toString() {
		String _youtubeIdString;
		if (_youtubeId == null) {
			_youtubeIdString = "";
		} else {
			_youtubeIdString = _youtubeId.toString();
		}

		return CenterModel.class.getName() + ":{centerVisibility:" + String.valueOf(_centerVisibility) + ";CenterText:"
				+ _centerText + ";YoutubeVisibility:" + String.valueOf(_youtubeVisibility) + ";YoutubeId:"
				+ _youtubeIdString + ";WebviewVisibility:" + String.valueOf(_webviewVisibility) + ";WebviewUrl:"
				+ _webviewUrl + "}";
	}
}
