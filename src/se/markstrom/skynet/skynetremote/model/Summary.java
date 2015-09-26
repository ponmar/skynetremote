package se.markstrom.skynet.skynetremote.model;

public class Summary {
	public final int majorApiVersion;
	public final int minorApiVersion;
	public final String site;
	public final boolean armed;
	public final double countdown;
	public final long latestEventId;
	public final int numInfoEvents;
	public final int numMinorEvents;
	public final int numMajorEvents;
	public final double logTimestamp;
	public final String controlChecksum;
	public final String time;
	
	public Summary(int majorApiVersion, int minorApiVersion, String site, boolean armed, double countdown, long latestEventId, int numInfoEvents, int numMinorEvents, int numMajorEvents, double logTimestamp, String controlChecksum, String time) {
		this.majorApiVersion = majorApiVersion;
		this.minorApiVersion = minorApiVersion;
		this.site = site;
		this.armed = armed;
		this.countdown = countdown;
		this.latestEventId = latestEventId;
		this.numInfoEvents = numInfoEvents;
		this.numMinorEvents = numMinorEvents;
		this.numMajorEvents = numMajorEvents;
		this.logTimestamp = logTimestamp;
		this.controlChecksum = controlChecksum;
		this.time = time;
	}
	
	public String getArmedStr() {
		if (armed) {
			return "armed";
		}
		else {
			return "disarmed";
		}
	}
}
