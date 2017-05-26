package guepardoapps.mediamirror.view.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import guepardoapps.games.controller.GameDialogController;

import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;

public class GameViewController {
    private static final String TAG = GameViewController.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private boolean _isInitialized;
    private boolean _screenEnabled;

    private GameDialogController _gameDialogController;
    private ReceiverController _receiverController;

    private BroadcastReceiver _pongStartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_pongStartReceiver onReceive");
            _gameDialogController.ShowDialogPong();
        }
    };

    private BroadcastReceiver _pongStopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_pongStopReceiver onReceive");
            _gameDialogController.CloseDialogCallback.run();
        }
    };

    private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = false;
        }
    };

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = true;
        }
    };

    private BroadcastReceiver _snakeStartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_snakeStartReceiver onReceive");
            _gameDialogController.ShowDialogSnake();
        }
    };

    private BroadcastReceiver _snakeStopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_snakeStopReceiver onReceive");
            _gameDialogController.CloseDialogCallback.run();
        }
    };

    private BroadcastReceiver _tetrisStartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_tetrisStartReceiver onReceive");
            _gameDialogController.ShowDialogTetris();
        }
    };

    private BroadcastReceiver _tetrisStopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_tetrisStopReceiver onReceive");
            _gameDialogController.CloseDialogCallback.run();
        }
    };

    public GameViewController(Context context) {
        _logger = new SmartMirrorLogger(TAG);
        _gameDialogController = new GameDialogController(context);
        _receiverController = new ReceiverController(context);
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
            _receiverController.RegisterReceiver(_pongStartReceiver, new String[]{Broadcasts.START_PONG});
            _receiverController.RegisterReceiver(_pongStopReceiver, new String[]{Broadcasts.STOP_PONG});
            _receiverController.RegisterReceiver(_screenDisableReceiver, new String[]{Broadcasts.SCREEN_OFF});
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_snakeStartReceiver, new String[]{Broadcasts.START_SNAKE});
            _receiverController.RegisterReceiver(_snakeStopReceiver, new String[]{Broadcasts.STOP_SNAKE});
            _receiverController.RegisterReceiver(_tetrisStartReceiver, new String[]{Broadcasts.START_TETRIS});
            _receiverController.RegisterReceiver(_tetrisStopReceiver, new String[]{Broadcasts.STOP_TETRIS});

            _isInitialized = true;
        } else {
            _logger.Warn("Is ALREADY initialized!");
        }
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");

        _gameDialogController.CloseDialogCallback.run();
        _receiverController.Dispose();
        _isInitialized = false;
    }
}
