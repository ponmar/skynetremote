package se.markstrom.skynet.skynetremote;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileWriter extends Thread {

	private String filename;
	private byte[] data;

	private FileWriter(String filename, byte[] data) {
		this.filename = filename;
		this.data = data;
	}

	public static void saveFile(String filename, byte[] data) {
		new FileWriter(filename, data).start();
	}
	
	public void run() {
		DataOutputStream os = null;
		try {
			os = new DataOutputStream(new FileOutputStream(filename));
			os.write(data);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (os != null) {
				try {
					os.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
