package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.Tools;
import guepardoapps.mediamirror.common.enums.RSSFeed;
import guepardoapps.mediamirror.model.RSSModel;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class RSSViewUpdater {

	private static final String TAG = RSSViewUpdater.class.getName();
	private SmartMirrorLogger _logger;

	private int _updateTime;
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
			RSSModel newRSSModel = (RSSModel) intent.getSerializableExtra(Constants.BUNDLE_RSS_MODEL);
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

		_updateTime = updateTime;
		_logger.Debug("UpdateTime is: " + String.valueOf(_updateTime));
		_rssFeed = RSSFeed.DEFAULT;
		_logger.Debug("RssFeed is: " + _rssFeed);

		_updateRunnable.run();

		_receiverController.RegisterReceiver(_resetRSSFeedReceiver,
				new String[] { Constants.BROADCAST_RESET_RSS_FEED });
		_receiverController.RegisterReceiver(_updateRSSFeedReceiver,
				new String[] { Constants.BROADCAST_UPDATE_RSS_FEED });
	}

	public void Dispose() {
		_logger.Debug("Dispose");

		_updater.removeCallbacks(_updateRunnable);

		_receiverController.UnregisterReceiver(_resetRSSFeedReceiver);
		_receiverController.UnregisterReceiver(_updateRSSFeedReceiver);
	}

	public void LoadRss() {
		_logger.Debug("LoadRss");

		if (Tools.IsMuteTime()) {
			_logger.Warn("Mute time!");
			return;
		}

		RSSModel model = new RSSModel(_rssFeed, true);
		_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_SHOW_RSS_DATA_MODEL,
				Constants.BUNDLE_RSS_DATA_MODEL, model);
	}
}
