package se.markstrom.skynet.skynetremote.xmlparsing;

public class Event {
	
	private static final String[] SEVERITIES = {"Info", "Minor", "Major"};
	private static final String ARMED_STR = "Armed";
	private static final String DISARMED_STR = "Disarmed";
	
	public int id;
	public String time;
	public String message;
	public String sensor;
	public String areas;
	public int severity;
	public boolean armed;
	
	public String getSeverityStr() {
		return SEVERITIES[severity];
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
