package guepardoapps.games.snake;

import java.util.ArrayList;
import java.util.Random;

import guepardoapps.games.snake.library.Game;
import guepardoapps.games.snake.library.GameFactory;
import guepardoapps.games.snake.library.Snake;
import guepardoapps.games.snake.library.exceptions.SnakeHitYardWallException;
import guepardoapps.games.snake.library.interfaces.SnakeRenderer;
import guepardoapps.games.snake.library.Yard;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import guepardoapps.games.common.GameConstants;
import guepardoapps.games.common.basic.Coordinates;
import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.Constants;

import guepardoapps.toolset.common.Logger;
import guepardoapps.toolset.controller.ReceiverController;

public class SnakeView extends TileView {

	private static final String TAG = SnakeView.class.getName();
	private Logger _logger;

	public static final int PAUSE = 0;
	public static final int READY = 1;
	public static final int RUNNING = 2;
	public static final int LOSE = 3;

	/**
	 * Current direction the snake is headed.
	 */
	/*
	 * TODO private int mDirection = NORTH; private int mNextDirection = NORTH;
	 */

	private static final int RED_STAR = 1;
	private static final int YELLOW_STAR = 2;
	private static final int GREEN_STAR = 3;

	private static final Random RNG = new Random();

	private int _mode = READY;

	private Game _game;
	private long _score = 0;
	// TODO private long mMoveDelay = 600;
	/**
	 * mLastMove: tracks the absolute time when the snake last moved, and is
	 * used to determine if a move should be made based on mMoveDelay.
	 */
	// TODO private long mLastMove;

	private TextView _statusText;

	/**
	 * mSnakeTrail: a list of Coordinates that make up the snake's body
	 * _appleList: the secret location of the juicy apples the snake craves.
	 */
	// TODO private ArrayList<Coordinate> mSnakeTrail = new
	// ArrayList<Coordinate>();
	private ArrayList<Coordinate> _appleList = new ArrayList<Coordinate>();

	private RefreshHandler _redrawHandler = new RefreshHandler();

	private class RefreshHandler extends Handler {
		@Override
		public void handleMessage(Message message) {
			SnakeView.this.update();
			SnakeView.this.invalidate();
		}

		public void sleep(long delayMillis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};

	private Context _context;
	private ReceiverController _receiverController;

