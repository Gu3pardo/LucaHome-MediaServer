package guepardoapps.mediamirror.common.constants;

public class RaspPiConstants {
	public static final String USER_NAME = "SmartMirror";
	public static final String PASS_PHRASE = "023884";

	public static final String[] SERVER_URLs = new String[] { "http://192.168.178.22" };

	public static final String ACTION_PATH = "/lib/lucahome.php?user=";

	public static final String BUNDLE_REST_ACTION = "BUNDLE_REST_ACTION";
	public static final String BUNDLE_REST_DATA = "BUNDLE_REST_DATA";
	public static final String BUNDLE_REST_BROADCAST = "BUNDLE_REST_BROADCAST";

	public static final String SOCKET_STATE_ON = "&state=1";
	public static final String SOCKET_STATE_OFF = "&state=0";

	// RASPBERRY VIEW
	public static final String GET_TEMPERATURES = "getcurrenttemperaturerest";
	// BIRTHDAY VIEW
	public static final String GET_BIRTHDAYS = "getbirthdays";
	// SCHEDULE DATA
	public static final String GET_SCHEDULES = "getschedules";
	// SOCKET DATA
	public static final String SET_SOCKET = "setsocket&socket=";
	public static final String GET_SOCKETS = "getsockets";
	// SHOPPING LIST DATA
	public static final String GET_SHOPPING_LIST = "getshoppinglist";
	// MENU DATA
	public static final String GET_MENU = "getmenu";
}
