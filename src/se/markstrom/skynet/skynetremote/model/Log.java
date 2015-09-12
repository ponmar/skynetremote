package se.markstrom.skynet.skynetremote.model;

public class Log {

	// TODO: store log messages in an ArrayList?
	public String text;
	
	public Log() {
		text = "";
	}
	
	public Log(String text) {
		this.text = text;
	}
	
	public void reset() {
		text = "";
	}
}
