package se.markstrom.skynet.skynetremote;

public class Settings {
	public boolean getNewEvents;
	public boolean pollSummary;
	public int summaryPollInterval;
	public int cameraImagePollInterval;
	public String host;
	public int port;
	
	public Settings() {
		resetDefaults();
		// TODO: read settings from file
	}
	
	public boolean save() {
		// TODO: save settings to file
		return true;
	}
	
	public void resetDefaults() {
		getNewEvents = true;
		pollSummary = true;
		summaryPollInterval = 30000;
		cameraImagePollInterval = 1000;
		host = "";
		port = 22;
	}
}
