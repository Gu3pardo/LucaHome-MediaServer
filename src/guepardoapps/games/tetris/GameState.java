package guepardoapps.games.tetris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import guepardoapps.games.common.basic.Coordinates;

import guepardoapps.mediamirror.common.Enables;

import guepardoapps.toolset.common.Logger;

public class GameState {

	private static final String TAG = GameState.class.getName();
	private Logger _logger;

	private GameSurfaceView _gameSurfaceView;
	private Shape _fallingShape;

	private List<Shape> _shapes = new ArrayList<Shape>();
	private List<int[]> _deleteMe;

	public GameState(GameSurfaceView gameSurfaceView) {
		_logger = new Logger(TAG, Enables.DEBUGGING_ENABLED);
		_logger.Debug("Created GameState...");

		Shape tShape = Shape.l(gameSurfaceView);
		_shapes.add(tShape);

		_fallingShape = tShape;
		_gameSurfaceView = gameSurfaceView;
	}

	public Shape GetFallingShape() {
		return _fallingShape;
	}

	public void UserPressedLeft() {
		_fallingShape.Left();
	}

	public void UserPressedRight() {
		_fallingShape.Right();
	}

	public void UserPressedDown() {
		_fallingShape.Fall();
	}

	public void UserPressedRotate() {
		_fallingShape.Rotate(_fallingShape.Id);
	}

	public List<Shape> GetShapes() {
		compareCoordinatess(getFalling(), getCoordinatess());
		isThisRowFull(getCoordinatess());

		if (!_fallingShape.IsFalling) {
			int shapeNumber = randomNumber();
			Shape addShape = makeNewShape(shapeNumber, _gameSurfaceView);
			_shapes.add(addShape);
			_fallingShape = _shapes.get(_shapes.size() - 1);
			_fallingShape.IsFalling = true;
		}

		return _shapes;
	}

	public List<Coordinates> DeleteThisRow() {
		List<Coordinates> coordinatessToDelete = new ArrayList<Coordinates>();
		for (int index = 0; index < _deleteMe.size(); index++) {
			int x = _deleteMe.get(index)[0];
			int y = _deleteMe.get(index)[1];

			Coordinates coordinates = new Coordinates(x, y);
			coordinatessToDelete.add(coordinates);
		}
		return coordinatessToDelete;
	}

	private Shape makeNewShape(int randomNumber, GameSurfaceView surface) {
		switch (randomNumber) {
		case 0:
			return Shape.t(surface);
		case 1:
			return Shape.l(surface);
		case 2:
			return Shape.z(surface);
		case 3:
			return Shape.s(surface);
		case 4:
			return Shape.ll(surface);
		default:
			return Shape.ll(surface);
		}
	}

	private int randomNumber() {
		Random rand = new Random();
		int newShape = rand.nextInt(5);
		return newShape;
	}

	private List<int[]> getFalling() {
		List<int[]> getFalling = new ArrayList<int[]>();

		int[] aPair = { _fallingShape.CoordinatesA.X, _fallingShape.CoordinatesA.Y + 100 };
		int[] bPair = { _fallingShape.CoordinatesB.X, _fallingShape.CoordinatesB.Y + 100 };
		int[] cPair = { _fallingShape.CoordinatesC.X, _fallingShape.CoordinatesC.Y + 100 };
		int[] dPair = { _fallingShape.CoordinatesD.X, _fallingShape.CoordinatesD.Y + 100 };

		getFalling.add(aPair);
		getFalling.add(bPair);
		getFalling.add(cPair);
		getFalling.add(dPair);

		return getFalling;
	}

	private List<int[]> getCoordinatess() {
		List<int[]> coordinatesList = new ArrayList<int[]>();

		for (int index1 = 0; index1 < _shapes.size(); index1++) {
			if (_shapes.get(index1) != _fallingShape) {
				Shape shape = _shapes.get(index1);
				List<Coordinates> shapeCoordinatesList = shape.ShapeCoordinates();

				for (int index2 = 0; index2 < shapeCoordinatesList.size(); index2++) {
					int[] pair = { shapeCoordinatesList.get(index2).X, shapeCoordinatesList.get(index2).Y };
					coordinatesList.add(pair);
				}
			}
		}
		return coordinatesList;
	}

	private void compareCoordinatess(List<int[]> getFalling, List<int[]> getCoordinatess) {
		for (int[] shapeCoordinatess : getCoordinatess) {
			String coordString = Arrays.toString(shapeCoordinatess);
			for (int[] fallingCoords : getFalling) {
				String fallingString = Arrays.toString(fallingCoords);
				if (coordString.equals(fallingString)) {
					_fallingShape.IsFalling = false;
				}
			}
		}
		isThisRowFull(getCoordinatess());
	}

	private void isThisRowFull(List<int[]> getCoordinatess) {
		List<int[]> nine = new ArrayList<int[]>();
		List<int[]> eight = new ArrayList<int[]>();
		List<int[]> seven = new ArrayList<int[]>();
		List<int[]> six = new ArrayList<int[]>();
		List<int[]> five = new ArrayList<int[]>();

		for (int index = 0; index < getCoordinatess().size(); index++) {
			int[] CoordinatesPair = getCoordinatess.get(index);

			switch (CoordinatesPair[1]) {
			case 900:
				nine.add(CoordinatesPair);
				break;
			case 800:
				eight.add(CoordinatesPair);
				break;
			case 700:
				seven.add(CoordinatesPair);
				break;
			case 600:
				six.add(CoordinatesPair);
				break;
			case 500:
				five.add(CoordinatesPair);
				break;
			default:
			}

		}

		_deleteMe = new ArrayList<int[]>();

		if (nine.size() == 7) {
			for (int index = 0; index < nine.size(); index++) {
				int[] addToDeleteMe = nine.get(index);
				_logger.Debug("nine 1 " + nine.get(index)[0] + "\nnine 2 " + nine.get(index)[1]);
				_deleteMe.add(addToDeleteMe);
			}
		}

		if (eight.size() == 7) {
			for (int index = 0; index < eight.size(); index++) {
				int[] addToDeleteMe = eight.get(index);
				_logger.Debug("eight 1 " + eight.get(index)[0] + "\neight 2 " + eight.get(index)[1]);
				_deleteMe.add(addToDeleteMe);
			}
		}
		if (seven.size() == 7) {
			for (int index = 0; index < seven.size(); index++) {
				int[] addToDeleteMe = seven.get(index);
				_deleteMe.add(addToDeleteMe);
			}
		}
		if (six.size() == 7) {
			for (int index = 0; index < six.size(); index++) {
				int[] addToDeleteMe = six.get(index);
				_deleteMe.add(addToDeleteMe);
			}
		}
		if (five.size() == 7) {
			for (int index = 0; index < five.size(); index++) {
				int[] addToDeleteMe = five.get(index);
				_deleteMe.add(addToDeleteMe);
			}
		}
	}
}
