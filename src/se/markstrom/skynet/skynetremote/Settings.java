package se.markstrom.skynet.skynetremote;

public class Settings {
	public boolean getNewEvents = true;
	public boolean pollSummary = true;
	public int summaryPollInterval = 30000;
	public int cameraImagePollInterval = 1000;
	
	public Settings() {
		// TODO: read settings from file
	}
	
	boolean save() {
		// TODO: save settings to file
		return true;
	}
}
