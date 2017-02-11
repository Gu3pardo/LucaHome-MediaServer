package guepardoapps.games.pong;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import guepardoapps.games.common.GameConstants;

import guepardoapps.mediamirror.common.Enables;

import guepardoapps.toolset.common.Logger;

public class GameState {

	private static final String TAG = GameState.class.getName();
	private Logger _logger;

	private static final int MAX_POINT = 5;

	private final int _screenWidth = 600;
	private final int _screenHeight = 960;

	private final int _ballSize = 10;
	private int _ballX = (_screenWidth / 2) - (_ballSize / 2);
	private int _ballY = (_screenHeight / 2) - (_ballSize / 2);
	private int _ballVelocityX = 2;
	private int _ballVelocityY = 2;

	private final int _boardLength = 75;
	private final int _boardHeight = 10;
	private final int _boardSpeed = 25;

	// Player 1
	private int _player1BoardX = (_screenWidth / 2) - (_boardLength / 2);
	private final int _player1BoardY = 20;
	private int _player1Point = 0;

	// Player 2
	private int _player2BoardX = (_screenWidth / 2) - (_boardLength / 2);
	private final int _player2BoardY = _screenHeight - 20;
	private int _player2Point = 0;

	private boolean _gameIsRunning = true;
	private boolean _gameIsPaused;

	public GameState() {
		_logger = new Logger(TAG, Enables.DEBUGGING_ENABLED);
		_logger.Debug("GameState created...");
	}

	public void Draw(Canvas canvas, Paint paint) {
		canvas.drawRGB(20, 20, 20);
		paint.setARGB(255, 255, 255, 255);

		drawBall(canvas, paint);
		drawBoards(canvas, paint);
		drawPoints(canvas, paint);
		drawInfo(canvas, paint);
	}

	public void Update() {
		if (!_gameIsRunning) {
			_logger.Warn("Game stopped!");
			return;
		}

		if (_gameIsPaused) {
			_logger.Info("Game paused!");
			return;
		}

		_ballX += _ballVelocityX;
		_ballY += _ballVelocityY;

		if (_ballY < 0) {
			_player2Point++;
			_gameIsRunning = checkPoints();
			resetBall();
		}
		if (_ballY > _screenHeight) {
			_player1Point++;
			_gameIsRunning = checkPoints();
			resetBall();
		}

		if (_ballX > _screenWidth || _ballX < 0) {
			_ballVelocityX *= -1;
		}
		if (_ballX > _player1BoardX && _ballX < _player1BoardX + _boardLength && _ballY < _player1BoardY) {
			_ballVelocityY *= -1;
		}
		if (_ballX > _player2BoardX && _ballX < _player2BoardX + _boardLength && _ballY > _player2BoardY) {
			_ballVelocityY *= -1;
		}
	}

	public void Pause() {
		_gameIsPaused = true;
	}

	public void Resume() {
		_gameIsPaused = false;
	}

	public void Restart() {
		_player1Point = 0;
		_player2Point = 0;
		_gameIsRunning = true;
		resetBall();
	}

	public void MoveBoard(int player, String move) {
		switch (player) {
		case 1:
			if (move != null) {
				if (move.contains(GameConstants.RIGHT)) {
					_player1BoardX -= _boardSpeed;
					if (_player1BoardX < 0) {
						_player1BoardX = 0;
					}
				} else if (move.contains(GameConstants.LEFT)) {
					_player1BoardX += _boardSpeed;
					if (_player1BoardX > _screenWidth - _boardLength) {
						_player1BoardX = _screenWidth - _boardLength;
					}
				} else {
					_logger.Warn("Move " + move + " is not supported!");
				}
			} else {
				_logger.Warn("Move is null!");
			}
			break;
		case 2:
			if (move != null) {
				if (move.contains(GameConstants.RIGHT)) {
					_player2BoardX += _boardSpeed;
					if (_player2BoardX > _screenWidth - _boardLength) {
						_player2BoardX = _screenWidth - _boardLength;
					}
				} else if (move.contains(GameConstants.LEFT)) {
					_player2BoardX -= _boardSpeed;
					if (_player2BoardX < 0) {
						_player2BoardX = 0;
					}
				} else {
					_logger.Warn("Move " + move + " is not supported!");
				}
			} else {
				_logger.Warn("Move is null!");
			}
			break;
		default:
			_logger.Warn("Player " + String.valueOf(player) + " is not valid!");
			break;
		}
	}

	private void drawBall(Canvas canvas, Paint paint) {
		canvas.drawRect(new Rect(_ballX, _ballY, _ballX + _ballSize, _ballY + _ballSize), paint);
	}

	private void drawBoards(Canvas canvas, Paint paint) {
		canvas.drawRect(
				new Rect(_player1BoardX, _player1BoardY, _player1BoardX + _boardLength, _player1BoardY + _boardHeight),
				paint);
		canvas.drawRect(
				new Rect(_player2BoardX, _player2BoardY, _player2BoardX + _boardLength, _player2BoardY + _boardHeight),
				paint);
	}

	private void drawPoints(Canvas canvas, Paint paint) {
		paint.setColor(Color.WHITE);
		paint.setTextSize(15);

		canvas.drawText("Player 1: " + String.valueOf(_player1Point), 10, 20, paint);
		canvas.drawText("Player 2: " + String.valueOf(_player2Point), 10, _screenHeight - 20, paint);
	}

	private void drawInfo(Canvas canvas, Paint paint) {
		if (_gameIsPaused) {
			paint.setTextSize(35);
			canvas.drawText("Paused!" + String.valueOf(_player2Point), _screenWidth / 2, _screenHeight / 2, paint);
		}
	}

	private boolean checkPoints() {
		if (_player1Point < MAX_POINT && _player2Point < MAX_POINT) {
			return true;
		}
		return false;
	}

	private void resetBall() {
		_ballX = (_screenWidth / 2) - (_ballSize / 2);
		_ballY = (_screenHeight / 2) - (_ballSize / 2);
	}
}