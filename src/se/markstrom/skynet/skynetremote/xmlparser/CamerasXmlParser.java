package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.markstrom.skynet.skynetremote.model.Camera;

public class CamerasXmlParser extends XmlParser {
	
	private ArrayList<Camera> cameras;
	
	public CamerasXmlParser(String xml) {
		super(xml);
	}
	
	public ArrayList<Camera> getCameras() {
		return cameras;
	}

	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		NodeList nl = xmlDoc.getElementsByTagName("camera");
		if (nl != null) {
			cameras = new ArrayList<Camera>();
			for (int i=0; i<nl.getLength(); i++) {
				Node cameraNode = nl.item(i);
				Integer index = null;
				Integer width = null;
				Integer height = null;
				for (Node cameraChildNode = cameraNode.getFirstChild(); cameraChildNode != null; cameraChildNode = cameraChildNode.getNextSibling()) {
					String nodeName = cameraChildNode.getNodeName();
					if (nodeName.equals("index")) {
						index = Integer.parseInt(cameraChildNode.getFirstChild().getNodeValue());
					}
					else if (nodeName.equals("width")) {
						width = Integer.parseInt(cameraChildNode.getFirstChild().getNodeValue());
					}
					else if (nodeName.equals("height")) {
						height = Integer.parseInt(cameraChildNode.getFirstChild().getNodeValue());
					}
				}
				if (index != null && width != null && height != null) {
					cameras.add(new Camera(index, width, height));
				}
			}
			return true;
		}
		return false;
	}
}
