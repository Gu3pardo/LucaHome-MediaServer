package guepardoapps.mediamirror.common.enums;

import java.io.Serializable;

public enum BirthdayDateType implements Serializable {

	TODAY(0),
	PREVIOUS(1),
	UPCOMING(2);

	private int _id;

	private BirthdayDateType(int id) {
		_id = id;
	}

	public int GetId() {
		return _id;
	}

	public static BirthdayDateType GetById(int id) {
		for (BirthdayDateType e : values()) {
			if (e._id == id) {
				return e;
			}
		}
		return null;
	}
}
