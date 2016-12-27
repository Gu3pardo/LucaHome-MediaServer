package guepardoapps.games.snake.library;

import guepardoapps.games.common.basic.Coordinates;
import guepardoapps.games.snake.library.exceptions.SnakeHitYardWallException;
import guepardoapps.games.snake.library.interfaces.ClockProvider;
import guepardoapps.games.snake.library.interfaces.RandomCoordinatesGenerator;

public class Game {

	@SuppressWarnings("unused")
	private static final String TAG = Game.class.getName();

	private Yard _yard;
	private ClockProvider _clockProvider;
	private RandomCoordinatesGenerator _randomCoordinatesGenerator;

	private long _time;
	private long _delay = 500;

	public void init() {
		Coordinates initialCoordinates = _randomCoordinatesGenerator.generate();
		Snake snake = new Snake();
		_yard.Put(snake, initialCoordinates);
	}

	public void SetYard(Yard newYard) {
		_yard = newYard;
	}

	public Yard GetYard() {
		return _yard;
	}

	public void SetTickingClock(ClockProvider clockProvider) {
		_clockProvider = clockProvider;
	}

	public void SetRandomCoordinatesGenerator(RandomCoordinatesGenerator randomCoordinatesGenerator) {
		_randomCoordinatesGenerator = randomCoordinatesGenerator;
	}

	public void SetDelay(long delay) {
		_delay = delay;
	}

	public long GetDelay() {
		return _delay;
	}

	public void Tick() throws SnakeHitYardWallException {
		long current = _clockProvider.getTime();
		long difference = current - _time;
		_time = current;
		
		if (difference >= _delay) {
			_yard.GetSnake().Move();
		}
	}
}
