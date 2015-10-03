package se.markstrom.skynet.skynetremote.model;

public class Device {
	
	public enum DeviceType {
		TELLSTICK,
		SIMULATED,
		GPIO
	}
	
	public static final String ON_STR = "On";
	public static final String OFF_STR = "Off";
	
	public static final String NO_TIME_LEFT_STR = "";
	
	public static final String TELLSTICK_STR = "Tellstick";
	public static final String SIMULATED_STR = "Simulated";
	public static final String GPIO_STR = "GPIO";
	public static final String UNKNOWN_STR = "Unknown";
	
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
			return TELLSTICK_STR;
		case SIMULATED:
			return SIMULATED_STR;
		case GPIO:
			return GPIO_STR;
		default:
			return UNKNOWN_STR;
		}
	}
	
	public String getTimeLeftStr() {
		if (timeLeft == -1) {
			return NO_TIME_LEFT_STR;
		}
		else {
			return String.valueOf(timeLeft);
		}
	}
}
