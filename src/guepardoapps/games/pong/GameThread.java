package guepardoapps.games.pong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.view.SurfaceHolder;

import guepardoapps.mediamirror.common.Constants;

import guepardoapps.toolset.common.Logger;

public class GameThread extends Thread {

	private static final String TAG = GameThread.class.getName();
	private Logger _logger;

	private SurfaceHolder _surfaceHolder;
	private Paint _paint;
	private GameState _state;

	private boolean _gameIsRunning = true;

	public GameThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
		_logger = new Logger(TAG, Constants.DEBUGGING_ENABLED);
		_logger.Debug("GameThread created...");

		_surfaceHolder = surfaceHolder;
		_paint = new Paint();
		_state = new GameState();
	}

	public GameState GetGameState() {
		return _state;
	}

	@Override
	public void run() {
		while (_gameIsRunning) {
			try {
				Canvas canvas = _surfaceHolder.lockCanvas();
				_state.Update();
				_state.Draw(canvas, _paint);
				_surfaceHolder.unlockCanvasAndPost(canvas);
			} catch (Exception e) {
				_logger.Error(e.toString());
				_gameIsRunning = false;
			}
		}
	}

	public void StopGame() {
		_gameIsRunning = false;
	}
}