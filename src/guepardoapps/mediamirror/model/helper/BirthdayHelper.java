package guepardoapps.mediamirror.model.helper;

import java.io.Serializable;
import java.util.Calendar;

public class BirthdayHelper implements Serializable {

	private static final long serialVersionUID = 5994640560542747092L;

	private String _name;
	private Calendar _birthday;

	public BirthdayHelper(String name, Calendar birthday, int id) {
		_name = name;
		_birthday = birthday;
	}

	public String GetName() {
		return _name;
	}

	public Calendar GetBirthday() {
		return _birthday;
	}

	public boolean HasBirthday() {
		Calendar today = Calendar.getInstance();
		if ((today.get(Calendar.DAY_OF_MONTH) == _birthday.get(Calendar.DAY_OF_MONTH))
				&& (today.get(Calendar.MONTH) == _birthday.get(Calendar.MONTH))) {
			return true;
		}
		return false;
	}

	public String GetBirthdayString() {
		String birthdayString = "";
		birthdayString += String.valueOf(_birthday.get(Calendar.DAY_OF_MONTH)) + "."
				+ String.valueOf(_birthday.get(Calendar.MONTH) + 1) + "."
				+ String.valueOf(_birthday.get(Calendar.YEAR));
		return birthdayString;
	}

	public String GetNotificationString() {
		return "It is " + _name + "'s " + String.valueOf(GetAge()) + "th birthday!";
	}

	public int GetAge() {
		Calendar today = Calendar.getInstance();
		int age;
		if ((today.get(Calendar.MONTH) > _birthday.get(Calendar.MONTH))
				|| (today.get(Calendar.MONTH) == _birthday.get(Calendar.MONTH)
						&& today.get(Calendar.DAY_OF_MONTH) >= _birthday.get(Calendar.DAY_OF_MONTH))) {
			age = today.get(Calendar.YEAR) - _birthday.get(Calendar.YEAR);
		} else {
			age = today.get(Calendar.YEAR) - _birthday.get(Calendar.YEAR) - 1;
		}
		return age;
	}

	public String toString() {
		return "{Birthday: {Name: " + _name + "};{Birthday: " + _birthday.toString() + "};{Hasbirthday: "
				+ String.valueOf(HasBirthday()) + "}}";
	}
}
