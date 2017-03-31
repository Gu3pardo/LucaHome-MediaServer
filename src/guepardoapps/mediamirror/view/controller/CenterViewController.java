package guepardoapps.mediamirror.view.controller;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import guepardoapps.library.lucahome.common.constants.Keys;
import guepardoapps.library.lucahome.common.enums.YoutubeId;
import guepardoapps.library.lucahome.tasks.DownloadYoutubeVideoTask;

import guepardoapps.library.toastview.ToastView;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.Enables;
import guepardoapps.mediamirror.controller.DatabaseController;
import guepardoapps.mediamirror.controller.MediaVolumeController;
import guepardoapps.mediamirror.model.*;

import guepardoapps.test.CenterViewControllerTest;

public class CenterViewController implements YouTubePlayer.OnInitializedListener {

	private static final CenterViewController SINGLETON_CONTROLLER = new CenterViewController();

	private static final String TAG = CenterViewController.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;
	private boolean _screenEnabled;

	private Context _context;
	private BroadcastController _broadcastController;
	private DatabaseController _dbController;
	private MediaVolumeController _mediaVolumeController;
	private ReceiverController _receiverController;

	private TextView _centerTextView;
	private ProgressDialog _progressDialog;
	private WebView _centerWebView;

	private boolean _youtTubePlayerIsInitialized;
	private YouTubePlayer _youtubePlayer;
	private YouTubePlayerView _youTubePlayerView;

	private String _centerText;
	private boolean _loadingVideo;
	private String _youtubeId;
	private boolean _loadingUrl;
	private String _webviewUrl;

	private CenterViewControllerTest _centerViewTest;

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

