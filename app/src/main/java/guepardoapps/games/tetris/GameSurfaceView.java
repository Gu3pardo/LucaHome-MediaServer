package guepardoapps.games.tetris;

import java.util.Arrays;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import guepardoapps.games.common.Coordinates;
import guepardoapps.games.common.GameConstants;
import guepardoapps.games.tetris.enums.Piece;

import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;

public class GameSurfaceView extends SurfaceView implements Runnable {

    private static final String TAG = GameSurfaceView.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private ReceiverController _receiverController;

    private static final int MOVE_DOWN_TIMEOUT = 1000;
    private static final int SQUARE_SIZE = 50;

    private Thread _thread = null;

    private Paint _backgroundPaint;
    private SurfaceHolder _surfaceHolder;

    private BroadcastReceiver _commandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_commandReceiver onReceive");
            String command = intent.getStringExtra(Bundles.GAME_COMMAND);
            if (command != null) {
                _logger.Warn("Command is: " + command);

                if (command.contains(GameConstants.DOWN)) {
                    Game.UserPressedDown();
                } else if (command.contains(GameConstants.RIGHT)) {
                    Game.UserPressedRight();
                } else if (command.contains(GameConstants.LEFT)) {
                    Game.UserPressedLeft();
                } else if (command.contains(GameConstants.ROTATE)) {
                    Game.UserPressedRotate();
                } else {
                    _logger.Warn("Command is not supported: " + command);
                }
            } else {
                _logger.Warn("Command is null!");
            }
        }
    };

    volatile boolean running = false;

    public GameState Game;

    public GameSurfaceView(@NonNull Context context) {
        super(context);
        _logger = new SmartMirrorLogger(TAG);
        _logger.Debug("GameSurfaceView created...");

        _receiverController = new ReceiverController(context);

        _surfaceHolder = getHolder();

        _backgroundPaint = new Paint();
        _backgroundPaint.setColor(Color.WHITE);

        Game = new GameState(this);
    }

    public void OnResumeGameSurfaceView() {
        running = true;
        _receiverController.RegisterReceiver(_commandReceiver, new String[]{Broadcasts.GAME_COMMAND});
        _thread = new Thread(this);
        _thread.start();
    }

    public void OnPauseGameSurfaceView() {
        boolean retry = true;
        running = false;
        _receiverController.UnregisterReceiver(_commandReceiver);
        while (retry) {
            try {
                _thread.join();
                retry = false;
            } catch (InterruptedException e) {
                _logger.Error(e.toString());
            }
        }
    }

    public final int DisplayHeight() {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    public final int DisplayWidth() {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    @Override
    public void run() {
        while (running) {
            if (_surfaceHolder.getSurface().isValid()) {
                Canvas canvas = _surfaceHolder.lockCanvas();

                int width = canvas.getWidth();
                int height = canvas.getHeight();
                canvas.drawRect(0, 0, width, height, _backgroundPaint);

                List<Shape> gameShapes = Game.GetShapes();

                for (int shapeIndex = 0; shapeIndex < gameShapes.size(); shapeIndex++) {
                    Shape shape = gameShapes.get(shapeIndex);

                    Paint paint = new Paint();
                    paint.setColor(getColor(shape.Id));

                    List<Coordinates> coordinatesToDelete = Game.DeleteThisRow();
                    List<Coordinates> coordinates = shape.ShapeCoordinates();

                    for (int coordinatesIndex = 0; coordinatesIndex < coordinatesToDelete.size(); coordinatesIndex++) {
                        int[] compareCoordToDelete = {coordinatesToDelete.get(coordinatesIndex).X,
                                coordinatesToDelete.get(coordinatesIndex).Y};

                        String compareDeleteCoordString = Arrays.toString(compareCoordToDelete);

                        for (int listIndex = 0; listIndex < coordinates.size(); listIndex++) {

                            int[] compareCoord = {coordinates.get(listIndex).X, coordinates.get(listIndex).Y};
                            String compareCoordString = Arrays.toString(compareCoord);

                            if (compareCoordString.equals(compareDeleteCoordString)) {
                                coordinates.remove(listIndex);
                            }
                        }
                    }

                    for (int listIndex = 0; listIndex < coordinates.size(); listIndex++) {
                        Rect rect = new Rect(
                                coordinates.get(listIndex).X,
                                coordinates.get(listIndex).Y,
                                coordinates.get(listIndex).X + SQUARE_SIZE,
                                coordinates.get(listIndex).Y + SQUARE_SIZE);
                        canvas.drawRect(rect, paint);
                    }
                }

                _surfaceHolder.unlockCanvasAndPost(canvas);

                try {
                    Thread.sleep(MOVE_DOWN_TIMEOUT);
                    Game.GetFallingShape().Fall();
                } catch (InterruptedException e) {
                    _logger.Error(e.toString());
                }
            }
        }
    }

    private int getColor(@NonNull Piece piece) {
        switch (piece) {
            case T:
                return Color.GREEN;
            case L:
                return Color.RED;
            case Z:
                return Color.YELLOW;
            case S:
                return Color.BLUE;
            case LL:
                return Color.MAGENTA;
            default:
                return Color.CYAN;
        }
    }
}
