package guepardoapps.mediamirror.view.model.helper;

import android.support.annotation.NonNull;

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

    public int GetIcon(@NonNull String temperature) {
        _logger.Debug("temperature: " + temperature);
        String pureDouble = temperature.replace("°C", "");
        _logger.Debug("pureDouble: " + pureDouble);

        double parsedDouble;
        try {
            parsedDouble = Double.parseDouble(pureDouble);
        } catch (Exception e) {
            _logger.Warn("Parsing ot string to double failed! Setting to -1");
            parsedDouble = -1;
        }
        _logger.Debug("parsedDouble: " + String.valueOf(parsedDouble));

        int drawable = 0;
        if (parsedDouble < 12) {
            drawable = R.xml.circle_blue;
        } else if (parsedDouble >= 12 && parsedDouble < 15) {
            drawable = R.xml.circle_red;
        } else if (parsedDouble >= 15 && parsedDouble < 18) {
            drawable = R.xml.circle_yellow;
        } else if (parsedDouble >= 18 && parsedDouble < 24) {
            drawable = R.xml.circle_green;
        } else if (parsedDouble >= 24 && parsedDouble < 30) {
            drawable = R.xml.circle_yellow;
        } else if (parsedDouble >= 30) {
            drawable = R.xml.circle_red;
        }

        _logger.Debug("Drawable: " + String.valueOf(drawable));
        return drawable;
    }
}
