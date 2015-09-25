package se.markstrom.skynet.skynetremote;

import se.markstrom.skynet.skynetremote.window.ApplicationWindow;

public class Main {
	public static void main(String[] args) {
		try {
			ApplicationWindow window = new ApplicationWindow();
			window.run();
			window.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
