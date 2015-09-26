package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.markstrom.skynet.skynetremote.model.Event;
import se.markstrom.skynet.skynetremote.model.Event.Severity;

public class EventsXmlParser extends XmlParser {

	private ArrayList<Event> events;
	
	public EventsXmlParser(String xml) {
		super(xml);
	}
	
	public ArrayList<Event> getEvents() {
		return events;
	}

	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		NodeList nl = xmlDoc.getElementsByTagName("event");
		if (nl != null) {
			events = new ArrayList<Event>();
			
			for (int i=0; i<nl.getLength(); i++) {
				Node eventNode = nl.item(i);
				
				Long id = null;
				String time = null;
				String message = null;
				String sensor = null;
				String areas = null;
				Severity severity = null;
				Boolean armed = null;
				Integer images = null;
				
				for (Node eventChildNode = eventNode.getFirstChild(); eventChildNode != null; eventChildNode = eventChildNode.getNextSibling()) {
					String nodeName = eventChildNode.getNodeName();
					if (nodeName.equals("id")) {
						id = Long.parseLong(eventChildNode.getFirstChild().getNodeValue());
					}
					else if (nodeName.equals("severity")) {
						severity = Severity.values()[Integer.parseInt(eventChildNode.getFirstChild().getNodeValue())];
					}
					else if (nodeName.equals("armed")) {
						armed = Integer.parseInt(eventChildNode.getFirstChild().getNodeValue()) == 1;
					}
					else if (nodeName.equals("time")) {
						time = eventChildNode.getFirstChild().getNodeValue();
					}
					else if (nodeName.equals("message")) {
						message = eventChildNode.getFirstChild().getNodeValue();
					}
					else if (nodeName.equals("sensor")) {
						sensor = eventChildNode.getFirstChild().getNodeValue();
					}
					else if (nodeName.equals("areas")) {
						Node child = eventChildNode.getFirstChild();
						if (child != null) {
							areas = child.getNodeValue();
						}
						else {
							areas = "";
						}
					}
					else if (nodeName.equals("numimages")) {
						images = Integer.parseInt(eventChildNode.getFirstChild().getNodeValue());
					}
				}
				
				if (id == null || time == null || message == null || sensor == null || areas == null || severity == null || armed == null || images == null) {
					return false;
				}

				Event event = new Event(id, time, message, sensor, areas, severity, armed, images);
				events.add(event);
			}
			return true;
		}
		return false;
	}
}
