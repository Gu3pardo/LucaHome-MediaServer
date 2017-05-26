package guepardoapps.mediamirror.view.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class CurrentWeatherModel implements Serializable {

    private static final long serialVersionUID = 402729935724501743L;

    private static final String TAG = CurrentWeatherModel.class.getSimpleName();

    private String _condition;
    private String _temperature;
    private String _humidity;
    private String _pressure;
    private String _updateTime;
    private int _imageId;

    private String _weekday;
    private String _date;
    private String _time;
    private String _temperatureRange;

    public CurrentWeatherModel(
            @NonNull String condition,
            @NonNull String temperature,
            @NonNull String humidity,
            @NonNull String pressure,
            @NonNull String updatedTime,
            int imageId,
            @NonNull String weekday,
            @NonNull String date,
            @NonNull String time,
            @NonNull String temperatureRange) {
        _condition = condition;
        _temperature = temperature;
        _humidity = humidity;
        _pressure = pressure;
        _updateTime = updatedTime;
        _imageId = imageId;

        _weekday = weekday;
        _date = date;
        _time = time;
        _temperatureRange = temperatureRange;
    }

    public String GetCondition() {
        return _condition;
    }

    public String GetTemperature() {
        return _temperature;
    }

    public String GetHumidity() {
        return _humidity;
    }

    public String GetPressure() {
        return _pressure;
    }

    public String GetUpdatedTime() {
        return _updateTime;
    }

    public int GetImageId() {
        return _imageId;
    }

    public String GetWeekday() {
        return _weekday;
    }

    public String GetDate() {
        return _date;
    }

    public String GetTime() {
        return _time;
    }

    public String GetTemperatureRange() {
        return _temperatureRange;
    }

    @Override
    public String toString() {
        return TAG
                + ":{Condition:" + _condition
                + ";Temperature:" + _temperature
                + ";Humidity:" + _humidity
                + ";Pressure:" + _pressure
                + ";UpdatedTime:" + _updateTime
                + ";ImageId:" + String.valueOf(_imageId)
                + ";Weekday:" + _weekday
                + ";Date:" + _date
                + ";Time:" + _time
                + ";TemperatureRange:" + _temperatureRange + "}";
    }
}
