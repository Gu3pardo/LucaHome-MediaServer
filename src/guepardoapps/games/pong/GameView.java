package guepardoapps.games.pong;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import guepardoapps.games.common.GameConstants;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;

import guepardoapps.toolset.controller.ReceiverController;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = GameView.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private GameThread _thread;

	private Context _context;
	private ReceiverController _receiverController;

	private BroadcastReceiver _commandReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_commandReceiver onReceive");
			String command = intent.getStringExtra(Bundles.GAME_COMMAND);

			if (command != null) {
				_logger.Debug("Command is: " + command);

				String[] data = command.split("\\:");
				if (data.length == 2) {
					if (data[0] != null) {
						if (data[0].contains(GameConstants.GAME)) {
							if (data[1] != null) {
								if (data[1].contains(GameConstants.PAUSE)) {
									_thread.GetGameState().Pause();
								} else if (data[1].contains(GameConstants.RESUME)) {
									_thread.GetGameState().Resume();
								} else if (data[1].contains(GameConstants.RESTART)) {
									_thread.GetGameState().Restart();
								} else {
									_logger.Warn("data[1] has unsupported data: " + data[1]);
								}
							} else {
								_logger.Warn("data[1] is null!");
							}
						} else if (data[0].contains(GameConstants.PLAYER_ONE)) {
							if (data[1] != null) {
								if (data[1].contains(GameConstants.LEFT)) {
									_thread.GetGameState().MoveBoard(1, GameConstants.LEFT);
								} else if (data[1].contains(GameConstants.RIGHT)) {
									_thread.GetGameState().MoveBoard(1, GameConstants.RIGHT);
								} else {
									_logger.Warn("data[1] has unsupported data: " + data[1]);
								}
							} else {
								_logger.Warn("data[1] is null!");
							}
						} else if (data[0].contains(GameConstants.PLAYER_TWO)) {
							if (data[1] != null) {
								if (data[1].contains(GameConstants.LEFT)) {
									_thread.GetGameState().MoveBoard(2, GameConstants.LEFT);
								} else if (data[1].contains(GameConstants.RIGHT)) {
									_thread.GetGameState().MoveBoard(2, GameConstants.RIGHT);
								} else {
									_logger.Warn("data[1] has unsupported data: " + data[1]);
								}
							} else {
								_logger.Warn("data[1] is null!");
							}
						} else {
							_logger.Warn("data[0] has unsupported data: " + data[0]);
						}
					} else {
						_logger.Warn("data[0] is null!");
					}
				} else {
					_logger.Warn("Data has wrong size: " + String.valueOf(data.length));
				}
			} else {
				_logger.Warn("Command is null!");
			}
		}
	};

	public GameView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		_logger = new SmartMirrorLogger(TAG);
		_logger.Debug("GameView created...");

		_context = context;
		_receiverController = new ReceiverController(_context);

		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		setFocusable(true);

		_thread = new GameThread(holder, context, new Handler());
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		_logger.Debug("surfaceCreated");
		_receiverController.RegisterReceiver(_commandReceiver, new String[] { Broadcasts.GAME_COMMAND });
		_thread.start();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		_logger.Debug("surfaceDestroyed");
		_receiverController.UnregisterReceiver(_commandReceiver);
		_thread.StopGame();
		try {
			_thread.stop();
		} catch (Exception e) {
			_logger.Error(e.toString());
		}
	}
}
