package guepardoapps.mediamirror.view.controller;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;
import com.taishi.library.Indicator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.constants.Keys;
import guepardoapps.library.lucahome.common.enums.YoutubeId;
import guepardoapps.library.lucahome.tasks.DownloadYoutubeVideoTask;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.controller.DatabaseController;
import guepardoapps.mediamirror.controller.MediaVolumeController;
import guepardoapps.mediamirror.view.model.*;

public class CenterViewController implements YouTubePlayer.OnInitializedListener {

    private static final CenterViewController SINGLETON = new CenterViewController();

    private static final String TAG = CenterViewController.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private boolean _isInitialized;
    private boolean _screenEnabled;

    private Context _context;
    private BroadcastController _broadcastController;
    private DatabaseController _databaseController;
    private MediaVolumeController _mediaVolumeController;
    private ReceiverController _receiverController;

    private TextView _centerTextView;
    private ProgressDialog _progressDialog;
    private WebView _centerWebView;

    private Indicator _musicIndicator;
    private boolean _youTubePlayerIsInitialized;
    private YouTubePlayer _youtubePlayer;
    private YouTubePlayerView _youTubePlayerView;

    private boolean _loadingVideo;
    private String _youtubeId = YoutubeId.DEFAULT.GetYoutubeId();
    private boolean _loadingUrl;

