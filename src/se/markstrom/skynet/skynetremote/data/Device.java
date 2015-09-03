package se.markstrom.skynet.skynetremote.data;

public class Device {
	
	public static final String ON_STR = "On";
	public static final String OFF_STR = "Off";
	
	public int id;
	public String name;
	public boolean state;
	public int timeLeft;
	public int type;
	
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
		case 0:
			return "Tellstick";
		case 1:
			return "Simulated";
		case 2:
			return "GPIO";
		default:
			return "Unknown";
		}
	}
}
