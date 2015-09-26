package se.markstrom.skynet.skynetremote.model;

public class Device {
	
	public enum DeviceType {
		TELLSTICK,
		SIMULATED,
		GPIO
	}
	
	public static final String ON_STR = "On";
	public static final String OFF_STR = "Off";
	
	public final int id;
	public final String name;
	public final boolean state;
	public final int timeLeft;
	public final DeviceType type;
	
	public Device(int id, String name, boolean state, int timeLeft, DeviceType type) {
		this.id = id;
		this.name = name;
		this.state = state;
		this.timeLeft = timeLeft;
		this.type = type; 
	}
	
	public String getStateStr() {
		if (state) {
			return ON_STR;
		}
		else {
			return OFF_STR;
		}
	}
	
	public String getTypeStr() {
		switch (type) {
		case TELLSTICK:
			return "Tellstick";
		case SIMULATED:
			return "Simulated";
		case GPIO:
			return "GPIO";
		default:
			return "Unknown";
		}
	}
}
