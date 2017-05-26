package guepardoapps.games.snake.library;

import android.annotation.SuppressLint;
import guepardoapps.games.common.Coordinates;
import guepardoapps.games.snake.library.exceptions.SnakeHitYardWallException;
import guepardoapps.games.snake.library.interfaces.SnakeMotionObserver;

public class Yard {

	@SuppressWarnings("unused")
	private static final String TAG = Yard.class.getName();

	private static int _width = 10;
	private static int _height = 10;

	private Snake _snake;
	private char[][] _matrix = new char[_height][_width];

	public class YardSnakeMotionObserver implements SnakeMotionObserver {
		@Override
		public void updateSnakePosition(Coordinates headCoordinates) throws SnakeHitYardWallException {
			if (headCoordinates.X < 0 || headCoordinates.Y < 0) {
				throw new SnakeHitYardWallException();
			}
			if (headCoordinates.X >= Yard._width) {
				throw new SnakeHitYardWallException();
			}
			if (headCoordinates.Y >= Yard._height) {
				throw new SnakeHitYardWallException();
			}
		}
	}

	public Yard() {

	}

	public Yard(int width, int height) {
		_width = width;
		_height = height;
		_matrix = new char[height][width];
	}

	public int GetWidth() {
		return _width;
	}

	public int GetHeight() {
		return _height;
	}

	public Snake GetSnake() {
		return _snake;
	}

	public void Put(Snake snake, Coordinates coordinates) {
		snake.SetHeadCoordinates(coordinates);
		_snake = snake;
		_snake.SetSnakeMotionObserver(new YardSnakeMotionObserver());
	}

	public String Render() {
		if (_snake != null) {
			cleanMatrix();
			_snake.PlaceIn2DMatrix(_matrix);
		}

		StringBuffer stringBuffer = new StringBuffer();
		renderMatrix(stringBuffer);
		renderWalls(stringBuffer);

		return stringBuffer.toString();
	}

	private void cleanMatrix() {
		for (int heightIndex = 0; heightIndex < _height; heightIndex++) {
			for (int widthIndex = 0; widthIndex < _width; widthIndex++) {
				_matrix[heightIndex][widthIndex] = 0;
			}
		}
	}

	private void renderMatrix(StringBuffer stringBuffer) {
		for (int heightIndex = 0; heightIndex < _height; heightIndex++) {
			for (int widthIndex = 0; widthIndex < _width; widthIndex++) {
				char code = _matrix[heightIndex][widthIndex];
				if (code == 0) {
					code = ' ';
				}
				stringBuffer.append(code);
			}
			stringBuffer.append('\n');
		}
	}

	@SuppressLint("DefaultLocale")
	private void renderWalls(StringBuffer stringBuffer) {
		int fromIndex = stringBuffer.indexOf("\n");
		int toIndex = 0;

		while (fromIndex > -1) {
			stringBuffer.insert(toIndex, '*');
			stringBuffer.insert(fromIndex + 1, '*');
			toIndex = fromIndex + 3;
			fromIndex = stringBuffer.indexOf("\n", fromIndex + 3);
		}

		String hWall = String.format(String.format("%%0%dd", _width + 2), 0).replace("0", "*");
		stringBuffer.insert(0, hWall + "\n");
		stringBuffer.append(hWall + "\n");
	}
}
