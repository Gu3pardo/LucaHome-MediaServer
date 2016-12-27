package guepardoapps.mediamirror.model;

import java.io.Serializable;

public class RaspberryModel implements Serializable {

	private static final long serialVersionUID = 7784194968878152248L;
	
	private String _raspberry1Name;
	private String _raspberry1Temperature;

	public RaspberryModel(String raspberry1Name, String raspberry1Temperature) {
		_raspberry1Name = raspberry1Name;
		_raspberry1Temperature = raspberry1Temperature;
	}

	public String GetRaspberry1Name() {
		return _raspberry1Name;
	}

	public String GetRaspberry1Temperature() {
		return _raspberry1Temperature;
	}

	@Override
	public String toString() {
		return RaspberryModel.class.getName() + ":{Raspberry1Name:" + _raspberry1Name + ";Raspberry1Temperature:"
				+ _raspberry1Temperature + "}";
	}
}
