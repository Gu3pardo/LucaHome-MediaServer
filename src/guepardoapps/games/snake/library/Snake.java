package guepardoapps.games.snake.library;

import guepardoapps.games.common.basic.Coordinates;
import guepardoapps.games.snake.library.exceptions.SnakeHitYardWallException;
import guepardoapps.games.snake.library.interfaces.SnakeMotionObserver;
import guepardoapps.games.snake.library.interfaces.SnakeRenderer;

public class Snake {

	@SuppressWarnings("unused")
	private static final String TAG = Snake.class.getName();

	/*
	 * The initial state reads: The snake is moving Left, and the 3 remaining
	 * parts of the body are placed to its Right.
	 */
	private String _state = "LRRR";
	private boolean _grow = false;

	private SnakeMotionObserver _snakeMotionObserver = new SnakeMotionObserver() {
		@Override
		public void updateSnakePosition(Coordinates HeadCoordinates) throws SnakeHitYardWallException {
			// NullObjectPattern, don't track the snake's position
		}
	};

	/*
	 * The default coordinates (0,0) makes the initial state a valid one to be
	 * represented in a 1x4 matrix. [L,R,R,R], which is also visually correct.
	 */
	private Coordinates _headCoordinates = new Coordinates(0, 0);

	public void SetSnakeMotionObserver(SnakeMotionObserver snakeMotionObserver) {
		_snakeMotionObserver = snakeMotionObserver;
	}

	public SnakeMotionObserver GetSnakeMotionObserver() {
		return _snakeMotionObserver;
	}

	public void SetHeadCoordinates(Coordinates coordinates) {
		_headCoordinates = coordinates;
	}

	public Coordinates GetHeadCoordinates() {
		return _headCoordinates;
	}

	public int GetLength() {
		return _state.length();
	}

	public String GetCurrentDirection() {
		return "" + _state.charAt(0);
	}

	public void HeadUp() {
		if (!"D".equals(GetCurrentDirection())) {
			_state = "U" + _state.substring(1);
		}
	}

	public void HeadDown() {
		if (!"U".equals(GetCurrentDirection())) {
			_state = "D" + _state.substring(1);
		}
	}

	public void HeadLeft() {
		if (!"R".equals(GetCurrentDirection())) {
			_state = "L" + _state.substring(1);
		}
	}

	public void HeadRight() {
		if (!"L".equals(GetCurrentDirection())) {
			_state = "R" + _state.substring(1);
		}
	}

	public void Move() throws SnakeHitYardWallException {
		String tail;

		if (!_grow) {
			tail = _state.substring(1, _state.length() - 1);
		} else {
			tail = _state.substring(1, _state.length());
			_grow = false;
		}

		String head = GetCurrentDirection();
		String neck = "D";

		if ("D".equals(head)) {
			neck = "U";
			_headCoordinates.Y++;
		} else if ("U".equals(head)) {
			neck = "D";
			_headCoordinates.Y--;
		} else if ("R".equals(head)) {
			neck = "L";
			_headCoordinates.X++;
		} else if ("L".equals(head)) {
			neck = "R";
			_headCoordinates.X--;
		}

		_state = head + neck + tail;
		_snakeMotionObserver.updateSnakePosition(_headCoordinates);
	}

	public void Eat() {
		_grow = true;
	}

	/**
	 * 
	 * @param matrix,
	 *            in (row, col) zero-based index fashion. e.g. In the following
	 *            matrix of size 2x3 (2 rows, 3 cols) the number 1 is at (1,2):
	 *            0 0 0 0 0 1 Thus, the (0,0) coordinates is at top-left.
	 */
	public void PlaceIn2DMatrix(char[][] matrix) {
		// FIXME Duplicate with render(SnakeRenderer). Unify.
		int hx = _headCoordinates.X;
		int hy = _headCoordinates.Y;

		matrix[hy][hx] = _state.charAt(0);

		int x = hx;
		int y = hy;

		for (int index = 1; index < _state.length(); index++) {
			char code = _state.charAt(index);
			
			switch (code) {
			case 'U':
				y--;
				break;
			case 'D':
				y++;
				break;
			case 'R':
				x++;
				break;
			case 'L':
				x--;
				break;
			}

			if (x >= 0 && y >= 0 && y < matrix.length && x < matrix[0].length) {
				matrix[y][x] = code;
			}
		}
	}

	public void Render(SnakeRenderer renderer) {
		int hx = _headCoordinates.X;
		int hy = _headCoordinates.Y;
		
		char direction = _state.charAt(0);
		renderer.renderHead(_headCoordinates, direction);
		
		int x = hx;
		int y = hy;
		
		for (int index = 1; index < _state.length(); index++) {
			char code = _state.charAt(index);
			
			switch (code) {
			case 'U':
				y--;
				break;
			case 'D':
				y++;
				break;
			case 'R':
				x++;
				break;
			case 'L':
				x--;
				break;
			}
			
			Coordinates coordinates = new Coordinates(x, y);
			renderer.renderBody(coordinates);
		}
	}
}
