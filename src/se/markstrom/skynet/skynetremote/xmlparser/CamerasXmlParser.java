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
		NodeList nl = xmlDoc.getElementsByTagName("index");
		if (nl != null) {
			cameras = new ArrayList<Camera>();
			for (int i=0; i<nl.getLength(); i++) {
				Node indexNode = nl.item(i);
				Node indexDataNode = indexNode.getFirstChild();
				cameras.add(new Camera(Integer.parseInt(indexDataNode.getNodeValue())));
			}
			return true;
		}
		return false;
	}
}
