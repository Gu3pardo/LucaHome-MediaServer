package guepardoapps.games.snake.library;

public class GameFactory {

	@SuppressWarnings("unused")
	private static final String TAG = GameFactory.class.getName();

	public static Game createGameWithYardDimensions(int width, int height) {
		Game game = new Game();

		game.SetYard(new Yard(width, height));
		game.SetRandomCoordinatesGenerator(new DefaultRandomCoordinatesGenerator(width, height));
		game.SetTickingClock(new SystemClockProvider());

		return game;
	}
}