				if (model.GetCenterVisibility()) {
					if (_loadingUrl) {
						_centerWebView.stopLoading();
						_progressDialog.dismiss();
					}

					_youTubePlayerView.setVisibility(View.INVISIBLE);
					_centerWebView.setVisibility(View.INVISIBLE);
					_centerTextView.setVisibility(View.VISIBLE);

					stopVideo();

					_centerText = model.GetCenterText();
					_centerTextView.setText(_centerText);
				} else if (model.GetYoutubeVisibility()) {
					if (_loadingUrl) {
						_centerWebView.stopLoading();
						_progressDialog.dismiss();
					}

					_youTubePlayerView.setVisibility(View.VISIBLE);
					_centerWebView.setVisibility(View.INVISIBLE);
					_centerTextView.setVisibility(View.INVISIBLE);

					_youtubeId = model.GetYoutubeId();
					startVideo(_youtubeId);
				} else if (model.GetWebViewVisibility()) {
					_youTubePlayerView.setVisibility(View.INVISIBLE);
					_centerWebView.setVisibility(View.VISIBLE);
					_centerTextView.setVisibility(View.INVISIBLE);

					stopVideo();

					_webviewUrl = model.GetWebViewUrl();
					if (!_loadingUrl) {
						_loadingUrl = true;
						_progressDialog = ProgressDialog.show(_context, "Loading url...", "");
						_progressDialog.setCancelable(true);
						_centerWebView.loadUrl(_webviewUrl);
					} else {
						_logger.Warn("Webview is already loading a website!");
					}
				} else {
					if (_loadingUrl) {
						_centerWebView.stopLoading();
						_progressDialog.dismiss();
					}

					_youTubePlayerView.setVisibility(View.INVISIBLE);
					_centerWebView.setVisibility(View.INVISIBLE);
					_centerTextView.setVisibility(View.VISIBLE);

					_centerTextView.setText("Error: received center model is buggy!");
				}
			} else {
				_logger.Warn("model is null!");
			}

			if (Enables.TESTING) {
				_centerViewTest.ValidateView((_centerTextView.getVisibility() == View.VISIBLE),
						_centerTextView.getText().toString(), (_youTubePlayerView.getVisibility() == View.VISIBLE),
						model.GetYoutubeId(), (_centerWebView.getVisibility() == View.VISIBLE), "-1");
			}
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
			if (Keys.YOUTUBE_API_KEY_1 != null) {
				_youTubePlayerView.initialize(Keys.YOUTUBE_API_KEY_1, CenterViewController.this);
			} else {
				_logger.Warn("Please enter your youtube api key!");
				ToastView.error(_context, "Please enter your youtube api key!", Toast.LENGTH_LONG).show();
			}
		}
	};

	private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = false;
			_youtubePlayer.release();
			_youtTubePlayerIsInitialized = false;
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

	private CenterViewController() {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Debug("Created...");
	}

	public static CenterViewController getInstance() {
		return SINGLETON_CONTROLLER;
	}

	@Override
	public void onInitializationFailure(Provider provider, YouTubeInitializationResult result) {
		videoError("Failured to initialize YoutubePlayer!" + result);
	}

	@Override
	public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
		if (!_screenEnabled) {
			_logger.Debug("Screen is not enabled!");
			return;
		}

		if (!_youtTubePlayerIsInitialized) {
			_youtubePlayer = player;

			/** add listeners to YouTubePlayer instance **/
			_youtubePlayer.setPlayerStateChangeListener(_playerStateChangeListener);
			_youtubePlayer.setPlaybackEventListener(_playbackEventListener);

			_youtTubePlayerIsInitialized = true;
		}

		/** Start buffering **/
		if (!wasRestored) {
			startVideo(_youtubeId);
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Context context) {
		_logger.Debug("onCreate");

		if (_context != null) {
			_logger.Warn("Already created!");
			return;
		}

		_context = context;
		_broadcastController = new BroadcastController(_context);
		_dbController = DatabaseController.getInstance();
		_dbController.Initialize(_context);
		_mediaVolumeController = MediaVolumeController.getInstance();
		_receiverController = new ReceiverController(_context);

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
		if (Keys.YOUTUBE_API_KEY_1 != null) {
			_youTubePlayerView.initialize(Keys.YOUTUBE_API_KEY_1, this);
		} else {
			_logger.Warn("Please enter your youtube api key!");
			ToastView.error(_context, "Please enter your youtube api key!", Toast.LENGTH_LONG).show();
		}
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_receiverController.RegisterReceiver(_updateViewReceiver, new String[] { Broadcasts.SHOW_CENTER_MODEL });
			_receiverController.RegisterReceiver(_playVideoReceiver, new String[] { Broadcasts.PLAY_VIDEO });
			_receiverController.RegisterReceiver(_pauseVideoReceiver, new String[] { Broadcasts.PAUSE_VIDEO });
			_receiverController.RegisterReceiver(_stopVideoReceiver, new String[] { Broadcasts.STOP_VIDEO });
			_receiverController.RegisterReceiver(_videoPositionReceiver,
					new String[] { Broadcasts.SET_VIDEO_POSITION });
			_receiverController.RegisterReceiver(_playBirthdaySongReceiver,
					new String[] { Broadcasts.PLAY_BIRTHDAY_SONG });
			_receiverController.RegisterReceiver(_screenEnableReceiver, new String[] { Broadcasts.SCREEN_ENABLED });
			_receiverController.RegisterReceiver(_screenDisableReceiver,
					new String[] { Broadcasts.SCREEN_OFF, Broadcasts.SCREEN_SAVER });
			_receiverController.RegisterReceiver(_youtubeIdReceiver,
					new String[] { guepardoapps.library.lucahome.common.constants.Broadcasts.YOUTUBE_ID });

			_isInitialized = true;
			_logger.Debug("Initializing!");

			if (Enables.TESTING) {
				if (_centerViewTest == null) {
					_centerViewTest = new CenterViewControllerTest(_context);
				}
			}
		} else {
			_logger.Warn("Is ALREADY initialized!");
		}
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");

		_receiverController.UnregisterReceiver(_updateViewReceiver);
		_receiverController.UnregisterReceiver(_playVideoReceiver);
		_receiverController.UnregisterReceiver(_pauseVideoReceiver);
		_receiverController.UnregisterReceiver(_stopVideoReceiver);
		_receiverController.UnregisterReceiver(_videoPositionReceiver);
		_receiverController.UnregisterReceiver(_playBirthdaySongReceiver);
		_receiverController.UnregisterReceiver(_screenEnableReceiver);
		_receiverController.UnregisterReceiver(_screenDisableReceiver);
		_receiverController.UnregisterReceiver(_youtubeIdReceiver);

		_isInitialized = false;
	}

	public boolean IsYoutubePlaying() {
		return _youtubePlayer.isPlaying();
	}

	public int GetCurrentPlayPosition() {
		if (!_youtubePlayer.isPlaying()) {
			return -1;
		}

		int playPositionMillis = _youtubePlayer.getCurrentTimeMillis();
		int playPositionSec = playPositionMillis / 1000;
		return playPositionSec;
	}

	public int GetYoutubeDuration() {
		if (!_youtubePlayer.isPlaying()) {
			return -1;
		}

		int playDurationMillis = _youtubePlayer.getDurationMillis();
		int playDuratioSec = playDurationMillis / 1000;
		return playDuratioSec;
	}

	private void startVideo(String youtubeId) {
		_logger.Debug(String.format("trying to start video %s", youtubeId));

		if (!_screenEnabled) {
			_logger.Debug("Screen is not enabled!");
			return;
		}

		if (!_youtTubePlayerIsInitialized) {
			_logger.Error("YouTubePlayer is not initialized!");
			return;
		}

		if (_loadingVideo) {
			_logger.Warn("Already loading a video!");
			return;
		}

		if (_youtubePlayer.isPlaying()) {
			ToastView.info(_context, "Stopping current played video!", Toast.LENGTH_SHORT).show();
			_logger.Warn("Stopping current played video!");
			stopVideo();
		}

		if (_youtubePlayer != null) {
			if (youtubeId == null) {
				_logger.Warn("YoutubeId is null!");
				return;
			}

			_dbController.SaveYoutubeId(new YoutubeDatabaseModel(_dbController.GetHighesId() + 1, youtubeId, 0));
			_youtubePlayer.cueVideo(youtubeId);
		}

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

		if (!_youtTubePlayerIsInitialized) {
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

		if (!_youtTubePlayerIsInitialized) {
			_logger.Error("YouTubePlayer is not initialized!");
			return;
		}

		if (!_youtubePlayer.isPlaying()) {
			_logger.Warn("Not playing a video!");
			return;
		}

		_youtubePlayer.pause();
		_youtubePlayer.seekToMillis(0);

		_youTubePlayerView.setVisibility(View.INVISIBLE);
	}

	private void videoError(String error) {
		if (!_screenEnabled) {
			_logger.Debug("Screen is not enabled!");
			return;
		}

		_loadingVideo = false;
		_logger.Error("Video Play Error :" + error);

		_youTubePlayerView.setVisibility(View.INVISIBLE);

		_centerTextView.setVisibility(View.VISIBLE);
		_centerTextView.setText("Video Play Error :" + error);
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

					String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q=The+Good+Life+24+7&key="
							+ Keys.YOUTUBE_API_KEY_2;

					DownloadYoutubeVideoTask task = new DownloadYoutubeVideoTask(_context, _broadcastController, "");
					task.SetSendFirstEntry(true);
					task.execute(new String[] { url });
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
				_youTubePlayerView.setVisibility(View.INVISIBLE);
				_centerWebView.setVisibility(View.INVISIBLE);
				_centerTextView.setVisibility(View.VISIBLE);
				_centerTextView.setText("MediaMirror by GuepardoApps");
			}
		}

		@Override
		public void onVideoStarted() {
			_mediaVolumeController.UnmuteVolume();
		}
	};
}
