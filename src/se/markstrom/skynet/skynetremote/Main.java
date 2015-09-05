package se.markstrom.skynet.skynetremote;

import java.io.IOException;
import java.net.URLClassLoader;

public class Main {
	public static void main(String[] args) {
		URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		SwtURLClassLoader swtLoader = new SwtURLClassLoader(urlClassLoader);

		ApplicationWindow window = new ApplicationWindow();
		window.run();
		window.close();
		
		try {
			swtLoader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
