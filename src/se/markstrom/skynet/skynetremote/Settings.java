package se.markstrom.skynet.skynetremote;

import se.markstrom.skynet.api.SkynetAPI.Protocol;

public class Settings {
	private static final int MIN_POLL_INTERVAL = 1;
	
	public boolean getNewEvents;
	public boolean getNewControl;
	public boolean getNewSensors;
	public boolean getNewLog;
	public int summaryPollInterval;
	public String host;
	public int port;
	public Protocol protocol;
	public boolean notifyOnNewEvent;
	
	public Settings() {
		resetDefaults();
	}
	
	public void resetDefaults() {
		getNewEvents = true;
		getNewControl = true;
		getNewSensors = true;
		getNewLog = true;
		summaryPollInterval = 30;
		host = "";
		port = 22;
		protocol = Protocol.SSH;
		notifyOnNewEvent = true;
	}

	public boolean validate() {
		return summaryPollInterval >= MIN_POLL_INTERVAL;
	}
	
	public static String createFilenameForEventImage(long eventId, int imageIndex) {
		return "event_" + eventId + "_image_" + imageIndex + ".jpg";
	}
}
