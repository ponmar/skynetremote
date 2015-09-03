package se.markstrom.skynet.skynetremote;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class SwtURLClassLoader extends URLClassLoader {

	private static final String SWT_32_JAR_FILENAME = "swt-4.5-win32-win32-x86.jar";
	private static final String SWT_64_JAR_FILENAME = "swt-4.5-win32-win32-x86_64.jar";

	public SwtURLClassLoader(URLClassLoader classLoader) {
		super(classLoader.getURLs());
		addSwtURL();
	}

	@Override
	public void addURL(URL url) {
		super.addURL(url);
	}

	private void addSwtURL() {
		String osArch = System.getProperty("os.arch");
		boolean is64BitJava = osArch.indexOf("64") != -1;
		
		// rsrc protocol is used for loading SWT classes from the SWT jar within application jar file
		// file protocol is ued when loading SWT classes from the SWT jar directly

		try {
			if (is64BitJava) {
				addURL(new URL("rsrc://" + SWT_64_JAR_FILENAME));
			}
			else {
				addURL(new URL("rsrc://" + SWT_32_JAR_FILENAME));
			}
		}
		catch (MalformedURLException e) {
			// This happens when running from within Eclipse when the SWT jar files are
			// not inside the built application jar file.
			
			// Load SWT classes from the SWT jar files instead
			try {
				if (is64BitJava) {
					addURL(new URL("file://" + SWT_64_JAR_FILENAME));
				}
				else {
					addURL(new URL("file://" + SWT_32_JAR_FILENAME));
				}
			}
			catch (MalformedURLException e2) {
				System.out.println("Could not find any SWT jar files");
				e2.printStackTrace();
			}
		}
	}
}
