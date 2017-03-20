package guepardoapps.mediamirror.server;

import java.util.ArrayList;
import java.util.Comparator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Handler;
import android.widget.Toast;

import guepardoapps.library.lucahome.common.constants.MediaMirrorIds;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.enums.RSSFeed;
import guepardoapps.library.lucahome.common.enums.ServerAction;
import guepardoapps.library.lucahome.common.enums.YoutubeId;

import guepardoapps.library.toastview.ToastView;

import guepardoapps.games.common.GameConstants;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.controller.DatabaseController;
import guepardoapps.mediamirror.controller.MediaVolumeController;
import guepardoapps.mediamirror.controller.ScreenController;
import guepardoapps.mediamirror.model.CenterModel;
import guepardoapps.mediamirror.model.RSSModel;
import guepardoapps.mediamirror.model.YoutubeDatabaseModel;

import guepardoapps.toolset.common.classes.SerializableList;
import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.CommandController;
import guepardoapps.toolset.controller.ReceiverController;
import guepardoapps.toolset.controller.UserInformationController;

public class DataHandler {

	private static final String TAG = DataHandler.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private static final int TIMEOUT_SHUTDOWN = 5 * 1000;
	private static final int TIMEOUT_REBOOT = 3 * 1000;

	private Context _context;

	private BroadcastController _broadcastController;
	private CommandController _commandController;
	private DatabaseController _dbController;
	private MediaVolumeController _mediaVolumeController;
	private ReceiverController _receiverController;
	private ScreenController _screenController;
	private UserInformationController _userInformationController;

