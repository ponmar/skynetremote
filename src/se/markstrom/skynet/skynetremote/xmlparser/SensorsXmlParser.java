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

				String name = null;
				String details = null;
				String updateFilter = null;
				String triggerFilter = null;
				Integer armedActions = null;
				Integer disarmedActions = null;
				Integer triggerCount = null;
				Boolean muted = null;
				String areas = null;
				
				Node sensorNode = nl.item(i);
				for (Node sensorChildNode = sensorNode.getFirstChild(); sensorChildNode != null; sensorChildNode = sensorChildNode.getNextSibling()) {
					String nodeName = sensorChildNode.getNodeName();
					if (nodeName.equals("name")) {
						name = sensorChildNode.getFirstChild().getNodeValue();
					}
					else if (nodeName.equals("details")) {
						details = sensorChildNode.getFirstChild().getNodeValue();
					}
					else if (nodeName.equals("updatefilter")) {
						updateFilter = sensorChildNode.getFirstChild().getNodeValue();
					}
					else if (nodeName.equals("triggerfilter")) {
						triggerFilter = sensorChildNode.getFirstChild().getNodeValue();
					}
					else if (nodeName.equals("mute")) {
						muted = Integer.parseInt(sensorChildNode.getFirstChild().getNodeValue()) != 0;
					}
					else if (nodeName.equals("armedactions")) {
						armedActions = Integer.parseInt(sensorChildNode.getFirstChild().getNodeValue());
					}
					else if (nodeName.equals("disarmedactions")) {
						disarmedActions = Integer.parseInt(sensorChildNode.getFirstChild().getNodeValue());
					}
					else if (nodeName.equals("triggercount")) {
						triggerCount = Integer.parseInt(sensorChildNode.getFirstChild().getNodeValue());
					}
					else if (nodeName.equals("areas")) {
						Node areasNode = sensorChildNode.getFirstChild();
						if (areasNode != null) {
							areas = areasNode.getNodeValue();
						}
						else {
							areas = "";
						}
					}
				}
				
				if (name == null || details == null || updateFilter == null || triggerFilter == null || armedActions == null || disarmedActions == null || triggerCount == null || muted == null || areas == null) {
					return false;
				}
				
				Sensor sensor = new Sensor(name, details, updateFilter, triggerFilter, armedActions, disarmedActions, triggerCount, muted, areas);
				sensors.add(sensor);
			}
			return true;
		}
		return false;
	}
}
