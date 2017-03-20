package guepardoapps.games.snake.library;

import java.util.Random;

import guepardoapps.games.common.Coordinates;
import guepardoapps.games.snake.library.interfaces.RandomCoordinatesGenerator;

public class DefaultRandomCoordinatesGenerator implements RandomCoordinatesGenerator {

	@SuppressWarnings("unused")
	private static final String TAG = DefaultRandomCoordinatesGenerator.class.getName();

	private int _width;
	private int _height;

	public DefaultRandomCoordinatesGenerator(int width, int height) {
		_width = width;
		_height = height;
	}

	@Override
	public Coordinates generate() {
		Random random = new Random();
		int x = random.nextInt(_width);
		int y = random.nextInt(_height);
		
		Coordinates coordinates = new Coordinates(x, y);
		return coordinates;
	}
}
