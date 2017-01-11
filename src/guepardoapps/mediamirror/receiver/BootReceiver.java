package guepardoapps.mediamirror.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = BootReceiver.class.getName();
	private SmartMirrorLogger _logger;

	private static final int DELAY = 30 * 1000;

	@Override
	public void onReceive(final Context context, Intent intent) {
		if (_logger == null) {
			_logger = new SmartMirrorLogger(TAG);
		}

		String action = intent.getAction();
		_logger.Debug("Action is " + action);

		if (action.equals("android.intent.action.BOOT_COMPLETED")) {
			_logger.Debug("Received BOOT_COMPLETED!");

			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent();
					intent.setClassName("guepardoapps.mediamirror", "guepardoapps.mediamirror.view.Main");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
				}
			}, DELAY);
		}
	}
}
