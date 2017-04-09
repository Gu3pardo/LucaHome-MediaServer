package guepardoapps.mediamirror.view.model;

import java.io.Serializable;

public class DateModel implements Serializable {

	private static final long serialVersionUID = -3097274504214063795L;
	
	private String _weekday;
	private String _date;
	private String _time;

	public DateModel(String weekday, String date, String time) {
		_weekday = weekday;
		_date = date;
		_time = time;
	}

	public String GetWeekday() {
		return _weekday;
	}

	public String GetDate() {
		return _date;
	}

	public String GetTime() {
		return _time;
	}

	@Override
	public String toString() {
		return DateModel.class.getName() + ":{Weekday:" + _weekday + ";Date:" + _date + ";Time:" + _time + "}";
	}
}
