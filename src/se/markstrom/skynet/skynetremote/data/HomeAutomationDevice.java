package se.markstrom.skynet.skynetremote.data;

public class HomeAutomationDevice {
	
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
}
