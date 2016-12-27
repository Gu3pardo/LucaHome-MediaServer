package guepardoapps.games.snake.library.interfaces;

import guepardoapps.games.common.basic.Coordinates;
import guepardoapps.games.snake.library.exceptions.SnakeHitYardWallException;

public interface SnakeMotionObserver {
	public void updateSnakePosition(Coordinates headCoordinates) throws SnakeHitYardWallException;
}
