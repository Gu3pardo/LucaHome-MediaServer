package guepardoapps.mediamirror.common;

public class Constants {
	// TESTING
	public static final boolean TESTING_ENABLED = false;
	// DEBUGGING
	public static final boolean DEBUGGING_ENABLED = false;
	// DATE VIEW
	public static final int DATE_UPDATE_TIMEOUT = 15 * 1000;
	public static final String BUNDLE_DATE_MODEL = "BUNDLE_DATE_MODEL";
	public static final String BROADCAST_SHOW_DATE_MODEL = "guepardoapps.mediamirror.show.date_model";
	// RASPBERRY VIEW
	public static final int TEMPERATURE_UPDATE_TIMEOUT = 5 * 60 * 1000;
	public static final String ACTION_GET_TEMPERATURES = "getcurrenttemperaturerest";
	public static final String BUNDLE_RASPBERRY_DATA_MODEL = "BUNDLE_RASPBERRY_DATA_MODEL";
	public static final String BROADCAST_SHOW_RASPBERRY_DATA_MODEL = "guepardoapps.mediamirror.show.raspberry_data_model";
	public static final String BROADCAST_DOWNLOAD_TEMPERATURE_FINISHED = "guepardoapps.mediamirror.broadcast.DOWNLOAD_TEMPERATURE_FINISHED";
	public static final String BROADCAST_PERFORM_TEMPERATURE_UPDATE = "guepardoapps.mediamirror.broadcast.PERFORM_TEMPERATURE_UPDATE";
	// BIRTHDAY VIEW
	public static final int BIRTHDAY_UPDATE_TIMEOUT = 12 * 60 * 60 * 1000;
	public static final String ACTION_GET_BIRTHDAYS = "getbirthdays";
	public static final String BUNDLE_BIRTHDAY_MODEL = "BUNDLE_BIRTHDAY_MODEL";
	public static final String BROADCAST_SHOW_BIRTHDAY_MODEL = "guepardoapps.mediamirror.show.birthday_model";
	public static final String BROADCAST_DOWNLOAD_BIRTHDAY_FINISHED = "guepardoapps.mediamirror.broadcast.DOWNLOAD_BIRTHDAY_FINISHED";
	public static final String BROADCAST_PERFORM_BIRTHDAY_UPDATE = "guepardoapps.mediamirror.broadcast.PERFORM_BIRTHDAY_UPDATE";
	// CURRENT WEATHER VIEW
	public static final String CITY = "Munich";
	public static final int CURRENT_WEATHER_UPDATE_TIMEOUT = 15 * 60 * 1000;
	public static final String BUNDLE_CURRENT_WEATHER_MODEL = "BUNDLE_CURRENT_WEATHER_MODEL";
	public static final String BROADCAST_SHOW_CURRENT_WEATHER_MODEL = "guepardoapps.mediamirror.show.current_weather_model";
	public static final String BROADCAST_PERFORM_CURRENT_WEATHER_UPDATE = "guepardoapps.mediamirror.broadcast.PERFORM_CURRENT_WEATHER_UPDATE";
	// FORECAST WEATHER VIEW
	public static final int FORECAST_WEATHER_UPDATE_TIMEOUT = 15 * 60 * 1000;
	public static final String BUNDLE_FORECAST_WEATHER_MODEL = "BUNDLE_FORECAST_WEATHER_MODEL";
	public static final String BROADCAST_SHOW_FORECAST_WEATHER_MODEL = "guepardoapps.mediamirror.show.forecast_weather_model";
	public static final String BROADCAST_PERFORM_FORECAST_WEATHER_UPDATE = "guepardoapps.mediamirror.broadcast.PERFORM_FORECAST_WEATHER_UPDATE";
	// IP ADRESS VIEW
	public static final int IP_ADRESS_UPDATE_TIMEOUT = 12 * 60 * 60 * 1000;
	public static final String BUNDLE_IP_ADRESS_MODEL = "BUNDLE_IP_ADRESS_MODEL";
	public static final String BROADCAST_SHOW_IP_ADRESS_MODEL = "guepardoapps.mediamirror.show.ip_adress_model";
	public static final String BROADCAST_PERFORM_IP_ADDRESS_UPDATE = "guepardoapps.mediamirror.broadcast.PERFORM_IP_ADDRESS_UPDATE";
	// CENTER VIEW
	public static final String BUNDLE_CENTER_MODEL = "BUNDLE_CENTER_MODEL";
	public static final String BROADCAST_SHOW_CENTER_MODEL = "guepardoapps.mediamirror.show.center_model";
	public static final String BROADCAST_PLAY_VIDEO = "guepardoapps.mediamirror.video.play";
	public static final String BROADCAST_STOP_VIDEO = "guepardoapps.mediamirror.video.stop";
	public static final String BROADCAST_SET_VIDEOVIEW_VISIBILITY = "guepardoapps.mediamirror.videoview.set.visibility";
	// RSS VIEW
	public static final int RSS_UPDATE_TIMEOUT = 5 * 60 * 1000;
	public static final String BUNDLE_RSS_DATA_MODEL = "BUNDLE_RSS_DATA_MODEL";
	public static final String BROADCAST_SHOW_RSS_DATA_MODEL = "guepardoapps.mediamirror.show.rss_data_model";
	public static final String BROADCAST_DOWNLOAD_RSS_FINISHED = "guepardoapps.mediamirror.broadcast.DOWNLOAD_RSS_FINISHED";
	public static final String BUNDLE_RSS_MODEL = "BUNDLE_RSS_MODEL";
	public static final String BROADCAST_UPDATE_RSS_FEED = "guepardoapps.mediamirror.broadcast.UPDATE_RSS_FEED";
	public static final String BROADCAST_RESET_RSS_FEED = "guepardoapps.mediamirror.broadcast.RESET_RSS_FEED";
	// VOLUME VIEW
	public static final String BUNDLE_VOLUME_MODEL = "BUNDLE_VOLUME_MODEL";
	public static final String BROADCAST_SHOW_VOLUME_MODEL = "guepardoapps.mediamirror.show.volume_model";
	// SCREEN BRIGHTNESS
	public static final String BUNDLE_SCREEN_BRIGHTNESS = "BUNDLE_SCREEN_BRIGHTNESS";
	public static final String BROADCAST_ACTION_SCREEN_BRIGHTNESS = "guepardoapps.mediamirror.action.screen_brightness";
	public static final String BROADCAST_VALUE_SCREEN_BRIGHTNESS = "guepardoapps.mediamirror.value.screen_brightness";
	// SPEAK DATA
	public static final String BUNDLE_SPEAK_TEXT = "BUNDLE_SPEAK_TEXT";
	public static final String BROADCAST_SPEAK_TEXT = "guepardoapps.mediamirror.speak.text";
	// TOAST DATA
	public static final String BUNDLE_TOAST_TEXT = "BUNDLE_TOAST_TEXT";
	public static final String BROADCAST_TOAST_TEXT = "guepardoapps.mediamirror.toast.text";
	// SCHEDULE DATA
	public static final int SCHEDULE_UPDATE_TIMEOUT = 2 * 60 * 60 * 1000;
	public static final String ACTION_GET_SCHEDULES = "getschedules";
	public static final String BUNDLE_SCHEDULE_MODEL = "BUNDLE_SCHEDULE_MODEL";
	public static final String BROADCAST_SHOW_SCHEDULE_MODEL = "guepardoapps.mediamirror.show.schedule_model";
	public static final String BROADCAST_DOWNLOAD_SCHEDULE_FINISHED = "guepardoapps.mediamirror.broadcast.DOWNLOAD_SCHEDULE_FINISHED";
	// SOCKET DATA
	public static final String ACTION_SET_SOCKET = "setsocket&socket=";
	public static final String SOCKET_NAME = "MediaMirror1";
	public static final String SOCKET_STATE_ON = "&state=1";
	public static final String SOCKET_STATE_OFF = "&state=0";
	// GAME DATA
	public static final String BUNDLE_GAME_COMMAND = "BUNDLE_GAME_COMMAND";
	public static final String BROADCAST_GAME_COMMAND = "guepardoapps.mediamirror.game.command";
	// PONG DATA
	public static final String BROADCAST_START_PONG = "guepardoapps.mediamirror.pong.start";
	public static final String BROADCAST_STOP_PONG = "guepardoapps.mediamirror.pong.stop";
	// SNAKE DATA
	public static final String BROADCAST_START_SNAKE = "guepardoapps.mediamirror.snake.start";
	public static final String BROADCAST_STOP_SNAKE = "guepardoapps.mediamirror.snake.stop";
	// TETRIS DATA
	public static final String BROADCAST_START_TETRIS = "guepardoapps.mediamirror.tetris.start";
	public static final String BROADCAST_STOP_TETRIS = "guepardoapps.mediamirror.tetris.stop";
	// YOUTUBE API
	public static final String YOUTUBE_API_KEY = "AIzaSyBsEHy5iVJs67Kktb0dEkwHxGaYNj6wx5E";
	// SERVER
	public static final int SERVERPORT = 8080;
	// TIME
	public static final int START_MUTE_TIME = 23;
	public static final int END_MUTE_TIME = 6;
	// DATA FOR RASPBERRY
	public static final String USER_NAME = "SmartMirror";
	public static final String PASS_PHRASE = "023884";
	public static final String[] SERVER_URLs = new String[] { "http://192.168.178.22" };
	public static final String ACTION_PATH = "/lib/lucahome.php?user=";
	public static final String BUNDLE_REST_ACTION = "BUNDLE_REST_ACTION";
	public static final String BUNDLE_REST_DATA = "BUNDLE_REST_DATA";
	public static final String BUNDLE_REST_BROADCAST = "BUNDLE_REST_BROADCAST";
	// SERVER ID
	public static final int SERVER_ID = 1;

}
