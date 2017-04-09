package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import guepardoapps.library.lucahome.common.enums.RSSFeed;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.view.model.RSSModel;

public class RSSViewUpdater {

	private static final String TAG = RSSViewUpdater.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private int _updateTime;
	private boolean _isRunning;
	private RSSFeed _rssFeed;

	private Handler _updater;

	private Context _context;
	private BroadcastController _broadcastController;
	private ReceiverController _receiverController;

	private Runnable _updateRunnable = new Runnable() {
		public void run() {
			_logger.Debug("_updateRunnable run");
			LoadRss();
			_updater.postDelayed(_updateRunnable, _updateTime);
		}
	};

	private BroadcastReceiver _resetRSSFeedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_resetRSSFeedReceiver onReceive");
			_rssFeed = RSSFeed.DEFAULT;
			_logger.Debug("Resetted RssFeed is: " + _rssFeed);

			_updater.removeCallbacks(_updateRunnable);
			_updateRunnable.run();
		}
	};

	private BroadcastReceiver _updateRSSFeedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateRSSFeedReceiver onReceive");
			RSSModel newRSSModel = (RSSModel) intent.getSerializableExtra(Bundles.RSS_MODEL);
			if (newRSSModel != null) {
				_rssFeed = newRSSModel.GetRSSFeed();
				_logger.Debug("New RssFeed is: " + _rssFeed.toString());

				_updater.removeCallbacks(_updateRunnable);
				_updateRunnable.run();
			}
		}
	};

	public RSSViewUpdater(Context context) {
		_logger = new SmartMirrorLogger(TAG);

		_updater = new Handler();

		_context = context;
		_broadcastController = new BroadcastController(_context);
		_receiverController = new ReceiverController(_context);
	}

	public void Start(int updateTime) {
		_logger.Debug("Initialize");
		if (_isRunning) {
			_logger.Warn("Already running!");
			return;
		}

		_updateTime = updateTime;
		_logger.Debug("UpdateTime is: " + String.valueOf(_updateTime));
		_rssFeed = RSSFeed.DEFAULT;
		_logger.Debug("RssFeed is: " + _rssFeed);

		_updateRunnable.run();

		_receiverController.RegisterReceiver(_resetRSSFeedReceiver, new String[] { Broadcasts.RESET_RSS_FEED });
		_receiverController.RegisterReceiver(_updateRSSFeedReceiver, new String[] { Broadcasts.PERFORM_RSS_UPDATE });

		_isRunning = false;
	}

	public void Dispose() {
		_logger.Debug("Dispose");

		_updater.removeCallbacks(_updateRunnable);

		_receiverController.UnregisterReceiver(_resetRSSFeedReceiver);
		_receiverController.UnregisterReceiver(_updateRSSFeedReceiver);

		_isRunning = false;
	}

	public void LoadRss() {
		_logger.Debug("LoadRss");

		RSSModel model = new RSSModel(_rssFeed, true);
		_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_RSS_DATA_MODEL, Bundles.RSS_DATA_MODEL, model);
	}
}
