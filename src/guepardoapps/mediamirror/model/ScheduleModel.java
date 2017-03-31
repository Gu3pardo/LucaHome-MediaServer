package guepardoapps.mediamirror.model;

import java.io.Serializable;
import java.sql.Time;

import guepardoapps.library.toolset.common.enums.Weekday;

public class ScheduleModel implements Serializable {

	private static final long serialVersionUID = 1065694448413782352L;

	@SuppressWarnings("unused")
	private static final String TAG = ScheduleModel.class.getSimpleName();

	protected String _name;
	protected String _socket;
	protected Weekday _weekday;
	protected Time _time;
	protected boolean _action;
	protected boolean _isTimer;
	protected boolean _isActive;

	private String _setBroadcastReceiverString;
	protected String _deleteBroadcastReceiverString;

	public ScheduleModel(String name, String socket, Weekday weekday, Time time, boolean action, boolean isTimer,
			boolean isActive) {
		_name = name;
		_socket = socket;
		_weekday = weekday;
		_time = time;
		_action = action;
		_isTimer = isTimer;
		_isActive = isActive;
	}

	public String GetName() {
		return _name;
	}

	public String GetSocket() {
		return _socket;
	}

	public Weekday GetWeekday() {
		return _weekday;
	}

	public Time GetTime() {
		return _time;
	}

	public boolean GetAction() {
		return _action;
	}

	public boolean GetIsTimer() {
		return _isTimer;
	}

	public boolean GetIsActive() {
		return _isActive;
	}

	public String toString() {
		String socket = "";
		if (_socket != null) {
			socket = _socket.toString();
		}
		return "{Schedule: {Name: " + _name + "};{WirelessSocket: " + socket + "};{Weekday: " + _weekday.toString()
				+ "};{Time: " + _time.toString() + "};{Action: " + String.valueOf(_action) + "};{isTimer: "
				+ String.valueOf(_isTimer) + "};{IsActive: " + String.valueOf(_isActive)
				+ "};{SetBroadcastReceiverString: " + _setBroadcastReceiverString + "};{DeleteBroadcastReceiverString: "
				+ _deleteBroadcastReceiverString + "}}";
	}
}
