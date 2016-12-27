package guepardoapps.games.snake.library.interfaces;

import guepardoapps.games.common.basic.Coordinates;

public interface SnakeRenderer {
	void renderHead(Coordinates headCoordinates, char direction);
	void renderBody(Coordinates coordinates);
}
