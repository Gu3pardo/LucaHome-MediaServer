package guepardoapps.mediamirror.model.helper;

import java.io.Serializable;
import java.sql.Time;

public class TemperatureHelper implements Serializable {

	private static final long serialVersionUID = -7750618759715343982L;

	private double _temperature;
	private String _area;
	private Time _lastUpdate;

	public TemperatureHelper(double temperature, String area, Time lastUpdate) {
		_temperature = temperature;
		_area = area;
		_lastUpdate = lastUpdate;
	}

	public double GetTemperatureValue() {
		return _temperature;
	}

	public void SetTemperature(double temperature) {
		_temperature = temperature;
	}

	public String GetTemperatureString() {
		return String.valueOf(_temperature) + "°C";
	}

	public String GetArea() {
		return _area;
	}

	public Time GetLastUpdate() {
		return _lastUpdate;
	}

	public void SetLastUpdate(Time lastUpdate) {
		_lastUpdate = lastUpdate;
	}

	public String toString() {
		return "{Temperature: {Value: " + GetTemperatureString() + "};{Area: " + _area + "};{LastUpdate: "
				+ _lastUpdate.toString() + "}}";
	}
}
