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

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.Keys;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.database.DBController;
import guepardoapps.mediamirror.model.*;
import guepardoapps.mediamirror.test.CenterViewControllerTest;
import guepardoapps.mediamirror.R;

import guepardoapps.toolset.controller.ReceiverController;

public class CenterViewController implements YouTubePlayer.OnInitializedListener {

	private static final String TAG = CenterViewController.class.getName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;
	private boolean _screenEnabled;

	private Context _context;
	private DBController _dbController;
	private ReceiverController _receiverController;

	private TextView _centerTextView;
	private ProgressDialog _progressDialog;
	private WebView _centerWebView;

	private boolean _youtTubePlayerIsInitialized;
	private YouTubePlayer _youtubePlayer;
	private YouTubePlayerView _youTubePlayerView;

	private String _centerText;
	private boolean _loadingVideo;
	private boolean _playingVideo;
	private String _youtubeId;
	private boolean _loadingUrl;
	private String _webviewUrl;

	private CenterViewControllerTest _centerViewTest;

	public CenterViewController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
		_dbController = new DBController(_context);
		_receiverController = new ReceiverController(_context);
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
	public void onCreate() {
		_logger.Debug("onCreate");

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
		if (Keys.YOUTUBE_API != null) {
			_youTubePlayerView.initialize(Keys.YOUTUBE_API, this);
		} else {
			_logger.Warn("Please enter your youtube api key!");
			Toast.makeText(_context, "Please enter your youtube api key!", Toast.LENGTH_LONG).show();
		}
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_receiverController.RegisterReceiver(_updateViewReceiver,
					new String[] { Constants.BROADCAST_SHOW_CENTER_MODEL });
			_receiverController.RegisterReceiver(_playVideoReceiver, new String[] { Constants.BROADCAST_PLAY_VIDEO });
			_receiverController.RegisterReceiver(_stopVideoReceiver, new String[] { Constants.BROADCAST_STOP_VIDEO });
			_receiverController.RegisterReceiver(_screenEnableReceiver,
					new String[] { Constants.BROADCAST_SCREEN_ENABLED });
			_receiverController.RegisterReceiver(_screenDisableReceiver,
					new String[] { Constants.BROADCAST_SCREEN_OFF, Constants.BROADCAST_SCREEN_SAVER });

			_isInitialized = true;
			_logger.Debug("Initializing!");

			if (Constants.TESTING_ENABLED) {
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
		_receiverController.UnregisterReceiver(_stopVideoReceiver);
		_receiverController.UnregisterReceiver(_screenEnableReceiver);
		_receiverController.UnregisterReceiver(_screenDisableReceiver);

		_isInitialized = false;
	}

	private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_logger.Debug("_updateViewReceiver onReceive");
			CenterModel model = (CenterModel) intent.getSerializableExtra(Constants.BUNDLE_CENTER_MODEL);
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

			if (Constants.TESTING_ENABLED) {
				_centerViewTest.ValidateView((_centerTextView.getVisibility() == View.VISIBLE),
						_centerTextView.getText().toString(), (_youTubePlayerView.getVisibility() == View.VISIBLE),
						model.GetYoutubeId(), (_centerWebView.getVisibility() == View.VISIBLE), "-1");
			}
		}
	};

	private BroadcastReceiver _playVideoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_logger.Debug("_playVideoReceiver onReceive");

			_youTubePlayerView.setVisibility(View.VISIBLE);
			_centerWebView.setVisibility(View.INVISIBLE);
			_centerTextView.setVisibility(View.INVISIBLE);

			startVideo(_youtubeId);
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
			if (Keys.YOUTUBE_API != null) {
				_youTubePlayerView.initialize(Keys.YOUTUBE_API, CenterViewController.this);
			} else {
				_logger.Warn("Please enter your youtube api key!");
				Toast.makeText(_context, "Please enter your youtube api key!", Toast.LENGTH_LONG).show();
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

	private void startVideo(String youtubeId) {
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

		if (_playingVideo) {
			Toast.makeText(_context, "Stopping current played video!", Toast.LENGTH_SHORT).show();
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
	}

	private void stopVideo() {
		if (!_screenEnabled) {
			_logger.Debug("Screen is not enabled!");
			return;
		}

		_logger.Debug("_stopVideoReceiver onReceive");

		if (!_youtTubePlayerIsInitialized) {
			_logger.Error("YouTubePlayer is not initialized!");
			return;
		}

		if (!_playingVideo) {
			_logger.Warn("Not playing a video!");
			return;
		}

		_youtubePlayer.pause();
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
			_playingVideo = false;
		}

		@Override
		public void onPlaying() {
			_playingVideo = true;
		}

		@Override
		public void onSeekTo(int arg0) {
		}

		@Override
		public void onStopped() {
			_playingVideo = false;
		}

	};

	private PlayerStateChangeListener _playerStateChangeListener = new PlayerStateChangeListener() {

		@Override
		public void onAdStarted() {
		}

		@Override
		public void onError(ErrorReason arg0) {
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
				_centerTextView.setText("MediaMirror by JSc");
			}
		}

		@Override
		public void onVideoStarted() {
		}
	};
}
