package guepardoapps.mediamirror.server;

import java.util.ArrayList;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.enums.MediaServerAction;
import guepardoapps.library.lucahome.common.enums.MediaServerSelection;
import guepardoapps.library.lucahome.common.enums.RSSFeed;
import guepardoapps.library.lucahome.common.enums.RadioStreams;
import guepardoapps.library.lucahome.common.enums.YoutubeId;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.CommandController;
import guepardoapps.library.toolset.controller.ReceiverController;
import guepardoapps.library.toolset.controller.UserInformationController;

import guepardoapps.games.common.GameConstants;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.Timeouts;
import guepardoapps.mediamirror.controller.MediaVolumeController;
import guepardoapps.mediamirror.controller.ScreenController;
import guepardoapps.mediamirror.view.controller.CenterViewController;
import guepardoapps.mediamirror.view.model.CenterModel;
import guepardoapps.mediamirror.view.model.RSSModel;
import guepardoapps.mediamirror.view.model.YoutubeDatabaseModel;

public class DataHandler {

    private static final String TAG = DataHandler.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private Context _context;

    private BroadcastController _broadcastController;
    private CenterViewController _centerViewController;
    private CommandController _commandController;
    private MediaVolumeController _mediaVolumeController;
    private ReceiverController _receiverController;
    private ScreenController _screenController;
    private UserInformationController _userInformationController;

    private int _batteryLevel = -1;

    private SerializableList<WirelessSocketDto> _socketList;

    private String _lastYoutubeId = YoutubeId.THE_GOOD_LIFE_STREAM.GetYoutubeId();

    private boolean _seaSoundIsRunning;
    private long _seaSoundStartTime = -1;
    private Handler _seaSoundHandler = new Handler();
    private Runnable _seaSoundRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_seaSoundRunnable run");
            CenterModel goodNightModel = new CenterModel(
                    true,
                    "Sleep well!",
                    false,
                    "",
                    false,
                    "",
                    false,
                    RadioStreams.NULL);
            _logger.Info("Created center model: " + goodNightModel.toString());

            _broadcastController.SendSerializableBroadcast(
                    Broadcasts.SHOW_CENTER_MODEL,
                    Bundles.CENTER_MODEL,
                    goodNightModel);

