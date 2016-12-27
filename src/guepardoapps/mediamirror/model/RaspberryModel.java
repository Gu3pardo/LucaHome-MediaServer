package guepardoapps.mediamirror.model;

import java.io.Serializable;

public class RaspberryModel implements Serializable {

	private static final long serialVersionUID = -8076590227637643687L;

	private String _raspberry1Name;
	private String _raspberry2Name;
	private String _raspberry1Temperature;
	private String _raspberry2Temperature;

	public RaspberryModel(String raspberry1Name, String raspberry2Name, String raspberry1Temperature,
			String raspberry2Temperature) {
		_raspberry1Name = raspberry1Name;
		_raspberry2Name = raspberry2Name;
		_raspberry1Temperature = raspberry1Temperature;
		_raspberry2Temperature = raspberry2Temperature;
	}

	public String GetRaspberry1Name() {
		return _raspberry1Name;
	}

	public String GetRaspberry2Name() {
		return _raspberry2Name;
	}

	public String GetRaspberry1Temperature() {
		return _raspberry1Temperature;
	}

	public String GetRaspberry2Temperature() {
		return _raspberry2Temperature;
	}

	@Override
	public String toString() {
		return RaspberryModel.class.getName() + ":{Raspberry1Name:" + _raspberry1Name + ";Raspberry2Name:"
				+ _raspberry2Name + ";Raspberry1Temperature:" + _raspberry1Temperature + ";Raspberry2Temperature:"
				+ _raspberry2Temperature + "}";
	}
}
