package guepardoapps.mediamirror.view.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ForecastWeatherModel implements Serializable {

	private static final long serialVersionUID = -2117141523794622123L;
	
	private ArrayList<CurrentWeatherModel> _forecasts;

	public ForecastWeatherModel() {
		_forecasts = new ArrayList<CurrentWeatherModel>();
	}

	public ForecastWeatherModel(ArrayList<CurrentWeatherModel> forecasts) {
		_forecasts = forecasts;
	}

	public void AddForecast(CurrentWeatherModel forecast) {
		if (_forecasts == null) {
			_forecasts = new ArrayList<CurrentWeatherModel>();
		}
		_forecasts.add(forecast);
	}

	public ArrayList<CurrentWeatherModel> GetForecasts() {
		return _forecasts;
	}

	public CurrentWeatherModel GetForecast(int id) {
		if (id > _forecasts.size() - 1) {
			return null;
		}
		return _forecasts.get(id);
	}

	@Override
	public String toString() {
		String string = ForecastWeatherModel.class.getName() + ":{";
		if (!_forecasts.isEmpty()) {
			for (int index = 0; index < _forecasts.size(); index++) {
				string += "Forecast" + String.valueOf(index) + _forecasts.get(index).toString() + ";";
			}
		}
		string += "}";
		return string;
	}
}
