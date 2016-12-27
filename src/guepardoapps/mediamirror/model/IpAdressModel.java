package guepardoapps.mediamirror.model;

import java.io.Serializable;

public class IpAdressModel implements Serializable {

	private static final long serialVersionUID = 2738438227273452818L;

	private boolean _isVisible;
	private String _ipAdress;

	public IpAdressModel(boolean isVisible, String ipAdress) {
		_isVisible = isVisible;
		_ipAdress = ipAdress;
	}

	public boolean GetIsVisible() {
		return _isVisible;
	}

	public String GetIpAdress() {
		return _ipAdress;
	}

	@Override
	public String toString() {
		return IpAdressModel.class.getName() + ":{IsVisible:" + String.valueOf(_isVisible) + ";IpAdress:" + _ipAdress
				+ "}";
	}
}
