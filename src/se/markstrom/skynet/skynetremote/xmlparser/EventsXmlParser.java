package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.markstrom.skynet.skynetremote.data.Event;

public class EventsXmlParser extends XmlParser {

	private ArrayList<Event> events;
	
	public EventsXmlParser(String xml) {
		super(xml);
	}
	
	public List<Event> getEvents() {
		return events;
	}

	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		NodeList nl = xmlDoc.getElementsByTagName("event");
		if (nl != null) {
			events = new ArrayList<Event>();
			
			for (int i=0; i<nl.getLength(); i++) {
				Node eventNode = nl.item(i);
				Event event = new Event();
				for (Node eventChildNode = eventNode.getFirstChild(); eventChildNode != null; eventChildNode = eventChildNode.getNextSibling()) {
					String nodeName = eventChildNode.getNodeName();
					if (nodeName.equals("id")) {
						event.id = Long.parseLong(eventChildNode.getFirstChild().getNodeValue());
					}
					else if (nodeName.equals("severity")) {
						event.severity = Integer.parseInt(eventChildNode.getFirstChild().getNodeValue());
					}
					else if (nodeName.equals("armed")) {
						event.armed = Integer.parseInt(eventChildNode.getFirstChild().getNodeValue()) == 1;
					}
					else if (nodeName.equals("time")) {
						event.time = eventChildNode.getFirstChild().getNodeValue();
					}
					else if (nodeName.equals("message")) {
						event.message = eventChildNode.getFirstChild().getNodeValue();
					}
					else if (nodeName.equals("sensor")) {
						event.sensor = eventChildNode.getFirstChild().getNodeValue();
					}
					else if (nodeName.equals("areas")) {
						Node child = eventChildNode.getFirstChild();
						if (child != null) {
							event.areas = child.getNodeValue();
						}
						else {
							event.areas = "";
						}
					}
					else if (nodeName.equals("numimages")) {
						event.images = Integer.parseInt(eventChildNode.getFirstChild().getNodeValue());
					}
				}
				events.add(event);
			}
			return true;
		}
		return false;
	}
}
