package se.markstrom.skynet.skynetremote.model;

import se.markstrom.skynet.api.SkynetAPI.Protocol;

public class Settings {
	private static final int MIN_POLL_INTERVAL = 1;
	
	public boolean getNewEvents;
	public boolean getNewControl;
	public boolean getNewSensors;
	public boolean getNewLog;
	public boolean getNewWeather;
	public int summaryPollInterval;
	public String host;
	public int port;
	public Protocol protocol;
	public boolean notifyOnNewInfoEvent;
	public boolean notifyOnNewMinorEvent;
	public boolean notifyOnNewMajorEvent;
	public boolean logDetails;
	
	public Settings() {
		resetDefaults();
	}
	
	public void resetDefaults() {
		getNewEvents = true;
		getNewControl = true;
		getNewSensors = true;
		getNewLog = true;
		getNewWeather = true;
		summaryPollInterval = 30;
		host = "";
		port = 22;
		protocol = Protocol.SSH;
		notifyOnNewInfoEvent = true;
		notifyOnNewMinorEvent = true;
		notifyOnNewMajorEvent = true;
		logDetails = false;
	}

	public boolean validate() {
		return summaryPollInterval >= MIN_POLL_INTERVAL;
	}
	
	public static String createFilenameForEventImage(long eventId, int imageIndex) {
		return "event_" + eventId + "_image_" + imageIndex + ".jpg";
	}
}
