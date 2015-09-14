package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.markstrom.skynet.skynetremote.model.Sensor;

public class SensorsXmlParser extends XmlParser {
	
	private ArrayList<Sensor> sensors;
	
	public SensorsXmlParser(String xml) {
		super(xml);
	}
	
	public ArrayList<Sensor> getSensors() {
		return sensors;
	}

	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		NodeList nl = xmlDoc.getElementsByTagName("sensor");
		if (nl != null) {
			sensors = new ArrayList<Sensor>();
			for (int i=0; i<nl.getLength(); i++) {
				Sensor sensor = new Sensor();
				Node sensorNode = nl.item(i);
				for (Node sensorChildNode = sensorNode.getFirstChild(); sensorChildNode != null; sensorChildNode = sensorChildNode.getNextSibling()) {
					String nodeName = sensorChildNode.getNodeName();
					if (nodeName.equals("name")) {
						sensor.name = sensorChildNode.getFirstChild().getNodeValue();
					}
					else if (nodeName.equals("details")) {
						sensor.details = sensorChildNode.getFirstChild().getNodeValue();
					}
					else if (nodeName.equals("updatefilter")) {
						sensor.updateFilter = sensorChildNode.getFirstChild().getNodeValue();
					}
					else if (nodeName.equals("triggerfilter")) {
						sensor.triggerFilter = sensorChildNode.getFirstChild().getNodeValue();
					}
					else if (nodeName.equals("mute")) {
						sensor.muted = Integer.parseInt(sensorChildNode.getFirstChild().getNodeValue()) != 0;
					}
					else if (nodeName.equals("armedactions")) {
						sensor.armedActions = Integer.parseInt(sensorChildNode.getFirstChild().getNodeValue());
					}
					else if (nodeName.equals("disarmedactions")) {
						sensor.disarmedActions = Integer.parseInt(sensorChildNode.getFirstChild().getNodeValue());
					}
					else if (nodeName.equals("triggercount")) {
						sensor.triggerCount = Integer.parseInt(sensorChildNode.getFirstChild().getNodeValue());
					}
					else if (nodeName.equals("areas")) {
						Node areasNode = sensorChildNode.getFirstChild();
						if (areasNode != null) {
							sensor.areas = areasNode.getNodeValue();
						}
						else {
							sensor.areas = "";
						}
					}
				}
				sensors.add(sensor);
			}
			return true;
		}
		return false;
	}
}
