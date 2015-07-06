package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.markstrom.skynet.skynetremote.data.Device;

public class ControlXmlParser extends XmlParser {
	
	private ArrayList<Device> devices;
	
	public ControlXmlParser(String xml) {
		super(xml);
	}
	
	public List<Device> getDevices() {
		return devices;
	}
	
	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		NodeList nl = xmlDoc.getElementsByTagName("device");
		if (nl != null) {
			devices = new ArrayList<Device>();
			for (int i=0; i<nl.getLength(); i++) {
				Device device = new Device();
				Node deviceNode = nl.item(i);
				NamedNodeMap attributes = deviceNode.getAttributes();
				device.id = Integer.parseInt(attributes.getNamedItem("id").getFirstChild().getNodeValue());
				device.name = attributes.getNamedItem("name").getFirstChild().getNodeValue();
				device.state = Boolean.parseBoolean(attributes.getNamedItem("state").getFirstChild().getNodeValue());
				device.timeLeft = Integer.parseInt(attributes.getNamedItem("timeleft").getFirstChild().getNodeValue());
				device.type = Integer.parseInt(attributes.getNamedItem("type").getFirstChild().getNodeValue());
				devices.add(device);
			}
			return true;
		}
		return false;
	}
}
