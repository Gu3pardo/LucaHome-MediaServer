package guepardoapps.games.tetris;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import guepardoapps.games.common.Coordinates;
import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class GameState {

    private static final String TAG = GameState.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private GameSurfaceView _gameSurfaceView;
    private Shape _fallingShape;

    private List<Shape> _shapes = new ArrayList<>();
    private List<int[]> _deleteMe;

    public GameState(@NonNull GameSurfaceView gameSurfaceView) {
        _logger = new SmartMirrorLogger(TAG);
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
        compareCoordinates(getFalling(), getCoordinates());
        isThisRowFull(getCoordinates());

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
        List<Coordinates> coordinatesToDelete = new ArrayList<>();
        for (int index = 0; index < _deleteMe.size(); index++) {
            int x = _deleteMe.get(index)[0];
            int y = _deleteMe.get(index)[1];

            Coordinates coordinates = new Coordinates(x, y);
            coordinatesToDelete.add(coordinates);
        }
        return coordinatesToDelete;
    }

    private Shape makeNewShape(
            int randomNumber,
            @NonNull GameSurfaceView surface) {
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
        return new Random().nextInt(5);
    }

    private List<int[]> getFalling() {
        List<int[]> getFalling = new ArrayList<>();

        int[] aPair = {_fallingShape.CoordinatesA.X, _fallingShape.CoordinatesA.Y + 100};
        int[] bPair = {_fallingShape.CoordinatesB.X, _fallingShape.CoordinatesB.Y + 100};
        int[] cPair = {_fallingShape.CoordinatesC.X, _fallingShape.CoordinatesC.Y + 100};
        int[] dPair = {_fallingShape.CoordinatesD.X, _fallingShape.CoordinatesD.Y + 100};

        getFalling.add(aPair);
        getFalling.add(bPair);
        getFalling.add(cPair);
        getFalling.add(dPair);

        return getFalling;
    }

    private List<int[]> getCoordinates() {
        List<int[]> coordinatesList = new ArrayList<>();

        for (int index1 = 0; index1 < _shapes.size(); index1++) {
            if (_shapes.get(index1) != _fallingShape) {
                Shape shape = _shapes.get(index1);
                List<Coordinates> shapeCoordinatesList = shape.ShapeCoordinates();

                for (int index2 = 0; index2 < shapeCoordinatesList.size(); index2++) {
                    int[] pair = {shapeCoordinatesList.get(index2).X, shapeCoordinatesList.get(index2).Y};
                    coordinatesList.add(pair);
                }
            }
        }
        return coordinatesList;
    }

    private void compareCoordinates(List<int[]> getFalling, List<int[]> getCoordinates) {
        for (int[] shapeCoordinates : getCoordinates) {
            String coordString = Arrays.toString(shapeCoordinates);
            for (int[] fallingCoords : getFalling) {
                String fallingString = Arrays.toString(fallingCoords);
                if (coordString.equals(fallingString)) {
                    _fallingShape.IsFalling = false;
                }
            }
        }
        isThisRowFull(getCoordinates());
    }

    private void isThisRowFull(List<int[]> getCoordinates) {
        List<int[]> nine = new ArrayList<>();
        List<int[]> eight = new ArrayList<>();
        List<int[]> seven = new ArrayList<>();
        List<int[]> six = new ArrayList<>();
        List<int[]> five = new ArrayList<>();

        for (int index = 0; index < getCoordinates().size(); index++) {
            int[] CoordinatesPair = getCoordinates.get(index);

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

        _deleteMe = new ArrayList<>();

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
