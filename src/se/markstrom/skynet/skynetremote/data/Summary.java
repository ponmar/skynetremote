package se.markstrom.skynet.skynetremote.data;

public class Summary {
	public int majorApiVersion;
	public int minorApiVersion;
	public String site;
	public boolean armed;
	public double countdown;
	public long latestEventId;
	public int numInfoEvents;
	public int numMinorEvents;
	public int numMajorEvents;
	public double logTimestamp;
	public String controlChecksum;
	public String time;
	
	public String getArmedStr() {
		if (armed) {
			return "armed";
		}
		else {
			return "disarmed";
		}
	}
}
