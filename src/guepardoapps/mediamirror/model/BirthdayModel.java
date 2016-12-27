package guepardoapps.mediamirror.model;

import java.io.Serializable;

public class BirthdayModel implements Serializable {

	private static final long serialVersionUID = 5397250846548435774L;
	
	private boolean _isVisible;
	private String _text;
	private boolean _hasBirthday;

	public BirthdayModel(boolean isVisible, String text, boolean hasBirthday) {
		_isVisible = isVisible;
		_text = text;
		_hasBirthday = hasBirthday;
	}

	public boolean GetIsVisible() {
		return _isVisible;
	}

	public String GetText() {
		return _text;
	}

	public boolean GetHasBirthday() {
		return _hasBirthday;
	}

	@Override
	public String toString() {
		return BirthdayModel.class.getName() + ":{IsVisible:" + String.valueOf(_isVisible) + ";Text:" + _text
				+ ";HasBirthday:" + String.valueOf(_hasBirthday) + "}";
	}
}
