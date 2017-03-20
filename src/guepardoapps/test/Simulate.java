package guepardoapps.test;

import android.content.Context;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class Simulate {

	private static final String TAG = Simulate.class.getSimpleName();
	private SmartMirrorLogger _logger;

	public Simulate(Context context) {
		_logger = new SmartMirrorLogger(TAG);
	}

	public void SimulateTouch(View view, float x, float y, int action, int metaState) {
		_logger.Debug("simulatingTouch");
		// Obtain MotionEvent object
		long downTime = SystemClock.uptimeMillis();
		long eventTime = SystemClock.uptimeMillis() + 50;
		// List of meta states found here:
		// developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
		MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, action, x, y, metaState);
		// Dispatch touch event to view
		view.dispatchTouchEvent(motionEvent);
		_logger.Debug("simulatedTouch");
	}

	public void SimulateKeyEvent(View view, int action, int keycode) {
		_logger.Debug("simulatingKeycode");
		view.dispatchKeyEvent(new KeyEvent(action, keycode));
		_logger.Debug("simulatedKeycode");
	}
}
