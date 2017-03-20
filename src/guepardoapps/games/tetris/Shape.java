package guepardoapps.games.tetris;

import java.util.ArrayList;
import java.util.List;

import guepardoapps.games.common.Coordinates;
import guepardoapps.games.tetris.enums.Piece;
import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class Shape {

	private static final String TAG = Shape.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private static final int SQUARE_SIZE = 55;

	private GameSurfaceView _gameSurfaceView;
	private int _down, _right, _rotate;

	public Coordinates CoordinatesA, CoordinatesB, CoordinatesC, CoordinatesD;
	public Piece Id;
	public Boolean IsFalling = true;

	public static Shape t(GameSurfaceView display) {
		return new Shape(new Coordinates(SQUARE_SIZE, -2 * SQUARE_SIZE), new Coordinates(2 * SQUARE_SIZE, -SQUARE_SIZE),
				new Coordinates(SQUARE_SIZE, -SQUARE_SIZE), new Coordinates(0, -SQUARE_SIZE), Piece.T, display);
	}

	public static Shape l(GameSurfaceView display) {
		return new Shape(new Coordinates(3 * SQUARE_SIZE, -4 * SQUARE_SIZE),
				new Coordinates(3 * SQUARE_SIZE, -3 * SQUARE_SIZE), new Coordinates(3 * SQUARE_SIZE, -2 * SQUARE_SIZE),
				new Coordinates(3 * SQUARE_SIZE, -SQUARE_SIZE), Piece.L, display);
	}

	public static Shape z(GameSurfaceView display) {
		return new Shape(new Coordinates(0, -2 * SQUARE_SIZE), new Coordinates(SQUARE_SIZE, -2 * SQUARE_SIZE),
				new Coordinates(SQUARE_SIZE, -SQUARE_SIZE), new Coordinates(2 * SQUARE_SIZE, -SQUARE_SIZE), Piece.Z,
				display);
	}

	public static Shape s(GameSurfaceView display) {
		return new Shape(new Coordinates(4 * SQUARE_SIZE, -2 * SQUARE_SIZE),
				new Coordinates(4 * SQUARE_SIZE, -SQUARE_SIZE), new Coordinates(5 * SQUARE_SIZE, -2 * SQUARE_SIZE),
				new Coordinates(5 * SQUARE_SIZE, -SQUARE_SIZE), Piece.S, display);
	}

	public static Shape ll(GameSurfaceView display) {
		return new Shape(new Coordinates(0, -3 * SQUARE_SIZE), new Coordinates(0, -2 * SQUARE_SIZE),
				new Coordinates(0, -SQUARE_SIZE), new Coordinates(SQUARE_SIZE, -SQUARE_SIZE), Piece.LL, display);
	}

	private Shape(Coordinates a, Coordinates b, Coordinates c, Coordinates d, Piece id,
			GameSurfaceView gameSurfaceView) {
		CoordinatesA = a;
		CoordinatesB = b;
		CoordinatesC = c;
		CoordinatesD = d;

		Id = id;

		_down = -2;
		_right = 0;

		_gameSurfaceView = gameSurfaceView;

		_logger = new SmartMirrorLogger(TAG);
		_logger.Debug("Created Shape...");
	}

	public List<Coordinates> ShapeCoordinates() {
		List<Coordinates> Coordinates = new ArrayList<Coordinates>();

		Coordinates.add(CoordinatesA);
		Coordinates.add(CoordinatesB);
		Coordinates.add(CoordinatesC);
		Coordinates.add(CoordinatesD);

		return Coordinates;
	}

	public void Fall() {
		if (CoordinatesA.Y < dH() && CoordinatesB.Y < dH() && CoordinatesC.Y < dH() && CoordinatesD.Y < dH()) {

			CoordinatesA.Y += SQUARE_SIZE;
			CoordinatesB.Y += SQUARE_SIZE;
			CoordinatesC.Y += SQUARE_SIZE;
			CoordinatesD.Y += SQUARE_SIZE;

			IsFalling = true;
			_down++;
		} else {
			IsFalling = false;

		}
	}

	public void Rotate(Piece id) {
		switch (id) {
		case T:
		case Z:
		case LL:
			for (int i = 0; i < ShapeCoordinates().size(); i++) {
				int tempY = ShapeCoordinates().get(i).Y - (_down * SQUARE_SIZE);
				int tempX = ShapeCoordinates().get(i).X - (_right * SQUARE_SIZE);

				ShapeCoordinates().get(i).X = (2 * SQUARE_SIZE - (tempY)) + (_right * SQUARE_SIZE);
				ShapeCoordinates().get(i).Y = tempX + (_down * SQUARE_SIZE);
			}
			break;
		case L:
			_rotate++;
			if ((_rotate & 1) == 0) {
				int y = ShapeCoordinates().get(1).Y;
				int x = ShapeCoordinates().get(1).X;

				for (int index = 0; index < ShapeCoordinates().size(); index++) {
					ShapeCoordinates().get(index).X = x;
					ShapeCoordinates().get(index).Y = y + (index * SQUARE_SIZE);
				}
			} else {
				int y = ShapeCoordinates().get(1).Y;
				int x = ShapeCoordinates().get(1).X;

				for (int index = 0; index < ShapeCoordinates().size(); index++) {
					ShapeCoordinates().get(index).Y = y;
					ShapeCoordinates().get(index).X = x + (index * SQUARE_SIZE);
				}
			}
		default:
		}
	}

	public void Left() {
		if (CoordinatesA.X > 0 && CoordinatesB.X > 0 && CoordinatesC.X > 0 && CoordinatesD.X > 0
				&& CoordinatesA.Y < dH() && CoordinatesB.Y < dH() && CoordinatesC.Y < dH() && CoordinatesD.Y < dH()) {

			CoordinatesA.X -= SQUARE_SIZE;
			CoordinatesB.X -= SQUARE_SIZE;
			CoordinatesC.X -= SQUARE_SIZE;
			CoordinatesD.X -= SQUARE_SIZE;

			_right--;
		}
	}

	public void Right() {
		if (CoordinatesA.X < dW() && CoordinatesB.X < dW() && CoordinatesC.X < dW() && CoordinatesD.X < dW()
				&& CoordinatesA.Y < dH() && CoordinatesB.Y < dH() && CoordinatesC.Y < dH() && CoordinatesD.Y < dH()) {

			CoordinatesA.X += SQUARE_SIZE;
			CoordinatesB.X += SQUARE_SIZE;
			CoordinatesC.X += SQUARE_SIZE;
			CoordinatesD.X += SQUARE_SIZE;

			_right++;
		}
	}

	private int dH() {
		int dh = _gameSurfaceView.DisplayHeight() - 6 * SQUARE_SIZE;
		return dh;
	}

	private int dW() {
		int dw = _gameSurfaceView.DisplayWidth() - 4 * SQUARE_SIZE;
		return dw;
	}
}
