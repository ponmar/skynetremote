package se.markstrom.skynet.skynetremote.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.markstrom.skynet.api.SkynetAPI.Protocol;
import se.markstrom.skynet.skynetremote.window.ApplicationWindow;
import se.markstrom.skynet.skynetremote.xmlparser.CamerasXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.ControlXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.EventsXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.LogXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.SensorsXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.SettingsXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.SummaryXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.WeatherXmlParser;
import se.markstrom.skynet.skynetremote.xmlwriter.SettingsXmlWriter;

public class Model {

	private static final Logger logger = Logger.getLogger(ApplicationWindow.class.getName());
	static {
		logger.setLevel(Level.ALL);
	}
	
	private static final String SETTINGS_FILENAME = "SkynetRemote.xml";
	
	private Integer majorApiVersion;
	private Integer minorApiVersion;
	private String site;
	private Boolean armed;
	private Double countdown;
	private Long latestEventId;
	private Integer numInfoEvents;
	private Integer numMinorEvents;
	private Integer numMajorEvents;
	private Double logTimestamp;
	private String controlChecksum;
	private String weatherChecksum;
	private String time;
	
	private ArrayList<Event> events = new ArrayList<Event>();
	private ArrayList<Device> devices = new ArrayList<Device>();
	private ArrayList<Camera> cameras = new ArrayList<Camera>();
	private ArrayList<Sensor> sensors = new ArrayList<Sensor>();
	private ArrayList<WeatherReport> weatherReports = new ArrayList<WeatherReport>();
	private ArrayList<String> logItems = new ArrayList<String>();
	
	private Settings settings = null;

	public Model() {
		reset();
		readSettings();
	}

	public void reset() {
		majorApiVersion = null;
		minorApiVersion = null;
		site = null;
		armed = null;
		countdown = null;
		latestEventId = null;
		numInfoEvents = null;
		numMinorEvents = null;
		numMajorEvents = null;
		logTimestamp = null;
		controlChecksum = null;
		weatherChecksum = null;
		time = null;
		
		events.clear();
		devices.clear();
		cameras.clear();
		sensors.clear();
		weatherReports.clear();
		logItems.clear();
	}
	
	private void readSettings() {
		SettingsXmlParser parser = new SettingsXmlParser(SETTINGS_FILENAME);
		if (parser.isValid()) {
			logger.info("Using settings from " + SETTINGS_FILENAME);
			settings = parser.getSettings();
		}
		else {
			logger.info("Using default settings");
			settings = new Settings();
		}
	}
	
	public boolean updateFromSummaryXml(String summaryXml) {
		SummaryXmlParser parser = new SummaryXmlParser(summaryXml);
		if (parser.isValid()) {
			majorApiVersion = parser.majorApiVersion;
			minorApiVersion = parser.minorApiVersion;
			site = parser.site;
			armed = parser.armed;
			countdown = parser.countdown;
			latestEventId = parser.latestEventId;
			numInfoEvents = parser.numInfoEvents;
			numMinorEvents = parser.numMinorEvents;
			numMajorEvents = parser.numMajorEvents;
			logTimestamp = parser.logTimestamp;
			controlChecksum = parser.controlChecksum;
			weatherChecksum = parser.weatherChecksum;
			time = parser.time;
			return true;
		}
		return false;
	}
	
	public boolean updateFromEventsXml(String eventsXml) {
		EventsXmlParser parser = new EventsXmlParser(eventsXml);
		if (parser.isValid()) {
			events = parser.getEvents();
			return true;
		}
		return false;
	}
	
	public boolean updateFromWeatherXml(String weatherXml) {
		WeatherXmlParser parser = new WeatherXmlParser(weatherXml);
		if (parser.isValid()) {
			weatherReports = parser.getWeatherReports();
			return true;
		}
		return false;
	}
	
	public boolean updateFromSensorsXml(String sensorsXml) {
		SensorsXmlParser parser = new SensorsXmlParser(sensorsXml);
		if (parser.isValid()) {
			sensors = parser.getSensors();
			return true;
		}
		return false;
	}

	public boolean updateFromControlXml(String controlXml) {
		ControlXmlParser parser = new ControlXmlParser(controlXml);
		if (parser.isValid()) {
			devices = parser.getDevices();
			return true;
		}
		return false;
	}
	
