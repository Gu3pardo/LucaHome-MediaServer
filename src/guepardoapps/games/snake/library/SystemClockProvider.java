package guepardoapps.games.snake.library;

import guepardoapps.games.snake.library.interfaces.ClockProvider;

public class SystemClockProvider implements ClockProvider {

	@SuppressWarnings("unused")
	private static final String TAG = SystemClockProvider.class.getName();

	@Override
	public long getTime() {
		return System.currentTimeMillis();
	}
}
