package guepardoapps.games.controller;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import guepardoapps.games.snake.SnakeView;
import guepardoapps.games.tetris.GameSurfaceView;

import guepardoapps.library.toolset.controller.DialogController;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class GameDialogController extends DialogController {

    private static final String TAG = GameDialogController.class.getSimpleName();

    private boolean _isPongGame;

    private SnakeView _snakeView;
    private boolean _isSnakeGame;

    private GameSurfaceView _gameSurfaceView;
    private boolean _isTetrisGame;

    public Runnable CloseDialogCallback = new Runnable() {
        @Override
        public void run() {
            if (_dialog != null) {
                _logger.Debug("Trying to close dialog!");

                if (_isPongGame) {
                    _logger.Debug("Closing pong game!");

                    _isPongGame = false;

                    closeDialog();
                } else if (_isSnakeGame) {
                    _logger.Debug("Closing snake game!");

                    _snakeView.onDestroy();
                    _snakeView = null;
                    _isSnakeGame = false;

                    closeDialog();
                } else if (_isTetrisGame) {
                    _logger.Debug("Closing tetris game!");

                    _gameSurfaceView.OnPauseGameSurfaceView();
                    _gameSurfaceView = null;
                    _isTetrisGame = false;

                    closeDialog();
                } else {
                    _logger.Warn("No game started!");

                    if (_dialog != null) {
                        _logger.Warn("But dialog seems to be open! Closing...");
                        closeDialog();
                    }
                }
            }
        }
    };

    private Runnable _startSnakeRunnable = new Runnable() {
        @Override
        public void run() {
            _snakeView.Touch(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 50,
                    MotionEvent.ACTION_DOWN, 360, 640, 1));
        }
    };

    public GameDialogController(@NonNull Context context) {
        super(context,
                ContextCompat.getColor(context, R.color.TextIcon),
                ContextCompat.getColor(context, R.color.Primary));
        _logger = new SmartMirrorLogger(TAG);
        _logger.Debug("GameDialogController created...");

        _context = context;
    }

    public void ShowDialogPong() {
        _logger.Debug("ShowDialogPong");

        if (_dialog != null) {
            _logger.Warn("Dialog open! Closing dialog...");
            CloseDialogCallback.run();
        }

        _dialog = new Dialog(_context);

        _dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        _dialog.setContentView(R.layout.game_view_pong);

        _isPongGame = true;

        _dialog.setCancelable(false);

        _isDialogOpen = true;
        _dialog.show();
    }

    public void ShowDialogSnake() {
        _logger.Debug("ShowDialogSnake");

        if (_dialog != null) {
            _logger.Warn("Dialog open! Closing dialog...");
            CloseDialogCallback.run();
        }

        _dialog = new Dialog(_context);

        _dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        _dialog.setContentView(R.layout.game_view_snake);

        _snakeView = (SnakeView) _dialog.findViewById(R.id.snake);
        _snakeView.SetTextView((TextView) _dialog.findViewById(R.id.text));
        _snakeView.onCreate(_context);
        _snakeView.SetMode(SnakeView.READY);

        _isSnakeGame = true;

        _dialog.setCancelable(false);

        _isDialogOpen = true;
        _dialog.show();

        Handler handler = new Handler();
        handler.postDelayed(_startSnakeRunnable, 3000);
    }

    public void ShowDialogTetris() {
        _logger.Debug("ShowDialogTetris");

        if (_dialog != null) {
            _logger.Warn("Dialog open! Closing dialog...");
            CloseDialogCallback.run();
        }

        _dialog = new Dialog(_context);
        _dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        _gameSurfaceView = new GameSurfaceView(_context);
        FrameLayout gameFrameLayout = new FrameLayout(_context);
        gameFrameLayout.addView(_gameSurfaceView);

        _isTetrisGame = true;

        _dialog.setContentView(gameFrameLayout);
        _dialog.setCancelable(false);

        _gameSurfaceView.OnResumeGameSurfaceView();

        _isDialogOpen = true;
        _dialog.show();
    }

    private void closeDialog() {
        _dialog.dismiss();
        _dialog = null;
        _isDialogOpen = false;
    }
}
