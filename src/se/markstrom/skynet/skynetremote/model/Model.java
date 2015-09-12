package se.markstrom.skynet.skynetremote.model;

import java.util.ArrayList;
import java.util.List;

import se.markstrom.skynet.skynetremote.xmlparser.CamerasXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.ControlXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.EventsXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.LogXmlParser;

public class Model {

	private ArrayList<Event> events = new ArrayList<Event>();
	private ArrayList<Device> devices = new ArrayList<Device>();
	private ArrayList<Integer> cameraIndexes = new ArrayList<Integer>();
	private String log = "";
	
	public Model() {
	}
	
	public boolean updateEvents(String eventsXml) {
		EventsXmlParser parser = new EventsXmlParser(eventsXml);
		if (parser.isValid()) {
			events = parser.getEvents();
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
			cameraIndexes = parser.getCameraIndexes();
			return true;
		}
		return false;
	}
	
	public boolean updateLog(String logXml) {
		LogXmlParser parser = new LogXmlParser(logXml);
		if (parser.isValid()) {
			log = parser.getLogText();
			return true;
		}
		return false;
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

	public List<Integer> getCameras() {
		return cameraIndexes;
	}
	
	public String getLog() {
		return log;
	}
}
