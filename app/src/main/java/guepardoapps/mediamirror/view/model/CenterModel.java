package guepardoapps.mediamirror.view.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

import guepardoapps.library.lucahome.common.enums.YoutubeId;

import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class CenterModel implements Serializable {

    private static final long serialVersionUID = -4643092994785889917L;

    private static final String TAG = CenterModel.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private static final String DEFAULT_CENTER_TEXT = "Hello, this is your media mirror!";
    private static final String DEFAULT_YOUTUBE_VIDEO_ID = YoutubeId.DEFAULT.GetYoutubeId();
    private static final String DEFAULT_WEB_VIEW_URL = "http://imgur.com/";

    private boolean _centerVisibility;
    private String _centerText;
    private boolean _youtubeVisibility;
    private String _youtubeId;
    private boolean _webViewVisibility;
    private String _webViewUrl;

    public CenterModel(
            boolean centerVisibility,
            @NonNull String centerText,
            boolean youtubeVisibility,
            @NonNull String youtubeId,
            boolean webViewVisibility,
            @NonNull String webViewUrl) {
        _logger = new SmartMirrorLogger(TAG);

        _centerVisibility = centerVisibility;
        _centerText = centerText;
        _youtubeVisibility = youtubeVisibility;
        _youtubeId = youtubeId;
        _webViewVisibility = webViewVisibility;
        _webViewUrl = webViewUrl;

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
        return _webViewVisibility;
    }

    public String GetWebViewUrl() {
        return _webViewUrl;
    }

    private void checkPlausibility() {
        if ((_centerVisibility && _youtubeVisibility)
                || (_youtubeVisibility && _webViewVisibility)
                || (_webViewVisibility && _centerVisibility)) {

            _logger.Warn("Invalid visibilities!");

            if (_centerText.length() > 0) {
                _centerVisibility = true;
                _logger.Warn("Resetting video and webView!");
                _youtubeVisibility = false;
                _youtubeId = "";
                _webViewVisibility = false;
                _webViewUrl = "";
            }

            if (_webViewUrl.length() > 0) {
                _youtubeVisibility = true;
                _logger.Warn("Resetting webView and center!");
                _webViewVisibility = false;
                _webViewUrl = "";
                _centerVisibility = false;
                _centerText = "";
            }

            if (_youtubeId != null) {
                _webViewVisibility = true;
                _logger.Warn("Resetting center and video!");
                _centerVisibility = false;
                _centerText = "";
                _youtubeVisibility = false;
                _youtubeId = "";
            }
        }

        if (_centerVisibility) {
            if (_centerText.length() == 0) {
                _logger.Warn("Setting center to default: " + DEFAULT_CENTER_TEXT);
                _centerText = DEFAULT_CENTER_TEXT;
            }
        } else if (_youtubeVisibility) {
            if (_youtubeId.length() != 11) {
                _logger.Warn("Setting videoUrl to default!");
                _youtubeId = DEFAULT_YOUTUBE_VIDEO_ID;
            }
        } else if (_webViewVisibility) {
            if (_webViewUrl.length() == 0) {
                _logger.Warn("Setting webViewUrl to default: " + DEFAULT_WEB_VIEW_URL);
                _webViewUrl = DEFAULT_WEB_VIEW_URL;
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
        return TAG
                + ":{centerVisibility:" + String.valueOf(_centerVisibility)
                + ";CenterText:" + _centerText
                + ";YoutubeVisibility:" + String.valueOf(_youtubeVisibility)
                + ";YoutubeId:" + _youtubeId
                + ";WebViewVisibility:" + String.valueOf(_webViewVisibility)
                + ";WebViewUrl:" + _webViewUrl + "}";
    }
}
