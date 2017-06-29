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
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.constants.Keys;
import guepardoapps.library.lucahome.common.enums.RadioStreams;
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

    private LinearLayout _centerRadioStreamLinearLayout;
    private ImageButton _imageButtonRadioStreamPlay;
    private ImageButton _imageButtonRadioStreamStop;
    private RadioStreams _radioStream = RadioStreams.DEFAULT;
    private MediaPlayer _radioPlayer;

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
            _logger.Debug("_pauseVideoReceiver onReceive");

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            pauseVideo();
        }
    };

    private BroadcastReceiver _playBirthdaySongReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_playBirthdaySongReceiver onReceive");

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            startVideo(YoutubeId.BIRTHDAY_SONG.toString());
        }
    };

    private BroadcastReceiver _playRadioStreamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_playRadioStreamReceiver onReceive");

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            String radioStreamId = intent.getStringExtra(Bundles.RADIO_STREAM_ID);
            if (radioStreamId != null) {
                if (radioStreamId.length() > 0) {
                    try {
                        int id = Integer.parseInt(radioStreamId);
                        RadioStreams radioStream = RadioStreams.GetById(id);
                        if (radioStream == RadioStreams.NULL) {
                            radioStream = RadioStreams.DEFAULT;
                        }
                        _radioStream = radioStream;
                    } catch (Exception exception) {
                        _logger.Error(exception.toString());
                        _radioStream = RadioStreams.DEFAULT;
                    }
                }
            }

            stopVideo();
            stopWebViewLoading();

            startRadioPlaying();
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

            stopRadioPlaying();
            stopWebViewLoading();

            startVideo(_youtubeId);
        }
    };

    private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_screenDisableReceiver onReceive");

            _screenEnabled = false;

            pauseVideo();
            _youtubePlayer.release();
            _youTubePlayerIsInitialized = false;

            stopRadioPlaying();
        }
    };

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_screenEnableReceiver onReceive");

            _screenEnabled = true;

            _centerTextView = ((Activity) _context).findViewById(R.id.centerTextView);

            _centerWebView = ((Activity) _context).findViewById(R.id.centerWebView);
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

            _youTubePlayerView = ((Activity) _context).findViewById(R.id.centerYoutubePlayer);
            if (Keys.YOUTUBE_API_KEY.length() != 0) {
                _youTubePlayerView.initialize(Keys.YOUTUBE_API_KEY, CenterViewController.this);
            } else {
                _logger.Warn("Please enter your youtube api key!");
                Toasty.error(_context, "Please enter your youtube api key!", Toast.LENGTH_LONG).show();
            }
        }
    };

    private BroadcastReceiver _stopRadioStreamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_stopRadioStreamReceiver onReceive");

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            stopRadioPlaying();
        }
    };

    private BroadcastReceiver _stopVideoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_stopVideoReceiver onReceive");

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            stopVideo();
        }
    };

    private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateViewReceiver onReceive");

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            CenterModel model = (CenterModel) intent.getSerializableExtra(Bundles.CENTER_MODEL);

            if (model != null) {
                _logger.Debug(model.toString());

                if (model.IsCenterVisible()) {
                    stopWebViewLoading();
                    stopVideo();
                    stopRadioPlaying();

                    _musicIndicator.setVisibility(View.INVISIBLE);
                    _youTubePlayerView.setVisibility(View.INVISIBLE);
                    _centerWebView.setVisibility(View.INVISIBLE);
                    _centerTextView.setVisibility(View.VISIBLE);
                    _centerRadioStreamLinearLayout.setVisibility(View.GONE);

                    _centerTextView.setText(model.GetCenterText());
                } else if (model.IsYoutubeVisible()) {
                    stopWebViewLoading();
                    stopRadioPlaying();

                    _musicIndicator.setVisibility(View.VISIBLE);
                    _youTubePlayerView.setVisibility(View.VISIBLE);
                    _centerWebView.setVisibility(View.INVISIBLE);
                    _centerTextView.setVisibility(View.INVISIBLE);
                    _centerRadioStreamLinearLayout.setVisibility(View.GONE);

                    _youtubeId = model.GetYoutubeId();
                    startVideo(_youtubeId);
                } else if (model.IsWebViewVisible()) {
                    stopWebViewLoading();
                    stopVideo();
                    stopRadioPlaying();

                    _musicIndicator.setVisibility(View.INVISIBLE);
                    _youTubePlayerView.setVisibility(View.INVISIBLE);
                    _centerWebView.setVisibility(View.VISIBLE);
                    _centerTextView.setVisibility(View.INVISIBLE);
                    _centerRadioStreamLinearLayout.setVisibility(View.GONE);

                    _loadingUrl = true;
                    _progressDialog = ProgressDialog.show(_context, "Loading url...", "");
                    _progressDialog.setCancelable(true);
                    _centerWebView.loadUrl(model.GetWebViewUrl());
                } else if (model.IsRadioStreamVisible()) {
                    stopWebViewLoading();
                    stopVideo();
                    stopRadioPlaying();

                    _musicIndicator.setVisibility(View.VISIBLE);
                    _youTubePlayerView.setVisibility(View.INVISIBLE);
                    _centerWebView.setVisibility(View.INVISIBLE);
                    _centerTextView.setVisibility(View.VISIBLE);
                    _centerRadioStreamLinearLayout.setVisibility(View.VISIBLE);

                    RadioStreams radioStream = model.GetRadioStream();
                    if (radioStream != null) {
                        _radioStream = radioStream;
                        _centerTextView.setText(_radioStream.GetTitle());
                        startRadioPlaying();
                    } else {
                        _logger.Error("RadioStream is null!");
                        _centerTextView.setText("An error appeared receiving the radio stream!");
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

        _centerTextView = ((Activity) _context).findViewById(R.id.centerTextView);

        _centerRadioStreamLinearLayout = ((Activity) _context).findViewById(R.id.centerRadioStreamLinearLayout);
        _imageButtonRadioStreamPlay = ((Activity) _context).findViewById(R.id.imageButtonRadioStreamPlay);
        _imageButtonRadioStreamPlay.setOnClickListener(view -> {
            _logger.Debug("_imageButtonRadioStreamPlay onClick");
            startRadioPlaying();
        });
        _imageButtonRadioStreamStop = ((Activity) _context).findViewById(R.id.imageButtonRadioStreamStop);
        _imageButtonRadioStreamStop.setOnClickListener(view -> {
            _logger.Debug("_imageButtonRadioStreamStop onClick");
            stopRadioPlaying();
        });
        initializeMediaPlayer();

        _centerWebView = ((Activity) _context).findViewById(R.id.centerWebView);
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

        _musicIndicator = ((Activity) _context).findViewById(R.id.musicIndicator);
        _youTubePlayerView = ((Activity) _context).findViewById(R.id.centerYoutubePlayer);
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
        stopWebViewLoading();
        stopVideo();
        stopRadioPlaying();
    }

    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_playBirthdaySongReceiver, new String[]{Broadcasts.PLAY_BIRTHDAY_SONG});
            _receiverController.RegisterReceiver(_playRadioStreamReceiver, new String[]{Broadcasts.PLAY_RADIO_STREAM});
            _receiverController.RegisterReceiver(_playVideoReceiver, new String[]{Broadcasts.PLAY_VIDEO});
            _receiverController.RegisterReceiver(_pauseVideoReceiver, new String[]{Broadcasts.PAUSE_VIDEO});
            _receiverController.RegisterReceiver(_screenDisableReceiver, new String[]{Broadcasts.SCREEN_OFF});
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_stopRadioStreamReceiver, new String[]{Broadcasts.STOP_RADIO_STREAM});
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
        stopWebViewLoading();
        stopVideo();
        stopRadioPlaying();
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

    public int GetRadioStreamId() {
        return _radioStream.GetId();
    }

    public boolean IsRadioStreamPlaying() {
        return _radioPlayer.isPlaying();
    }

    private void stopWebViewLoading() {
        _logger.Warn("stopWebViewLoading");

        if (_loadingUrl) {
            _logger.Warn("WebView is loading a website! Cancel loading!");
            _centerWebView.stopLoading();
            _loadingUrl = false;
            _progressDialog.dismiss();
        }
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
        _centerTextView.setText(String.format(Locale.getDefault(), "Video Play Error : %s", error));
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
        public void onError(ErrorReason errorReason) {
            _logger.Error(errorReason.toString());

            if (errorReason == ErrorReason.USER_DECLINED_RESTRICTED_CONTENT) {
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

    private void initializeMediaPlayer() {
        _logger.Debug("initializeMediaPlayer");

        _radioPlayer = new MediaPlayer();

        try {
            _radioPlayer.setDataSource(_radioStream.GetUrl());
            _logger.Debug(String.format(Locale.getDefault(), "Set DataSource to %s", _radioStream.GetUrl()));
        } catch (Exception e) {
            _logger.Error(e.toString());
            Toasty.error(_context, "An error appeared settings url for radio player!", Toast.LENGTH_LONG).show();
        }

        _radioPlayer.setOnBufferingUpdateListener((mediaPlayer, percent) -> _logger.Info(String.format(Locale.getDefault(), "Buffered to %d%%", percent)));
        _radioPlayer.setOnPreparedListener(mediaPlayer -> {
            _logger.Debug("onPreparedListener...");
            _radioPlayer.start();
        });
    }

    private void startRadioPlaying() {
        _logger.Debug("startRadioPlaying");

        _imageButtonRadioStreamPlay.setEnabled(false);
        _imageButtonRadioStreamStop.setEnabled(true);

        try {
            _radioPlayer.prepareAsync();
        } catch (Exception exception) {
            _logger.Error(exception.toString());
        }
    }

    private void stopRadioPlaying() {
        _logger.Debug("stopRadioPlaying");

        if (_radioPlayer.isPlaying()) {
            _radioPlayer.stop();
        }
        _radioPlayer.release();
        initializeMediaPlayer();

        _imageButtonRadioStreamPlay.setEnabled(true);
        _imageButtonRadioStreamStop.setEnabled(false);
    }
}
