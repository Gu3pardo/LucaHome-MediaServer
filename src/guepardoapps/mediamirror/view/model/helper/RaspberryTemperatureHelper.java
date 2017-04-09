package guepardoapps.mediamirror.view.model.helper;

import java.io.Serializable;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.R;

public class RaspberryTemperatureHelper implements Serializable {

	private static final long serialVersionUID = -8359166934848014777L;

	private static final String TAG = RaspberryTemperatureHelper.class.getSimpleName();
	private SmartMirrorLogger _logger;

	public RaspberryTemperatureHelper() {
		_logger = new SmartMirrorLogger(TAG);
	}

	public int GetIcon(String temperature) {
		if (temperature == null) {
			_logger.Warn("temperature is null!");
			return R.drawable.circle_red;
		}

		_logger.Debug("temperature: " + temperature);
		String pureDouble = temperature.replace("°C", "");
		_logger.Debug("pureDouble: " + pureDouble);

		double parsedDouble = -1;
		try {
			parsedDouble = Double.parseDouble(pureDouble);
		} catch (Exception e) {
			_logger.Warn("Parsing ot string to double failed! Setting to -1");
			parsedDouble = -1;
		}
		_logger.Debug("parsedDouble: " + String.valueOf(parsedDouble));

		int drawable = 0;
		if (parsedDouble < 12) {
			drawable = R.drawable.circle_blue;
		} else if (parsedDouble >= 12 && parsedDouble < 15) {
			drawable = R.drawable.circle_red;
		} else if (parsedDouble >= 15 && parsedDouble < 18) {
			drawable = R.drawable.circle_yellow;
		} else if (parsedDouble >= 18 && parsedDouble < 24) {
			drawable = R.drawable.circle_green;
		} else if (parsedDouble >= 24 && parsedDouble < 30) {
			drawable = R.drawable.circle_yellow;
		} else if (parsedDouble >= 30) {
			drawable = R.drawable.circle_red;
		}

		_logger.Debug("Drawable: " + String.valueOf(drawable));
		return drawable;
	}
}
