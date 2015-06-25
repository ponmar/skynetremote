package se.markstrom.skynet.skynetremote.xmlparsing;

public class Event {
	
	private static final String[] SEVERITIES = {"Info", "Minor", "Major"};
	
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
}
