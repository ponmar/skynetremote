package se.markstrom.skynet.skynetremote.model;

import java.util.ArrayList;
import java.util.List;

public class Log {

	private ArrayList<String> items = new ArrayList<String>();
	
	public void reset() {
		items.clear();
	}
	
	public void addItem(String item) {
		items.add(item);
	}
	
	public List<String> getItems() {
		return items;
	}
	
	public String getText() {
		String text = "";
		for (String item : items) {
			text += item + "\n";
		}
		return text;
	}
}
