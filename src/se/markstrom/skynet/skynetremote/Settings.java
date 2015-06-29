package se.markstrom.skynet.skynetremote;

public class Settings {
	public static final String FILENAME = "settings.xml";
	
	public boolean getNewEvents;
	public boolean pollSummary;
	public int summaryPollInterval;
	public int cameraImagePollInterval;
	public String host;
	public int port;
	public boolean showEventNotification;
	
	public Settings() {
		resetDefaults();
	}
	
	public void resetDefaults() {
		getNewEvents = true;
		pollSummary = true;
		summaryPollInterval = 30000;
		cameraImagePollInterval = 1000;
		host = "";
		port = 22;
		showEventNotification = true;
	}
}
