package se.markstrom.skynet.skynetremote.model;

public class WeatherReport {

	public final String provider;
	public final String updated;
	public final String validFrom;
	public final String validTo;
	public final String area;
	public final String sunrise;
	public final String sunset;
	public final String windCode;
	
	public final Double temperature;
	public final Double humidity;
	public final Double precipitation;
	public final Double windspeed;
	public final Double pressure;
	
	public WeatherReport(String provider, String updated, String validFrom, String validTo, String area, String sunrise, String sunset, String windCode, Double temperature, Double humidity, Double precipitation, Double windspeed, Double pressure) {
		this.provider = provider;
		this.updated = updated;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.area = area;
		this.sunrise = sunrise;
		this.sunset = sunset;
		this.windCode = windCode;
		this.temperature = temperature;
		this.humidity = humidity; 
		this.precipitation = precipitation;
		this.windspeed = windspeed;
		this.pressure = pressure;
	}
	
	public String getTemperatureStr() {
		if (temperature != null) {
			return temperature.toString();
		}
		return "";
	}

	public String getHumidityStr() {
		if (humidity != null) {
			return humidity.toString();
		}
		return "";
	}

	public String getPrecipitationStr() {
		if (precipitation != null) {
			return precipitation.toString();
		}
		return "";
	}

	public String getWindspeedStr() {
		if (windspeed != null) {
			return windspeed.toString() + " " + windCode;
		}
		return "";
	}

	public String getPressureStr() {
		if (pressure != null) {
			return pressure.toString();
		}
		return "";
	}

}