	private BroadcastReceiver _commandReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_commandReceiver onReceive");
			String command = intent.getStringExtra(Constants.BUNDLE_GAME_COMMAND);
			if (command != null) {
				_logger.Warn("Command is: " + command);

				Snake snake = _game.GetYard().GetSnake();
				String _currentDirection = snake.GetCurrentDirection();

				if (command.contains(GameConstants.UP)) {
					if (!_currentDirection.contains(GameConstants.UP)) {
						snake.HeadUp();
					}
				} else if (command.contains(GameConstants.DOWN)) {
					if (!_currentDirection.contains(GameConstants.DOWN)) {
						snake.HeadDown();
					}
				} else if (command.contains(GameConstants.RIGHT)) {
					if (!_currentDirection.contains(GameConstants.RIGHT)) {
						snake.HeadRight();
					}
				} else if (command.contains(GameConstants.LEFT)) {
					if (!_currentDirection.contains(GameConstants.LEFT)) {
						snake.HeadLeft();
					}
				} else {
					_logger.Warn("Command is not supported: " + command);
				}
			} else {
				_logger.Warn("Command is null!");
			}
		}
	};

	public SnakeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initSnakeView();
	}

	public SnakeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initSnakeView();
	}

	public void onCreate(Context context) {
		_logger.Debug("onCreate");
		_context = context;
		_receiverController = new ReceiverController(_context);
		_receiverController.RegisterReceiver(_commandReceiver, new String[] { Constants.BROADCAST_GAME_COMMAND });
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
		_receiverController.UnregisterReceiver(_commandReceiver);
	}

	public Bundle saveState() {
		Bundle map = new Bundle();

		map.putIntArray("_appleList", coordArrayListToArray(_appleList));
		// TODO map.putInt("mDirection", Integer.valueOf(mDirection));
		// TODO map.putInt("mNextDirection", Integer.valueOf(mNextDirection));
		// TODO map.putLong("mMoveDelay", Long.valueOf(mMoveDelay));
		map.putLong("_score", Long.valueOf(_score));
		// TODO map.putIntArray("mSnakeTrail",
		// coordArrayListToArray(mSnakeTrail));

		return map;
	}

	public void restoreState(Bundle icicle) {
		setMode(PAUSE);

		_appleList = coordArrayToArrayList(icicle.getIntArray("_appleList"));
		// TODO mDirection = icicle.getInt("mDirection");
		// TODO mNextDirection = icicle.getInt("mNextDirection");
		// TODO mMoveDelay = icicle.getLong("mMoveDelay");
		_score = icicle.getLong("_score");
		// TODO mSnakeTrail =
		// coordArrayToArrayList(icicle.getIntArray("mSnakeTrail"));
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Touch(event);
		return true;
	}

	public void setTextView(TextView newView) {
		_statusText = newView;
	}

	public void setMode(int newMode) {
		int prevMode = _mode;
		_mode = newMode;

		if (newMode == RUNNING & prevMode != RUNNING) {
			_statusText.setVisibility(View.INVISIBLE);
			update();
			return;
		}

		Resources resources = getContext().getResources();
		CharSequence message = "";
		if (newMode == PAUSE) {
			message = resources.getText(R.string.mode_pause);
		}
		if (newMode == READY) {
			message = resources.getText(R.string.mode_ready);
		}
		if (newMode == LOSE) {
			message = resources.getString(R.string.mode_lose_prefix) + _score
					+ resources.getString(R.string.mode_lose_suffix);
		}

		_statusText.setText(message);
		_statusText.setVisibility(View.VISIBLE);
	}

	public void update() {
		if (_mode == RUNNING) {
			/*
			 * TODO long now = System.currentTimeMillis();
			 * 
			 * if (now - mLastMove > mMoveDelay) { clearTiles(); updateWalls();
			 * updateSnake(); updateApples(); mLastMove = now; }
			 */
			clearTiles();
			updateWalls();
			updateSnake();
			updateApples();
			try {
				_game.Tick();
			} catch (SnakeHitYardWallException e) {
				setMode(LOSE);
				_logger.Error(e.toString());
			}
			long mMoveDelay = _game.GetDelay();
			_redrawHandler.sleep(mMoveDelay);
		}
	}

	public void Touch(MotionEvent event) {
		if (_mode == READY | _mode == LOSE) {
			initNewGame();
			setMode(RUNNING);
			update();
		}

		if (_mode == PAUSE) {
			setMode(RUNNING);
			update();
		}

		// On every touch of the phone screen the snake turns clockwise
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Snake snake = _game.GetYard().GetSnake();
			String _currentDirection = snake.GetCurrentDirection();

			if (GameConstants.DOWN.equals(_currentDirection)) {
				snake.HeadLeft();
			}
			if (GameConstants.LEFT.equals(_currentDirection)) {
				snake.HeadUp();
			}
			if (GameConstants.UP.equals(_currentDirection)) {
				snake.HeadRight();
			}
			if (GameConstants.RIGHT.equals(_currentDirection)) {
				snake.HeadDown();
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void initSnakeView() {
		_logger = new Logger(TAG, Constants.DEBUGGING_ENABLED);
		_logger.Debug("SnakeView created...");

		setFocusable(true);

		Resources resources = this.getContext().getResources();

		resetTiles(4);

		loadTile(RED_STAR, resources.getDrawable(R.drawable.redstar));
		loadTile(YELLOW_STAR, resources.getDrawable(R.drawable.yellowstar));
		loadTile(GREEN_STAR, resources.getDrawable(R.drawable.greenstar));
	}

	private void initNewGame() {
		// TODO mSnakeTrail.clear();
		_appleList.clear();

		// For now we're just going to load up a short default eastbound snake
		// that's just turned north

		/*
		 * TODO mSnakeTrail.add(new Coordinate(7, 7)); mSnakeTrail.add(new
		 * Coordinate(6, 7)); mSnakeTrail.add(new Coordinate(5, 7));
		 * mSnakeTrail.add(new Coordinate(4, 7)); mSnakeTrail.add(new
		 * Coordinate(3, 7)); mSnakeTrail.add(new Coordinate(2, 7));
		 * mNextDirection = NORTH;
		 */
		_game = GameFactory.createGameWithYardDimensions(TileView._tileCountX, TileView._tileCountY);
		_game.init();

		// Two apples to start with
		addRandomApple();
		addRandomApple();

		// TODO mMoveDelay = 600;
		_score = 0;
	}

	private int[] coordArrayListToArray(ArrayList<Coordinate> coordinateList) {
		int count = coordinateList.size();
		int[] rawArray = new int[count * 2];
		for (int index = 0; index < count; index++) {
			Coordinate coordinate = coordinateList.get(index);
			rawArray[2 * index] = coordinate.X;
			rawArray[2 * index + 1] = coordinate.Y;
		}
		return rawArray;
	}

	private ArrayList<Coordinate> coordArrayToArrayList(int[] rawArray) {
		ArrayList<Coordinate> coordArrayList = new ArrayList<Coordinate>();
		int coordinateCount = rawArray.length;
		for (int index = 0; index < coordinateCount; index += 2) {
			Coordinate coordinate = new Coordinate(rawArray[index], rawArray[index + 1]);
			coordArrayList.add(coordinate);
		}
		return coordArrayList;
	}

	private void addRandomApple() {
		Coordinate newCoordinate = null;
		boolean found = false;
		while (!found) {
			// Choose a new location for our apple
			int newX = 1 + RNG.nextInt(_tileCountX - 2);
			int newY = 1 + RNG.nextInt(_tileCountY - 2);
			newCoordinate = new Coordinate(newX, newY);

			// Make sure it's not already under the snake
			boolean collision = false;
			/*
			 * TODO int snakelength = mSnakeTrail.size(); for (int index = 0;
			 * index < snakelength; index++) { if
			 * (mSnakeTrail.get(index).equals(newCoord)) { collision = true; } }
			 */
			// if we're here and there's been no collision, then we have
			// a good location for an apple. Otherwise, we'll circle back
			// and try again
			found = !collision;
		}
		if (newCoordinate == null) {
			_logger.Error("Somehow ended up with a null newCoord!");
		}
		_appleList.add(newCoordinate);
	}

	private void updateWalls() {
		for (int x = 0; x < _tileCountX; x++) {
			setTile(GREEN_STAR, x, 0);
			setTile(GREEN_STAR, x, _tileCountY - 1);
		}
		for (int y = 1; y < _tileCountY - 1; y++) {
			setTile(GREEN_STAR, 0, y);
			setTile(GREEN_STAR, _tileCountX - 1, y);
		}
	}

	private void updateApples() {
		for (Coordinate coordinate : _appleList) {
			setTile(YELLOW_STAR, coordinate.X, coordinate.Y);
		}
	}

	private void updateSnake() {
		// TODO boolean growSnake = false;

		// grab the snake by the head
		// TODO Coordinate head = mSnakeTrail.get(0);
		// TODO Coordinate newHead = new Coordinate(1, 1);

		Yard yard = _game.GetYard();
		Snake snake = yard.GetSnake();

		/*
		 * TODO mDirection = mNextDirection;
		 * 
		 * switch (mDirection) { case EAST: { newHead = new Coordinate(head.x +
		 * 1, head.y); break; } case WEST: { newHead = new Coordinate(head.x -
		 * 1, head.y); break; } case NORTH: { newHead = new Coordinate(head.x,
		 * head.y - 1); break; } case SOUTH: { newHead = new Coordinate(head.x,
		 * head.y + 1); break; } }
		 */

		// Collision detection
		// For now we have a 1-square wall around the entire arena
		/*
		 * TODO if ((newHead.x < 1) || (newHead.y < 1) || (newHead.x >
		 * mXTileCount - 2) || (newHead.y > mYTileCount - 2)) { setMode(LOSE);
		 * return;
		 * 
		 * }
		 */

		// Look for collisions with itself
		/*
		 * TODO int snakelength = mSnakeTrail.size(); for (int snakeindex = 0;
		 * snakeindex < snakelength; snakeindex++) { Coordinate c =
		 * mSnakeTrail.get(snakeindex); if (c.equals(newHead)) { setMode(LOSE);
		 * return; } }
		 */

		// Look for apples
		int applecount = _appleList.size();
		for (int appleindex = 0; appleindex < applecount; appleindex++) {
			Coordinate coordinate = _appleList.get(appleindex);
			Coordinates head = snake.GetHeadCoordinates();
			if (coordinate.X == head.X && coordinate.Y == head.Y) {
				// TODO if (c.equals(newHead)) {
				_appleList.remove(coordinate);
				addRandomApple();

				_score++;
				// TODO mMoveDelay *= 0.9;

				// TODO growSnake = true;
				snake.Eat();
			}
		}

		// push a new head onto the ArrayList and pull off the tail
		/*
		 * TODO mSnakeTrail.add(0, newHead); // except if we want the snake to
		 * grow if (!growSnake) { mSnakeTrail.remove(mSnakeTrail.size() - 1); }
		 */

		/*
		 * TODO int index = 0; for (Coordinate c : mSnakeTrail) { if (index ==
		 * 0) { setTile(YELLOW_STAR, c.x, c.y); } else { setTile(RED_STAR, c.x,
		 * c.y); } index++; }
		 */

		snake.Render(new SnakeRenderer() {

			@Override
			public void renderHead(Coordinates headCoordinates, char direction) {
				setTile(YELLOW_STAR, headCoordinates.X, headCoordinates.Y);
			}

			@Override
			public void renderBody(Coordinates coordinates) {
				setTile(RED_STAR, coordinates.X, coordinates.Y);
			}
		});

	}

	private class Coordinate {
		public int X;
		public int Y;

		public Coordinate(int newX, int newY) {
			X = newX;
			Y = newY;
		}

		@SuppressWarnings("unused")
		public boolean Equals(Coordinate compareCoordinate) {
			if (X == compareCoordinate.X && Y == compareCoordinate.Y) {
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return "Coordinate: [" + X + "," + Y + "]";
		}
	}
}
