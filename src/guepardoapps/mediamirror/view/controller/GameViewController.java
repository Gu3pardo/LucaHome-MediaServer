package guepardoapps.mediamirror.view.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import guepardoapps.games.controller.GameDialogController;
import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.toolset.controller.ReceiverController;

public class GameViewController {
	private static final String TAG = GameViewController.class.getName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;
	private boolean _screenEnabled;

	private Context _context;
	private GameDialogController _gameDialogController;
	private ReceiverController _receiverController;

	private BroadcastReceiver _pongStartReveicer = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_logger.Debug("_pongStartReveicer onReceive");
			_gameDialogController.ShowDialogPong();
		}
	};

	private BroadcastReceiver _pongStopReveicer = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_logger.Debug("_pongStopReveicer onReceive");
			_gameDialogController.CloseDialogCallback.run();
		}
	};

	private BroadcastReceiver _snakeStartReveicer = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_logger.Debug("_snakeStartReveicer onReceive");
			_gameDialogController.ShowDialogSnake();
		}
	};

	private BroadcastReceiver _snakeStopReveicer = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_logger.Debug("_snakeStopReveicer onReceive");
			_gameDialogController.CloseDialogCallback.run();
		}
	};

	private BroadcastReceiver _tetrisStartReveicer = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_logger.Debug("_tetrisStartReveicer onReceive");
			_gameDialogController.ShowDialogTetris();
		}
	};

	private BroadcastReceiver _tetrisStopReveicer = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!_screenEnabled) {
				_logger.Debug("Screen is not enabled!");
				return;
			}

			_logger.Debug("_tetrisStopReveicer onReceive");
			_gameDialogController.CloseDialogCallback.run();
		}
	};

	private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = true;
		}
	};

	private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_screenEnabled = false;
		}
	};

	public GameViewController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
		_gameDialogController = new GameDialogController(_context);
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_screenEnabled = true;
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			_logger.Debug("Initializing!");
			_receiverController.RegisterReceiver(_pongStartReveicer, new String[] { Constants.BROADCAST_START_PONG });
			_receiverController.RegisterReceiver(_pongStopReveicer, new String[] { Constants.BROADCAST_STOP_PONG });
			_receiverController.RegisterReceiver(_snakeStartReveicer, new String[] { Constants.BROADCAST_START_SNAKE });
			_receiverController.RegisterReceiver(_snakeStopReveicer, new String[] { Constants.BROADCAST_STOP_SNAKE });
			_receiverController.RegisterReceiver(_tetrisStartReveicer,
					new String[] { Constants.BROADCAST_START_TETRIS });
			_receiverController.RegisterReceiver(_tetrisStopReveicer, new String[] { Constants.BROADCAST_STOP_TETRIS });
			_receiverController.RegisterReceiver(_screenEnableReceiver,
					new String[] { Constants.BROADCAST_SCREEN_ENABLED });
			_receiverController.RegisterReceiver(_screenDisableReceiver,
					new String[] { Constants.BROADCAST_SCREEN_OFF, Constants.BROADCAST_SCREEN_SAVER });
			_isInitialized = true;
		} else {
			_logger.Warn("Is ALREADY initialized!");
		}
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
		_gameDialogController.CloseDialogCallback.run();
		_receiverController.UnregisterReceiver(_pongStartReveicer);
		_receiverController.UnregisterReceiver(_pongStopReveicer);
		_receiverController.UnregisterReceiver(_snakeStartReveicer);
		_receiverController.UnregisterReceiver(_snakeStopReveicer);
		_receiverController.UnregisterReceiver(_tetrisStartReveicer);
		_receiverController.UnregisterReceiver(_tetrisStopReveicer);
		_receiverController.UnregisterReceiver(_screenEnableReceiver);
		_receiverController.UnregisterReceiver(_screenDisableReceiver);
		_isInitialized = false;
	}
}