    private BroadcastReceiver _pauseVideoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_pauseVideoReceiver onReceive");
            pauseVideo();
        }
    };

    private BroadcastReceiver _playBirthdaySongReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_playBirthdaySongReceiver onReceive");
            startVideo(YoutubeId.BIRTHDAY_SONG.toString());
        }
    };

    private BroadcastReceiver _playVideoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_playVideoReceiver onReceive");

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            String youtubeId = intent.getStringExtra(Bundles.YOUTUBE_ID);
            if (youtubeId != null) {
                if (youtubeId.length() > 0) {
                    _youtubeId = youtubeId;
                }
            }

            startVideo(_youtubeId);
        }
    };

    private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = false;
            pauseVideo();
            _youtubePlayer.release();
            _youTubePlayerIsInitialized = false;
        }
    };

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = true;

            _centerTextView = (TextView) ((Activity) _context).findViewById(R.id.centerTextView);

            _centerWebView = (WebView) ((Activity) _context).findViewById(R.id.centerWebView);
            _centerWebView.getSettings().setBuiltInZoomControls(true);
            _centerWebView.getSettings().setSupportZoom(true);
            _centerWebView.getSettings().setJavaScriptEnabled(true);
            _centerWebView.getSettings().setLoadWithOverviewMode(true);
            _centerWebView.setWebViewClient(new WebViewClient());
            _centerWebView.setWebChromeClient(new WebChromeClient());
            _centerWebView.setInitialScale(100);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(false);
            _centerWebView.setWebViewClient(new WebViewClient() {
                public void onPageFinished(WebView view, String url) {
                    _progressDialog.dismiss();
                    _loadingUrl = false;
                }
            });

            _youTubePlayerView = (YouTubePlayerView) ((Activity) _context).findViewById(R.id.centerYoutubePlayer);
            if (Keys.YOUTUBE_API_KEY.length() != 0) {
                _youTubePlayerView.initialize(Keys.YOUTUBE_API_KEY, CenterViewController.this);
            } else {
                _logger.Warn("Please enter your youtube api key!");
                Toasty.error(_context, "Please enter your youtube api key!", Toast.LENGTH_LONG).show();
            }
        }
    };

    private BroadcastReceiver _stopVideoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_stopVideoReceiver onReceive");
            stopVideo();
        }
    };

    private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_updateViewReceiver onReceive");
            CenterModel model = (CenterModel) intent.getSerializableExtra(Bundles.CENTER_MODEL);

            if (model != null) {
                _logger.Debug(model.toString());

                if (model.IsCenterVisible()) {
                    if (_loadingUrl) {
                        _centerWebView.stopLoading();
                        _progressDialog.dismiss();
                    }

                    _musicIndicator.setVisibility(View.INVISIBLE);
                    _youTubePlayerView.setVisibility(View.INVISIBLE);
                    _centerWebView.setVisibility(View.INVISIBLE);
                    _centerTextView.setVisibility(View.VISIBLE);

                    stopVideo();

                    _centerTextView.setText(model.GetCenterText());
                } else if (model.IsYoutubeVisible()) {
                    if (_loadingUrl) {
                        _centerWebView.stopLoading();
                        _progressDialog.dismiss();
                    }

                    _musicIndicator.setVisibility(View.VISIBLE);
                    _youTubePlayerView.setVisibility(View.VISIBLE);
                    _centerWebView.setVisibility(View.INVISIBLE);
                    _centerTextView.setVisibility(View.INVISIBLE);

                    _youtubeId = model.GetYoutubeId();
                    startVideo(_youtubeId);
                } else if (model.IsWebViewVisible()) {
                    _musicIndicator.setVisibility(View.INVISIBLE);
                    _youTubePlayerView.setVisibility(View.INVISIBLE);
                    _centerWebView.setVisibility(View.VISIBLE);
                    _centerTextView.setVisibility(View.INVISIBLE);

                    stopVideo();

                    if (!_loadingUrl) {
                        _loadingUrl = true;
                        _progressDialog = ProgressDialog.show(_context, "Loading url...", "");
                        _progressDialog.setCancelable(true);
                        _centerWebView.loadUrl(model.GetWebViewUrl());
                    } else {
                        _logger.Warn("WebView is already loading a website!");
                    }
                } else {
                    if (_loadingUrl) {
                        _centerWebView.stopLoading();
                        _progressDialog.dismiss();
                    }

                    _musicIndicator.setVisibility(View.INVISIBLE);
                    _youTubePlayerView.setVisibility(View.INVISIBLE);
                    _centerWebView.setVisibility(View.INVISIBLE);
                    _centerTextView.setVisibility(View.VISIBLE);

                    _centerTextView.setText(R.string.errorCenterModel);
                }
            } else {
                _logger.Warn("model is null!");
            }
        }
    };

    private BroadcastReceiver _videoPositionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_videoPositionReceiver onReceive");

            int positionPercent = intent.getIntExtra(Bundles.VIDEO_POSITION_PERCENT, -1);
            if (positionPercent != -1) {
                _logger.Debug("Setting video to position of percentage " + String.valueOf(positionPercent));

                if (_youtubePlayer.isPlaying()) {
                    int duration = _youtubePlayer.getDurationMillis();
                    _youtubePlayer.seekToMillis((duration * positionPercent) / 100);
                }
            }
        }
    };

    private BroadcastReceiver _youtubeIdReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_youtubeIdReceiver onReceive");
            String youtubeId = intent.getStringExtra(guepardoapps.library.lucahome.common.constants.Bundles.YOUTUBE_ID);
            if (youtubeId != null) {
                _logger.Debug("received youtubeId: " + youtubeId);
                _youtubeId = youtubeId;

                startVideo(_youtubeId);
            }
        }
    };

    public static CenterViewController getInstance() {
        return SINGLETON;
    }

    private CenterViewController() {
        _logger = new SmartMirrorLogger(TAG);
        _logger.Debug("Created...");
    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult result) {
        videoError("Failed to initialize YoutubePlayer!" + result);
    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
        if (!_screenEnabled) {
            _logger.Debug("Screen is not enabled!");
            return;
        }

        if (!_youTubePlayerIsInitialized) {
            _youtubePlayer = player;

            _youtubePlayer.setPlayerStateChangeListener(_playerStateChangeListener);
            _youtubePlayer.setPlaybackEventListener(_playbackEventListener);

            _youTubePlayerIsInitialized = true;
        }

        if (!wasRestored) {
            startVideo(_youtubeId);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void onCreate(@NonNull Context context) {
        _logger.Debug("onCreate");

        _screenEnabled = true;

        _context = context;
        _broadcastController = new BroadcastController(_context);
        _databaseController = DatabaseController.getSingleton();
        _databaseController.Initialize(_context);
        _mediaVolumeController = MediaVolumeController.getInstance();
        _receiverController = new ReceiverController(_context);

        _centerTextView = (TextView) ((Activity) _context).findViewById(R.id.centerTextView);

        _centerWebView = (WebView) ((Activity) _context).findViewById(R.id.centerWebView);
        _centerWebView.getSettings().setBuiltInZoomControls(true);
        _centerWebView.getSettings().setSupportZoom(true);
        _centerWebView.getSettings().setJavaScriptEnabled(true);
        _centerWebView.getSettings().setLoadWithOverviewMode(true);
        _centerWebView.setWebViewClient(new WebViewClient());
        _centerWebView.setWebChromeClient(new WebChromeClient());
        _centerWebView.setInitialScale(100);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(false);
        _centerWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                _progressDialog.dismiss();
                _loadingUrl = false;
            }
        });

        _musicIndicator = (Indicator) ((Activity) _context).findViewById(R.id.musicIndicator);
        _youTubePlayerView = (YouTubePlayerView) ((Activity) _context).findViewById(R.id.centerYoutubePlayer);
        if (Keys.YOUTUBE_API_KEY.length() != 0) {
            _youTubePlayerView.initialize(Keys.YOUTUBE_API_KEY, this);
        } else {
            _logger.Warn("Please enter your youtube api key!");
            Toasty.error(_context, "Please enter your youtube api key!", Toast.LENGTH_LONG).show();
        }
    }

    public void onStart() {
        _logger.Debug("onStart");
    }

    public void onPause() {
        _logger.Debug("onPause");
    }

    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_pauseVideoReceiver, new String[]{Broadcasts.PAUSE_VIDEO});
            _receiverController.RegisterReceiver(_playVideoReceiver, new String[]{Broadcasts.PLAY_VIDEO});
            _receiverController.RegisterReceiver(_playBirthdaySongReceiver, new String[]{Broadcasts.PLAY_BIRTHDAY_SONG});
            _receiverController.RegisterReceiver(_screenDisableReceiver, new String[]{Broadcasts.SCREEN_OFF});
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_stopVideoReceiver, new String[]{Broadcasts.STOP_VIDEO});
            _receiverController.RegisterReceiver(_updateViewReceiver, new String[]{Broadcasts.SHOW_CENTER_MODEL});
            _receiverController.RegisterReceiver(_videoPositionReceiver, new String[]{Broadcasts.SET_VIDEO_POSITION});
            _receiverController.RegisterReceiver(_youtubeIdReceiver, new String[]{guepardoapps.library.lucahome.common.constants.Broadcasts.YOUTUBE_ID});

            _isInitialized = true;
            _logger.Debug("Initializing!");
        } else {
            _logger.Warn("Is ALREADY initialized!");
        }
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _isInitialized = false;
    }

    public boolean IsYoutubePlaying() {
        return _youtubePlayer.isPlaying();
    }

    public int GetCurrentPlayPosition() {
        if (!_youtubePlayer.isPlaying()) {
            return -1;
        }

        return _youtubePlayer.getCurrentTimeMillis() / 1000;
    }

    public int GetYoutubeDuration() {
        if (!_youtubePlayer.isPlaying()) {
            return -1;
        }

        return _youtubePlayer.getDurationMillis() / 1000;
    }

    public ArrayList<YoutubeDatabaseModel> GetYoutubeIds() {
        return _databaseController.GetYoutubeIds();
    }

    private void startVideo(@NonNull String youtubeId) {
        _logger.Debug(String.format("trying to start video %s", youtubeId));

        if (!_screenEnabled) {
            _logger.Debug("Screen is not enabled!");
            return;
        }

        if (!_youTubePlayerIsInitialized) {
            _logger.Error("YouTubePlayer is not initialized!");
            return;
        }

        if (_loadingVideo) {
            _logger.Warn("Already loading a video!");
            return;
        }

        if (_youtubePlayer.isPlaying()) {
            Toasty.info(_context, "Stopping current played video!", Toast.LENGTH_SHORT).show();
            _logger.Warn("Stopping current played video!");
            stopVideo();
        }

        if (_youtubePlayer != null) {
            _databaseController.SaveYoutubeId(new YoutubeDatabaseModel(_databaseController.GetHighestId() + 1, youtubeId, 0));
            _youtubePlayer.cueVideo(youtubeId);
        }

        _musicIndicator.setVisibility(View.VISIBLE);
        _youTubePlayerView.setVisibility(View.VISIBLE);
        _centerWebView.setVisibility(View.INVISIBLE);
        _centerTextView.setVisibility(View.INVISIBLE);
    }

    private void pauseVideo() {
        _logger.Debug("pauseVideo");

        if (!_screenEnabled) {
            _logger.Debug("Screen is not enabled!");
            return;
        }

        if (!_youTubePlayerIsInitialized) {
            _logger.Error("YouTubePlayer is not initialized!");
            return;
        }

        if (!_youtubePlayer.isPlaying()) {
            _logger.Warn("Not playing a video!");
            return;
        }

        _youtubePlayer.pause();
    }

    private void stopVideo() {
        _logger.Debug("stopVideo");

        if (!_screenEnabled) {
            _logger.Debug("Screen is not enabled!");
            return;
        }

        if (!_youTubePlayerIsInitialized) {
            _logger.Error("YouTubePlayer is not initialized!");
            return;
        }

        if (!_youtubePlayer.isPlaying()) {
            _logger.Warn("Not playing a video!");
            return;
        }

        _youtubePlayer.pause();
        _youtubePlayer.seekToMillis(0);

        _musicIndicator.setVisibility(View.INVISIBLE);
        _youTubePlayerView.setVisibility(View.INVISIBLE);
    }

    private void videoError(@NonNull String error) {
        if (!_screenEnabled) {
            _logger.Debug("Screen is not enabled!");
            return;
        }

        _loadingVideo = false;
        _logger.Error("Video Play Error :" + error);

        _musicIndicator.setVisibility(View.INVISIBLE);
        _youTubePlayerView.setVisibility(View.INVISIBLE);

        _centerTextView.setVisibility(View.VISIBLE);
        _centerTextView.setText(String.format(Locale.GERMAN, "Video Play Error : %s", error));
    }

    private PlaybackEventListener _playbackEventListener = new PlaybackEventListener() {

        @Override
        public void onBuffering(boolean arg0) {
        }

        @Override
        public void onPaused() {
        }

        @Override
        public void onPlaying() {
        }

        @Override
        public void onSeekTo(int arg0) {
        }

        @Override
        public void onStopped() {
        }

    };

    private PlayerStateChangeListener _playerStateChangeListener = new PlayerStateChangeListener() {

        @Override
        public void onAdStarted() {
            _mediaVolumeController.MuteVolume();
        }

        @Override
        public void onError(ErrorReason arg0) {
            _logger.Error(arg0.toString());

            if (arg0 == ErrorReason.USER_DECLINED_RESTRICTED_CONTENT) {
                if (YoutubeId.GetByYoutubeId(_youtubeId) == YoutubeId.THE_GOOD_LIFE_STREAM) {
                    _logger.Debug("Stream is " + YoutubeId.THE_GOOD_LIFE_STREAM.GetTitle() + "! Searching other id!");

                    String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q=The+Good+Life+24+7&key=" + Keys.YOUTUBE_API_KEY;

                    DownloadYoutubeVideoTask task = new DownloadYoutubeVideoTask(_context, _broadcastController, "");
                    task.SetSendFirstEntry(true);
                    task.execute(url);
                }
            }
        }

        @Override
        public void onLoaded(String arg0) {
            _loadingVideo = false;
            _youtubePlayer.play();
        }

        @Override
        public void onLoading() {
        }

        @Override
        public void onVideoEnded() {
            if (_youtubePlayer.hasNext()) {
                _youtubePlayer.next();
            } else {
                _musicIndicator.setVisibility(View.INVISIBLE);
                _youTubePlayerView.setVisibility(View.INVISIBLE);
                _centerWebView.setVisibility(View.INVISIBLE);
                _centerTextView.setVisibility(View.VISIBLE);
                _centerTextView.setText(R.string.madeByGuepardoApps);
            }
        }

        @Override
        public void onVideoStarted() {
            _mediaVolumeController.UnMuteVolume();
        }
    };
}