	public boolean updateFromCamerasXml(String camerasXml) {
		CamerasXmlParser parser = new CamerasXmlParser(camerasXml);
		if (parser.isValid()) {
			cameras = parser.getCameras();
			return true;
		}
		return false;
	}
	
	public boolean updateFromLogXml(String logXml) {
		LogXmlParser parser = new LogXmlParser(logXml);
		if (parser.isValid()) {
			logItems = parser.getLogItems();
			return true;
		}
		return false;
	}
	
	public void updateSettings(Settings settings) {
		this.settings = settings;
		writeSettings();
	}
	
	public void updateSettingsFromGui(String host, int port, Protocol protocol) {
		settings.host = host;
		settings.port = port;
		settings.protocol = protocol;
		writeSettings();
	}
	
	public Settings getSettings() {
		return settings;
	}
	
	public List<Device> getDevices() {
		return devices;
	}
	
	public Device getDevice(int deviceId) {
		for (Device device : devices) {
			if (device.id == deviceId) {
				return device;
			}
		}
		return null;
	}
	
	public List<Event> getEvents() {
		return events;
	}

	public Event getEvent(long eventId) {
		for (Event event : events) {
			if (event.id == eventId) {
				return event;
			}
		}
		return null;
	}

	public List<WeatherReport> getWeatherReports() {
		return weatherReports;
	}
	
	public List<Camera> getCameras() {
		return cameras;
	}
	
	public List<String> getLogItems() {
		return logItems;
	}

	public String getLogText() {
		String text = "";
		for (String item : logItems) {
			text += item + "\n";
		}
		return text;
	}

	public List<Sensor> getSensors() {
		return sensors;
	}
	
	private void writeSettings() {
		SettingsXmlWriter settingsWriter = new SettingsXmlWriter(settings, SETTINGS_FILENAME);
		settingsWriter.write();
		logger.info("Settings written to " + SETTINGS_FILENAME);
	}
	
	public Integer getMajorApiVersion(Integer defaultValue) {
		if (majorApiVersion != null) {
			return majorApiVersion;
		}
		else {
			return defaultValue;
		}
	}

	public Integer getMinorApiVersion(Integer defaultValue) {
		if (minorApiVersion != null) {
			return minorApiVersion;
		}
		else {
			return defaultValue;
		}
	}

	public String getSite(String defaultValue) {
		if (site != null) {
			return site;
		}
		else {
			return defaultValue;
		}
	}

	public Boolean getArmed(Boolean defaultValue) {
		if (armed != null) {
			return armed;
		}
		else {
			return defaultValue;
		}
	}
	
	public String getArmedStr(String defaultValue) {
		if (armed != null) {
			if (armed) {
				return "armed";
			}
			else {
				return "disarmed";
			}
		}
		else {
			return defaultValue;
		}
	}

	public Double getCountdown(Double defaultValue) {
		if (countdown != null) {
			return countdown;
		}
		else {
			return defaultValue;
		}
	}

	public Long getLatestEventId(Long defaultValue) {
		if (latestEventId != null) {
			return latestEventId;
		}
		else {
			return defaultValue;
		}
	}

	public Integer getNumInfoEvents(Integer defaultValue) {
		if (numInfoEvents != null) {
			return numInfoEvents;
		}
		else {
			return defaultValue;
		}
	}

	public Integer getNumMinorEvents(Integer defaultValue) {
		if (numMinorEvents != null) {
			return numMinorEvents;
		}
		else {
			return defaultValue;
		}
	}

	public Integer getNumMajorEvents(Integer defaultValue) {
		if (numMajorEvents != null) {
			return numMajorEvents;
		}
		else {
			return defaultValue;
		}
	}

	public Double getLogTimestamp(Double defaultValue) {
		if (logTimestamp != null) {
			return logTimestamp;
		}
		else {
			return defaultValue;
		}
	}

	public String getControlChecksum(String defaultValue) {
		if (controlChecksum != null) {
			return controlChecksum;
		}
		else {
			return defaultValue;
		}
	}

	public String getWeatherChecksum(String defaultValue) {
		if (weatherChecksum != null) {
			return weatherChecksum;
		}
		else {
			return defaultValue;
		}
	}

	public String getTime(String defaultValue) {
		if (time != null) {
			return time;
		}
		else {
			return defaultValue;
		}
	}
}
