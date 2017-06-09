package guepardoapps.mediamirror.view.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

import guepardoapps.library.lucahome.common.enums.RadioStreams;
import guepardoapps.library.lucahome.common.enums.YoutubeId;

import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class CenterModel implements Serializable {

    private static final String TAG = CenterModel.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private static final String DEFAULT_CENTER_TEXT = "Hello, this is your media mirror!";
    private static final String DEFAULT_YOUTUBE_VIDEO_ID = YoutubeId.DEFAULT.GetYoutubeId();
    private static final String DEFAULT_WEB_VIEW_URL = "http://imgur.com/";

    private boolean _centerVisible;
    private String _centerText;

    private boolean _youtubeVisible;
    private String _youtubeId;

    private boolean _webViewVisible;
    private String _webViewUrl;

    public CenterModel(
            boolean centerVisible,
            @NonNull String centerText,
            boolean youtubeVisible,
            @NonNull String youtubeId,
            boolean webViewVisible,
            @NonNull String webViewUrl) {
        _logger = new SmartMirrorLogger(TAG);

        _centerVisible = centerVisible;
        _centerText = centerText;

        _youtubeVisible = youtubeVisible;
        _youtubeId = youtubeId;

        _webViewVisible = webViewVisible;
        _webViewUrl = webViewUrl;

        checkPlausibility();
    }

    public boolean IsCenterVisible() {
        return _centerVisible;
    }

    public String GetCenterText() {
        return _centerText;
    }

    public boolean IsYoutubeVisible() {
        return _youtubeVisible;
    }

    public String GetYoutubeId() {
        return _youtubeId;
    }

    public boolean IsWebViewVisible() {
        return _webViewVisible;
    }

    public String GetWebViewUrl() {
        return _webViewUrl;
    }

    private void checkPlausibility() {
        if ((_centerVisible && _youtubeVisible)
                || (_centerVisible && _webViewVisible)
                || (_youtubeVisible && _webViewVisible)) {

            _logger.Warn("Invalid visibilities!");

            if (_youtubeId.length() > 0) {
                _youtubeVisible = true;
                _logger.Warn("Resetting webView, radio and center!");

                _centerVisible = false;
                _centerText = "";

                _webViewVisible = false;
                _webViewUrl = "";
            }

            if (_centerText.length() > 0) {
                _centerVisible = true;
                _logger.Warn("Resetting video, radio and webView!");

                _youtubeVisible = false;
                _youtubeId = "";

                _webViewVisible = false;
                _webViewUrl = "";
            }

            if (_webViewUrl.length() > 0) {
                _webViewVisible = true;
                _logger.Warn("Resetting center, radio and video!");

                _youtubeVisible = false;
                _youtubeId = "";

                _centerVisible = false;
                _centerText = "";
            }
        }

        if (_centerVisible) {
            if (_centerText.length() == 0) {
                _logger.Warn("Setting center to default: " + DEFAULT_CENTER_TEXT);
                _centerText = DEFAULT_CENTER_TEXT;
            }
        } else if (_youtubeVisible) {
            if (_youtubeId.length() != 11) {
                _logger.Warn("Setting videoUrl to default!");
                _youtubeId = DEFAULT_YOUTUBE_VIDEO_ID;
            }
        } else if (_webViewVisible) {
            if (_webViewUrl.length() == 0) {
                _logger.Warn("Setting webViewUrl to default: " + DEFAULT_WEB_VIEW_URL);
                _webViewUrl = DEFAULT_WEB_VIEW_URL;
            }
        } else {
            _centerVisible = true;
            if (_centerText.length() == 0) {
                _logger.Warn("Setting center to default!");
                _centerText = DEFAULT_CENTER_TEXT;
            }
        }
    }

    @Override
    public String toString() {
        return TAG
                + ":{CenterVisible:" + String.valueOf(_centerVisible)
                + ";CenterText:" + _centerText
                + ";YoutubeVisible:" + String.valueOf(_youtubeVisible)
                + ";YoutubeId:" + _youtubeId
                + ";WebViewVisible:" + String.valueOf(_webViewVisible)
                + ";WebViewUrl:" + _webViewUrl
                + "}";
    }
}
