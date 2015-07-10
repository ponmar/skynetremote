package se.markstrom.skynet.skynetremote;

import java.util.HashMap;

public class FileCache {

	private HashMap<String, byte[]> files = new HashMap<String, byte[]>();
	
	public byte[] getFileContent(String filename) {
		if (files.containsKey(filename)) {
			return files.get(filename);
		}
		else {
			return null;
		}
	}
	
	public void addFile(String filename, byte[] content) {
		files.put(filename, content);
	}
}