            _broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_OFF);
            _seaSoundIsRunning = false;
            _seaSoundStartTime = -1;
        }
    };

    public DataHandler(@NonNull Context context) {
        _logger = new SmartMirrorLogger(TAG);

        _context = context;

        _broadcastController = new BroadcastController(_context);
        _centerViewController = CenterViewController.getInstance();
        _commandController = new CommandController(_context);
        _mediaVolumeController = MediaVolumeController.getInstance();
        _receiverController = new ReceiverController(_context);
        _screenController = new ScreenController(_context);
        _userInformationController = new UserInformationController(_context);

        _receiverController.RegisterReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                _batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            }
        }, new String[]{Intent.ACTION_BATTERY_CHANGED});

        _receiverController.RegisterReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                @SuppressWarnings("unchecked")
                SerializableList<WirelessSocketDto> socketList = (SerializableList<WirelessSocketDto>) intent.getSerializableExtra(Bundles.SOCKET_LIST);
                if (socketList != null) {
                    _socketList = socketList;
                }
            }
        }, new String[]{Broadcasts.SOCKET_LIST});
    }

    public String PerformAction(@NonNull String command) {
        _logger.Debug("PerformAction with data: " + command);

        if (command.startsWith("ACTION:")) {
            MediaServerAction action = convertCommandToAction(command);

            if (action != null) {
                _logger.Debug("action: " + action.toString());
                String data = convertCommandToData(command);
                _logger.Debug("data: " + data);

                switch (action) {
                    case PING:
                        return "MediaMirror available!";

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

                                CenterModel youtubeModel = new CenterModel(
                                        false,
                                        "",
                                        true,
                                        youtubeId.GetYoutubeId(),
                                        false,
                                        "",
                                        false,
                                        RadioStreams.NULL);
                                _logger.Info("Created center model: " + youtubeModel.toString());

                                _broadcastController.SendSerializableBroadcast(
                                        Broadcasts.SHOW_CENTER_MODEL,
                                        Bundles.CENTER_MODEL,
                                        youtubeModel);

                                _lastYoutubeId = youtubeId.GetYoutubeId();
                            }
                        } else if (data.length() == 11) {
                            CenterModel youtubeModel = new CenterModel(
                                    false,
                                    "",
                                    true,
                                    data,
                                    false,
                                    "",
                                    false,
                                    RadioStreams.NULL);
                            _logger.Info("Created center model: " + youtubeModel.toString());

                            _broadcastController.SendSerializableBroadcast(
                                    Broadcasts.SHOW_CENTER_MODEL,
                                    Bundles.CENTER_MODEL,
                                    youtubeModel);

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

                        if (data != null) {
                            if (data.length() > 0) {
                                _lastYoutubeId = data;
                                _broadcastController.SendStringBroadcast(
                                        Broadcasts.PLAY_VIDEO,
                                        Bundles.YOUTUBE_ID,
                                        data);
                            } else {
                                _broadcastController.SendSimpleBroadcast(Broadcasts.PLAY_VIDEO);
                            }
                        } else {
                            _broadcastController.SendSimpleBroadcast(Broadcasts.PLAY_VIDEO);
                        }
                        break;

                    case PAUSE_YOUTUBE_VIDEO:
                        if (!_screenController.IsScreenOn()) {
                            _logger.Error("Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendSimpleBroadcast(Broadcasts.PAUSE_VIDEO);
                        break;

                    case STOP_YOUTUBE_VIDEO:
                        if (!_screenController.IsScreenOn()) {
                            _logger.Error("Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendSimpleBroadcast(Broadcasts.STOP_VIDEO);
                        break;

                    case GET_SAVED_YOUTUBE_IDS:
                        ArrayList<YoutubeDatabaseModel> loadedList = _centerViewController.GetYoutubeIds();
                        loadedList.sort((elementOne, elementTwo) -> Integer.valueOf(elementTwo.GetPlayCount()).compareTo(elementOne.GetPlayCount()));

                        String answer = "";
                        for (YoutubeDatabaseModel entry : loadedList) {
                            answer += entry.GetCommunicationString();
                        }

                        return action.toString() + ":" + answer;

                    case SET_YOUTUBE_PLAY_POSITION:
                        if (!_screenController.IsScreenOn()) {
                            _logger.Error("Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        int positionPercent = -1;
                        try {
                            positionPercent = Integer.parseInt(data);
                        } catch (Exception ex) {
                            _logger.Error(ex.toString());
                        }

                        _broadcastController.SendIntBroadcast(
                                Broadcasts.SET_VIDEO_POSITION,
                                Bundles.VIDEO_POSITION_PERCENT,
                                positionPercent);

                        break;

                    case SHOW_RADIO_STREAM:
                        if (!_screenController.IsScreenOn()) {
                            _logger.Error("Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        RadioStreams radioStream = RadioStreams.DEFAULT;
                        try {
                            int radioStreamId = Integer.parseInt(data);
                            radioStream = RadioStreams.GetById(radioStreamId);
                            if (radioStream == RadioStreams.NULL) {
                                _logger.Warn(String.format(Locale.getDefault(), "Failed to find id %d! Setting to default!", radioStreamId));
                                radioStream = RadioStreams.DEFAULT;
                            }
                        } catch (Exception exception) {
                            _logger.Error(exception.toString());
                        }

                        CenterModel radioStreamModel = new CenterModel(
                                false,
                                "",
                                false,
                                "",
                                false,
                                "",
                                true,
                                radioStream);
                        _logger.Info("Created center model: " + radioStreamModel.toString());

                        _broadcastController.SendSerializableBroadcast(
                                Broadcasts.SHOW_CENTER_MODEL,
                                Bundles.CENTER_MODEL,
                                radioStreamModel);
                        break;

                    case IS_RADIO_STREAM_PLAYING:
                        return action.toString() + ":" + String.valueOf(_centerViewController.IsRadioStreamPlaying());

                    case PLAY_RADIO_STREAM:
                        if (!_screenController.IsScreenOn()) {
                            _logger.Error("Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        if (data != null) {
                            if (data.length() > 0) {
                                _lastYoutubeId = data;
                                _broadcastController.SendStringBroadcast(
                                        Broadcasts.PLAY_RADIO_STREAM,
                                        Bundles.RADIO_STREAM_ID,
                                        data);
                            } else {
                                _broadcastController.SendSimpleBroadcast(Broadcasts.PLAY_RADIO_STREAM);
                            }
                        } else {
                            _broadcastController.SendSimpleBroadcast(Broadcasts.PLAY_RADIO_STREAM);
                        }
                        break;

                    case STOP_RADIO_STREAM:
                        if (!_screenController.IsScreenOn()) {
                            _logger.Error("Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendSimpleBroadcast(Broadcasts.STOP_RADIO_STREAM);
                        break;

                    case PLAY_SEA_SOUND:
                        if (!_screenController.IsScreenOn()) {
                            _logger.Error("Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _logger.Debug(String.format("Received data for PLAY_SEA_SOUND is %s", data));
                        int timeOut;
                        try {
                            timeOut = Integer.parseInt(data) * 60 * 1000;
                            _logger.Debug(String.format(Locale.getDefault(), "timeOut for PLAY_SEA_SOUND is %s", timeOut));
                        } catch (Exception ex) {
                            _logger.Error(ex.toString());
                            Toasty.error(_context, ex.toString(), Toast.LENGTH_LONG).show();
                            timeOut = Timeouts.SEA_SOUND_STOP;
                        }
                        CenterModel playSeaSoundModel = new CenterModel(
                                false,
                                "",
                                true,
                                YoutubeId.SEA_SOUND.GetYoutubeId(),
                                false,
                                "",
                                false,
                                RadioStreams.NULL);
                        _logger.Info("Created center model: " + playSeaSoundModel.toString());

                        _broadcastController.SendSerializableBroadcast(
                                Broadcasts.SHOW_CENTER_MODEL,
                                Bundles.CENTER_MODEL,
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

                        CenterModel stopSeaSoundModel = new CenterModel(
                                true,
                                "",
                                false,
                                "",
                                false,
                                "",
                                false,
                                RadioStreams.NULL);
                        _logger.Info("Created center model: " + stopSeaSoundModel.toString());

                        _broadcastController.SendSerializableBroadcast(
                                Broadcasts.SHOW_CENTER_MODEL,
                                Bundles.CENTER_MODEL,
                                stopSeaSoundModel);

                        _seaSoundHandler.removeCallbacks(_seaSoundRunnable);
                        _seaSoundIsRunning = false;
                        _seaSoundStartTime = -1;
                        break;

                    case IS_SEA_SOUND_PLAYING:
                        String seaSSoundIsPlaying;

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
                        long differenceTimeSec = (Timeouts.SEA_SOUND_STOP - (currentTime - _seaSoundStartTime)) / 1000;
                        while (differenceTimeSec < 0) {
                            differenceTimeSec += 24 * 60 * 60;
                        }
                        return action.toString() + ":" + differenceTimeSec;

                    case SHOW_WEBVIEW:
                        if (!_screenController.IsScreenOn()) {
                            _logger.Error("Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        CenterModel webViewModel = new CenterModel(
                                false,
                                "",
                                false,
                                "",
                                true,
                                data,
                                false,
                                RadioStreams.NULL);
                        _logger.Info("Created center model: " + webViewModel.toString());

                        _broadcastController.SendSerializableBroadcast(
                                Broadcasts.SHOW_CENTER_MODEL,
                                Bundles.CENTER_MODEL,
                                webViewModel);
                        break;

                    case SHOW_CENTER_TEXT:
                        if (!_screenController.IsScreenOn()) {
                            _logger.Error("Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        CenterModel centerTextModel = new CenterModel(
                                true,
                                data,
                                false,
                                "",
                                false,
                                "",
                                false,
                                RadioStreams.NULL);
                        _logger.Info("Created center model: " + centerTextModel.toString());

                        _broadcastController.SendSerializableBroadcast(
                                Broadcasts.SHOW_CENTER_MODEL,
                                Bundles.CENTER_MODEL,
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
                            _logger.Info("Created rssFeed model: " + rSSFeedModel.toString());

                            _broadcastController.SendSerializableBroadcast(
                                    Broadcasts.PERFORM_RSS_UPDATE,
                                    Bundles.RSS_MODEL,
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
                        _mediaVolumeController.UnMuteVolume();
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

                        _broadcastController.SendStringBroadcast(
                                Broadcasts.GAME_COMMAND,
                                Bundles.GAME_COMMAND,
                                data);
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

                        _broadcastController.SendStringBroadcast(
                                Broadcasts.GAME_COMMAND,
                                Bundles.GAME_COMMAND,
                                GameConstants.GAME + ":" + GameConstants.PAUSE);
                        break;

                    case GAME_PONG_RESUME:
                        if (!_screenController.IsScreenOn()) {
                            _logger.Error("Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendStringBroadcast(
                                Broadcasts.GAME_COMMAND,
                                Bundles.GAME_COMMAND,
                                GameConstants.GAME + ":" + GameConstants.RESUME);
                        break;

                    case GAME_PONG_RESTART:
                        if (!_screenController.IsScreenOn()) {
                            _logger.Error("Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendStringBroadcast(
                                Broadcasts.GAME_COMMAND,
                                Bundles.GAME_COMMAND,
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

                        _broadcastController.SendIntBroadcast(
                                Broadcasts.ACTION_SCREEN_BRIGHTNESS,
                                Bundles.SCREEN_BRIGHTNESS,
                                ScreenController.INCREASE);

                        return action.toString() + ":" + String.valueOf(_screenController.GetCurrentBrightness());

                    case DECREASE_SCREEN_BRIGHTNESS:
                        if (!_screenController.IsScreenOn()) {
                            _logger.Error("Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendIntBroadcast(
                                Broadcasts.ACTION_SCREEN_BRIGHTNESS,
                                Bundles.SCREEN_BRIGHTNESS,
                                ScreenController.DECREASE);

                        return action.toString() + ":" + String.valueOf(_screenController.GetCurrentBrightness());

                    case GET_SCREEN_BRIGHTNESS:
                        return action.toString() + ":" + String.valueOf(_screenController.GetCurrentBrightness());

                    case SCREEN_ON:
                        _broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_ON);
                        break;

                    case SCREEN_OFF:
                        _broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_OFF);
                        break;

                    case SCREEN_NORMAL:
                        if (!_screenController.IsScreenOn()) {
                            _logger.Error("Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_NORMAL);
                        break;

                    case SYSTEM_REBOOT:
                        _commandController.RebootDevice(Timeouts.REBOOT);
                        break;
                    case SYSTEM_SHUTDOWN:
                        _commandController.ShutDownDevice(Timeouts.SHUTDOWN);
                        break;

                    case GET_BATTERY_LEVEL:
                        return action.toString() + ":" + String.valueOf(_batteryLevel);

                    case GET_SERVER_VERSION:
                        return action.toString() + ":" + _context.getString(R.string.serverVersion);

                    case GET_MEDIAMIRROR_DTO:
                        String serverIp = _userInformationController.GetIp();
                        String batteryLevel = String.valueOf(_batteryLevel);
                        String socketName = MediaServerSelection.GetByIp(serverIp).GetSocket();
                        String socketState = "0";

                        if (_socketList != null) {
                            for (int index = 0; index < _socketList.getSize(); index++) {
                                WirelessSocketDto socket = _socketList.getValue(index);
                                if (socket.GetName().contains(socketName)) {
                                    socketState = socket.IsActivated() ? "1" : "0";
                                    break;
                                }
                            }
                        } else {
                            _logger.Warn("Cannot search socket state! _socketList is null!");
                        }

                        String volume = String.valueOf(_mediaVolumeController.GetCurrentVolume());

                        String youtubeId = _lastYoutubeId;
                        String isYoutubePlaying = _centerViewController.IsYoutubePlaying() ? "1" : "0";
                        String youtubeCurrentPlayPosition = String.valueOf(_centerViewController.GetCurrentPlayPosition());
                        String youtubeCurrentVideoDuration = String.valueOf(_centerViewController.GetYoutubeDuration());

                        String playedYoutubeIds = "";

                        ArrayList<YoutubeDatabaseModel> loadedListFromDb = _centerViewController.GetYoutubeIds();
                        loadedListFromDb.sort((elementOne, elementTwo) -> Integer.valueOf(elementTwo.GetPlayCount()).compareTo(elementOne.GetPlayCount()));

                        for (YoutubeDatabaseModel entry : loadedListFromDb) {
                            playedYoutubeIds += entry.GetCommunicationString();
                        }

                        String radioStreamId = String.valueOf(_centerViewController.GetRadioStreamId());
                        String isRadioStreamPlaying = _centerViewController.IsRadioStreamPlaying() ? "1" : "0";

                        String isSeaSSoundPlaying = _seaSoundIsRunning ? "1" : "0";
                        String seaSoundCountdown;
                        if (!_seaSoundIsRunning) {
                            seaSoundCountdown = "-1";
                        } else {
                            if (_seaSoundStartTime == -1) {
                                seaSoundCountdown = "-1";
                            } else {
                                long currentTimeMSec = System.currentTimeMillis();
                                long differenceTimeInSec = (currentTimeMSec - _seaSoundStartTime) / 1000;
                                while (differenceTimeInSec < 0) {
                                    differenceTimeInSec += 24 * 60 * 60;
                                }
                                seaSoundCountdown = String.valueOf(differenceTimeInSec);
                            }
                        }

                        String serverVersion = _context.getString(R.string.serverVersion);
                        String screenBrightness = String.valueOf(_screenController.GetCurrentBrightness());

                        String mediaMirrorDto = String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s",
                                serverIp, batteryLevel, socketName,
                                socketState, volume, youtubeId,
                                isYoutubePlaying, youtubeCurrentPlayPosition, youtubeCurrentVideoDuration,
                                playedYoutubeIds, radioStreamId, isRadioStreamPlaying,
                                isSeaSSoundPlaying, seaSoundCountdown, serverVersion,
                                screenBrightness);

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
        _logger.Debug("Dispose");

        _mediaVolumeController.Dispose();
        _receiverController.Dispose();
    }

    private MediaServerAction convertCommandToAction(@NonNull String command) {
        _logger.Debug(command);

        String[] entries = command.split("\\&");
        if (entries.length == 2) {
            String action = entries[0];
            action = action.replace("ACTION:", "");
            _logger.Debug("Action is: " + action);

            MediaServerAction serverAction = MediaServerAction.GetByString(action);
            _logger.Debug(String.format(Locale.getDefault(), "Found action: %s", serverAction));

            return serverAction;
        }

        _logger.Warn("Wrong size of entries: " + String.valueOf(entries.length));
        return MediaServerAction.NULL;
    }

    private String convertCommandToData(@NonNull String command) {
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
