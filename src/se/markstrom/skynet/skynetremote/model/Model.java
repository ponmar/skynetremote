package se.markstrom.skynet.skynetremote.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import se.markstrom.skynet.api.SkynetAPI.Protocol;
import se.markstrom.skynet.skynetremote.Settings;
import se.markstrom.skynet.skynetremote.window.ApplicationWindow;
import se.markstrom.skynet.skynetremote.xmlparser.CamerasXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.ControlXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.EventsXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.LogXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.SensorsXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.SettingsXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.SummaryXmlParser;
import se.markstrom.skynet.skynetremote.xmlwriter.SettingsXmlWriter;

public class Model {

	private static final Logger logger = Logger.getLogger(ApplicationWindow.class.getName());
	
	private static final String SETTINGS_FILENAME = "settings.xml";
	
	private Summary summary;
	private ArrayList<Event> events = new ArrayList<Event>();
	private ArrayList<Device> devices = new ArrayList<Device>();
	private ArrayList<Camera> cameras = new ArrayList<Camera>();
	private ArrayList<Sensor> sensors = new ArrayList<Sensor>();
	private Log log = new Log();
	private Settings settings = null;

	public Model() {
		reset();
		readSettings();
	}

	public void reset() {
		summary = null;
		events.clear();
		devices.clear();
		cameras.clear();
		sensors.clear();
		log.reset();
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
	
	public boolean updateSummary(String summaryXml) {
		SummaryXmlParser parser = new SummaryXmlParser(summaryXml);
		if (parser.isValid()) {
			summary = parser.getSummary();
			return true;
		}
		return false;
	}
	
	public void resetSummary() {
		summary = null;
	}
	
	public boolean updateEvents(String eventsXml) {
		EventsXmlParser parser = new EventsXmlParser(eventsXml);
		if (parser.isValid()) {
			events = parser.getEvents();
			return true;
		}
		return false;
	}
	
	public boolean updateSensors(String sensorsXml) {
		SensorsXmlParser parser = new SensorsXmlParser(sensorsXml);
		if (parser.isValid()) {
			sensors = parser.getSensors();
			return true;
		}
		return false;
	}

	public boolean updateDevices(String controlXml) {
		ControlXmlParser parser = new ControlXmlParser(controlXml);
		if (parser.isValid()) {
			devices = parser.getDevices();
			return true;
		}
		return false;
	}
	
	public boolean updateCameras(String camerasXml) {
		CamerasXmlParser parser = new CamerasXmlParser(camerasXml);
		if (parser.isValid()) {
			cameras = parser.getCameras();
			return true;
		}
		return false;
	}
	
	public boolean updateLog(String logXml) {
		LogXmlParser parser = new LogXmlParser(logXml);
		if (parser.isValid()) {
			log = parser.getLog();
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
	
	public Summary getSummary() {
		return summary;
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
	
	public long getLatestEventId() {
		if (events.size() > 0) {
			return events.get(events.size()-1).id;
		}
		return -1;
	}

	public List<Camera> getCameras() {
		return cameras;
	}
	
	public Log getLog() {
		return log;
	}
	
	public List<Sensor> getSensors() {
		return sensors;
	}
	
	private void writeSettings() {
		SettingsXmlWriter settingsWriter = new SettingsXmlWriter(settings, SETTINGS_FILENAME);
		settingsWriter.write();
		logger.info("Settings written to " + SETTINGS_FILENAME);
	}
	
	public long getLatestEventIdFromSummary() {
		if (summary != null) {
			return summary.latestEventId;
		}
		else {
			return -1;
		}
	}
	
	public String getControlChecksumFromSummary() {
		if (summary != null) {
			return summary.controlChecksum;
		}
		else {
			return "";
		}
	}
	
	public double getLogTimestampFromSummary() {
		if (summary != null) {
			return summary.logTimestamp;
		}
		else {
			return -1;
		}
	}
}
