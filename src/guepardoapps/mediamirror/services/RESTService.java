package guepardoapps.mediamirror.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class RESTService extends Service {

	private static final String TAG = RESTService.class.getName();
	private SmartMirrorLogger _logger;

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		_logger = new SmartMirrorLogger(TAG);

		Bundle bundle = intent.getExtras();
		if (bundle == null) {
			_logger.Warn("Bundle is null!");
			stopSelf();
			return -1;
		}

		String action = bundle.getString(Constants.BUNDLE_REST_ACTION);
		if (action == null) {
			_logger.Warn("Action is null!");
			stopSelf();
			return -1;
		}
		_logger.Debug("Action: " + action);

		String[] actions = new String[Constants.SERVER_URLs.length];
		for (int index = 0; index < Constants.SERVER_URLs.length; index++) {
			actions[index] = Constants.SERVER_URLs[index] + Constants.ACTION_PATH + Constants.USER_NAME + "&password="
					+ Constants.PASS_PHRASE + "&action=" + action;
			_logger.Debug("index " + String.valueOf(index) + ": " + actions[index]);
		}

		String data = bundle.getString(Constants.BUNDLE_REST_DATA);
		_logger.Debug("data: " + data);
		String broadcast = bundle.getString(Constants.BUNDLE_REST_BROADCAST);
		_logger.Debug("broadcast: " + broadcast);

		RestCommunicationTask task = new RestCommunicationTask();
		task.setValues(data, broadcast, actions.length);
		task.execute(actions);

		return 0;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private class RestCommunicationTask extends AsyncTask<String, Void, String> {
		private final String TAG = RestCommunicationTask.class.getName();
		private SmartMirrorLogger _logger;

		private String _data;
		private String _broadcast;
		private String[] _answer;

		public void setValues(String data, String broadcast, int answerSize) {
			_logger = new SmartMirrorLogger(TAG);

			_data = data;
			_broadcast = broadcast;
			_answer = new String[answerSize];
		}

		@Override
		protected String doInBackground(String... actions) {
			String response = "";
			int answerIndex = 0;
			for (String action : actions) {
				try {
					response = "";

					URL url = new URL(action);
					URLConnection connection = url.openConnection();
					InputStream inputStream = connection.getInputStream();

					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

					String line;
					while ((line = reader.readLine()) != null) {
						response += line;
					}

					_answer[answerIndex] = response;
					_logger.Debug(response);

				} catch (IOException e) {
					_logger.Error(e.getMessage());
				} finally {
					answerIndex++;
				}
			}

			return "FINISHED";
		}

		@Override
		protected void onPostExecute(String result) {
			_logger.Debug(result);

			if (_broadcast != null && _broadcast != "" && _data != null && _data != "") {
				Intent broadcastIntent = new Intent(_broadcast);
				Bundle broadcastData = new Bundle();
				broadcastData.putStringArray(_data, _answer);
				broadcastIntent.putExtras(broadcastData);
				sendBroadcast(broadcastIntent);
			}

			stopSelf();
		}
	}
}
