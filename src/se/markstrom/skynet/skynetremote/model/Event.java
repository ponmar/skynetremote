package se.markstrom.skynet.skynetremote.model;

public class Event {
	
	private static final String INFO_STR = "Info";
	private static final String MINOR_STR = "Minor";
	private static final String MAJOR_STR = "Major";
	private static final String ARMED_STR = "Armed";
	private static final String DISARMED_STR = "Disarmed";
	
	public enum Severity { INFO, MINOR, MAJOR }
	
	public long id;
	public String time;
	public String message;
	public String sensor;
	public String areas;
	public Severity severity;
	public boolean armed;
	public int images = 0;
	
	public String getSeverityStr() {
		switch (severity) {
		case INFO:
			return INFO_STR;
		case MINOR:
			return MINOR_STR;
		case MAJOR:
			return MAJOR_STR;
		default:
			return null;
		}
	}
	
	public String getArmedStr() {
		if (armed) {
			return ARMED_STR;
		}
		else {
			return DISARMED_STR;
		}
	}
}
