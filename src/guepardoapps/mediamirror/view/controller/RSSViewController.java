package guepardoapps.mediamirror.view.controller;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.enums.RSSFeed;
import guepardoapps.mediamirror.model.*;
import guepardoapps.mediamirror.rss.RssItem;
import guepardoapps.mediamirror.rss.RssService;
import guepardoapps.mediamirror.test.RSSViewControllerTest;
import guepardoapps.mediamirror.R;

import guepardoapps.toolset.controller.ReceiverController;

public class RSSViewController {

	private static final String TAG = RSSViewController.class.getName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;
	private boolean _screenEnabled;

	private int _index;
	private String _title;
	private List<RssItem> _items;

	private Context _context;
	private ReceiverController _receiverController;

	private TextView _rssTitleTextView;
	private TextView _rssSeparatorTextView;
	private TextView _rssTextView1;
	private TextView _rssTextView1Description;
	private TextView _rssTextView2;
	private TextView _rssTextView2Description;
	private TextView _rssTextView3;
	private TextView _rssTextView3Description;

	private Handler _changeTextHandler = new Handler();
	private int _changeTextTime = 15 * 1000;

	private RSSViewControllerTest _rssViewTest;

	public RSSViewController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_screenEnabled = true;

		_rssTitleTextView = (TextView) ((Activity) _context).findViewById(R.id.rssTitleTextView);
		_rssSeparatorTextView = (TextView) ((Activity) _context).findViewById(R.id.rssSeparatorTextView);

		_rssTextView1 = (TextView) ((Activity) _context).findViewById(R.id.rssTextView1);
		_rssTextView1Description = (TextView) ((Activity) _context).findViewById(R.id.rssDescriptionTextView1);
		_rssTextView2 = (TextView) ((Activity) _context).findViewById(R.id.rssTextView2);
		_rssTextView2Description = (TextView) ((Activity) _context).findViewById(R.id.rssDescriptionTextView2);
		_rssTextView3 = (TextView) ((Activity) _context).findViewById(R.id.rssTextView3);
		_rssTextView3Description = (TextView) ((Activity) _context).findViewById(R.id.rssDescriptionTextView3);
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_receiverController.RegisterReceiver(_updateViewReceiver,
					new String[] { Constants.BROADCAST_SHOW_RSS_DATA_MODEL });
			_receiverController.RegisterReceiver(_screenEnableReceiver,
					new String[] { Constants.BROADCAST_SCREEN_ENABLE });
			_receiverController.RegisterReceiver(_screenDisableReceiver,
					new String[] { Constants.BROADCAST_DISABLE_SCREEN });
			_isInitialized = true;
			_logger.Debug("Initializing!");

