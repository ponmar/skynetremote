package se.markstrom.skynet.skynetremote.model;

public class Event {
	
	private static final String INFO_STR = "Info";
	private static final String MINOR_STR = "Minor";
	private static final String MAJOR_STR = "Major";
	private static final String ARMED_STR = "Armed";
	private static final String DISARMED_STR = "Disarmed";
	
	public enum Severity { INFO, MINOR, MAJOR }
	
	public final long id;
	public final String time;
	public final String message;
	public final String sensor;
	public final String areas;
	public final Severity severity;
	public final boolean armed;
	public final int images;
	
	public Event(long id, String time, String message, String sensor, String areas, Severity severity, boolean armed, int images) {
		this.id = id;
		this.time = time;
		this.message = message;
		this.sensor = sensor;
		this.areas = areas;
		this.severity = severity;
		this.armed = armed;
		this.images = images;
	}
	
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
