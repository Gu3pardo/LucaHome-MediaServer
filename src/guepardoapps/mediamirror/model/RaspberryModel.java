package guepardoapps.mediamirror.model;

import java.io.Serializable;

public class RaspberryModel implements Serializable {

	private static final long serialVersionUID = -7204415758428216530L;

	private String _raspberry1Name;
	private String _raspberry1Temperature;
	private String _raspberry1TemperatureGraphUrl;

	public RaspberryModel(String raspberry1Name, String raspberry1Temperature, String raspberry1TemperatureGraphUrl) {
		_raspberry1Name = raspberry1Name;
		_raspberry1Temperature = raspberry1Temperature;
		_raspberry1TemperatureGraphUrl = raspberry1TemperatureGraphUrl;
	}

	public String GetRaspberry1Name() {
		return _raspberry1Name;
	}

	public String GetRaspberry1Temperature() {
		return _raspberry1Temperature;
	}

	public String GetRaspberry1TemperatureGraphUrl() {
		return _raspberry1TemperatureGraphUrl;
	}

	@Override
	public String toString() {
		return RaspberryModel.class.getName() + ":{Raspberry1Name:" + _raspberry1Name + ";Raspberry1Temperature:"
				+ _raspberry1Temperature + ";Raspberry1TemperatureGraphUrl:" + _raspberry1TemperatureGraphUrl + "}";
	}
}
