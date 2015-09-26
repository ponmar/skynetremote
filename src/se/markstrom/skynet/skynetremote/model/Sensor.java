package se.markstrom.skynet.skynetremote.model;

public class Sensor {

	public final String name;
	public final String details;
	public final String updateFilter;
	public final String triggerFilter;
	public final int armedActions;
	public final int disarmedActions;
	public final int triggerCount;
	public final boolean muted;
	public final String areas;
	
	public Sensor(String name, String details, String updateFilter, String triggerFilter, int armedActions, int disarmedActions, int triggerCount, boolean muted, String areas) {
		this.name = name;
		this.details = details;
		this.updateFilter = updateFilter;
		this.triggerFilter = triggerFilter;
		this.armedActions = armedActions;
		this.disarmedActions = disarmedActions;
		this.triggerCount = triggerCount;
		this.muted = muted;
		this.areas = areas;
	}
}