			if (Constants.TESTING_ENABLED) {
				if (_rssViewTest == null) {
					_rssViewTest = new RSSViewControllerTest(_context);
				}
			}
		} else {
			_logger.Warn("Is ALREADY initialized!");
		}
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
		_receiverController.UnregisterReceiver(_updateViewReceiver);
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
			RSSModel model = (RSSModel) intent.getSerializableExtra(Constants.BUNDLE_RSS_DATA_MODEL);

			if (model != null) {
				_logger.Debug(model.toString());

				if (model.GetVisibility()) {
					startService(model.GetRSSFeed());
				} else {
					_rssTitleTextView.setVisibility(View.GONE);
					_rssSeparatorTextView.setVisibility(View.GONE);

					_rssTextView1.setVisibility(View.GONE);
					_rssTextView1Description.setVisibility(View.GONE);
					_rssTextView2.setVisibility(View.GONE);
					_rssTextView2Description.setVisibility(View.GONE);
					_rssTextView3.setVisibility(View.GONE);
					_rssTextView3Description.setVisibility(View.GONE);

					_changeTextHandler.removeCallbacks(_updateRSSTextViewRunnable);
				}
			}

			if (Constants.TESTING_ENABLED) {
				_rssViewTest.ValidateView(model.GetRSSFeed(), _rssTextView1.getVisibility() == View.VISIBLE);
			}
		}
	};

	private void startService(RSSFeed rssFeed) {
		Intent intent = new Intent(_context, RssService.class);
		intent.putExtra(RssService.RECEIVER, _resultReceiver);
		intent.putExtra(RssService.FEED, rssFeed);
		_context.startService(intent);
	}

	private final ResultReceiver _resultReceiver = new ResultReceiver(new Handler()) {
		@SuppressWarnings("unchecked")
		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_index = 0;
			_title = resultData.getString(RssService.TITLE);
			_items = (List<RssItem>) resultData.getSerializable(RssService.ITEMS);
			if (_items != null && _title != null) {
				_changeTextHandler.removeCallbacks(_updateRSSTextViewRunnable);
				_updateRSSTextViewRunnable.run();

				_rssTitleTextView.setText(_title);
				_rssTitleTextView.setVisibility(View.VISIBLE);

				_rssSeparatorTextView.setVisibility(View.VISIBLE);

				_rssTextView1.setVisibility(View.VISIBLE);
				_rssTextView1Description.setVisibility(View.VISIBLE);
				_rssTextView2.setVisibility(View.VISIBLE);
				_rssTextView2Description.setVisibility(View.VISIBLE);
				_rssTextView3.setVisibility(View.VISIBLE);
				_rssTextView3Description.setVisibility(View.VISIBLE);
			} else {
				_changeTextHandler.removeCallbacks(_updateRSSTextViewRunnable);

				_rssTitleTextView.setVisibility(View.GONE);

				_rssSeparatorTextView.setVisibility(View.GONE);

				_rssTextView1.setVisibility(View.GONE);
				_rssTextView1Description.setVisibility(View.GONE);
				_rssTextView2.setVisibility(View.GONE);
				_rssTextView2Description.setVisibility(View.GONE);
				_rssTextView3.setVisibility(View.GONE);
				_rssTextView3Description.setVisibility(View.GONE);

				Toast.makeText(_context, "An error occured while downloading the rss feed.", Toast.LENGTH_LONG).show();
			}
		};
	};

	private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = true;

			_rssTitleTextView = (TextView) ((Activity) _context).findViewById(R.id.rssTitleTextView);
			_rssSeparatorTextView = (TextView) ((Activity) _context).findViewById(R.id.rssSeparatorTextView);

			_rssTextView1 = (TextView) ((Activity) _context).findViewById(R.id.rssTextView1);
			_rssTextView1Description = (TextView) ((Activity) _context).findViewById(R.id.rssDescriptionTextView1);
			_rssTextView2 = (TextView) ((Activity) _context).findViewById(R.id.rssTextView2);
			_rssTextView2Description = (TextView) ((Activity) _context).findViewById(R.id.rssDescriptionTextView2);
			_rssTextView3 = (TextView) ((Activity) _context).findViewById(R.id.rssTextView3);
			_rssTextView3Description = (TextView) ((Activity) _context).findViewById(R.id.rssDescriptionTextView3);
		}
	};

	private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = false;
		}
	};

	private Runnable _updateRSSTextViewRunnable = new Runnable() {
		public void run() {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_logger.Debug("Update RSS text view!");
			_logger.Debug("RSS List size is: " + String.valueOf(_items.size()));
			_logger.Debug("Index is: " + String.valueOf(_index));

			if (_index >= _items.size()) {
				_index = 0;
			}

			_rssTextView1.setText(_items.get(_index).GetTitle());
			_rssTextView1Description.setText(_items.get(_index).GetDescription());
			if (_index + 2 >= _items.size()) {
				if (_index + 1 >= _items.size()) {
					_rssTextView2.setText(_items.get(2).GetTitle());
					_rssTextView2Description.setText(_items.get(2).GetDescription());
					_rssTextView3.setText(_items.get(3).GetTitle());
					_rssTextView3Description.setText(_items.get(3).GetDescription());
				} else {
					_rssTextView2.setText(_items.get(_index + 1).GetTitle());
					_rssTextView2Description.setText(_items.get(_index + 1).GetDescription());
					_rssTextView3.setText(_items.get(2).GetTitle());
					_rssTextView3Description.setText(_items.get(2).GetDescription());
				}
			} else {
				_rssTextView2.setText(_items.get(_index + 1).GetTitle());
				_rssTextView2Description.setText(_items.get(_index + 1).GetDescription());
				_rssTextView3.setText(_items.get(_index + 2).GetTitle());
				_rssTextView3Description.setText(_items.get(_index + 2).GetDescription());
			}
			_index++;

			_changeTextHandler.postDelayed(this, _changeTextTime);
		}
	};
}