	private static final int SEA_SOUND_STOP_TIMEOUT = 30 * 60 * 1000;
	private boolean _seaSoundIsRunning;
	private long _seaSoundStartTime;
	private Handler _seaSoundHandler = new Handler();
	private Runnable _seaSoundRunnable = new Runnable() {
		@Override
		public void run() {
			_logger.Debug("_seaSoundRunnable run");
			CenterModel goodNightModel = new CenterModel(true, "Sleep well!", false, "", false, "");
			_logger.Info("Created center model: " + goodNightModel.toString());
			_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL, Bundles.CENTER_MODEL,
					goodNightModel);
			_broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_OFF);
			_seaSoundIsRunning = false;
			_seaSoundStartTime = -1;
		}
	};

	private int _batteryLevel = -1;
	private BroadcastReceiver _batteryInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctxt, Intent intent) {
			_batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		}
	};

	private SerializableList<WirelessSocketDto> _socketList;
	private BroadcastReceiver _socketListReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			@SuppressWarnings("unchecked")
			SerializableList<WirelessSocketDto> socketList = (SerializableList<WirelessSocketDto>) intent
					.getSerializableExtra(Bundles.SOCKET_LIST);
			if (socketList != null) {
				_socketList = socketList;
			}
		}
	};

	private String _lastYoutubeId = "";

	public DataHandler(Context context) {
		_logger = new SmartMirrorLogger(TAG);

		_context = context;

		_broadcastController = new BroadcastController(_context);
		_commandController = new CommandController(_context);
		_dbController = new DatabaseController(_context);
		_mediaVolumeController = MediaVolumeController.getInstance();
		_receiverController = new ReceiverController(_context);
		_screenController = new ScreenController(_context);
		_userInformationController = new UserInformationController(_context);

		_receiverController.RegisterReceiver(_batteryInfoReceiver, new String[] { Intent.ACTION_BATTERY_CHANGED });
		_receiverController.RegisterReceiver(_socketListReceiver, new String[] { Broadcasts.SOCKET_LIST });
	}

	public String PerformAction(String command) {
		if (command == null) {
			_logger.Warn("Command is null!");
			return "Command is null!";
		}

		_logger.Debug("PerformAction with data: " + command);
		if (command.startsWith("ACTION:")) {
			ServerAction action = convertCommandToAction(command);

			if (action != null) {
				_logger.Debug("action: " + action.toString());
				String data = convertCommandToData(command);
				_logger.Debug("data: " + data);

				switch (action) {
				case PING:
					return "Mediamirror available!";

				case SHOW_YOUTUBE_VIDEO:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					if (data.length() < 4) {
						int youtubeIdInt = -1;
						try {
							youtubeIdInt = Integer.parseInt(data);
						} catch (Exception e) {
							_logger.Error(e.toString());
							_logger.Warn("Setting youtubeId to 0!");
							youtubeIdInt = 0;
						} finally {
							YoutubeId youtubeId = YoutubeId.GetById(youtubeIdInt);
							if (youtubeId == null) {
								_logger.Warn("youtubeId is null! Setting to default");
								youtubeId = YoutubeId.THE_GOOD_LIFE_STREAM;
							}
							CenterModel youtubeModel = new CenterModel(false, "", true, youtubeId.GetYoutubeId(), false,
									"");
							_logger.Info("Created center model: " + youtubeModel.toString());
							_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL,
									Bundles.CENTER_MODEL, youtubeModel);
							_lastYoutubeId = youtubeId.GetYoutubeId();
						}
					} else if (data.length() == 11) {
						CenterModel youtubeModel = new CenterModel(false, "", true, data, false, "");
						_logger.Info("Created center model: " + youtubeModel.toString());
						_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL,
								Bundles.CENTER_MODEL, youtubeModel);
						_lastYoutubeId = data;
					} else {
						_logger.Warn("Wrong size for data of youtube id!");
					}
					break;
				case PLAY_YOUTUBE_VIDEO:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_context.sendBroadcast(new Intent(Broadcasts.PLAY_VIDEO));
					break;
				case PAUSE_YOUTUBE_VIDEO:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_context.sendBroadcast(new Intent(Broadcasts.PAUSE_VIDEO));
					break;
				case STOP_YOUTUBE_VIDEO:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_context.sendBroadcast(new Intent(Broadcasts.STOP_VIDEO));
					break;
				case GET_SAVED_YOUTUBE_IDS:
					ArrayList<YoutubeDatabaseModel> loadedList = _dbController.GetYoutubeIds();
					// sort the list in descending order
					loadedList.sort(new Comparator<YoutubeDatabaseModel>() {
						@Override
						public int compare(YoutubeDatabaseModel elementOne, YoutubeDatabaseModel elementTwo) {
							return Integer.valueOf(elementTwo.GetPlayCount()).compareTo(elementOne.GetPlayCount());
						}
					});
					String answer = "";
					for (YoutubeDatabaseModel entry : loadedList) {
						answer += entry.GetCommunicationString();
					}
					return action.toString() + ":" + answer;

				case PLAY_SEA_SOUND:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_logger.Debug(String.format("Received data for PLAY_SEA_SOUND is %s", data));
					int timeOut;
					try {
						timeOut = Integer.parseInt(data) * 60 * 1000;
						_logger.Debug(String.format("timeOut for PLAY_SEA_SOUND is %s", timeOut));
					} catch (Exception ex) {
						_logger.Error(ex.toString());
						ToastView.error(_context, ex.toString(), Toast.LENGTH_LONG).show();
						timeOut = SEA_SOUND_STOP_TIMEOUT;
					}
					CenterModel playSeaSoundModel = new CenterModel(false, "", true, YoutubeId.SEA_SOUND.GetYoutubeId(),
							false, "");
					_logger.Info("Created center model: " + playSeaSoundModel.toString());
					_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL, Bundles.CENTER_MODEL,
							playSeaSoundModel);
					_seaSoundHandler.postDelayed(_seaSoundRunnable, timeOut);
					_seaSoundIsRunning = true;
					_seaSoundStartTime = System.currentTimeMillis();
					break;
				case STOP_SEA_SOUND:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					CenterModel stopSeaSoundModel = new CenterModel(true, "", false, "", false, "");
					_logger.Info("Created center model: " + stopSeaSoundModel.toString());
					_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL, Bundles.CENTER_MODEL,
							stopSeaSoundModel);
					_seaSoundHandler.removeCallbacks(_seaSoundRunnable);
					_seaSoundIsRunning = false;
					_seaSoundStartTime = -1;
					break;
				case IS_SEA_SOUND_PLAYING:
					String seaSSoundIsPlaying = "";
					if (_seaSoundIsRunning) {
						seaSSoundIsPlaying = "1";
					} else {
						seaSSoundIsPlaying = "0";
					}
					return action.toString() + ":" + seaSSoundIsPlaying;
				case GET_SEA_SOUND_COUNTDOWN:
					if (_seaSoundStartTime == -1) {
						return action.toString() + ":-1";
					}
					long currentTime = System.currentTimeMillis();
					long differenceTimeSec = (currentTime - _seaSoundStartTime) / 1000;
					while (differenceTimeSec < 0) {
						differenceTimeSec += 24 * 60 * 60;
					}
					return action.toString() + ":" + differenceTimeSec;

				case SHOW_WEBVIEW:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					CenterModel webviewModel = new CenterModel(false, "", false, null, true, data);
					_logger.Info("Created center model: " + webviewModel.toString());
					_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL, Bundles.CENTER_MODEL,
							webviewModel);
					break;

				case SHOW_CENTER_TEXT:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					CenterModel centerTextModel = new CenterModel(true, data, false, null, false, "");
					_logger.Info("Created center model: " + centerTextModel.toString());
					_broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL, Bundles.CENTER_MODEL,
							centerTextModel);
					break;

				case SET_RSS_FEED:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					int feedIdInt = -1;
					try {
						feedIdInt = Integer.parseInt(data);
					} catch (Exception e) {
						_logger.Error(e.toString());
						_logger.Warn("Setting feedIdInt to 0!");
						feedIdInt = 0;
					} finally {
						RSSFeed rssFeed = RSSFeed.GetById(feedIdInt);
						if (rssFeed == null) {
							_logger.Warn("rssFeed is null! Setting to default");
							rssFeed = RSSFeed.DEFAULT;
						}
						RSSModel rSSFeedModel = new RSSModel(rssFeed, true);
						_logger.Info("Created rssfeed model: " + rSSFeedModel.toString());
						_broadcastController.SendSerializableBroadcast(Broadcasts.PERFORM_RSS_UPDATE, Bundles.RSS_MODEL,
								rSSFeedModel);
					}
					break;
				case RESET_RSS_FEED:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendSimpleBroadcast(Broadcasts.RESET_RSS_FEED);
					break;

				case UPDATE_CURRENT_WEATHER:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_CURRENT_WEATHER_UPDATE);
					break;
				case UPDATE_FORECAST_WEATHER:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_FORECAST_WEATHER_UPDATE);
					break;
				case UPDATE_RASPBERRY_TEMPERATURE:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_TEMPERATURE_UPDATE);
					break;
				case UPDATE_IP_ADDRESS:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_IP_ADDRESS_UPDATE);
					break;
				case UPDATE_BIRTHDAY_ALARM:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_BIRTHDAY_UPDATE);
					break;
				case UPDATE_CALENDAR_ALARM:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_CALENDAR_UPDATE);
					break;

				case INCREASE_VOLUME:
					_mediaVolumeController.IncreaseVolume();
					return action.toString() + ":" + _mediaVolumeController.GetCurrentVolume();
				case DECREASE_VOLUME:
					_mediaVolumeController.DecreaseVolume();
					return action.toString() + ":" + _mediaVolumeController.GetCurrentVolume();
				case MUTE_VOLUME:
					_mediaVolumeController.MuteVolume();
					return action.toString() + ":Muted";
				case UNMUTE_VOLUME:
					_mediaVolumeController.UnmuteVolume();
					return action.toString() + ":" + _mediaVolumeController.GetCurrentVolume();
				case GET_CURRENT_VOLUME:
					return action.toString() + ":" + _mediaVolumeController.GetCurrentVolume();

				case PLAY_ALARM:
					// TODO implement
					break;
				case STOP_ALARM:
					// TODO implement
					break;

				case GAME_COMMAND:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendStringBroadcast(Broadcasts.GAME_COMMAND, Bundles.GAME_COMMAND, data);
					break;
				case GAME_PONG_START:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendSimpleBroadcast(Broadcasts.START_PONG);
					break;
				case GAME_PONG_STOP:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendSimpleBroadcast(Broadcasts.STOP_PONG);
					break;
				case GAME_PONG_PAUSE:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendStringBroadcast(Broadcasts.GAME_COMMAND, Bundles.GAME_COMMAND,
							GameConstants.GAME + ":" + GameConstants.PAUSE);
					break;
				case GAME_PONG_RESUME:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendStringBroadcast(Broadcasts.GAME_COMMAND, Bundles.GAME_COMMAND,
							GameConstants.GAME + ":" + GameConstants.RESUME);
					break;
				case GAME_PONG_RESTART:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendStringBroadcast(Broadcasts.GAME_COMMAND, Bundles.GAME_COMMAND,
							GameConstants.GAME + ":" + GameConstants.RESTART);
					break;
				case GAME_SNAKE_START:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendSimpleBroadcast(Broadcasts.START_SNAKE);
					break;
				case GAME_SNAKE_STOP:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendSimpleBroadcast(Broadcasts.STOP_SNAKE);
					break;
				case GAME_TETRIS_START:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendSimpleBroadcast(Broadcasts.START_TETRIS);
					break;
				case GAME_TETRIS_STOP:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendSimpleBroadcast(Broadcasts.STOP_TETRIS);
					break;

				case INCREASE_SCREEN_BRIGHTNESS:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendIntBroadcast(Broadcasts.ACTION_SCREEN_BRIGHTNESS,
							Bundles.SCREEN_BRIGHTNESS, ScreenController.INCREASE);
					return action.toString() + ":" + String.valueOf(_screenController.GetCurrentBrightness());
				case DECREASE_SCREEN_BRIGHTNESS:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendIntBroadcast(Broadcasts.ACTION_SCREEN_BRIGHTNESS,
							Bundles.SCREEN_BRIGHTNESS, ScreenController.DECREASE);
					return action.toString() + ":" + String.valueOf(_screenController.GetCurrentBrightness());
				case GET_SCREEN_BRIGHTNESS:
					return action.toString() + ":" + String.valueOf(_screenController.GetCurrentBrightness());
				case SCREEN_ON:
					_broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_ON);
					break;
				case SCREEN_OFF:
					_broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_OFF);
					break;
				case SCREEN_SAVER:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_SAVER);
					break;
				case SCREEN_NORMAL:
					if (!_screenController.IsScreenOn()) {
						_logger.Error("Screen is not enabled!");
						return "Error:Screen is not enabled!";
					}

					_broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_NORMAL);
					break;

				case SYSTEM_REBOOT:
					_commandController.RebootDevice(TIMEOUT_REBOOT);
					break;
				case SYSTEM_SHUTDOWN:
					_commandController.ShutDownDevice(TIMEOUT_SHUTDOWN);
					break;

				case GET_BATTERY_LEVEL:
					return action.toString() + ":" + String.valueOf(_batteryLevel);
				case GET_SERVER_VERSION:
					return action.toString() + ":" + _context.getString(R.string.serverVersion);

				case GET_MEDIAMIRROR_DTO:
					String serverIp = _userInformationController.GetIp();

					String batteryLevel = String.valueOf(_batteryLevel);

					String socketName = MediaMirrorIds.IPs.get(serverIp);
					String socketState = "0";
					if (_socketList != null) {
						for (int index = 0; index < _socketList.getSize(); index++) {
							WirelessSocketDto socket = _socketList.getValue(index);
							if (socket.GetName().contains(socketName)) {
								socketState = socket.GetIsActivated() ? "1" : "0";
								break;
							}
						}
					} else {
						_logger.Warn("Cannot search socket state! _socketList is null!");
					}

					String volume = String.valueOf(_mediaVolumeController.GetCurrentVolume());

					String youtubeId = _lastYoutubeId;
					String playedYoutubeIds = "";
					ArrayList<YoutubeDatabaseModel> loadedListFromDb = _dbController.GetYoutubeIds();
					// sort the list in descending order
					loadedListFromDb.sort(new Comparator<YoutubeDatabaseModel>() {
						@Override
						public int compare(YoutubeDatabaseModel elementOne, YoutubeDatabaseModel elementTwo) {
							return Integer.valueOf(elementTwo.GetPlayCount()).compareTo(elementOne.GetPlayCount());
						}
					});
					for (YoutubeDatabaseModel entry : loadedListFromDb) {
						playedYoutubeIds += entry.GetCommunicationString();
					}

					String isSeaSSoundPlaying = _seaSoundIsRunning ? "1" : "0";
					String seaSoundCountdown = "";
					if (_seaSoundStartTime == -1) {
						seaSoundCountdown = "-1";
					} else {
						long currentTimeMsec = System.currentTimeMillis();
						long differenceTimeInSec = (currentTimeMsec - _seaSoundStartTime) / 1000;
						while (differenceTimeInSec < 0) {
							differenceTimeInSec += 24 * 60 * 60;
						}
						seaSoundCountdown = String.valueOf(differenceTimeInSec);
					}

					String serverVersion = _context.getString(R.string.serverVersion);

					String screenBrightness = String.valueOf(_screenController.GetCurrentBrightness());

					String mediaMirrorDto = String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s", serverIp, batteryLevel,
							socketName, socketState, volume, youtubeId, playedYoutubeIds, isSeaSSoundPlaying,
							seaSoundCountdown, serverVersion, screenBrightness);
					return action.toString() + ":" + mediaMirrorDto;

				default:
					_logger.Warn("Action not handled!\n" + action.toString());
					return "Action not handled!\n" + action.toString();
				}
				return "OK:Command performed:" + action.toString();
			} else {
				_logger.Warn("Action failed to be converted! Is null!\n" + command);
				return "Action failed to be converted! Is null!\n" + command;
			}
		} else {
			_logger.Warn("Command has wrong format!\n" + command);
			return "Command has wrong format!\n" + command;
		}
	}

	public void Dispose() {
		_mediaVolumeController.Dispose();
		_receiverController.UnregisterReceiver(_batteryInfoReceiver);
		_receiverController.UnregisterReceiver(_socketListReceiver);
	}

	private ServerAction convertCommandToAction(String command) {
		_logger.Debug(command);

		String[] entries = command.split("\\&");
		if (entries.length == 2) {
			String action = entries[0];
			action = action.replace("ACTION:", "");
			_logger.Debug("Action is: " + action);

			ServerAction serverAction = ServerAction.GetByString(action);
			_logger.Debug("Found action: " + serverAction.toString());
			return serverAction;
		}

		_logger.Warn("Wrong size of entries: " + String.valueOf(entries.length));
		return ServerAction.NULL;
	}

	private String convertCommandToData(String command) {
		_logger.Debug(command);

		String[] entries = command.split("\\&");
		if (entries.length == 2) {
			String data = entries[1];
			data = data.replace("DATA:", "");
			_logger.Debug("Found data: " + data);
			return data;
		}

		_logger.Warn("Wrong size of entries: " + String.valueOf(entries.length));
		return "";
	}
}
