package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.markstrom.skynet.skynetremote.model.Device;
import se.markstrom.skynet.skynetremote.model.Device.DeviceType;

public class ControlXmlParser extends XmlParser {
	
	private ArrayList<Device> devices;
	
	public ControlXmlParser(String xml) {
		super(xml);
	}
	
	public ArrayList<Device> getDevices() {
		return devices;
	}
	
	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		NodeList nl = xmlDoc.getElementsByTagName("device");
		if (nl != null) {
			devices = new ArrayList<Device>();
			for (int i=0; i<nl.getLength(); i++) {
				Node deviceNode = nl.item(i);
				
				NamedNodeMap attributes = deviceNode.getAttributes();
				int id = Integer.parseInt(attributes.getNamedItem("id").getFirstChild().getNodeValue());
				String name = attributes.getNamedItem("name").getFirstChild().getNodeValue();
				boolean state = attributes.getNamedItem("state").getFirstChild().getNodeValue().equals("1");
				int timeLeft = (int) Double.parseDouble(attributes.getNamedItem("timeleft").getFirstChild().getNodeValue());
				DeviceType type = Device.DeviceType.values()[Integer.parseInt(attributes.getNamedItem("type").getFirstChild().getNodeValue())];
				
				Device device = new Device(id, name, state, timeLeft, type);
				devices.add(device);
			}
			return true;
		}
		return false;
	}
}
