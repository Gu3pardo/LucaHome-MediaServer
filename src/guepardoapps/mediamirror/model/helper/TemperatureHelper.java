package guepardoapps.mediamirror.model.helper;

import java.io.Serializable;
import java.sql.Time;

public class TemperatureHelper implements Serializable {

	private static final long serialVersionUID = 8819776017467352561L;

	private double _temperature;
	private String _area;
	private Time _lastUpdate;
	private String _graphUrl;

	public TemperatureHelper(double temperature, String area, Time lastUpdate, String graphUrl) {
		_temperature = temperature;
		_area = area;
		_lastUpdate = lastUpdate;
		_graphUrl = graphUrl;
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

	public String GetGraphUrl() {
		return _graphUrl;
	}

	public void SetLastUpdate(Time lastUpdate) {
		_lastUpdate = lastUpdate;
	}

	public String toString() {
		return "{Temperature: {Value: " + GetTemperatureString() + "};{Area: " + _area + "};{LastUpdate: "
				+ _lastUpdate.toString() + "};{GraphUrl: " + _graphUrl + "}}";
	}
}
